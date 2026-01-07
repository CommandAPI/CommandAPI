package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.MapArgument;
import dev.jorel.commandapi.arguments.MapArgumentBuilder;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.StringParser;
import dev.jorel.commandapi.exceptions.GreedyArgumentException;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the {@link MapArgument}
 */
public class ArgumentMapTests extends TestBase {

	/*********
	 * Setup *
	 *********/

	@BeforeEach
	public void setUp() {
		super.setUp();
	}

	@AfterEach
	public void tearDown() {
		super.tearDown();
	}

	/*********
	 * Tests *
	 *********/

	// All ways of constructing the MapArgument:
	//  Default separators | define delimiter | define separator
	//  Key mapper | map to non-string | map and throw errors
	//  Value mapper | map to non-string | map and throw errors
	//  Without key list | with key list | keys containing delimiter
	//  Without value list | with value list | values containing delimiter
	//  No duplicates | allow duplicate values
	@Test
	void exceptionTestWithMapArgument() {
		// A MapArgument is a GreedyArgument. It is only allowed at the end of the arguments list.
		// A GreedyArgumentException should be thrown when it is not the last argument.
		CommandAPICommand command =
			new CommandAPICommand("test")
				.withArguments(
					// 'Default' MapArgument
					new MapArgumentBuilder<String, String>("map")
						.withKeyMapper(s -> s)
						.withValueMapper(s -> s)
						.withoutKeyList()
						.withoutValueList()
						.build()
				)
				.executesPlayer(P_EXEC);
		assertDoesNotThrow(() -> command.register());

		command.withArguments(new StringArgument("string"));
		assertThrows(GreedyArgumentException.class, command::register);

		// Separator cannot be null or an empty string
		assertThrows(IllegalArgumentException.class, () -> new MapArgumentBuilder<>("map", ':', null));
		assertThrows(IllegalArgumentException.class, () -> new MapArgumentBuilder<>("map", ':', ""));
		assertDoesNotThrow(() -> new MapArgumentBuilder<>("map", ':', " "));
	}

