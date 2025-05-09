/*******************************************************************************
 * Copyright 2018, 2020 Jorel Ali (Skepter) - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package dev.jorel.commandapi;

import com.mojang.brigadier.Message;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.Collection;
import java.util.function.Function;

/**
 * This class represents a suggestion for an argument with a hover tooltip text
 * for that suggestion. This class is parameterized over some object S that
 * represents the safe cast type for argument suggestions. This class is to be
 * used with safe suggestion overrides, via the
 * SafeOverrideableArgument.safeOverrideSuggestionsT method.
 *
 * @param <S> the object that the argument suggestions use
 */
public class BukkitTooltip<S> extends Tooltip<S> {

	protected BukkitTooltip(S object, Message tooltip) {
		super(object, tooltip);
	}

	/**
	 * Constructs a collection of {@link Tooltip <S>} objects from an array of suggestions, and a function which generates a
	 * tooltip formatted as an array of {@link BaseComponent}s for each suggestion
	 *
	 * @param <S> the object that the argument suggestions use
	 * @param tooltipGenerator function which returns a formatted tooltip for the suggestion, an array of {@link BaseComponent}s
	 * @param suggestions array of suggestions to provide to the user
	 *
	 * @return a collection of {@link Tooltip <S>} objects from the provided suggestions, with the generated formatted
	 * 	tooltips
	 */
	@SafeVarargs
	public static <S> Collection<Tooltip<S>> generateBaseComponents(Function<S, BaseComponent[]> tooltipGenerator, S... suggestions) {
		return generate(tooltipGenerator, BukkitTooltip::ofBaseComponents, suggestions);
	}

	/**
	 * Constructs a collection of {@link Tooltip <S>} objects from a collection of suggestions, and a function which generates a
	 * tooltip formatted as an array of {@link BaseComponent}s for each suggestion
	 *
	 * @param <S> the object that the argument suggestions use
	 * @param tooltipGenerator function which returns a formatted tooltip for the suggestion, an array of {@link BaseComponent}s
	 * @param suggestions collection of suggestions to provide to the user
	 *
	 * @return a collection of {@link Tooltip <S>} objects from the provided suggestions, with the generated formatted
	 * 	tooltips
	 */
	public static <S> Collection<Tooltip<S>> generateBaseComponents(Function<S, BaseComponent[]> tooltipGenerator, Collection<S> suggestions) {
		return generate(tooltipGenerator, BukkitTooltip::ofBaseComponents, suggestions);
	}

	/**
	 * Constructs a collection of {@link Tooltip <S>} objects from an array of suggestions, and a function which generates a
	 * tooltip formatted as an adventure {@link ComponentLike} for each suggestion
	 *
	 * @param <S> the object that the argument suggestions use
	 * @param tooltipGenerator function which returns a formatted tooltip for the suggestion, an adventure {@link ComponentLike}
	 * @param suggestions array of suggestions to provide to the user
	 *
	 * @return a collection of {@link Tooltip <S>} objects from the provided suggestions, with the generated formatted
	 * 	tooltips
	 */
	@SafeVarargs
	public static <S> Collection<Tooltip<S>> generateAdventureComponents(Function<S, ComponentLike> tooltipGenerator, S... suggestions) {
		return generate(tooltipGenerator, BukkitTooltip::ofAdventureComponent, suggestions);
	}

	/**
	 * Constructs a collection of {@link Tooltip <S>} objects from a collection of suggestions, and a function which generates a
	 * tooltip formatted as an adventure {@link ComponentLike} for each suggestion
	 *
	 * @param <S> the object that the argument suggestions use
	 * @param tooltipGenerator function which returns a formatted tooltip for the suggestion, an adventure {@link ComponentLike}
	 * @param suggestions collection of suggestions to provide to the user
	 *
	 * @return a collection of {@link Tooltip <S>} objects from the provided suggestions, with the generated formatted
	 * 	tooltips
	 */
	public static <S> Collection<Tooltip<S>> generateAdventureComponents(Function<S, ComponentLike> tooltipGenerator, Collection<S> suggestions) {
		return generate(tooltipGenerator, BukkitTooltip::ofAdventureComponent, suggestions);
	}

	/**
	 * Constructs a <code>BukkitTooltip&lt;S&gt;</code> with a suggestion and a formatted tooltip
	 *
	 * @param <S> the object that the argument suggestions use
	 * @param object the suggestion to provide to the user
	 * @param tooltip    the formatted tooltip to show to the user when they hover over the
	 *                   suggestion
	 * @return a <code>BukkitTooltip&lt;S&gt;</code> representing this suggestion and tooltip
	 */
	public static <S> Tooltip<S> ofBaseComponents(S object, BaseComponent... tooltip) {
		return ofMessage(object, messageFromBaseComponents(tooltip));
	}

	/**
	 * Constructs a <code>BukkitTooltip&lt;S&gt;</code> with a suggestion and a formatted tooltip
	 *
	 * @param <S> the object that the argument suggestions use
	 * @param object the suggestion to provide to the user
	 * @param tooltip    the formatted tooltip to show to the user when they hover over the
	 *                   suggestion
	 * @return a <code>BukkitTooltip&lt;S&gt;</code> representing this suggestion and tooltip
	 */
	public static <S> Tooltip<S> ofAdventureComponent(S object, ComponentLike tooltip) {
		return ofMessage(object, messageFromAdventureComponent(tooltip));
	}

	/**
	 * Converts a formatted bungee text component to a native minecraft text component which can be used natively by brigadier.
	 *
	 * This supports all forms of formatting including entity selectors, scores,
	 * click &amp; hover events, translations, keybinds and more.
	 *
	 * Note: the bungee component api is deprecated, and the adventure text component api should be used instead
	 *
	 * @param components array of bungee text components
	 * @return native minecraft message object which can be used natively by brigadier.
	 */
	public static Message messageFromBaseComponents(BaseComponent... components) {
		return CommandAPIBukkit.get().getNMS().generateMessageFromJson(ComponentSerializer.toString(components));
	}

	/**
	 * Converts a formatted adventure text component to a native minecraft text component which can be used natively by brigadier.
	 *
	 * This supports all forms of formatting including entity selectors, scores,
	 * click &amp; hover events, translations, keybinds and more.
	 **
	 * @param component adventure text component
	 * @return native minecraft message object which can be used natively by brigadier.
	 */
	public static Message messageFromAdventureComponent(ComponentLike component) {
		return CommandAPIBukkit.get().getNMS().generateMessageFromJson(GsonComponentSerializer.gson().serialize(component.asComponent()));
	}

}
