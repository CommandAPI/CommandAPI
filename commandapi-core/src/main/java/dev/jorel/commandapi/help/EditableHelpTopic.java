package dev.jorel.commandapi.help;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import dev.jorel.commandapi.RegisteredCommand;

/**
 * An {@link CommandAPIHelpTopic} that can have its short description, full description, and usage edited.
 */
public class EditableHelpTopic<CommandSender> implements CommandAPIHelpTopic<CommandSender> {
	private ShortDescriptionGenerator shortDescription = Optional::empty;
	private FullDescriptionGenerator<CommandSender> fullDescription = forWho -> Optional.empty();
	private UsageGenerator<CommandSender> usage = (forWho, argumentTree) -> Optional.empty();

	/**
	 * Creates a new {@link EditableHelpTopic} that returns empty {@link Optional}s
	 * by default for its short description, full description, and usage.
	 */
	public EditableHelpTopic() {

	}

	/**
	 * Creates a new {@link EditableHelpTopic} that returns the given short description, full description, and usage by default.
	 *
	 * @param shortDescription The short description {@link String} for this command help.
	 * @param fullDescription  The full description {@link String} for this command help.
	 * @param usage            The {@link String} array that holds the usage for this command help.
	 */
	public EditableHelpTopic(@Nullable String shortDescription, @Nullable String fullDescription, @Nullable String[] usage) {
		Optional<String> shortDescriptionResult = Optional.ofNullable(shortDescription);
		this.shortDescription = () -> shortDescriptionResult;

		Optional<String> fullDescriptionResult = Optional.ofNullable(fullDescription);
		this.fullDescription = forWho -> fullDescriptionResult;

		Optional<String[]> usageResult = Optional.ofNullable(usage);
		this.usage = (forWho, argumentTree) -> usageResult;
	}

	// Static help results

	/**
	 * Sets the short description for this command help. This is the help which is
	 * shown in the main /help menu.
	 *
	 * @param description the short description for the command help
	 * @return this {@link EditableHelpTopic}
	 */
	public EditableHelpTopic<CommandSender> withShortDescription(@Nullable String description) {
		Optional<String> result = Optional.ofNullable(description);
		this.shortDescription = () -> result;

		return this;
	}

	/**
	 * Sets the full description for this command help. This is the help which is
	 * shown in the specific /help page for the command (e.g. /help mycommand).
	 *
	 * @param description the full description for this command help
	 * @return this {@link EditableHelpTopic}
	 */
	public EditableHelpTopic<CommandSender> withFullDescription(@Nullable String description) {
		Optional<String> result = Optional.ofNullable(description);
		this.fullDescription = forWho -> result;

		return this;
	}

	/**
	 * Sets the short and full description for this command help. This is a short-hand
	 * for the {@link #withShortDescription} and {@link #withFullDescription} methods.
	 *
	 * @param shortDescription the short description for this command help
	 * @param fullDescription  the full description for this command help
	 * @return this {@link EditableHelpTopic}
	 */
	public EditableHelpTopic<CommandSender> withHelp(@Nullable String shortDescription, @Nullable String fullDescription) {
		this.withShortDescription(shortDescription);
		this.withFullDescription(fullDescription);

		return this;
	}

	/**
	 * Sets the usage for this command help. This is the usage which is
	 * shown in the specific /help page for the command (e.g. /help mycommand).
	 *
	 * @param usage the full usage for this command
	 * @return this {@link EditableHelpTopic}
	 */
	public EditableHelpTopic<CommandSender> withUsage(@Nullable String... usage) {
		Optional<String[]> result = Optional.ofNullable(usage);
		this.usage = (forWho, argumentTree) -> result;

		return this;
	}

	// Dynamic help results

	/**
	 * Sets the short description of this command help to be generated using the given {@link ShortDescriptionGenerator}.
	 * This is the help which is shown in the main /help menu.
	 *
	 * @param description The {@link ShortDescriptionGenerator} to use to generate the short description.
	 * @return this {@link EditableHelpTopic}
	 */
	public EditableHelpTopic<CommandSender> withShortDescription(ShortDescriptionGenerator description) {
		this.shortDescription = description;

		return this;
	}

	/**
	 * Sets the full description of this command help to be generated using the given {@link FullDescriptionGenerator}.
	 * This is the help which is shown in the specific /help page for the command (e.g. /help mycommand).
	 *
	 * @param description The {@link FullDescriptionGenerator} to use to generate the full description.
	 * @return this {@link EditableHelpTopic}
	 */
	public EditableHelpTopic<CommandSender> withFullDescription(FullDescriptionGenerator<CommandSender> description) {
		this.fullDescription = description;

		return this;
	}

	/**
	 * Sets the usage of this command help to be generated using the given {@link UsageGenerator}.
	 * This is the usage which is shown in the specific /help page for the command (e.g. /help mycommand).
	 *
	 * @param usage The {@link UsageGenerator} to use to generate the usage.
	 * @return this {@link EditableHelpTopic}
	 */
	public EditableHelpTopic<CommandSender> withUsage(UsageGenerator<CommandSender> usage) {
		this.usage = usage;

		return this;
	}

	// Implement CommandAPIHelpTopic methods
	@Override
	public Optional<String> getShortDescription() {
		return shortDescription.getShortDescription();
	}

	@Override
	public Optional<String> getFullDescription(@Nullable CommandSender forWho) {
		return fullDescription.getFullDescription(forWho);
	}

	@Override
	public Optional<String[]> getUsage(@Nullable CommandSender forWho, @Nullable RegisteredCommand.Node<CommandSender> argumentTree) {
		return usage.getUsage(forWho, argumentTree);
	}

	// equals, hashCode, toString
	//  Since our fields are functions, they aren't easily compared
	//  However, the 'default' values returned by passing null parameters tend to make sense
	//  Just keep in mind that these return Optionals, and in the case of usage, the String[] should use Arrays methods
	@Override
	public final boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof EditableHelpTopic)) {
			return false;
		}
		EditableHelpTopic<?> other = (EditableHelpTopic<?>) obj;
		return Objects.equals(shortDescription.getShortDescription(), other.shortDescription.getShortDescription())
			&& Objects.equals(fullDescription.getFullDescription(null), other.fullDescription.getFullDescription(null))
			&& Arrays.equals(usage.getUsage(null, null).orElse(null), other.usage.getUsage(null, null).orElse(null));
	}

	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(usage.getUsage(null, null).orElse(null));
		result = prime * result + Objects.hash(shortDescription.getShortDescription(), fullDescription.getFullDescription(null));
		return result;
	}

	@Override
	public final String toString() {
		return "EditableHelpTopic ["
			+ "shortDescription=" + shortDescription.getShortDescription()
			+ ", fullDescription=" + fullDescription.getFullDescription(null)
			+ ", usage=" + usage.getUsage(null, null).map(Arrays::toString) + "]";
	}
}
