package com.notsussy.lifesteal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Lets a player convert their own hearts back into carryable Heart items, never
 * dropping below the 5-heart floor.
 */
public final class WithdrawCommand {

	private WithdrawCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("withdraw")
			.then(Commands.argument("amount", IntegerArgumentType.integer(1))
				.executes(context -> {
					ServerPlayer player = context.getSource().getPlayerOrException();
					int amount = IntegerArgumentType.getInteger(context, "amount");

					if (!HeartManager.canWithdraw(player, amount)) {
						context.getSource().sendFailure(Component.literal(
							"You can't withdraw " + amount + " hearts without dropping below the 5-heart floor."));
						return 0;
					}

					HeartManager.removeHearts(player, amount);

					int remaining = amount;
					while (remaining > 0) {
						int stackSize = Math.min(remaining, HeartManager.HEART_STACK_SIZE);
						ItemStack stack = HeartManager.createHeartStack(stackSize);
						if (!player.getInventory().add(stack)) {
							player.spawnAtLocation((ServerLevel) player.level(), stack);
						}
						remaining -= stackSize;
					}

					context.getSource().sendSuccess(
						() -> Component.literal("Withdrew " + amount + " heart" + (amount == 1 ? "" : "s") + "."),
						false);
					return amount;
				})));
	}
}
