package com.notsussy.lifesteal.mixin;

import com.notsussy.lifesteal.HeartManager;
import com.notsussy.lifesteal.Lifesteal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Extra conditions on top of the data-driven Heart recipe, which by itself only
 * requires (and would only consume) 1 item per grid slot:
 * <ul>
 *   <li>Blocks the result once the crafting player already has 9 or more hearts,
 *   so the recipe simply shows no result instead of letting the item be taken. This
 *   is a lower, separate limit than the overall 20-heart ceiling.</li>
 *   <li>Blocks the result unless each diamond slot already holds a full stack of 3
 *   (see {@link HeartRecipeCost}); {@link ResultSlotMixin} removes the rest on take.</li>
 *   <li>Blocks the result if the player already has a Heart item within 8 blocks
 *   (inventory, ender chest, a nearby container, or on the ground), so Hearts have
 *   to actually be used instead of stockpiled.</li>
 * </ul>
 */
@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin {

	@Inject(method = "slotChangedCraftingGrid", at = @At("TAIL"))
	private static void lifesteal$gateHeartResult(AbstractContainerMenu menu, ServerLevel level, Player player,
			CraftingContainer craftSlots, ResultContainer resultSlots, RecipeHolder<?> recipeHolder, CallbackInfo ci) {
		ItemStack result = resultSlots.getItem(0);
		if (!result.is(Lifesteal.HEART) || !(player instanceof ServerPlayer serverPlayer)) {
			return;
		}

		boolean blocked = !HeartManager.canCraft(serverPlayer);

		if (!blocked) {
			for (int slot : HeartRecipeCost.DIAMOND_SLOTS) {
				if (craftSlots.getItem(slot).getCount() < HeartRecipeCost.DIAMONDS_PER_SLOT) {
					blocked = true;
					break;
				}
			}
		}

		if (!blocked && HeartManager.hasHeartNearby(serverPlayer)) {
			blocked = true;
		}

		if (blocked) {
			resultSlots.setItem(0, ItemStack.EMPTY);
		}
	}
}
