package dev.jorel.commandapi.help;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.help.HelpTopic;
import org.jetbrains.annotations.NotNull;

import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.RegisteredCommand;

public class CustomCommandAPIHelpTopic extends HelpTopic {
	private final String aliasString;
	private final CommandAPIHelpTopic<CommandSender> helpTopic;
	private final RegisteredCommand.Node<CommandSender> argumentTree;

	public CustomCommandAPIHelpTopic(String name, String[] aliases, CommandAPIHelpTopic<CommandSender> helpTopic, RegisteredCommand.Node<CommandSender> argumentTree) {
		this.name = name;

		// Pre-generate alias help text since it doesn't depend on sender
		if (aliases.length > 0) {
			this.aliasString = "\n" + ChatColor.GOLD + "Aliases: " + ChatColor.WHITE + String.join(", ", aliases);
		} else {
			this.aliasString = "";
		}

		this.helpTopic = helpTopic;
		this.argumentTree = argumentTree;
	}

	@Override
	public boolean canSee(@NotNull CommandSender sender) {
		// Check if sender can see root node
		return canSeeNode(sender, argumentTree);
	}

	private boolean canSeeNode(CommandSender sender, RegisteredCommand.Node<CommandSender> node) {
		final boolean hasPermission;

		// Evaluate the CommandPermission
		CommandPermission permission = node.permission();
		if (permission.equals(CommandPermission.NONE)) {
			hasPermission = true;
		} else if (permission.equals(CommandPermission.OP)) {
			hasPermission = sender.isOp();
		} else {
			Optional<String> optionalStringPermission = permission.getPermission();
			if (optionalStringPermission.isPresent()) {
				hasPermission = sender.hasPermission(optionalStringPermission.get());
			} else {
				hasPermission = true;
			}
		}

		// If sender doesn't have permission (when negated if needed), they can't see this help
		if (!hasPermission ^ permission.isNegated()) return false;

		// Check requirements
		return node.requirements().test(sender);
	}

	@Override
	public @NotNull String getShortText() {
		Optional<String> shortDescriptionOptional = helpTopic.getShortDescription();
		if (shortDescriptionOptional.isPresent()) {
			return shortDescriptionOptional.get();
		} else {
			return helpTopic.getFullDescription(null)
				.orElse("A command by the " + CommandAPIBukkit.getConfiguration().getPlugin().getName() + " plugin.");
		}
	}

	@Override
	public @NotNull String getFullText(@NotNull CommandSender forWho) {
		// Generate full text for the given sender
		StringBuilder sb = new StringBuilder(this.getShortText());

		// Add fullDescription if present
		Optional<String> fullDescriptionOptional = this.helpTopic.getFullDescription(forWho);
		if (fullDescriptionOptional.isPresent()) {
			sb.append("\n").append(ChatColor.GOLD).append("Description: ").append(ChatColor.WHITE).append(fullDescriptionOptional.get());
		}

		// Add usage if present, and otherwise generate default usage
		String[] usages = this.helpTopic.getUsage(forWho, this.argumentTree).orElseGet(() -> generateDefaultUsage(forWho));

		if (usages.length > 0) {
			sb.append("\n").append(ChatColor.GOLD).append("Usage: ").append(ChatColor.WHITE);
			// If 1 usage, put it on the same line, otherwise format like a list
			if (usages.length == 1) {
				sb.append(usages[0]);
			} else {
				for (String usage : usages) {
					sb.append("\n- ").append(usage);
				}
			}
		}

		// Add aliases
		sb.append(this.aliasString);

		return sb.toString();
	}

	private String[] generateDefaultUsage(CommandSender forWho) {
		List<String> usages = new ArrayList<>();
		StringBuilder usageSoFar = new StringBuilder("/");
		addUsageForNode(this.argumentTree, usages, usageSoFar, forWho);
		return usages.toArray(String[]::new);
	}

	private void addUsageForNode(RegisteredCommand.Node<CommandSender> node, List<String> usages, StringBuilder usageSoFar, CommandSender forWho) {
		// If sender can't see the node, don't include it in the usage
		if (!canSeeNode(forWho, node)) return;

		// Add node to usage
		usageSoFar.append(node.helpString());

		// Add usage to the list if this is executable
		if (node.executable()) usages.add(usageSoFar.toString());

		// Add children
		usageSoFar.append(" ");
		int currentLength = usageSoFar.length();
		for (RegisteredCommand.Node<CommandSender> child : node.children()) {
			// Reset the string builder to the usage up to and including this node
			usageSoFar.delete(currentLength, usageSoFar.length());

			addUsageForNode(child, usages, usageSoFar, forWho);
		}
	}
}