	@Test
	void executionTestWithMapArgument() {
		Mut<Map<String, String>> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(
				// 'Default' MapArgument
				new MapArgumentBuilder<String, String>("map")
					.withKeyMapper(s -> s)
					.withValueMapper(s -> s)
					.withoutKeyList()
					.withoutValueList()
					.build()
			)
			.executesPlayer((player, args) -> {
				results.set(args.getUnchecked("map"));
			})
			.register();

		Player player = addPlayer();


		Map<String, String> expectedMap = new LinkedHashMap<>();

		expectedMap.put("key1", "value1");
		// /test key1:value1
		// Input map with 1 unquoted key-value pair
		assertStoresResult(player, "test key1:value1", results, expectedMap);
		// /test "key1":"value1"
		// Input map with 1 quoted key-value pair
		assertStoresResult(player, "test \"key1\":\"value1\"", results, expectedMap);

		expectedMap.put("key2", "value2");
		// /test key1:value1 key2:value2
		// Input map with 2 unquoted key-value pairs
		assertStoresResult(player, "test key1:value1 key2:value2", results, expectedMap);
		// /test "key1":"value1" "key2":"value2"
		// Input map with 2 quoted key-value pairs
		assertStoresResult(player, "test \"key1\":\"value1\" \"key2\":\"value2\"", results, expectedMap);

		expectedMap.put("key3", "value3");
		// /test key1:value1 key2:value2 key3:value3
		// Input map with 3 unquoted key-value pairs
		assertStoresResult(player, "test key1:value1 key2:value2 key3:value3", results, expectedMap);
		// /test "key1":"value1" "key2":"value2" "key3":"value3"
		// Input map with 3 quoted key-value pairs
		assertStoresResult(player, "test \"key1\":\"value1\" \"key2\":\"value2\" \"key3\":\"value3\"", results, expectedMap);


		// /test key1:value1 key1:value2
		// Error for having duplicate unquoted key1
		assertCommandFailsWith(player, "test key1:value1 key1:value2", "Could not parse command: Duplicate keys are not allowed! at position 12: ...y1:value1 <--[HERE]");
		// /test key1:value1 "key1":value2
		// Error for having duplicate quoted key1
		assertCommandFailsWith(player, "test key1:value1 \"key1\":value2", "Could not parse command: Duplicate keys are not allowed! at position 12: ...y1:value1 <--[HERE]");
		// /test key1:value1 key1
		// Error for having duplicate unquoted key1 without delimiter
		assertCommandFailsWith(player, "test key1:value1 key1", "Could not parse command: Duplicate keys are not allowed! at position 12: ...y1:value1 <--[HERE]");

		// /test key1:value1 key2:value1
		// Error for having duplicate unquoted value1
		assertCommandFailsWith(player, "test key1:value1 key2:value1", "Could not parse command: Duplicate values are not allowed! at position 17: ...lue1 key2:<--[HERE]");
		// /test key1:value1 key2:"value1"
		// Error for having duplicate quoted value1
		assertCommandFailsWith(player, "test key1:value1 key2:\"value1\"", "Could not parse command: Duplicate values are not allowed! at position 17: ...lue1 key2:<--[HERE]");


		// /test key1:value1 key2:
		// Error for missing value after unquoted key
		assertCommandFailsWith(player, "test key1:value1 key2:", "Could not parse command: Expected a value after the delimiter at position 17: ...lue1 key2:<--[HERE]");
		// /test key1:value1 "key2":
		// Error for missing value after quoted key
		assertCommandFailsWith(player, "test key1:value1 \"key2\":", "Could not parse command: Expected a value after the delimiter at position 19: ...e1 \"key2\":<--[HERE]");

		// /test key1:value1
		// Error for missing key after unquoted value
		assertCommandFailsWith(player, "test key1:value1 ", "Could not parse command: Expected a key after the separator at position 12: ...y1:value1 <--[HERE]");
		// /test key1:"value1"
		// Error for missing key after quoted value
		assertCommandFailsWith(player, "test key1:\"value1\" ", "Could not parse command: Expected a key after the separator at position 14: ...:\"value1\" <--[HERE]");


		// /test key1:value1 key2
		// Error for missing delimiter after unquoted key
		assertCommandFailsWith(player, "test key1:value1 key2", "Could not parse command: Delimiter \":\" required after writing a key at position 12: ...y1:value1 <--[HERE]");
		// /test key1:value1 "key2"
		// Error for missing delimiter after quoted key
		assertCommandFailsWith(player, "test key1:value1 \"key2\"", "Could not parse command: Delimiter \":\" required after writing a key at position 18: ...ue1 \"key2\"<--[HERE]");


		// /test key1:value1 "key2"=value2
		// Error for incorrect delimiter
		assertCommandFailsWith(player, "test key1:value1 \"key2\"=value2", "Could not parse command: Delimiter \":\" required after writing a key at position 18: ...ue1 \"key2\"<--[HERE]");
		// /test key1:"value1",key2=value2
		// Error for incorrect separator
		assertCommandFailsWith(player, "test key1:\"value1\",key2=value2", "Could not parse command: Separator \" \" required after writing a value at position 13: ...1:\"value1\"<--[HERE]");


		// /test key1:value1 "key2
		// Error for quoted key not closing
		assertCommandFailsWith(player, "test key1:value1 \"key2", "Could not parse command: A quoted key must end with a quotation mark at position 13: ...1:value1 \"<--[HERE]");
		// /test key1:value1 key2:"value2
		// Error for quoted value not closing
		assertCommandFailsWith(player, "test key1:value1 key2:\"value2", "Could not parse command: A quoted value must end with a quotation mark at position 18: ...ue1 key2:\"<--[HERE]");


		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithSpecialCharacters() {
		// Special characters are \ " and the separator and delimiter
		Mut<Map<String, String>> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(
				// 'Default' MapArgument
				new MapArgumentBuilder<String, String>("map")
					.withKeyMapper(s -> s)
					.withValueMapper(s -> s)
					.withoutKeyList()
					.withoutValueList()
					.build()
			)
			.executesPlayer((player, args) -> {
				results.set(args.getUnchecked("map"));
			})
			.register();

		Player player = addPlayer();


		// /test \"HelloWorld":value
		// Input quotes into unquoted key
		assertStoresResult(player, "test \\\"HelloWorld\":value", results, Map.of("\"HelloWorld\"", "value"));
		// /test "\"HelloWorld\"":value
		// Input quotes into quoted key
		assertStoresResult(player, "test \"\\\"HelloWorld\\\"\":value", results, Map.of("\"HelloWorld\"", "value"));

		// /test key:\"HelloWorld"
		// Input quotes into unquoted value
		assertStoresResult(player, "test key:\\\"HelloWorld\"", results, Map.of("key", "\"HelloWorld\""));
		// /test key:"\"HelloWorld\""
		// Input quotes into quoted value
		assertStoresResult(player, "test key:\"\\\"HelloWorld\\\"\"", results, Map.of("key", "\"HelloWorld\""));

		// /test "key1\":value1 "key2":value2
		// Error from escaping instead of closing quoted key
		assertCommandFailsWith(player, "test \"key1\\\":value1 \"key2\":value2", "Could not parse command: Delimiter \":\" required after writing a key at position 16: ...\":value1 \"<--[HERE]");
		// /test key1:"value\" key2:"value2"
		// Error from escaping instead of closing quoted value
		assertCommandFailsWith(player, "test key1:\"value1\\\" key2:\"value2\"", "Could not parse command: Separator \" \" required after writing a value at position 21: ...1\\\" key2:\"<--[HERE]");


		// /test ke\\y:value
		// Input backslash into unquoted key
		assertStoresResult(player, "test ke\\\\y:value", results, Map.of("ke\\y", "value"));
		// /test "ke\\y":value
		// Input backslash into quoted key
		assertStoresResult(player, "test \"ke\\\\y\":value", results, Map.of("ke\\y", "value"));

		// /test key:val\\ue
		// Input backslash into unquoted value
		assertStoresResult(player, "test key:val\\\\ue", results, Map.of("key", "val\\ue"));
		// /test key:"val\\ue"
		// Input backslash into quoted value
		assertStoresResult(player, "test key:val\\\\ue", results, Map.of("key", "val\\ue"));


		// /test ke\:y1:value1 key2:value2
		// Input delimiter into unquoted key
		assertStoresResult(player, "test ke\\:y1:value1 key2:value2", results, Map.of("ke:y1", "value1", "key2", "value2"));
		// /test "ke:y1":value1 key2:value2
		// Input delimiter into quoted key
		assertStoresResult(player, "test \"ke:y1\":value1 key2:value2", results, Map.of("ke:y1", "value1", "key2", "value2"));

		// /test key1:val\ ue1 key2:value2
		// Input separator into unquoted value
		assertStoresResult(player, "test key1:val\\ ue1 key2:value2", results, Map.of("key1", "val ue1", "key2", "value2"));
		// /test key1:"val ue1" key2:value2
		// Input separator into quoted value
		assertStoresResult(player, "test key1:\"val ue1\" key2:value2", results, Map.of("key1", "val ue1", "key2", "value2"));


		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithNonSpecialCharacters() {
		// I believe the characters that may appear in chat messages are defined by the
		//  net.minecraft.server.SharedConstants#isAllowedChatCharacter method
		// We'll only bother testing ASCII characters that fit that method
		// https://bukkit.org/threads/printing-special-characters-%E2%99%A0-%E2%99%A3-%E2%99%A5-%E2%99%A6-in-chat.72293/
		//  - Thanks for helping me find that
		Mut<Map<String, String>> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(
				// 'Default' MapArgument
				new MapArgumentBuilder<String, String>("map")
					.withKeyMapper(s -> s)
					.withValueMapper(s -> s)
					.withoutKeyList()
					.withoutValueList()
					.build()
			)
			.executesPlayer((player, args) -> {
				results.set(args.getUnchecked("map"));
			})
			.register();

		Player player = addPlayer();

		for (char c = 32; c < 127; c++) {
			if (c == '\"' || c == '\\' || c == ':' || c == ' ') continue; // Skip special characters

			// Use c as the key and value, unquoted
			assertStoresResult(player, "test " + c + ":" + c, results, Map.of(String.valueOf(c), String.valueOf(c)));

			// Use c as the key and value, quoted
			assertStoresResult(player, "test \"" + c + "\":\"" + c + "\"", results, Map.of(String.valueOf(c), String.valueOf(c)));
		}


		assertNoMoreResults(results);
	}

	@RepeatedTest(10)
	void executionTestWithPlayerNameKeys() {
		Mut<Map<String, String>> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(
				// 'Default' MapArgument
				new MapArgumentBuilder<String, String>("map")
					.withKeyMapper(s -> s)
					.withValueMapper(s -> s)
					.withoutKeyList()
					.withoutValueList()
					.build()
			)
			.executesPlayer((player, args) -> {
				results.set(args.getUnchecked("map"));
			})
			.register();

		Player player = addPlayer();

		// A username is 3 - 16 characters long, has no spaces and only consist
		// of a-z, A-Z, 0-9 and underscore (_)

		String possibleChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
		StringBuilder playerNameBuilder = new StringBuilder();
		for (int i = 0; i < ThreadLocalRandom.current().nextInt(3, 17); i++) {
			playerNameBuilder.append(possibleChars.charAt(ThreadLocalRandom.current().nextInt(0, possibleChars.length())));
		}
		final String playerName = playerNameBuilder.toString();

		// /test <random player name, derived above>:value
		assertStoresResult(player, "test " + playerName + ":\"value\"", results, Map.of(playerName, "value"));
		// /test "<random player name, derived above>":"value"
		assertStoresResult(player, "test \"" + playerName + "\":\"value\"", results, Map.of(playerName, "value"));


		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithLongTerminators() {
		Mut<Map<String, String>> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(
				// Taking advantage of String terminators
				new MapArgumentBuilder<String, String>("map", ':', ",  ")
					.withKeyMapper(s -> s)
					.withValueMapper(s -> s)
					.withoutKeyList()
					.withoutValueList()
					.build()
			)
			.executesPlayer((player, args) -> {
				results.set(args.getUnchecked("map"));
			})
			.register();

		Player player = addPlayer();


		Map<String, String> expectedMap = new LinkedHashMap<>();

		expectedMap.put("key1", "value1");
		// /test key1:value1
		// Input map with 1 unquoted key-value pair
		assertStoresResult(player, "test key1:value1", results, expectedMap);
		// /test "key1":"value1"
		// Input map with 1 quoted key-value pair
		assertStoresResult(player, "test \"key1\":\"value1\"", results, expectedMap);

		expectedMap.put("key2", "value2");
		// /test key1:value1,  key2:value2
		// Input map with 2 unquoted key-value pairs
		assertStoresResult(player, "test key1:value1,  key2:value2", results, expectedMap);
		// /test "key1":"value1",  "key2":"value2"
		// Input map with 2 quoted key-value pairs
		assertStoresResult(player, "test \"key1\":\"value1\",  \"key2\":\"value2\"", results, expectedMap);

		expectedMap.put("key3", "value3");
		// /test key1:value1,  key2:value2,  key3:value3
		// Input map with 3 unquoted key-value pairs
		assertStoresResult(player, "test key1:value1,  key2:value2,  key3:value3", results, expectedMap);
		// /test "key1":"value1",  "key2":"value2",  "key3":"value3"
		// Input map with 3 quoted key-value pairs
		assertStoresResult(player, "test \"key1\":\"value1\",  \"key2\":\"value2\",  \"key3\":\"value3\"", results, expectedMap);


		// /test key1:value1,  key2
		// Error for missing delimiter after unquoted key
		assertCommandFailsWith(player, "test key1:value1,  key2", "Could not parse command: Delimiter \":\" required after writing a key at position 14: ...:value1,  <--[HERE]");
		// /test key1:value1,  "key2"
		// Error for missing delimiter after quoted key
		assertCommandFailsWith(player, "test key1:value1,  \"key2\"", "Could not parse command: Delimiter \":\" required after writing a key at position 20: ...1,  \"key2\"<--[HERE]");


		// /test key1:value1,  "key2"=value2
		// Error for incorrect delimiter
		assertCommandFailsWith(player, "test key1:value1,  \"key2\"=value2", "Could not parse command: Delimiter \":\" required after writing a key at position 20: ...1,  \"key2\"<--[HERE]");
		// /test key1:"value1",key2:value2
		// Error for incorrect separator
		assertCommandFailsWith(player, "test key1:\"value1\",key2:value2", "Could not parse command: Separator \",  \" required after writing a value at position 13: ...1:\"value1\"<--[HERE]");


		// /test a-b-c:a, b, and c,  key2:value2
		// Input values that initially look like separator
		assertStoresResult(player, "test a-b-c:a, b, and c,  key2:value2", results, Map.of("a-b-c", "a, b, and c", "key2", "value2"));


		// /test key1:value1,  key2--
		// Error for incomplete delimiter after unquoted key
		assertCommandFailsWith(player, "test key1:value1,  key2--", "Could not parse command: Delimiter \":\" required after writing a key at position 14: ...:value1,  <--[HERE]");
		// /test key1:value1,  "key2"--
		// Error for incomplete delimiter after quoted key
		assertCommandFailsWith(player, "test key1:value1,  \"key2\"--", "Could not parse command: Delimiter \":\" required after writing a key at position 20: ...1,  \"key2\"<--[HERE]");

		// /test key1:value",
		// Input incomplete delimiter after unquoted value
		// Since we're not using a value list, "value1," is a valid value
		assertStoresResult(player, "test key1:value1,", results, Map.of("key1", "value1,"));
		// /test key1:"value1",
		// Error for incomplete delimiter after quoted value
		assertCommandFailsWith(player, "test key1:\"value1\",", "Could not parse command: Separator \",  \" required after writing a value at position 13: ...1:\"value1\"<--[HERE]");


		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithCustomMappers() {
		Mut<Map<Integer, Integer>> results = Mut.of();

		StringParser<Integer> customMapper = (s) -> {
			int i = Integer.parseInt(s);
			if (i < 0 || i > 255) throw CommandAPI.failWithString("Must be between 0 and 255");
			return i;
		};

		new CommandAPICommand("test")
			.withArguments(
				new MapArgumentBuilder<Integer, Integer>("map")
					// Using a custom Integer key and value mapper
					.withKeyMapper(customMapper)
					.withValueMapper(customMapper)
					.withoutKeyList()
					.withoutValueList()
					.build()
			)
			.executesPlayer((player, args) -> {
				results.set(args.getUnchecked("map"));
			})
			.register();

		Player player = addPlayer();


		Map<Integer, Integer> expectedMap = new LinkedHashMap<>();

		expectedMap.put(0, 10);
		// /test 0:10
		// Input map with 1 unquoted key-value pair
		assertStoresResult(player, "test 0:10", results, expectedMap);
		// /test "0":"10"
		// Input map with 1 quoted key-value pair
		assertStoresResult(player, "test \"0\":\"10\"", results, expectedMap);

		expectedMap.put(20, 100);
		// /test 0:10 20:100
		// Input map with 2 unquoted key-value pairs
		assertStoresResult(player, "test 0:10 20:100", results, expectedMap);
		// /test "0":"10" "20":"100"
		// Input map with 2 quoted key-value pairs
		assertStoresResult(player, "test \"0\":\"10\" \"20\":\"100\"", results, expectedMap);


		// /test key:100
		// Error from unquoted key string not converting to int
		assertCommandFailsWith(player, "test key:100", "Could not parse command: Invalid key (key): cannot be converted to a key at position 0: <--[HERE]");
		// /test "key":100
		// Error from quoted key string not converting to int
		assertCommandFailsWith(player, "test \"key\":100", "Could not parse command: Invalid key (key): cannot be converted to a key at position 0: <--[HERE]");

		// /test 100:value
		// Error from unquoted value string not converting to int
		assertCommandFailsWith(player, "test 100:value", "Could not parse command: Invalid value (value): cannot be converted to a value at position 4: 100:<--[HERE]");
		// /test 100:"value"
		// Error from quoted value not converting to int
		assertCommandFailsWith(player, "test 100:\"value\"", "Could not parse command: Invalid value (value): cannot be converted to a value at position 4: 100:<--[HERE]");


		// /test 1000:0
		// Error from unquoted key being too large
		assertCommandFailsWith(player, "test 1000:0", "Could not parse command: Must be between 0 and 255 at position 0: <--[HERE]");
		// /test -10:0
		// Error from unquoted key being too small
		assertCommandFailsWith(player, "test -10:0", "Could not parse command: Must be between 0 and 255 at position 0: <--[HERE]");
		// /test "1000":0
		// Error from quoted key being too large
		assertCommandFailsWith(player, "test \"1000\":0", "Could not parse command: Must be between 0 and 255 at position 0: <--[HERE]");
		// /test "-10":0
		// Error from quoted key being too small
		assertCommandFailsWith(player, "test \"-10\":0", "Could not parse command: Must be between 0 and 255 at position 0: <--[HERE]");

		// /test 0:1000
		// Error from unquoted value being too large
		assertCommandFailsWith(player, "test 0:1000", "Could not parse command: Must be between 0 and 255 at position 2: 0:<--[HERE]");
		// /test 0:-10
		// Error from unquoted value being too small
		assertCommandFailsWith(player, "test 0:-10", "Could not parse command: Must be between 0 and 255 at position 2: 0:<--[HERE]");
		// /test 0:"1000"
		// Error from quoted value being too large
		assertCommandFailsWith(player, "test 0:\"1000\"", "Could not parse command: Must be between 0 and 255 at position 2: 0:<--[HERE]");
		// /test 0:"-10"
		// Error from quoted value being too small
		assertCommandFailsWith(player, "test 0:\"-10\"", "Could not parse command: Must be between 0 and 255 at position 2: 0:<--[HERE]");
	}

	@Test
	void executionTestWithFloatKey() {
		Mut<LinkedHashMap<Float, String>> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new MapArgumentBuilder<Float, String>("map")
				.withKeyMapper(Float::valueOf)
				.withValueMapper(s -> s)
				.withoutKeyList()
				.withoutValueList()
				.build()
			)
			.executesPlayer((player, args) -> {
				results.set(args.getUnchecked(0));
			})
			.register();

		Player player = addPlayer();

		LinkedHashMap<Float, String> testMap = new LinkedHashMap<>();
		testMap.put(3.5F, "Hello World");
		testMap.put(12.25F, "This is a test!");
		// /test 3.5:"Hello World" 12.25:"This is a test!"
		assertStoresResult(player, "test 3.5:\"Hello World\" 12.25:\"This is a test!\"", results, testMap);

		testMap.put(6.25F, "And this is a third value!");
		// /test 3.5:"Hello World" 12.25:"This is a test!" 6.25:"And this is a third value!"
		assertStoresResult(player, "test 3.5:\"Hello World\" 12.25:\"This is a test!\" 6.25:\"And this is a third value!\"", results, testMap);

		// /test 3,5:"Hello world!"
		assertCommandFailsWith(player, "test 3,5:\"Hello world!\"", "Could not parse command: Invalid key (3,5): cannot be converted to a key at position 0: <--[HERE]");


		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithLists() {
		new CommandAPICommand("test")
			.withArguments(
				new MapArgumentBuilder<String, String>("map")
					.withKeyMapper(s -> s)
					.withValueMapper(s -> s)
					// Give key and value list
					.withKeyList(List.of("key1", "key2", "key3"))
					.withValueList(List.of("value1", "value2", "value3"))
					.build()
			)
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// From previous test we know everything works so here only relevant exceptions will be tested

		// /test key1:value1 key1:value2
		// Error for having duplicate unquoted key1
		assertCommandFailsWith(player, "test key1:value1 key1:value2", "Could not parse command: Duplicate keys are not allowed! at position 12: ...y1:value1 <--[HERE]");
		// /test key1:value1 "key1":value2
		// Error for having duplicate quoted key1
		assertCommandFailsWith(player, "test key1:value1 \"key1\":value2", "Could not parse command: Duplicate keys are not allowed! at position 12: ...y1:value1 <--[HERE]");
		// /test key1:value1 key1
		// Error for having duplicate unquoted key1 without delimiter
		assertCommandFailsWith(player, "test key1:value1 key1", "Could not parse command: Duplicate keys are not allowed! at position 12: ...y1:value1 <--[HERE]");

		// /test key1:value1 key2:value1
		// Error for having duplicate value1
		assertCommandFailsWith(player, "test key1:value1 key2:value1", "Could not parse command: Duplicate values are not allowed! at position 17: ...lue1 key2:<--[HERE]");
		// /test key1:value1 key2:"value1"
		// Error for having duplicate quoted value1
		assertCommandFailsWith(player, "test key1:value1 key2:\"value1\"", "Could not parse command: Duplicate values are not allowed! at position 17: ...lue1 key2:<--[HERE]");


		// /test key1:value1 key4:value2
		// Error for having invalid unquoted key4
		assertCommandFailsWith(player, "test key1:value1 key4:value2", "Could not parse command: Invalid key: key4 at position 12: ...y1:value1 <--[HERE]");
		// /test key1:value1 "key4":value2
		// Error for having invalid quoted key1
		assertCommandFailsWith(player, "test key1:value1 \"key4\":value2", "Could not parse command: Invalid key: key4 at position 12: ...y1:value1 <--[HERE]");
		// /test key1:value1 key4
		// Error for having invalid unquoted key4 without delimiter
		assertCommandFailsWith(player, "test key1:value1 key4", "Could not parse command: Invalid key: key4 at position 12: ...y1:value1 <--[HERE]");

		// /test key1:value1 key2:value4
		// Error for having invalid value4
		assertCommandFailsWith(player, "test key1:value1 key2:value4", "Could not parse command: Invalid value: value4 at position 17: ...lue1 key2:<--[HERE]");
		// /test key1:value1 key2:"value4"
		// Error for having invalid quoted value4
		assertCommandFailsWith(player, "test key1:value1 key2:\"value4\"", "Could not parse command: Invalid value: value4 at position 17: ...lue1 key2:<--[HERE]");


		// /test key1:value1 key2
		// Check this still gives missing delimiter message in special case b/c key is valid
		assertCommandFailsWith(player, "test key1:value1 key2", "Could not parse command: Delimiter \":\" required after writing a key at position 12: ...y1:value1 <--[HERE]");
	}

	@Test
	void executionTestWithDuplicateValues() {
		Mut<Map<String, String>> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(
				new MapArgumentBuilder<String, String>("map")
					.withKeyMapper(s -> s)
					.withValueMapper(s -> s)
					.withoutKeyList()
					// Allowing duplicate values
					.withoutValueList(true)
					.build()
			)
			.executesPlayer((player, args) -> {
				results.set(args.getUnchecked("map"));
			})
			.register();

		Player player = addPlayer();

		// Allowing duplicate values doesn't do much else beyond that, so there is not much extra to test

		// /test key1:value key2:value
		// Input a duplicate value
		assertStoresResult(player, "test key1:value key2:value", results, Map.of("key1", "value", "key2", "value"));


		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithDuplicateValuesAndList() {
		Mut<Map<String, String>> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(
				new MapArgumentBuilder<String, String>("map")
					.withKeyMapper(s -> s)
					.withValueMapper(s -> s)
					.withoutKeyList()
					// Giving list and allowing duplicate values
					.withValueList(List.of("value1", "value2", "value3"), true)
					.build()
			)
			.executesPlayer((player, args) -> {
				results.set(args.getUnchecked("map"));
			})
			.register();

		Player player = addPlayer();

		// Allowing duplicate values doesn't do much else beyond that, so there is not much extra to test

		// /test key1:value1 key2:value1
		// Input a duplicate value
		assertStoresResult(player, "test key1:value1 key2:value1", results, Map.of("key1", "value1", "key2", "value1"));


		// /test key1:value1 key2:value4
		// Error for having invalid value4
		assertCommandFailsWith(player, "test key1:value1 key2:value4", "Could not parse command: Invalid value: value4 at position 17: ...lue1 key2:<--[HERE]");


		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithMapArgument() {
		new CommandAPICommand("test")
			.withArguments(
				// 'Default' MapArgument
				new MapArgumentBuilder<String, String>("map")
					.withKeyMapper(s -> s)
					.withValueMapper(s -> s)
					.withoutKeyList()
					.withoutValueList()
					.build()
			)
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();


		////////////////
		// FIRST PAIR //
		////////////////


		// /test
		// No key list given, so we expect no suggestions
		assertNoSuggestions(player, "test ");

		// /test "
		// Start of quoted key, suggest ending those quotes
		assertCommandSuggests(player, "test \"", "\"\":");


		// Start of unquoted key, suggest closing
		// /test a
		assertCommandSuggests(player, "test a", "a:");
		// /test ab
		assertCommandSuggests(player, "test ab", "ab:");
		// /test abc
		assertCommandSuggests(player, "test abc", "abc:");

		// Start of quoted key, suggest closing
		// /test "a
		assertCommandSuggests(player, "test \"a", "\"a\":");
		// /test "ab
		assertCommandSuggests(player, "test \"ab", "\"ab\":");
		// /test "abc
		assertCommandSuggests(player, "test \"abc", "\"abc\":");


		// /test "abc"
		// Quoted key closed, suggest delimiter
		assertCommandSuggests(player, "test \"abc\"", ":");

		// /test "abc"=
		// Incorrect delimiter, error means no suggestions
		assertNoSuggestions(player, "test \"abc\"=");


		// /test abc:
		// No value list given, so we expect no suggestions
		assertNoSuggestions(player, "test abc:");

		// /test abc:"
		// Start of quoted value, suggest ending those quotes
		assertCommandSuggests(player, "test abc:\"", "\"\" ");


		// Start of unquoted value, suggest closing
		// /test abc:a
		assertCommandSuggests(player, "test abc:a", "a ");
		// /test abc:ab
		assertCommandSuggests(player, "test abc:ab", "ab ");
		// /test abc:abc
		assertCommandSuggests(player, "test abc:abc", "abc ");

		// Start of quoted value, suggest closing
		// /test abc:"a
		assertCommandSuggests(player, "test abc:\"a", "\"a\" ");
		// /test abc:"ab
		assertCommandSuggests(player, "test abc:\"ab", "\"ab\" ");
		// /test abc:"abc
		assertCommandSuggests(player, "test abc:\"abc", "\"abc\" ");


		// /test abc:"abc"
		// Quoted value closed, suggest separator
		assertCommandSuggests(player, "test abc:\"abc\"", " ");

		// /test abc:"abc",
		// Incorrect delimiter, error means no suggestions
		assertNoSuggestions(player, "test abc:\"abc\",");


		/////////////////
		// SECOND PAIR //
		/////////////////


		// /test k1:v1
		// No key list given, so we expect no suggestions
		assertNoSuggestions(player, "test k1:v1 ");

		// /test k1:v1 "
		// Start of quoted key, suggest ending those quotes
		assertCommandSuggests(player, "test k1:v1 \"", "\"\":");


		// Start of unquoted key, suggest closing
		// /test k1:v1 a
		assertCommandSuggests(player, "test k1:v1 a", "a:");
		// /test k1:v1 ab
		assertCommandSuggests(player, "test k1:v1 ab", "ab:");
		// /test k1:v1 abc
		assertCommandSuggests(player, "test k1:v1 abc", "abc:");

		// Start of quoted key, suggest closing
		// /test k1:v1 "a
		assertCommandSuggests(player, "test k1:v1 \"a", "\"a\":");
		// /test k1:v1 "ab
		assertCommandSuggests(player, "test k1:v1 \"ab", "\"ab\":");
		// /test k1:v1 "abc
		assertCommandSuggests(player, "test k1:v1 \"abc", "\"abc\":");


		// /test k1:v1 "abc"
		// Quoted key closed, suggest delimiter
		assertCommandSuggests(player, "test k1:v1 \"abc\"", ":");

		// /test k1:v1 "abc"=
		// Incorrect delimiter, error means no suggestions
		assertNoSuggestions(player, "test k1:v1 \"abc\"=");


		// /test k1:v1 abc:
		// No value list given, so we expect no suggestions
		assertNoSuggestions(player, "test k1:v1 abc:");

		// /test k1:v1 abc:"
		// Start of quoted value, suggest ending those quotes
		assertCommandSuggests(player, "test k1:v1 abc:\"", "\"\" ");


		// Start of unquoted value, suggest closing
		// /test k1:v1 abc:a
		assertCommandSuggests(player, "test k1:v1 abc:a", "a ");
		// /test k1:v1 abc:ab
		assertCommandSuggests(player, "test k1:v1 abc:ab", "ab ");
		// /test k1:v1 abc:abc
		assertCommandSuggests(player, "test k1:v1 abc:abc", "abc ");

		// Start of quoted value, suggest closing
		// /test k1:v1 abc:"a
		assertCommandSuggests(player, "test k1:v1 abc:\"a", "\"a\" ");
		// /test k1:v1 abc:"ab
		assertCommandSuggests(player, "test k1:v1 abc:\"ab", "\"ab\" ");
		// /test k1:v1 abc:"abc
		assertCommandSuggests(player, "test k1:v1 abc:\"abc", "\"abc\" ");


		// /test k1:v1 abc:"abc"
		// Quoted value closed, suggest separator
		assertCommandSuggests(player, "test k1:v1 abc:\"abc\"", " ");

		// /test k1:v1 abc:"abc",
		// Incorrect delimiter, error means no suggestions
		assertNoSuggestions(player, "test k1:v1 abc:\"abc\",");


		/////////////////
		// THIRD PAIR //
		/////////////////


		// /test k1:v1 k2:v2
		// No key list given, so we expect no suggestions
		assertNoSuggestions(player, "test k1:v1 k2:v2 ");

		// /test k1:v1 k2:v2 "
		// Start of quoted key, suggest ending those quotes
		assertCommandSuggests(player, "test k1:v1 k2:v2 \"", "\"\":");


		// Start of unquoted key, suggest closing
		// /test k1:v1 k2:v2 a
		assertCommandSuggests(player, "test k1:v1 k2:v2 a", "a:");
		// /test k1:v1 k2:v2 ab
		assertCommandSuggests(player, "test k1:v1 k2:v2 ab", "ab:");
		// /test k1:v1 k2:v2 abc
		assertCommandSuggests(player, "test k1:v1 k2:v2 abc", "abc:");

		// Start of quoted key, suggest closing
		// /test k1:v1 k2:v2 "a
		assertCommandSuggests(player, "test k1:v1 k2:v2 \"a", "\"a\":");
		// /test k1:v1 k2:v2 "ab
		assertCommandSuggests(player, "test k1:v1 k2:v2 \"ab", "\"ab\":");
		// /test k1:v1 k2:v2 "abc
		assertCommandSuggests(player, "test k1:v1 k2:v2 \"abc", "\"abc\":");


		// /test k1:v1 k2:v2 "abc"
		// Quoted key closed, suggest delimiter
		assertCommandSuggests(player, "test k1:v1 k2:v2 \"abc\"", ":");

		// /test k1:v1 k2:v2 "abc"=
		// Incorrect delimiter, error means no suggestions
		assertNoSuggestions(player, "test k1:v1 k2:v2 \"abc\"=");


		// /test k1:v1 k2:v2 abc:
		// No value list given, so we expect no suggestions
		assertNoSuggestions(player, "test k1:v1 k2:v2 abc:");

		// /test k1:v1 k2:v2 abc:"
		// Start of quoted value, suggest ending those quotes
		assertCommandSuggests(player, "test k1:v1 k2:v2 abc:\"", "\"\" ");


		// Start of unquoted value, suggest closing
		// /test k1:v1 k2:v2 abc:a
		assertCommandSuggests(player, "test k1:v1 k2:v2 abc:a", "a ");
		// /test k1:v1 k2:v2 abc:ab
		assertCommandSuggests(player, "test k1:v1 k2:v2 abc:ab", "ab ");
		// /test k1:v1 k2:v2 abc:abc
		assertCommandSuggests(player, "test k1:v1 k2:v2 abc:abc", "abc ");

		// Start of quoted value, suggest closing
		// /test k1:v1 k2:v2 abc:"a
		assertCommandSuggests(player, "test k1:v1 k2:v2 abc:\"a", "\"a\" ");
		// /test k1:v1 k2:v2 abc:"ab
		assertCommandSuggests(player, "test k1:v1 k2:v2 abc:\"ab", "\"ab\" ");
		// /test k1:v1 k2:v2 abc:"abc
		assertCommandSuggests(player, "test k1:v1 k2:v2 abc:\"abc", "\"abc\" ");


		// /test k1:v1 k2:v2 abc:"abc"
		// Quoted value closed, suggest separator
		assertCommandSuggests(player, "test k1:v1 k2:v2 abc:\"abc\"", " ");

		// /test k1:v1 k2:v2 abc:"abc",
		// Incorrect delimiter, error means no suggestions
		assertNoSuggestions(player, "test k1:v1 k2:v2 abc:\"abc\",");


		//////////////////
		// END OF PAIRS //
		//////////////////


		// /test key1:value1 key1:value2
		// Error for having duplicate unquoted key1, no suggestions
		assertNoSuggestions(player, "test key1:value1 key1:value2");
		// /test key1:value1 "key1":value2
		// Error for having duplicate quoted key1, no suggestions
		assertNoSuggestions(player, "test key1:value1 \"key1\":value2");

		// /test key1:value1 key2:value1 a
		// Error for having duplicate unquoted value1, no suggestions
		assertNoSuggestions(player, "test key1:value1 key2:value1 a");
		// /test key1:value1 key2:"value1" a
		// Error for having duplicate quoted value1, no suggestions
		assertNoSuggestions(player, "test key1:value1 key2:\"value1\" a");
	}

	@Test
	void suggestionTestWithSpecialCharacters() {
		// Special characters are \ " and the separator and delimiter
		new CommandAPICommand("test")
			.withArguments(
				// 'Default' MapArgument
				new MapArgumentBuilder<String, String>("map")
					.withKeyMapper(s -> s)
					.withValueMapper(s -> s)
					.withoutKeyList()
					.withoutValueList()
					.build()
			)
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();


		// /test \"a"b
		// Complete unquoted key containing quotes
		assertCommandSuggests(player, "test \\\"a\"b", "\\\"a\"b:");
		// /test "\"a\"b
		// Complete quoted key containing quotes
		assertCommandSuggests(player, "test \"\\\"a\\\"b", "\"\\\"a\\\"b\":");
		// /test "\"a\"b"
		// Suggest delimiter after closed quoted key containing quotes
		assertCommandSuggests(player, "test \"\\\"a\\\"b\"", ":");

		// /test abc:\"a"b
		// Complete unquoted value containing quotes
		assertCommandSuggests(player, "test abc:\\\"a\"b", "\\\"a\"b ");
		// /test abc:"\"a\"b
		// Complete quoted value containing quotes
		assertCommandSuggests(player, "test abc:\"\\\"a\\\"b", "\"\\\"a\\\"b\" ");
		// /test abc:"\"a\"b"
		// Suggest separator after closed quoted value containing quotes
		assertCommandSuggests(player, "test abc:\"\\\"a\\\"b\"", " ");

		// /test "key1\":value1 "key2":value2
		// Error from escaping instead of closing quoted key, no suggestions
		assertNoSuggestions(player, "test \"key1\\\":value1 \"key2\":value2");
		// /test key1:"value\" key2:"value2"
		// Error from escaping instead of closing quoted value, no suggestions
		assertNoSuggestions(player, "test key1:\"value1\\\" key2:\"value2\"");


		// /test \\a\\b
		// Complete unquoted key containing backslashes
		assertCommandSuggests(player, "test \\\\a\\\\b", "\\\\a\\\\b:");
		// /test "\\a\\b
		// Complete unquoted key containing backslashes
		assertCommandSuggests(player, "test \"\\\\a\\\\b", "\"\\\\a\\\\b\":");
		// /test "\\a\\b"
		// Suggest delimiter after closed quoted key containing backslashes
		assertCommandSuggests(player, "test \"\\\\a\\\\b\"", ":");

		// /test abc:\\a\\b
		// Complete unquoted value containing backslashes
		assertCommandSuggests(player, "test abc:\\\\a\\\\b", "\\\\a\\\\b ");
		// /test abc:"\\a\\b
		// Complete quoted value containing backslashes
		assertCommandSuggests(player, "test abc:\"\\\\a\\\\b", "\"\\\\a\\\\b\" ");
		// /test abc:"\\a\\b"
		// Suggest separator after closed quoted value containing backslashes
		assertCommandSuggests(player, "test abc:\"\\\\a\\\\b\"", " ");

		// Suggest extra backlash if unquoted key ends with unescaped backslash
		// /test \
		assertCommandSuggests(player, "test \\", "\\\\:");
		// /test \\
		assertCommandSuggests(player, "test \\\\", "\\\\:");
		// /test \\\
		assertCommandSuggests(player, "test \\\\\\", "\\\\\\\\:");
		// /test \\\\
		assertCommandSuggests(player, "test \\\\\\\\", "\\\\\\\\:");
		// /test a\
		assertCommandSuggests(player, "test a\\", "a\\\\:");
		// /test a\\
		assertCommandSuggests(player, "test a\\\\", "a\\\\:");
		// /test a\\\
		assertCommandSuggests(player, "test a\\\\\\", "a\\\\\\\\:");
		// /test a\\\\
		assertCommandSuggests(player, "test a\\\\\\\\", "a\\\\\\\\:");
		// /test \\a\
		assertCommandSuggests(player, "test \\\\a\\", "\\\\a\\\\:");
		// /test \\a\\
		assertCommandSuggests(player, "test \\\\a\\\\", "\\\\a\\\\:");
		// /test \\a\\\
		assertCommandSuggests(player, "test \\\\a\\\\\\", "\\\\a\\\\\\\\:");
		// /test \\a\\\\
		assertCommandSuggests(player, "test \\\\a\\\\\\\\", "\\\\a\\\\\\\\:");
		// /test \"a\
		assertCommandSuggests(player, "test \\\"a\\", "\\\"a\\\\:");
		// /test \"a\\
		assertCommandSuggests(player, "test \\\"a\\\\", "\\\"a\\\\:");
		// /test \"a\\\
		assertCommandSuggests(player, "test \\\"a\\\\\\", "\\\"a\\\\\\\\:");
		// /test \"a\\\\
		assertCommandSuggests(player, "test \\\"a\\\\\\\\", "\\\"a\\\\\\\\:");

		// Suggest extra backlash if quoted key ends with unescaped backslash
		// /test "\
		assertCommandSuggests(player, "test \"\\", "\"\\\\\":");
		// /test "\\
		assertCommandSuggests(player, "test \"\\\\", "\"\\\\\":");
		// /test "\\\
		assertCommandSuggests(player, "test \"\\\\\\", "\"\\\\\\\\\":");
		// /test "\\\\
		assertCommandSuggests(player, "test \"\\\\\\\\", "\"\\\\\\\\\":");
		// /test "a\
		assertCommandSuggests(player, "test \"a\\", "\"a\\\\\":");
		// /test "a\\
		assertCommandSuggests(player, "test \"a\\\\", "\"a\\\\\":");
		// /test "a\\\
		assertCommandSuggests(player, "test \"a\\\\\\", "\"a\\\\\\\\\":");
		// /test "a\\\\
		assertCommandSuggests(player, "test \"a\\\\\\\\", "\"a\\\\\\\\\":");
		// /test "\\a\
		assertCommandSuggests(player, "test \"\\\\a\\", "\"\\\\a\\\\\":");
		// /test "\\a\\
		assertCommandSuggests(player, "test \"\\\\a\\\\", "\"\\\\a\\\\\":");
		// /test "\\a\\\
		assertCommandSuggests(player, "test \"\\\\a\\\\\\", "\"\\\\a\\\\\\\\\":");
		// /test "\\a\\\\
		assertCommandSuggests(player, "test \"\\\\a\\\\\\\\", "\"\\\\a\\\\\\\\\":");
		// /test "\"a\
		assertCommandSuggests(player, "test \"\\\"a\\", "\"\\\"a\\\\\":");
		// /test "\"a\\
		assertCommandSuggests(player, "test \"\\\"a\\\\", "\"\\\"a\\\\\":");
		// /test "\"a\\\
		assertCommandSuggests(player, "test \"\\\"a\\\\\\", "\"\\\"a\\\\\\\\\":");
		// /test "\"a\\\\
		assertCommandSuggests(player, "test \"\\\"a\\\\\\\\", "\"\\\"a\\\\\\\\\":");

		// Suggest extra backlash if unquoted value ends with unescaped backslash
		// /test key:\
		assertCommandSuggests(player, "test key:\\", "\\\\ ");
		// /test key:\\
		assertCommandSuggests(player, "test key:\\\\", "\\\\ ");
		// /test key:\\\
		assertCommandSuggests(player, "test key:\\\\\\", "\\\\\\\\ ");
		// /test key:\\\\
		assertCommandSuggests(player, "test key:\\\\\\\\", "\\\\\\\\ ");
		// /test key:a\
		assertCommandSuggests(player, "test key:a\\", "a\\\\ ");
		// /test key:a\\
		assertCommandSuggests(player, "test key:a\\\\", "a\\\\ ");
		// /test key:a\\\
		assertCommandSuggests(player, "test key:a\\\\\\", "a\\\\\\\\ ");
		// /test key:a\\\\
		assertCommandSuggests(player, "test key:a\\\\\\\\", "a\\\\\\\\ ");
		// /test key:\\a\
		assertCommandSuggests(player, "test key:\\\\a\\", "\\\\a\\\\ ");
		// /test key:\\a\\
		assertCommandSuggests(player, "test key:\\\\a\\\\", "\\\\a\\\\ ");
		// /test key:\\a\\\
		assertCommandSuggests(player, "test key:\\\\a\\\\\\", "\\\\a\\\\\\\\ ");
		// /test key:\\a\\\\
		assertCommandSuggests(player, "test key:\\\\a\\\\\\\\", "\\\\a\\\\\\\\ ");
		// /test key:\"a\
		assertCommandSuggests(player, "test key:\\\"a\\", "\\\"a\\\\ ");
		// /test key:\"a\\
		assertCommandSuggests(player, "test key:\\\"a\\\\", "\\\"a\\\\ ");
		// /test key:\"a\\\
		assertCommandSuggests(player, "test key:\\\"a\\\\\\", "\\\"a\\\\\\\\ ");
		// /test key:\"a\\\\
		assertCommandSuggests(player, "test key:\\\"a\\\\\\\\", "\\\"a\\\\\\\\ ");

		// Suggest extra backlash if quoted value ends with unescaped backslash
		// /test key:"\
		assertCommandSuggests(player, "test key:\"\\", "\"\\\\\" ");
		// /test key:"\\
		assertCommandSuggests(player, "test key:\"\\\\", "\"\\\\\" ");
		// /test key:"\\\
		assertCommandSuggests(player, "test key:\"\\\\\\", "\"\\\\\\\\\" ");
		// /test key:"\\\\
		assertCommandSuggests(player, "test key:\"\\\\\\\\", "\"\\\\\\\\\" ");
		// /test key:"a\
		assertCommandSuggests(player, "test key:\"a\\", "\"a\\\\\" ");
		// /test key:"a\\
		assertCommandSuggests(player, "test key:\"a\\\\", "\"a\\\\\" ");
		// /test key:"a\\\
		assertCommandSuggests(player, "test key:\"a\\\\\\", "\"a\\\\\\\\\" ");
		// /test key:"a\\\\
		assertCommandSuggests(player, "test key:\"a\\\\\\\\", "\"a\\\\\\\\\" ");
		// /test key:"\\a\
		assertCommandSuggests(player, "test key:\"\\\\a\\", "\"\\\\a\\\\\" ");
		// /test key:"\\a\\
		assertCommandSuggests(player, "test key:\"\\\\a\\\\", "\"\\\\a\\\\\" ");
		// /test key:"\\a\\\
		assertCommandSuggests(player, "test key:\"\\\\a\\\\\\", "\"\\\\a\\\\\\\\\" ");
		// /test key:"\\a\\\\
		assertCommandSuggests(player, "test key:\"\\\\a\\\\\\\\", "\"\\\\a\\\\\\\\\" ");
		// /test key:"\"a\
		assertCommandSuggests(player, "test key:\"\\\"a\\", "\"\\\"a\\\\\" ");
		// /test key:"\"a\\
		assertCommandSuggests(player, "test key:\"\\\"a\\\\", "\"\\\"a\\\\\" ");
		// /test key:"\"a\\\
		assertCommandSuggests(player, "test key:\"\\\"a\\\\\\", "\"\\\"a\\\\\\\\\" ");
		// /test key:"\"a\\\\
		assertCommandSuggests(player, "test key:\"\\\"a\\\\\\\\", "\"\\\"a\\\\\\\\\" ");


		// /test ke\:y
		// Complete unquoted value containing delimiter
		assertCommandSuggests(player, "test ke\\:y", "ke\\:y:");
		// /test "ke:y
		// Complete quoted value containing delimiter
		assertCommandSuggests(player, "test \"ke:y", "\"ke:y\":");
		// /test "ke:y"
		// Suggest delimiter after closed quoted key containing delimiter
		assertCommandSuggests(player, "test \"ke:y\"", ":");

		// /test key:val\ ue
		// Complete unquoted value containing separator
		assertCommandSuggests(player, "test key:val\\ ue", "val\\ ue ");
		// /test key:"val ue
		// Complete quoted value containing separator
		assertCommandSuggests(player, "test key:\"val ue", "\"val ue\" ");
		// /test key:"val ue"
		// Suggest separator after closed quoted key containing separator
		assertCommandSuggests(player, "test key:\"val ue\"", " ");
	}

	@Test
	void suggestionTestWithNonSpecialCharacters() {
		// We'll only bother testing part of the ASCII characters, see executionTestWithNonSpecialCharacters for why
		new CommandAPICommand("test")
			.withArguments(
				// 'Default' MapArgument
				new MapArgumentBuilder<String, String>("map")
					.withKeyMapper(s -> s)
					.withValueMapper(s -> s)
					.withoutKeyList()
					.withoutValueList()
					.build()
			)
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		for (char c = 32; c < 127; c++) {
			if (c == '\"' || c == '\\' || c == ':' || c == ' ') continue; // Skip special characters

			// Complete suggestion containing unquoted character
			assertCommandSuggests(player, "test " + c, c + ":");
			assertCommandSuggests(player, "test key:" + c, c + " ");

			// Complete suggestion containing quoted character
			assertCommandSuggests(player, "test \"" + c, "\"" + c + "\":");
			assertCommandSuggests(player, "test key:\"" + c, "\"" + c + "\" ");
		}
	}

	@Test
	void suggestionTestWithLongTerminators() {
		new CommandAPICommand("test")
			.withArguments(
				// Taking advantage of String terminators
				new MapArgumentBuilder<String, String>("map", ':', ",  ")
					.withKeyMapper(s -> s)
					.withValueMapper(s -> s)
					.withoutKeyList()
					.withoutValueList()
					.build()
			)
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();


		////////////////
		// FIRST PAIR //
		////////////////


		// /test
		// No key list given, so we expect no suggestions
		assertNoSuggestions(player, "test ");

		// /test "
		// Start of quoted key, suggest ending those quotes
		assertCommandSuggests(player, "test \"", "\"\":");


		// /test abc
		// Start of unquoted key, suggest closing
		assertCommandSuggests(player, "test abc", "abc:");
		// /test "abc
		// Start of quoted key, suggest closing
		assertCommandSuggests(player, "test \"abc", "\"abc\":");


		// /test "abc"
		// Quoted key closed, suggest delimiter
		assertCommandSuggests(player, "test \"abc\"", ":");

		// /test "abc"===
		// Incorrect delimiter, error means no suggestions
		assertNoSuggestions(player, "test \"abc\"===");


		// /test abc:
		// No value list given, so we expect no suggestions
		assertNoSuggestions(player, "test abc:");

		// /test abc:"
		// Start of quoted value, suggest ending those quotes
		assertCommandSuggests(player, "test abc:\"", "\"\",  ");


		// /test abc:abc
		// Start of unquoted value, suggest closing
		assertCommandSuggests(player, "test abc:abc", "abc,  ");
		// /test abc:"abc
		// Start of quoted value, suggest closing
		assertCommandSuggests(player, "test abc:\"abc", "\"abc\",  ");


		// /test abc:"abc"
		// Quoted value closed, suggest separator
		assertCommandSuggests(player, "test abc:\"abc\"", ",  ");

		// Mistyped separator? suggest the right one
		// /test abc:"abc"=
		assertCommandSuggests(player, "test abc:\"abc\"=", ",  ");
		// /test abc:"abc"==
		assertCommandSuggests(player, "test abc:\"abc\"==", ",  ");

		// /test abc:"abc"===
		// Incorrect delimiter, error means no suggestions
		assertNoSuggestions(player, "test abc:\"abc\"===");


		/////////////////
		// SECOND PAIR //
		/////////////////


		// /test k1:v1,
		// No key list given, so we expect no suggestions
		assertNoSuggestions(player, "test k1:v1,  ");

		// /test k1:v1,  "
		// Start of quoted key, suggest ending those quotes
		assertCommandSuggests(player, "test k1:v1,  \"", "\"\":");


		// /test k1:v1,  abc
		// Start of unquoted key, suggest closing
		assertCommandSuggests(player, "test k1:v1,  abc", "abc:");
		// /test k1:v1,  "abc
		// Start of quoted key, suggest closing
		assertCommandSuggests(player, "test k1:v1,  \"abc", "\"abc\":");


		// /test k1:v1,  "abc"
		// Quoted key closed, suggest delimiter
		assertCommandSuggests(player, "test k1:v1,  \"abc\"", ":");

		// /test k1:v1,  "abc"===
		// Incorrect delimiter, error means no suggestions
		assertNoSuggestions(player, "test k1:v1,  \"abc\"===");


		// /test k1:v1,  abc:
		// No value list given, so we expect no suggestions
		assertNoSuggestions(player, "test k1:v1,  abc:");

		// /test k1:v1,  abc:"
		// Start of quoted value, suggest ending those quotes
		assertCommandSuggests(player, "test k1:v1,  abc:\"", "\"\",  ");


		// /test k1:v1,  abc:abc
		// Start of unquoted value, suggest closing
		assertCommandSuggests(player, "test k1:v1,  abc:abc", "abc,  ");
		// /test k1:v1,  abc:"abc
		// Start of quoted value, suggest closing
		assertCommandSuggests(player, "test k1:v1,  abc:\"abc", "\"abc\",  ");


		// /test k1:v1,  abc:"abc"
		// Quoted value closed, suggest separator
		assertCommandSuggests(player, "test k1:v1,  abc:\"abc\"", ",  ");

		// Mistyped separator? suggest the right one
		// /test k1:v1,  abc:"abc"=
		assertCommandSuggests(player, "test k1:v1,  abc:\"abc\"=", ",  ");
		// /test k1:v1,  abc:"abc"==
		assertCommandSuggests(player, "test k1:v1,  abc:\"abc\"==", ",  ");

		// /test k1:v1,  abc:"abc"===
		// Incorrect delimiter, error means no suggestions
		assertNoSuggestions(player, "test k1:v1,  abc:\"abc\"===");


		//////////////////
		// END OF PAIRS //
		//////////////////


		// /test key1:value1,  key1:value2
		// Error for having duplicate unquoted key1, no suggestions
		assertNoSuggestions(player, "test key1:value1,  key1:value2");
		// /test key1:value1,  "key1":value2
		// Error for having duplicate quoted key1, no suggestions
		assertNoSuggestions(player, "test key1:value1,  \"key1\":value2");

		// /test key1:value1,  key2:value1 a
		// Error for having duplicate unquoted value1, no suggestions
		assertNoSuggestions(player, "test key1:value1,  key2:value1,  a");
		// /test key1:value1,  key2:"value1"
		// Error for having duplicate quoted value1, no suggestions
		assertNoSuggestions(player, "test key1:value1,  key2:\"value1\",  a");
	}

	@Test
	void suggestionTestWithCustomMappers() {
		StringParser<Integer> customMapper = (s) -> {
			int i = Integer.parseInt(s);
			if (i < 0 || i > 255) throw CommandAPI.failWithString("Must be between 0 and 255");
			return i;
		};

		new CommandAPICommand("test")
			.withArguments(
				new MapArgumentBuilder<Integer, Integer>("map")
					// Using a custom Integer key and value mapper
					.withKeyMapper(customMapper)
					.withValueMapper(customMapper)
					.withoutKeyList()
					.withoutValueList()
					.build()
			)
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// Generating suggestions works the same as strings, but we should check suggestions stop when key/value is invalid

		// /test 0:10 "
		// Valid unquoted results, suggestions continue
		assertCommandSuggests(player, "test 0:10 \"", "\"\":");
		assertCommandSuggests(player, "test 0:10 20:100 \"", "\"\":");
		// /test "0":"10" "
		// Valid quoted results, suggestions continue
		assertCommandSuggests(player, "test \"0\":\"10\" \"", "\"\":");
		assertCommandSuggests(player, "test \"0\":\"10\" \"20\":\"100\" \"", "\"\":");


		// /test key:100
		// Error from unquoted key string not converting to int, no suggestions
		assertNoSuggestions(player, "test key:100");
		// /test "key":100
		// Error from quoted key string not converting to int, no suggestions
		assertNoSuggestions(player, "test \"key\":100");

		// /test 100:value
		// Error from unquoted value string not converting to int, no suggestions
		assertNoSuggestions(player, "test 100:value 1");
		// /test 100:"value"
		// Error from quoted value not converting to int
		assertNoSuggestions(player, "test 100:\"value\" 1");


		// /test 1000:0
		// Error from unquoted key being too large, no suggestions
		assertNoSuggestions(player, "test 1000:0");
		// /test -10:0
		// Error from unquoted key being too small, no suggestions
		assertNoSuggestions(player, "test -10:0");
		// /test "1000":0
		// Error from quoted key being too large, no suggestions
		assertNoSuggestions(player, "test \"1000\":0");
		// /test "-10":0
		// Error from quoted key being too small, no suggestions
		assertNoSuggestions(player, "test \"-10\":0");

		// /test 0:1000
		// Error from unquoted value being too large, no suggestions
		assertNoSuggestions(player, "test 0:1000 1");
		// /test 0:-10
		// Error from unquoted value being too small, no suggestions
		assertNoSuggestions(player, "test 0:-10 1");
		// /test 0:"1000"
		// Error from quoted value being too large, no suggestions
		assertNoSuggestions(player, "test 0:\"1000\" 1");
		// /test 0:"-10"
		// Error from quoted value being too small, no suggestions
		assertNoSuggestions(player, "test 0:\"-10\" 1");
	}

	@Test
	void suggestionTestWithLists() {
		new CommandAPICommand("test")
			.withArguments(
				new MapArgumentBuilder<String, String>("map")
					.withKeyMapper(s -> s)
					.withValueMapper(s -> s)
					// Give key and value list
					.withKeyList(List.of("alpha", "alphabet", "bear", "bearing", "charlie"))
					.withValueList(List.of("alpha", "alphabet", "bear", "bearing", "candy"))
					.build()
			)
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();


		////////////////
		// FIRST PAIR //
		////////////////


		// /test
		// Start of suggestions, give the list
		assertCommandSuggests(player, "test ", "alpha", "alphabet", "bear", "bearing", "charlie");

		// /test "
		// Start of quoted key, give the list in quotes
		assertCommandSuggests(player, "test \"", "\"alpha\"", "\"alphabet\"", "\"bear\"", "\"bearing\"", "\"charlie\"");


		// Start of unquoted key, match based on the input
		// /test a
		assertCommandSuggests(player, "test a", "alpha", "alphabet");
		// /test b
		assertCommandSuggests(player, "test b", "bear", "bearing");
		// /test c
		assertCommandSuggests(player, "test c", "charlie");

		// Start of quoted key, match based on the input
		// /test "a
		assertCommandSuggests(player, "test \"a", "\"alpha\"", "\"alphabet\"");
		// /test "b
		assertCommandSuggests(player, "test \"b", "\"bear\"", "\"bearing\"");
		// /test "c
		assertCommandSuggests(player, "test \"c", "\"charlie\"");


		// Match for unquoted key, suggest ending or continue possibilities
		// /test alpha
		assertCommandSuggests(player, "test alpha", "alpha:", "alphabet");
		// /test bear
		assertCommandSuggests(player, "test bear", "bear:", "bearing");
		// /test charlie
		assertCommandSuggests(player, "test charlie", "charlie:");
		// Match for quoted key, suggest ending or continue possibilities
		// /test "alpha
		assertCommandSuggests(player, "test \"alpha", "\"alpha\":", "\"alphabet\"");
		// /test "bear
		assertCommandSuggests(player, "test \"bear", "\"bear\":", "\"bearing\"");
		// /test "charlie
		assertCommandSuggests(player, "test \"charlie", "\"charlie\":");

		// /test "alpha"
		// Quoted key closed, suggest delimiter
		assertCommandSuggests(player, "test \"alpha\"", ":");

		// /test "alpha"=a
		// Incorrect delimiter, error means no suggestions
		assertNoSuggestions(player, "test \"alpha\"=");


		// /test alpha:
		// Start of suggestions, give the list
		assertCommandSuggests(player, "test alpha:", "alpha", "alphabet", "bear", "bearing", "candy");

		// /test alpha:"
		// Start of quoted key, give the list in quotes
		assertCommandSuggests(player, "test alpha:\"", "\"alpha\"", "\"alphabet\"", "\"bear\"", "\"bearing\"", "\"candy\"");


		// Start of unquoted value, match based on the input
		// /test alpha:a
		assertCommandSuggests(player, "test alpha:a", "alpha", "alphabet");
		// /test alpha:b
		assertCommandSuggests(player, "test alpha:b", "bear", "bearing");
		// /test alpha:c
		assertCommandSuggests(player, "test alpha:c", "candy");

		// Start of quoted value, match based on the input
		// /test alpha:"a
		assertCommandSuggests(player, "test alpha:\"a", "\"alpha\"", "\"alphabet\"");
		// /test alpha:"b
		assertCommandSuggests(player, "test alpha:\"b", "\"bear\"", "\"bearing\"");
		// /test alpha:"c
		assertCommandSuggests(player, "test alpha:\"c", "\"candy\"");


		// Match for unquoted value, suggest ending or continue possibilities
		// /test alpha:alpha
		assertCommandSuggests(player, "test alpha:alpha", "alpha ", "alphabet");
		// /test alpha:bear
		assertCommandSuggests(player, "test alpha:bear", "bear ", "bearing");
		// /test alpha:candy
		assertCommandSuggests(player, "test alpha:candy", "candy ");
		// Match for quoted value, suggest ending or continue possibilities
		// /test alpha:"alpha
		assertCommandSuggests(player, "test alpha:\"alpha", "\"alpha\" ", "\"alphabet\"");
		// /test alpha:"bear
		assertCommandSuggests(player, "test alpha:\"bear", "\"bear\" ", "\"bearing\"");
		// /test alpha:"candy
		assertCommandSuggests(player, "test alpha:\"candy", "\"candy\" ");

		// /test alpha:"alpha"
		// Quoted value closed, suggest separator
		assertCommandSuggests(player, "test alpha:\"alpha\"", " ");

		// /test alpha:"alpha"=a
		// Incorrect separator, error means no suggestions
		assertNoSuggestions(player, "test alpha:\"alpha\"=");


		/////////////////
		// SECOND PAIR //
		/////////////////


		// Make sure duplicate key/values are not suggested
		// /test alpha:alpha
		assertCommandSuggests(player, "test alpha:alpha ", "alphabet", "bear", "bearing", "charlie");
		// /test alpha:alpha "
		assertCommandSuggests(player, "test alpha:alpha \"", "\"alphabet\"", "\"bear\"", "\"bearing\"", "\"charlie\"");
		// /test alpha:alpha alpha:
		assertCommandSuggests(player, "test alpha:alpha bear:", "alphabet", "bear", "bearing", "candy");
		// /test alpha:alpha alpha:"
		assertCommandSuggests(player, "test alpha:alpha bear:\"", "\"alphabet\"", "\"bear\"", "\"bearing\"", "\"candy\"");

		// /test alphabet:alphabet
		assertCommandSuggests(player, "test alphabet:alphabet ", "alpha", "bear", "bearing", "charlie");
		// /test alphabet:alphabet "
		assertCommandSuggests(player, "test alphabet:alphabet \"", "\"alpha\"", "\"bear\"", "\"bearing\"", "\"charlie\"");
		// /test alphabet:alphabet alpha:
		assertCommandSuggests(player, "test alphabet:alphabet alpha:", "alpha", "bear", "bearing", "candy");
		// /test alphabet:alphabet alpha:"
		assertCommandSuggests(player, "test alphabet:alphabet alpha:\"", "\"alpha\"", "\"bear\"", "\"bearing\"", "\"candy\"");


		////////////////
		// THIRD PAIR //
		////////////////


		// Make sure duplicate key/values are not suggested
		// /test alpha:alpha alphabet:alphabet
		assertCommandSuggests(player, "test alpha:alpha alphabet:alphabet ", "bear", "bearing", "charlie");
		// /test alpha:alpha alphabet:alphabet "
		assertCommandSuggests(player, "test alpha:alpha alphabet:alphabet \"", "\"bear\"", "\"bearing\"", "\"charlie\"");
		// /test alpha:alpha alphabet:alphabet bear:
		assertCommandSuggests(player, "test alpha:alpha alphabet:alphabet bear:", "bear", "bearing", "candy");
		// /test alpha:alpha alphabet:alphabet bear:"
		assertCommandSuggests(player, "test alpha:alpha alphabet:alphabet bear:\"", "\"bear\"", "\"bearing\"", "\"candy\"");


		//////////////////
		// END OF PAIRS //
		//////////////////


		// /test alpha:alpha alpha:alphabet
		// Error for having duplicate unquoted key alpha, no suggestions
		assertNoSuggestions(player, "test alpha:alpha alpha:alphabet");
		// /test alpha:alpha "alpha":alphabet
		// Error for having duplicate quoted alpha, no suggestions
		assertNoSuggestions(player, "test alpha:alpha \"alpha\":alphabet");

		// /test alpha:alpha alphabet:alpha b
		// Error for having duplicate unquoted value alpha, no suggestions
		assertNoSuggestions(player, "test alpha:alpha alphabet:alpha b");
		// /test alpha:alpha alphabet:"alpha" b
		// Error for having duplicate quoted value alpha, no suggestions
		assertNoSuggestions(player, "test alpha:alpha alphabet:\"alpha\" b");


		// /test key:
		// Error for having invalid unquoted key, no suggestions
		assertNoSuggestions(player, "test key:");
		// /test "key":
		// Error for having duplicate quoted key, no suggestions
		assertNoSuggestions(player, "test \"key\":");

		// /test alpha:value b
		// Error for having invalid unquoted value, no suggestions
		assertNoSuggestions(player, "test alpha:value b");
		// /test alpha:"value" b
		// Error for having invalid quoted value, no suggestions
		assertNoSuggestions(player, "test alpha:\"value\" b");
	}

	@Test
	void suggestionTestWithDuplicateValues() {
		new CommandAPICommand("test")
			.withArguments(
				new MapArgumentBuilder<String, String>("map")
					.withKeyMapper(s -> s)
					.withValueMapper(s -> s)
					.withoutKeyList()
					// Allowing duplicate values
					.withoutValueList(true)
					.build()
			)
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// Allowing duplicate values doesn't do much else beyond that, so there is not much extra to test

		// /test key1:value key2:value
		// Keep suggesting after duplicate value
		assertCommandSuggests(player, "test key1:value key2:value a", "a:");
	}

	@Test
	void suggestionTestWithDuplicateValuesAndList() {
		new CommandAPICommand("test")
			.withArguments(
				new MapArgumentBuilder<String, String>("map")
					.withKeyMapper(s -> s)
					.withValueMapper(s -> s)
					.withoutKeyList()
					// Giving list and allowing duplicate values
					.withValueList(List.of("value1", "value2", "value3"), true)
					.build()
			)
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// Allowing duplicate values doesn't do much else beyond that, so there is not much extra to test

		// /test key1:value1 key2:value1
		// Keep suggesting after duplicate value
		assertCommandSuggests(player, "test k1:value1 k2:value2 k3:value3 k4:", "value1", "value2", "value3");
	}

	@Test
	void suggestionTestWithSpecialCharactersAndList() {
		// Escape sequences need to be suggested in their unescaped form
		new CommandAPICommand("test")
			.withArguments(
				new MapArgumentBuilder<String, String>("map")
					.withKeyMapper(s -> s)
					.withValueMapper(s -> s)
					// Give lists containing results with special values
					.withKeyList(List.of("a:b", "a b", "a\\b", "a\"b", "\"ab"))
					.withValueList(List.of("a:b", "a b", "a\\b", "a\"b", "\"ab"))
					.build()
			)
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// We prefer results containing their own terminator to be quoted
		// /test
		assertCommandSuggests(player, "test ", "\"a:b\"", "\\\"ab", "a b", "a\"b", "a\\\\b");
		// /test a b:
		assertCommandSuggests(player, "test a b:", "\"a b\"", "\\\"ab", "a\"b", "a:b", "a\\\\b");

		// Give correct suggestions when unquoted
		// /test a
		assertCommandSuggests(player, "test a", "a b", "a\"b", "a\\:b", "a\\\\b");
		// /test a b:a
		assertCommandSuggests(player, "test a b:a", "a\"b", "a:b", "a\\ b", "a\\\\b");

		// Give correct suggestions when quoted
		// /test "a
		assertCommandSuggests(player, "test \"a", "\"a b\"", "\"a:b\"", "\"a\\\"b\"", "\"a\\\\b\"");
		// /test a b:"a
		assertCommandSuggests(player, "test a b:\"a", "\"a b\"", "\"a:b\"", "\"a\\\"b\"", "\"a\\\\b\"");
	}

	@Test
	void suggestionTestWithLongTerminatorsAndLists() {
		// There are a few edge cases to clean up here
		new CommandAPICommand("test")
			.withArguments(
				// Take advantage of String terminators
				new MapArgumentBuilder<String, String>("map", ':', ",  ")
					.withKeyMapper(s -> s)
					.withValueMapper(s -> s)
					// Give key and value list
					.withKeyList(List.of("alpha", "alphabet", "bear", "bearing", "charlie", "a-b-c"))
					.withValueList(List.of("alpha", "alphabet", "bear", "bearing", "candy", "a, b, c"))
					.build()
			)
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// /test alphab
		// Either mistyped terminator or is continuing a key
		assertCommandSuggests(player, "test alphab", "alpha:", "alphabet");
		// /test alpha:alphab
		// Either mistyped separator or is continuing a value
		assertCommandSuggests(player, "test alpha:alphab", "alpha,  ", "alphabet");


		// /test a
		// `a-b-c` initially looks to start the delimiter, but doesn't and so shouldn't be escaped
		assertCommandSuggests(player, "test a", "a-b-c", "alpha", "alphabet");
		// /test alpha:a
		// `a, b, c` initially looks to start the separator, but doesn't and so shouldn't be escaped
		assertCommandSuggests(player, "test alpha:a", "a, b, c", "alpha", "alphabet");
	}
}
