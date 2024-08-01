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
package dev.jorel.commandapi.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.executors.CommandArguments;

import java.util.Collection;

/**
 * An argument that represents a scoreholder's name, or a collection of scoreholder names
 * 
 * @since 3.0
 */
public class ScoreHolderArgument {

	private ScoreHolderArgument() {
		throw new IllegalStateException("Use ScoreHolderArgument.Single or ScoreHolderArgument.Multiple instead");
	}

	/**
	 * An argument that represents a scoreholder's name
	 *
	 * @apiNote Returns a {@link String} object
	 */
	public static class Single extends Argument<String> {

		/**
		 * A Score Holder argument. Represents a single score holder
		 * @param nodeName the name of the node for this argument
		 */
		public Single(String nodeName) {
			super(nodeName, CommandAPIBukkit.get().getNMS()._ArgumentScoreholder(ArgumentSubType.SCOREHOLDER_SINGLE));
		}

		@Override
		public Class<String> getPrimitiveType() {
			return String.class;
		}

		@Override
		public CommandAPIArgumentType getArgumentType() {
			return CommandAPIArgumentType.SCORE_HOLDER;
		}

		@Override
		public <CommandSourceStack> String parseArgument(CommandContext<CommandSourceStack> cmdCtx, String key, CommandArguments previousArgs) throws CommandSyntaxException {
			return CommandAPIBukkit.<CommandSourceStack>get().getNMS().getScoreHolderSingle(cmdCtx, key);
		}

	}

	/**
	 * An argument that represents a collection of scoreholder names
	 *
	 * @apiNote Returns a {@link Collection<String>} object
	 */
	@SuppressWarnings("rawtypes")
	public static class Multiple extends Argument<Collection> {

		/**
		 * A Score Holder argument. Represents a collection of score holders.
		 * @param nodeName the name of the node for this argument
		 */
		public Multiple(String nodeName) {
			super(nodeName, CommandAPIBukkit.get().getNMS()._ArgumentScoreholder(ArgumentSubType.SCOREHOLDER_MULTIPLE));
		}

		@Override
		public Class<Collection> getPrimitiveType() {
			return Collection.class;
		}

		@Override
		public CommandAPIArgumentType getArgumentType() {
			return CommandAPIArgumentType.SCORE_HOLDER;
		}

		@Override
		public <CommandSourceStack> Collection<String> parseArgument(CommandContext<CommandSourceStack> cmdCtx, String key, CommandArguments previousArgs) throws CommandSyntaxException {
			return CommandAPIBukkit.<CommandSourceStack>get().getNMS().getScoreHolderMultiple(cmdCtx, key);
		}

	}
}
