package dev.jorel.commandapi.arguments;

import java.util.List;

/**
 * A builder to create a {@link MapArgument}
 *
 * @param <K> The type of keys this map should contain
 * @param <V> The type of values this map should contain
 */
public class MapArgumentBuilder<K, V> {

	private final String nodeName;
	private final String delimiter;
	private final String separator;

	/**
	 * Creates a new MapArgumentBuilder with a specified node name. Defaults the
	 * delimiter for each key/value pair to a colon
	 *
	 * @param nodeName the name of the node for this argument
	 */
	public MapArgumentBuilder(String nodeName) {
		this(nodeName, ':');
	}

	/**
	 * Creates a new MapArgumentBuilder with a specified node name
	 *
	 * @param nodeName  the name of the node for this argument
	 * @param delimiter the separator for each key/value pair
	 */
	public MapArgumentBuilder(String nodeName, char delimiter) {
		this(nodeName, delimiter, " ");
	}

	/**
	 * Creates a new MapArgumentBuilder with a specified node name
	 *
	 * @param nodeName  the name of the node for this argument
	 * @param delimiter the separator for each key/value pair
	 * @param separator the separator between a key and a value
	 */
	public MapArgumentBuilder(String nodeName, char delimiter, String separator) {
		if(separator == null) throw new IllegalArgumentException("The separator cannot be null!");
		if(separator.isEmpty()) throw new IllegalArgumentException("The separator cannot be an empty String!");
		
		this.nodeName = nodeName;
		this.delimiter = String.valueOf(delimiter);
		this.separator = separator;
	}

	/**
	 * This starts the builder for the {@link MapArgument}
	 *
	 * @return this map argument builder
	 */
	public MapArgumentBuilderValueMapper withKeyMapper(StringParser<K> keyMapper) {
		return new MapArgumentBuilderValueMapper(keyMapper);
	}

	/**
	 * An intermediary class for the {@link MapArgumentBuilder}
	 */
	public class MapArgumentBuilderValueMapper {

		private final StringParser<K> keyMapper;

		public MapArgumentBuilderValueMapper(StringParser<K> keyMapper) {
			this.keyMapper = keyMapper;
		}

		/**
		 * Specifies the mapping function to convert a <code>String</code>
		 * into the specific type <code>V</code>
		 *
		 * @param valueMapper the mapping function that creates an instance of <code>V</code>
		 * @return this map argument builder
		 */
		public MapArgumentBuilderSuggestsKey withValueMapper(StringParser<V> valueMapper) {
			return new MapArgumentBuilderSuggestsKey(valueMapper);
		}

		/**
		 * An intermediary class for the {@link MapArgumentBuilder}
		 */
		public class MapArgumentBuilderSuggestsKey {

			private final StringParser<V> valueMapper;

			public MapArgumentBuilderSuggestsKey(StringParser<V> valueMapper) {
				this.valueMapper = valueMapper;
			}

			/**
			 * Provides a list of keys to suggest when a user types this argument. All keys given for the final map must
			 * come from this list, otherwise a CommandSyntaxException is thrown.
			 * <p>
			 * Duplicate keys are never allowed since one key can only be mapped to one value. If a duplicate key is given,
			 * a CommandSyntaxException is thrown.
			 *
			 * @param keyList A list of keys to suggest.
			 * @return this map argument builder
			 */
			public MapArgumentBuilderSuggestsValue withKeyList(List<String> keyList) {
				return new MapArgumentBuilderSuggestsValue(keyList);
			}

			/**
			 * When using this method, no key suggestions are displayed and any keys can be given for the final map. To
			 * restrict the keys that can be given for this map, use {@link #withKeyList(List)} instead.
			 * <p>
			 * Duplicate keys are never allowed since one key can only be mapped to one value. If a duplicate key is given,
			 * a CommandSyntaxException is thrown.
			 *
			 * @return this map argument builder
			 */
			public MapArgumentBuilderSuggestsValue withoutKeyList() {
				return withKeyList(null);
			}

			/**
			 * An intermediary class for the {@link MapArgumentBuilder}
			 */
			public class MapArgumentBuilderSuggestsValue {

				private final List<String> keyList;

				public MapArgumentBuilderSuggestsValue(List<String> keyList) {
					this.keyList = keyList;
				}

				/**
				 * Provides a list of values to suggest when a user types this argument. All values given for the final
				 * map must come from this list, otherwise a CommandSyntaxException is thrown.
				 * <p>
				 * Additionally, duplicate values are not allowed. If a duplicate value is given, a CommandSyntaxException
				 * is thrown. To allow duplicate values, use the {@link #withValueList(List, boolean)} method instead.
				 *
				 * @param valueList A list of values to suggest. The values need to be Strings, so they can be suggested
				 * @return this map argument builder
				 */
				public MapArgumentBuilderFinished withValueList(List<String> valueList) {
					return withValueList(valueList, false);
				}

				/**
				 * Provides a list of values to suggest when a user types this argument. All values given for the final
				 * map must come from this list, otherwise a CommandSyntaxException is thrown.
				 * <p>
				 * If allowDuplicates is true, then multiple keys may be given the same value. Otherwise, if a duplicate
				 * value is given, a CommandSyntaxException is thrown.
				 *
				 * @param valueList A list of values to suggest. The values need to be Strings, so they can be suggested
				 * @param allowDuplicates Decides if a value can be written more than once
				 * @return this map argument builder
				 */
				public MapArgumentBuilderFinished withValueList(List<String> valueList, boolean allowDuplicates) {
					return new MapArgumentBuilderFinished(valueList, allowDuplicates);
				}

				/**
				 * When using this method, no value suggestions are displayed and any values can be given for the final
				 * map. To restrict the values that can be given for this map, use {@link #withValueList(List)} instead.
				 * <p>
				 * Additionally, duplicate values are not allowed. If a duplicate value is given, a CommandSyntaxException
				 * is thrown. To allow duplicate values, use the {@link #withoutValueList(boolean)} method instead.
				 *
				 * @return this map argument builder
				 */
				public MapArgumentBuilderFinished withoutValueList() {
					return withValueList(null);
				}

				/**
				 * When using this method, no value suggestions are displayed and any values can be given for the final
				 * map. To restrict the values that can be given for this map, use {@link #withValueList(List, boolean)} instead.
				 * <p>
				 * If allowDuplicates is true, then multiple keys may be given the same value. Otherwise, if a duplicate
				 * value is given, a CommandSyntaxException is thrown.
				 *
				 * @param allowDuplicates Decides if a value can be written more than once
				 * @return this map argument builder
				 */
				public MapArgumentBuilderFinished withoutValueList(boolean allowDuplicates) {
					return withValueList(null, allowDuplicates);
				}

				/**
				 * An intermediary class for the {@link MapArgumentBuilder}
				 */
				public class MapArgumentBuilderFinished {

					private final List<String> valueList;
					private final boolean allowValueDuplicates;

					public MapArgumentBuilderFinished(List<String> valueList, boolean allowValueDuplicates) {
						this.valueList = valueList;
						this.allowValueDuplicates = allowValueDuplicates;
					}

					/**
					 * Builds this {@link MapArgument}
					 *
					 * @return a new {@link MapArgument}
					 */
					public MapArgument<K, V> build() {
						return new MapArgument<>(nodeName, delimiter, separator, keyMapper, valueMapper, keyList, valueList, allowValueDuplicates);
					}
				}
			}
		}
	}
}
