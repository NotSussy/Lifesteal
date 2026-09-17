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
 * Blocks handing out a crafted heart once the crafting player already has 8 or more
 * hearts, so the recipe simply shows no result instead of letting the item be taken.
 * This is a lower, separate limit from the overall 20-heart ceiling: it only stops
 * crafting new hearts, not gaining them some other way (like picking one up).
 */
@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin {

	@Inject(method = "slotChangedCraftingGrid", at = @At("TAIL"))
	private static void lifesteal$blockHeartPastCraftLimit(AbstractContainerMenu menu, ServerLevel level, Player player,
			CraftingContainer craftSlots, ResultContainer resultSlots, RecipeHolder<?> recipeHolder, CallbackInfo ci) {
		ItemStack result = resultSlots.getItem(0);
		if (result.is(Lifesteal.HEART) && player instanceof ServerPlayer serverPlayer
				&& !HeartManager.canCraft(serverPlayer)) {
			resultSlots.setItem(0, ItemStack.EMPTY);
		}
	}
}
