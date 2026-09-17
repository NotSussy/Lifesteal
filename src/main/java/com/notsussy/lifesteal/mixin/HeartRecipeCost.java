package com.notsussy.lifesteal.mixin;

/**
 * The Heart recipe's 3x3 grid, slots numbered left-to-right/top-to-bottom (0-8):
 *
 * <pre>
 * 0:Diamond  1:Scrap    2:Diamond
 * 3:Apple    4:Totem    5:Apple
 * 6:Diamond  7:Emerald  8:Diamond
 * </pre>
 *
 * A vanilla shaped recipe only ever requires (and consumes) exactly 1 item per grid
 * slot, so the extra cost per diamond/apple slot beyond that is enforced separately:
 * {@link CraftingMenuMixin} blocks the result unless every slot below already holds
 * enough, and {@link ResultSlotMixin} removes the rest on top of vanilla's own 1 when
 * the item is actually taken.
 */
final class HeartRecipeCost {

	static final int[] DIAMOND_SLOTS = {0, 2, 6, 8};
	static final int DIAMONDS_PER_SLOT = 3;

	static final int[] GOLDEN_APPLE_SLOTS = {3, 5};
	static final int GOLDEN_APPLES_PER_SLOT = 4;

	private HeartRecipeCost() {
	}
}
