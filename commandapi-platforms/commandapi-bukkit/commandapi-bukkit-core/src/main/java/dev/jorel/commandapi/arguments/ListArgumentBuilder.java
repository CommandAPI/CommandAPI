package dev.jorel.commandapi.arguments;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.IStringTooltip;
import dev.jorel.commandapi.StringTooltip;
import dev.jorel.commandapi.SuggestionInfo;

/**
 * A builder to create a ListArgument
 * 
 * @param <T> the type that the list argument generates a list of.
 */
public class ListArgumentBuilder<T> {

	private final String nodeName;
	private final String delimiter;
	private boolean allowDuplicates = false;
	private boolean allowAnyValue = false;

	/**
	 * Creates a new ListArgumentBuilder with a specified node name. Defaults the
	 * delimiter for each element in the list to a space
	 * 
	 * @param nodeName the name of the node for this argument
	 */
	public ListArgumentBuilder(String nodeName) {
		this(nodeName, " ");
	}

	/**
	 * Creates a new ListArgumentBuilder with a specified node name
	 * 
	 * @param nodeName  the name of the node for this argument
	 * @param delimiter the separator for each element in the list (for example, a
	 *                  space or a comma)
	 */
	public ListArgumentBuilder(String nodeName, String delimiter) {
		this.nodeName = nodeName;
		this.delimiter = delimiter;
	}

	/**
	 * Whether duplicates are allowed in the provided list. By default, duplicates
	 * are not allowed.
	 * 
	 * @param allowDuplicates whether to enable duplicates or not
	 * @return this list argument builder
	 */
	public ListArgumentBuilder<T> allowDuplicates(boolean allowDuplicates) {
		this.allowDuplicates = allowDuplicates;
		return this;
	}

	/**
	 * Whether any value should be allowed in the list
	 *
	 * @param allowAnyValue whether to allow any value
	 * @return this list argument builder
	 */
	public ListArgumentBuilder<T> allowAnyValue(boolean allowAnyValue) {
		this.allowAnyValue = allowAnyValue;
		return this;
	}

	/**
	 * Specifies the list to use to generate suggestions for the list argument
	 * 
	 * @param list a function that accepts a {@link SuggestionInfo} and returns a collection
	 *             of elements to suggest for this list argument
	 * @return this list argument builder
	 */
	public ListArgumentBuilderSuggests withList(Function<SuggestionInfo<CommandSender>, Collection<T>> list) {
		return new ListArgumentBuilderSuggests(list);
	}

	/**
	 * Specifies the list to use to generate suggestions for the list argument
	 * 
	 * @param list a supplier that returns a collection of elements to suggest for
	 *             this list argument
	 * @return this list argument builder
	 */
	public ListArgumentBuilderSuggests withList(Supplier<Collection<T>> list) {
		return withList(info -> list.get());
	}

	/**
	 * Specifies the list to use to generate suggestions for the list argument
	 * 
	 * @param list a collection of elements to suggest for this list argument
	 * @return this list argument builder
	 */
	public ListArgumentBuilderSuggests withList(Collection<T> list) {
		return withList(info -> list);
	}

	/**
	 * Specifies the list to use to generate suggestions for the list argument
	 *
	 * @param array an array of elements to suggest for this list argument
	 * @return this list argument builder
	 */
	@SafeVarargs
	public final ListArgumentBuilderSuggests withList(T... array) {
		List<T> list = List.of(array);
		return withList(info -> list);
	}

	/**
	 * An intermediary class for the {@link ListArgumentBuilder}
	 */
	public class ListArgumentBuilderSuggests {

		private final Function<SuggestionInfo<CommandSender>, Collection<T>> supplier;

		private ListArgumentBuilderSuggests(Function<SuggestionInfo<CommandSender>, Collection<T>> list) {
			this.supplier = list;
		}

		/**
		 * Specifies that the mapping function for this argument calls the
		 * <code>toString()</code> method.
		 * 
		 * @return this list argument builder
		 */
		public ListArgumentBuilderFinished withStringMapper() {
			return withStringTooltipMapper(t -> StringTooltip.none(String.valueOf(t)));
		}

		/**
		 * Specifies the mapping function of the specific type <code>T</code> to a
		 * <code>String</code> so an element can be shown to a user as suggestions.
		 * 
		 * @param mapper the mapping function that creates a {@link String}
		 * @return this list argument builder
		 */
		public ListArgumentBuilderFinished withMapper(Function<T, String> mapper) {
			if (allowAnyValue && !(mapper instanceof StringTooltip)){
				throw new IllegalArgumentException("allowAnyValue cannot be used with non-string mappers");
			}
			return withStringTooltipMapper(t -> StringTooltip.none(mapper.apply(t)));
		}

		/**
		 * Specifies the mapping function of the specific type <code>T</code> to a
		 * {@link IStringTooltip} so an element can be shown to a user as a suggestion
		 * with a tooltip.
		 * 
		 * @param mapper the mapping function that creates an {@link IStringTooltip}
		 * @return this list argument builder
		 */
		public ListArgumentBuilderFinished withStringTooltipMapper(Function<T, IStringTooltip> mapper) {
			return new ListArgumentBuilderFinished(mapper);
		}

		/**
		 * An intermediary class for the {@link ListArgumentBuilder}
		 */
		public class ListArgumentBuilderFinished {

			private final Function<T, IStringTooltip> mapper;

			private ListArgumentBuilderFinished(Function<T, IStringTooltip> mapper) {
				this.mapper = mapper;
			}
			
			/**
			 * Builds this list argument.
			 * 
			 * @return a {@link ListArgument}
			 */
			public ListArgument<T> buildGreedy() {
				return new ListArgument<>(nodeName, delimiter, allowDuplicates, supplier, mapper, allowAnyValue);
			}
			
			/**
			 * Builds this list argument using a {@link TextArgument} as the underlying implementation.
			 * 
			 * @return a {@link ListTextArgument}
			 */
			public ListTextArgument<T> buildText() {
				return new ListTextArgument<>(nodeName, delimiter, allowDuplicates, supplier, mapper, allowAnyValue);
			}
		}
	}
}