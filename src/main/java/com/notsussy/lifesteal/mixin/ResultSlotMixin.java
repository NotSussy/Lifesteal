package com.notsussy.lifesteal.mixin;

import com.notsussy.lifesteal.Lifesteal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A vanilla shaped recipe only ever consumes 1 item per grid slot. {@link CraftingMenuMixin}
 * already refuses to show a Heart result unless every diamond slot holds a full stack of
 * what it's really supposed to cost; this removes the rest of that stack (on top of the 1
 * vanilla already took) the moment the crafted Heart is actually picked up.
 */
@Mixin(ResultSlot.class)
public abstract class ResultSlotMixin {

	@Final
	@Shadow
	private CraftingContainer craftSlots;

	@Inject(method = "onTake", at = @At("TAIL"))
	private void lifesteal$chargeExtraIngredients(Player player, ItemStack stack, CallbackInfo ci) {
		if (!stack.is(Lifesteal.HEART)) {
			return;
		}

		for (int slot : HeartRecipeCost.DIAMOND_SLOTS) {
			craftSlots.getItem(slot).shrink(HeartRecipeCost.DIAMONDS_PER_SLOT - 1);
		}
	}
}
