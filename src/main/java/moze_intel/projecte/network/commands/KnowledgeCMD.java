package moze_intel.projecte.network.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import moze_intel.projecte.PECore;
import moze_intel.projecte.PEPermissions;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.utils.text.ILangEntry;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class KnowledgeCMD {

	private enum ActionType {
		LEARN,
		UNLEARN,
		TEST
	}

	public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext context) {
		return Commands.literal("knowledge")
				.requires(PEPermissions.COMMAND_KNOWLEDGE)
				.then(subCommandClear())
				.then(Commands.literal("learn")
						.requires(PEPermissions.COMMAND_KNOWLEDGE_LEARN)
						.then(executeWithParameters(ActionType.LEARN, context))
				)
				.then(Commands.literal("unlearn")
						.requires(PEPermissions.COMMAND_KNOWLEDGE_UNLEARN)
						.then(executeWithParameters(ActionType.UNLEARN, context))
				)
				.then(Commands.literal("test")
						.requires(PEPermissions.COMMAND_KNOWLEDGE_TEST)
						.then(executeWithParameters(ActionType.TEST, context))
				);
	}

	private static ArgumentBuilder<CommandSourceStack, ?> executeWithParameters(ActionType actionType, CommandBuildContext context) {
		return Commands.argument("player", EntityArgument.player())
				.then(Commands.argument("item", ItemArgument.item(context))
						.executes(ctx -> handle(ctx, actionType))
				);
	}

	private static @Nullable IKnowledgeProvider getProvider(ServerPlayer player) {
		return player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
	}

	private static ArgumentBuilder<CommandSourceStack, ?> subCommandClear() {
		return Commands.literal("clear")
				.requires(PEPermissions.COMMAND_KNOWLEDGE_CLEAR)
				.then(Commands.argument("targets", EntityArgument.players())
						.executes(ctx -> {
							CommandSourceStack source = ctx.getSource();
							int successCount = 0;
							for (ServerPlayer player : EntityArgument.getPlayers(ctx, "targets")) {
								IKnowledgeProvider provider = getProvider(player);
								if (provider == null) {
									source.sendFailure(PELang.COMMAND_PROVIDER_FAIL.translate(player.getDisplayName()));
								} else if (provider.getKnowledge().isEmpty()) {
									source.sendFailure(PELang.COMMAND_KNOWLEDGE_CLEAR_FAIL.translate(player.getDisplayName()));
								} else {
									provider.clearKnowledge();
									PECore.packetHandler().clearKnowledge(player);
									source.sendSuccess(() -> PELang.COMMAND_KNOWLEDGE_CLEAR_SUCCESS.translateColored(ChatFormatting.GREEN, player.getDisplayName()), true);
									successCount++;
								}
							}

							return successCount;
						})
				);
	}

	private static int handle(CommandContext<CommandSourceStack> ctx, ActionType action) throws CommandSyntaxException {
		ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
		CommandSourceStack source = ctx.getSource();
		IKnowledgeProvider provider = getProvider(player);
		if (provider == null) {
			source.sendFailure(PELang.COMMAND_PROVIDER_FAIL.translate(player.getDisplayName()));
			return 0;
		}
		//TODO - 1.21: Figure out about this and persistent info
		ItemStack item = ItemArgument.getItem(ctx, "item").createItemStack(1, false);

		if (!IEMCProxy.INSTANCE.hasValue(item)) {
			source.sendFailure(PELang.COMMAND_KNOWLEDGE_INVALID.translate(item.getDisplayName()));
			return 0;
		}

		switch (action) {
			case LEARN -> {
				if (provider.hasKnowledge(item)) {
					return failure(source, PELang.COMMAND_KNOWLEDGE_LEARN_FAIL, player, item);
				}
				provider.addKnowledge(item);
				source.sendSuccess(() -> PELang.COMMAND_KNOWLEDGE_LEARN_SUCCESS.translateColored(ChatFormatting.GREEN, player.getDisplayName(), item.getDisplayName()), true);
			}
			case UNLEARN -> {
				if (!provider.hasKnowledge(item)) {
					return failure(source, PELang.COMMAND_KNOWLEDGE_UNLEARN_FAIL, player, item);
				}
				provider.removeKnowledge(item);
				source.sendSuccess(() -> PELang.COMMAND_KNOWLEDGE_UNLEARN_SUCCESS.translateColored(ChatFormatting.GREEN, player.getDisplayName(), item.getDisplayName()), true);
			}
			case TEST -> {
				if (provider.hasKnowledge(item)) {
					source.sendSuccess(() -> PELang.COMMAND_KNOWLEDGE_TEST_SUCCESS.translateColored(ChatFormatting.GREEN, player.getDisplayName(), item.getDisplayName()), true);
					return Command.SINGLE_SUCCESS;
				}
				return failure(source, PELang.COMMAND_KNOWLEDGE_TEST_FAIL, player, item);
			}
		}
		provider.syncKnowledgeChange(player, IEMCProxy.INSTANCE.getPersistentInfo(ItemInfo.fromStack(item)), action == ActionType.LEARN);

		return Command.SINGLE_SUCCESS;
	}

	private static int failure(CommandSourceStack source, ILangEntry failureMessage, Player player, ItemStack item) {
		source.sendFailure(failureMessage.translate(player.getDisplayName(), item.getDisplayName()));
		return 0;
	}
}
