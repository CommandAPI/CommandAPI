/*******************************************************************************
 * Copyright 2022 Jorel Ali (Skepter) - MIT License
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

import java.util.Collection;
import java.util.function.Function;

import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.IStringTooltip;
import dev.jorel.commandapi.SuggestionInfo;

/**
 * An argument that accepts a list of objects
 * 
 * @since 8.6.0
 * 
 * @param <T> the type that this list argument generates a list of.
 */
public class ListTextArgument<T> extends ListArgumentCommon<T> {
	ListTextArgument(String nodeName, String delimiter, boolean allowDuplicates, Function<SuggestionInfo<CommandSender>, Collection<T>> supplier, Function<T, IStringTooltip> suggestionsMapper, boolean allowAnyValue) {
		super(nodeName, delimiter, allowDuplicates, supplier, suggestionsMapper, true, allowAnyValue);
	}
}
