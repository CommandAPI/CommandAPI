package dev.jorel.commandapi.arguments;

public record PreviewInfo<T, Player> (
	/** @param player the Player typing this command */
	Player player,

	/**
	 * @param input the current partially typed argument. For example "/mycmd tes"
	 *              will return "tes"
	 */
	String input,

	/**
	 * @param fullInput a string representing the full current input (including /)
	 */
	String fullInput,

	/**
	 * @param parsedInput the parsed input as a BaseComponent[] (spigot) or
	 *                    Component (paper)
	 */
	T parsedInput) {

}
