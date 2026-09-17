package com.notsussy.lifesteal;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Consumed on use to grant the player an extra heart, up to the 20-heart cap.
 */
public class HeartItem extends Item {

	public HeartItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.PASS;
		}

		if (HeartManager.isAtCeiling(serverPlayer)) {
			serverPlayer.sendSystemMessage(
				Component.literal("You already have the maximum number of hearts!"), true);
			return InteractionResult.FAIL;
		}

		if (HeartManager.addHeart(serverPlayer)) {
			stack.shrink(1);
			level.playSound(null, serverPlayer.blockPosition(), SoundEvents.PLAYER_LEVELUP,
				SoundSource.PLAYERS, 1.0F, 1.0F);
			serverPlayer.sendSystemMessage(Component.literal("You gained a heart!"), true);
			return InteractionResult.CONSUME;
		}

		return InteractionResult.FAIL;
	}
}
