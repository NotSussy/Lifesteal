package com.notsussy.lifesteal;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Lifesteal implements ModInitializer {

	public static final String MOD_ID = "lifesteal";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS)
			.register(output -> output.accept(HeartManager.createHeartStack(1)));

		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) ->
			HeartManager.copyOnRespawn(oldPlayer, newPlayer));

		ServerLivingEntityEvents.AFTER_DEATH.register(Lifesteal::onEntityDeath);

		UseItemCallback.EVENT.register(Lifesteal::onUseItem);

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
			WithdrawCommand.register(dispatcher));

		LOGGER.info("Lifesteal loaded: floor {} hearts, ceiling {} hearts",
			(int) HeartManager.MIN_HEARTS, (int) HeartManager.MAX_HEARTS);
	}

	private static void onEntityDeath(LivingEntity entity, DamageSource damageSource) {
		if (!(entity instanceof ServerPlayer victim)) {
			return;
		}

		if (!HeartManager.removeHeart(victim)) {
			// Already at the 5-heart floor: nothing to lose, nothing drops.
			return;
		}

		victim.spawnAtLocation((ServerLevel) victim.level(), HeartManager.createHeartStack(1));
		victim.sendSystemMessage(Component.literal("You lost a heart!"), true);
	}

	/**
	 * Consumed on use to grant the player an extra heart, up to the 20-heart cap.
	 * Only reacts to stacks that {@link HeartManager#isHeartStack} recognizes as a
	 * Heart; every other item (including a plain, unmarked Nether Star) passes through
	 * untouched.
	 */
	private static InteractionResult onUseItem(Player player, Level level, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!HeartManager.isHeartStack(stack)) {
			return InteractionResult.PASS;
		}

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
