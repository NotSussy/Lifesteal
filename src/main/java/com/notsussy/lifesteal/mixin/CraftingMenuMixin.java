package com.notsussy.lifesteal.mixin;

import com.notsussy.lifesteal.HeartManager;
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
 * Blocks the crafted result once the player's total heart count - their equipped
 * hearts plus every loose Heart item within 8 blocks (inventory, ender chest, a
 * nearby container, or on the ground) - would reach 9 or more. This is a lower,
 * separate limit than the overall 20-heart ceiling: it only stops crafting new
 * hearts, not gaining more from kills or drops. See
 * {@link HeartManager#countHeartsEverywhere}.
 */
@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin {

	@Inject(method = "slotChangedCraftingGrid", at = @At("TAIL"))
	private static void lifesteal$gateHeartResult(AbstractContainerMenu menu, ServerLevel level, Player player,
			CraftingContainer craftSlots, ResultContainer resultSlots, RecipeHolder<?> recipeHolder, CallbackInfo ci) {
		ItemStack result = resultSlots.getItem(0);
		if (!HeartManager.isHeartStack(result) || !(player instanceof ServerPlayer serverPlayer)) {
			return;
		}

		if (!HeartManager.canCraft(serverPlayer)) {
			resultSlots.setItem(0, ItemStack.EMPTY);
		}
	}
}
