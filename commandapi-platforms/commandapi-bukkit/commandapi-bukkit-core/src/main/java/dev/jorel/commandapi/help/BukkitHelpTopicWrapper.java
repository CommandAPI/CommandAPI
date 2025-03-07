package dev.jorel.commandapi.help;

import java.util.Optional;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.help.HelpTopic;

import dev.jorel.commandapi.RegisteredCommand.Node;

/**
 * A {@link CommandAPIHelpTopic} that wraps Bukkit's {@link HelpTopic}.
 * 
 * @param helpTopic The Bukkit {@link HelpTopic} being wrapped
 */
public record BukkitHelpTopicWrapper(

	/**
	 * @return The Bukkit {@link HelpTopic} being wrapped
	 */
	HelpTopic helpTopic) implements CommandAPIHelpTopic<CommandSender> {

	@Override
	public Optional<String> getShortDescription() {
		return Optional.of(helpTopic.getShortText());
	}

	@Override
	public Optional<String> getFullDescription(@Nullable CommandSender forWho) {
		if (forWho == null) return Optional.empty();

		return Optional.of(helpTopic.getFullText(forWho));
	}

	@Override
	public Optional<String[]> getUsage(@Nullable CommandSender forWho, @Nullable Node<CommandSender> argumentTree) {
		return Optional.empty();
	}
}
