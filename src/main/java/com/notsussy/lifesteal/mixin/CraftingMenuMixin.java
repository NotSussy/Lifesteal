package com.notsussy.lifesteal.mixin;

import com.notsussy.lifesteal.HeartManager;
import com.notsussy.lifesteal.Lifesteal;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Blocks handing out a crafted heart while the crafting player is already at the 8-heart
 * ceiling, so the recipe simply shows no result instead of letting the item be taken.
 */
@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin {

	@Inject(method = "slotChangedCraftingGrid", at = @At("TAIL"))
	private static void lifesteal$blockHeartAtCeiling(AbstractContainerMenu menu, Level level, Player player,
			CraftingContainer craftSlots, ResultContainer resultSlots, CallbackInfo ci) {
		ItemStack result = resultSlots.getItem(0);
		if (result.is(Lifesteal.HEART) && player instanceof ServerPlayer serverPlayer
				&& HeartManager.isAtCeiling(serverPlayer)) {
			resultSlots.setItem(0, ItemStack.EMPTY);
		}
	}
}
