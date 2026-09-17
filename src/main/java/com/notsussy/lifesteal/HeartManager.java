package com.notsussy.lifesteal;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Tracks the lifesteal max-health rules: a floor of 5 hearts, an overall ceiling of
 * 20 hearts, a separate lower limit of 8 hearts on crafting new Heart items, and the
 * 1-heart-per-heart-point increments used to move between them.
 */
public final class HeartManager {

	public static final float HEALTH_PER_HEART = 2.0F;
	public static final float MIN_HEARTS = 5.0F;
	public static final float MAX_HEARTS = 20.0F;
	public static final float CRAFT_LIMIT_HEARTS = 8.0F;
	public static final float MIN_HEALTH = MIN_HEARTS * HEALTH_PER_HEART;
	public static final float MAX_HEALTH = MAX_HEARTS * HEALTH_PER_HEART;

	private HeartManager() {
	}

	public static float getHearts(ServerPlayer player) {
		return getMaxHealth(player) / HEALTH_PER_HEART;
	}

	public static float getMaxHealth(ServerPlayer player) {
		AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
		return attribute == null ? MIN_HEALTH : (float) attribute.getBaseValue();
	}

	public static boolean isAtCeiling(ServerPlayer player) {
		return getHearts(player) >= MAX_HEARTS;
	}

	public static boolean isAtFloor(ServerPlayer player) {
		return getHearts(player) <= MIN_HEARTS;
	}

	/**
	 * Whether a player is allowed to craft a new Heart item. This is a separate, lower
	 * limit than the overall 20-heart ceiling: crafting stops at 8 hearts, but hearts
	 * picked up from other players' deaths can still carry someone all the way to 20.
	 */
	public static boolean canCraft(ServerPlayer player) {
		return getHearts(player) < CRAFT_LIMIT_HEARTS;
	}

	/**
	 * Grants one heart, clamped at the 20-heart ceiling. Returns true if a heart was actually
	 * gained.
	 */
	public static boolean addHeart(ServerPlayer player) {
		AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
		if (attribute == null || isAtCeiling(player)) {
			return false;
		}

		double newValue = Math.min(attribute.getBaseValue() + HEALTH_PER_HEART, MAX_HEALTH);
		if (newValue <= attribute.getBaseValue()) {
			return false;
		}

		attribute.setBaseValue(newValue);
		return true;
	}

	/**
	 * Takes away one heart, clamped at the 5-heart floor. Returns true if a heart was actually
	 * lost.
	 */
	public static boolean removeHeart(ServerPlayer player) {
		AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
		if (attribute == null || isAtFloor(player)) {
			return false;
		}

		double newValue = Math.max(attribute.getBaseValue() - HEALTH_PER_HEART, MIN_HEALTH);
		if (newValue >= attribute.getBaseValue()) {
			return false;
		}

		attribute.setBaseValue(newValue);
		return true;
	}

	/**
	 * Carries the max-health attribute across a respawn. Vanilla creates a brand-new player
	 * instance on respawn and does not copy custom attribute changes on its own, so without
	 * this every death would silently reset a player back to the vanilla default.
	 */
	public static void copyOnRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
		AttributeInstance oldAttribute = oldPlayer.getAttribute(Attributes.MAX_HEALTH);
		AttributeInstance newAttribute = newPlayer.getAttribute(Attributes.MAX_HEALTH);
		if (oldAttribute == null || newAttribute == null) {
			return;
		}

		newAttribute.setBaseValue(oldAttribute.getBaseValue());
	}
}
