package com.notsussy.lifesteal;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Lifesteal implements ModInitializer {

	public static final String MOD_ID = "lifesteal";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final ResourceKey<Item> HEART_KEY =
		ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "heart"));

	public static final Item HEART = Registry.register(BuiltInRegistries.ITEM, HEART_KEY,
		new HeartItem(new Item.Properties().setId(HEART_KEY).stacksTo(16)));

	@Override
	public void onInitialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS)
			.register(output -> output.accept(HEART));

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
			HeartManager.initializeIfNeeded(handler.player));

		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) ->
			HeartManager.copyOnRespawn(oldPlayer, newPlayer));

		ServerLivingEntityEvents.AFTER_DEATH.register(Lifesteal::onEntityDeath);

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

		victim.spawnAtLocation(new ItemStack(HEART));
		victim.sendSystemMessage(Component.literal("You lost a heart!"), true);
	}
}
