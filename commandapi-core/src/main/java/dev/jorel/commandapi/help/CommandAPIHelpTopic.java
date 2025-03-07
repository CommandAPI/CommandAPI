package dev.jorel.commandapi.help;

/**
 * An interface for providing the short description, full description, and usage in a command help.
 * This combines the {@link FunctionalInterface}s {@link ShortDescriptionGenerator}, {@link FullDescriptionGenerator}, and {@link UsageGenerator}.
 * 
 * @param <CommandSender> The class for running platform commands.
 */
public interface CommandAPIHelpTopic<CommandSender> extends ShortDescriptionGenerator, FullDescriptionGenerator<CommandSender>, UsageGenerator<CommandSender> {
    
}
