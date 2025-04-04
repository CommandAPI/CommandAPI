package dev.jorel.commandapi.arguments;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.BukkitTooltip;
import dev.jorel.commandapi.arguments.parser.ParserArgument;
import dev.jorel.commandapi.arguments.parser.ParserLiteral;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

/**
 * Utilities for creating mock argument parsers
 */
public class ArgumentUtilities {
	private ArgumentUtilities() {
	}

	// Translatable messages
	/**
	 * Turns a translation key into a Brigadier {@link Message}. These keys can be a default
	 * <a href=https://gist.github.com/sppmacd/82af47c83b225d4ffd33bb0c27b0d932>Minecraft Language Code</a>
	 * or any translation loaded by a plugin.
	 *
	 * @param key The key for the message translation.
	 * @return A Brigadier {@link Message} that represents the given language key.
	 */
	public static Message translatedMessage(String key) {
		return BukkitTooltip.messageFromAdventureComponent(Component.translatable(key));
	}

	/**
	 * Turns a translation key into a Brigadier {@link Message}. These keys can be a default
	 * <a href=https://gist.github.com/sppmacd/82af47c83b225d4ffd33bb0c27b0d932>Minecraft Language Code</a>
	 * or any translation loaded by a plugin.
	 *
	 * @param key  The key for the message translation.
	 * @param args Objects to insert into the string at the location of
	 *             {@code %s} markers in the resulting translated message.
	 * @return A Brigadier {@link Message} that represents the given language key.
	 */
	public static Message translatedMessage(String key, Object... args) {
		TextComponent[] argsComponents = new TextComponent[args.length];
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			String text = arg.toString();
			argsComponents[i] = Component.text(text);
		}
		return BukkitTooltip.messageFromAdventureComponent(Component.translatable(key, argsComponents));
	}

	// Parser utilities

	/**
	 * Returns a new {@link ParserLiteral}. When the returned parser is invoked, it tries to read the given
	 * {@code literal} String from the input {@link StringReader}. If the {@code literal} is present, this parser
	 * succeeds and moves {@link StringReader#getCursor()} to the end of the {@code literal}. Otherwise, this parser
	 * will fail and throw a {@link CommandSyntaxException} with type {@link BuiltInExceptionProvider#literalIncorrect()}.
	 *
	 * @param literal The exact String that is expected to be at the start of the input {@link StringReader}.
	 * @return A {@link ParserLiteral} that checks if the {@code literal} String can be read from the input {@link StringReader}.
	 */
	public static ParserLiteral literal(String literal) {
		return reader -> {
			if (reader.canRead(literal.length())) {
				int start = reader.getCursor();
				int end = start + literal.length();

				if (reader.getString().substring(start, end).equals(literal)) {
					reader.setCursor(end);
					return;
				}
			}
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().createWithContext(reader, literal);
		};
	}

	/**
	 * Returns a new {@link ParserArgument} that reads characters from the input {@link StringReader} until it reaches
	 * the given terminator character. If the terminator character is not found, the entire
	 * {@link StringReader#getRemaining()} String will be read.
	 * <p>
	 * Note - This is intended to work like {@link StringReader#readStringUntil(char)} but with two differences:
	 * 1. This method does not treat {@code \} as a special character. {@link StringReader#readStringUntil(char)} allows
	 * the terminator character to be escaped by unescaped {@code \}.
	 * 2. This method does not throw a {@link CommandSyntaxException} if the end of the string is reached without a terminator.
	 * {@link StringReader#readStringUntil(char)} will throw {@link BuiltInExceptionProvider#readerExpectedEndOfQuote()}
	 * if this happens.
	 *
	 * @param terminator The character to stop reading at.
	 * @return A {@link ParserArgument} that reads until it finds the given terminator. Note that the returned String will
	 * include the terminator at the end, unless the end of the input {@link StringReader} is reached without finding the
	 * terminator.
	 */
	public static ParserArgument<String> readUntilWithoutEscapeCharacter(char terminator) {
		return reader -> {
			int start = reader.getCursor();

			int end = reader.getString().indexOf(terminator, start);
			if (end == -1) {
				end = reader.getTotalLength();
			}

			reader.setCursor(end);
			return reader.getString().substring(start, end);
		};
	}
}
