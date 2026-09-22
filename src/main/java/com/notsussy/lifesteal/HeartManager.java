package com.notsussy.lifesteal;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

/**
 * Tracks the lifesteal max-health rules: a floor of 5 hearts, an overall ceiling of
 * 20 hearts, a separate lower limit of 9 hearts on crafting new Heart items, and the
 * 1-heart-per-heart-point increments used to move between them.
 *
 * <p>The Heart item is deliberately not its own registered item: a truly custom item
 * would need every connecting client to have Fabric Loader and Fabric API installed
 * just to join the server, since the client has to understand any registry entry the
 * server might send it. Instead a Heart is a plain vanilla Nether Star carrying a
 * {@code minecraft:custom_data} marker and a custom name - both are stock vanilla
 * components any client already understands, so the server works with vanilla clients
 * too.
 */
public final class HeartManager {

	public static final float HEALTH_PER_HEART = 2.0F;
	public static final float MIN_HEARTS = 5.0F;
	public static final float MAX_HEARTS = 20.0F;
	public static final float CRAFT_LIMIT_HEARTS = 9.0F;
	public static final float MIN_HEALTH = MIN_HEARTS * HEALTH_PER_HEART;
	public static final float MAX_HEALTH = MAX_HEARTS * HEALTH_PER_HEART;

	public static final Item HEART_BASE_ITEM = Items.NETHER_STAR;
	public static final int HEART_STACK_SIZE = 64;

	private static final String HEART_MARKER_KEY = "lifesteal_heart";
	private static final int NEARBY_HEART_RADIUS = 8;

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
	 * Whether the given stack is a Heart: a Nether Star carrying our marker data.
	 * A plain, unmarked Nether Star is not a Heart.
	 */
	public static boolean isHeartStack(ItemStack stack) {
		if (!stack.is(HEART_BASE_ITEM)) {
			return false;
		}

		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		return data != null && data.copyTag().contains(HEART_MARKER_KEY);
	}

	/**
	 * Builds a new Heart item stack: a Nether Star marked so {@link #isHeartStack}
	 * recognizes it, with a custom display name.
	 */
	public static ItemStack createHeartStack(int count) {
		ItemStack stack = new ItemStack(HEART_BASE_ITEM, count);
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(HEART_MARKER_KEY, true));
		stack.set(DataComponents.CUSTOM_NAME,
			Component.literal("Heart").withStyle(style -> style.withItalic(false).withColor(ChatFormatting.RED)));
		return stack;
	}

	/**
	 * Whether a player is allowed to craft a new Heart item. This is a separate, lower
	 * limit than the overall 20-heart ceiling: crafting stops once the player's total
	 * heart count - see {@link #countHeartsEverywhere} - reaches 9, but hearts picked
	 * up from other players' deaths can still carry someone all the way to 20.
	 */
	public static boolean canCraft(ServerPlayer player) {
		return countHeartsEverywhere(player) < CRAFT_LIMIT_HEARTS;
	}

	/**
	 * Adds up a player's equipped hearts and every loose Heart item close at hand: their
	 * own inventory, their ender chest, a nearby chest (or any other container block), or
	 * one sitting on the ground, all within an 8-block radius. Used so crafting a Heart is
	 * only blocked once that grand total would actually reach the craft limit, rather than
	 * merely because a Heart item exists somewhere nearby.
	 */
	public static float countHeartsEverywhere(ServerPlayer player) {
		float total = getHearts(player);
		total += countHearts(player.getInventory());
		total += countHearts(player.getEnderChestInventory());

		ServerLevel level = (ServerLevel) player.level();
		BlockPos center = player.blockPosition();
		BlockPos min = center.offset(-NEARBY_HEART_RADIUS, -NEARBY_HEART_RADIUS, -NEARBY_HEART_RADIUS);
		BlockPos max = center.offset(NEARBY_HEART_RADIUS, NEARBY_HEART_RADIUS, NEARBY_HEART_RADIUS);

		for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity instanceof Container container) {
				total += countHearts(container);
			}
		}

		AABB area = new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1);
		for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, area)) {
			ItemStack stack = itemEntity.getItem();
			if (isHeartStack(stack)) {
				total += stack.getCount();
			}
		}

		return total;
	}

	private static int countHearts(Container container) {
		int count = 0;
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (isHeartStack(stack)) {
				count += stack.getCount();
			}
		}
		return count;
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
	 * Whether the player can withdraw this many hearts without dropping below the 5-heart floor.
	 */
	public static boolean canWithdraw(ServerPlayer player, int amount) {
		return getHearts(player) - amount >= MIN_HEARTS;
	}

	/**
	 * Removes the given number of hearts from a player's max health, clamped at the 5-heart
	 * floor. Does not hand out any Heart items; callers are expected to check
	 * {@link #canWithdraw} first and hand out items themselves.
	 */
	public static void removeHearts(ServerPlayer player, int amount) {
		AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
		if (attribute == null) {
			return;
		}

		double newValue = Math.max(attribute.getBaseValue() - amount * HEALTH_PER_HEART, MIN_HEALTH);
		attribute.setBaseValue(newValue);
		if (player.getHealth() > newValue) {
			player.setHealth((float) newValue);
		}
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
