package dev.jorel.commandapi.arguments;

import dev.jorel.commandapi.wrappers.PreviewableFunction;

import java.util.Optional;

/**
 * An interface representing that the argument can be previewed using
 * Minecraft's chat preview feature. To use this, the server must have
 * {@code previews-chat=true} set in its {@code server.properties} file
 */
public interface Previewable<Impl
/// @cond DOX
extends AbstractArgument<?, ?, ?, ?>
/// @endcond
, T, Player> {

	/**
	 * Sets the {@link PreviewableFunction} for this argument. This function will
	 * run when a player requests a chat preview from the server. The function's
	 * {@link PreviewInfo} will be populated based on the input for the relevant
	 * player that requests the chat preview.
	 * 
	 * @param preview the function to use for chat preview generation
	 * @return the current argument
	 */
	public Impl withPreview(PreviewableFunction<T, Player> preview);

	/**
	 * @return the {@link PreviewableFunction} for this argument, as an
	 *         {@link Optional}.
	 */
	public Optional<PreviewableFunction<T, Player>> getPreview();

	/**
	 * Whether this argument should use the preview result as the argument value for
	 * execution. If set to true, accessing this argument in {@code args} will
	 * return the same value generated from
	 * {@link Previewable#withPreview(PreviewableFunction)}
	 * 
	 * @param usePreview whether to use the preview as the value for this argument
	 * @return the current argument
	 */
	public Impl usePreview(boolean usePreview);

	/**
	 * @return whether this argument uses legacy chat component APIs
	 */
	public boolean isLegacy();

}
