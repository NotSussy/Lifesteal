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
 * Two extra conditions on top of the data-driven Heart recipe, which by itself only
 * requires (and would only consume) 1 item per grid slot:
 * <ul>
 *   <li>Blocks the result once the crafting player already has 8 or more hearts,
 *   so the recipe simply shows no result instead of letting the item be taken. This
 *   is a lower, separate limit than the overall 20-heart ceiling.</li>
 *   <li>Blocks the result unless each diamond/golden apple slot already holds a full
 *   stack of the amount that ingredient is actually supposed to cost (see
 *   {@link HeartRecipeCost}); {@link ResultSlotMixin} removes the rest on take.</li>
 * </ul>
 */
@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin {

	@Inject(method = "slotChangedCraftingGrid", at = @At("TAIL"))
	private static void lifesteal$gateHeartResult(AbstractContainerMenu menu, ServerLevel level, Player player,
			CraftingContainer craftSlots, ResultContainer resultSlots, RecipeHolder<?> recipeHolder, CallbackInfo ci) {
		ItemStack result = resultSlots.getItem(0);
		if (!result.is(Lifesteal.HEART)) {
			return;
		}

		boolean blocked = player instanceof ServerPlayer serverPlayer && !HeartManager.canCraft(serverPlayer);

		if (!blocked) {
			for (int slot : HeartRecipeCost.DIAMOND_SLOTS) {
				if (craftSlots.getItem(slot).getCount() < HeartRecipeCost.DIAMONDS_PER_SLOT) {
					blocked = true;
					break;
				}
			}
		}

		if (!blocked) {
			for (int slot : HeartRecipeCost.GOLDEN_APPLE_SLOTS) {
				if (craftSlots.getItem(slot).getCount() < HeartRecipeCost.GOLDEN_APPLES_PER_SLOT) {
					blocked = true;
					break;
				}
			}
		}

		if (blocked) {
			resultSlots.setItem(0, ItemStack.EMPTY);
		}
	}
}
