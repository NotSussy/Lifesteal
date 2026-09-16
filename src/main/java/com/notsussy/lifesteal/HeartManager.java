package com.notsussy.lifesteal;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Tracks the lifesteal max-health rules: a floor of 5 hearts, a ceiling of 8 hearts,
 * and the 1-heart-per-heart-point increments used to move between them.
 */
public final class HeartManager {

	public static final float HEALTH_PER_HEART = 2.0F;
	public static final float MIN_HEARTS = 5.0F;
	public static final float MAX_HEARTS = 8.0F;
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
	 * Sets a player's max health to the lifesteal floor if it has never been touched by this
	 * mod before (i.e. it is still at vanilla's default of 20.0). Called on every join so that
	 * brand-new players start at the 5-heart floor instead of vanilla's 10 hearts.
	 */
	public static void initializeIfNeeded(ServerPlayer player) {
		AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
		if (attribute == null) {
			return;
		}

		if (attribute.getBaseValue() > MAX_HEALTH) {
			attribute.setBaseValue(MIN_HEALTH);
			if (player.getHealth() > MIN_HEALTH) {
				player.setHealth(MIN_HEALTH);
			}
		}
	}

	/**
	 * Grants one heart, clamped at the 8-heart ceiling. Returns true if a heart was actually
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
}
