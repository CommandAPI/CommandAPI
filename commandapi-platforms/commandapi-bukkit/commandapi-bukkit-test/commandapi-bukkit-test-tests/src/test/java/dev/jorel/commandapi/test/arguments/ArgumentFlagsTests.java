package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.FlagsArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.exceptions.GreedyArgumentException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;

/**
 * Tests for the {@link FlagsArgument}
 */
class ArgumentFlagsTests extends TestBase {

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
	@Test
	void commandBuildingTestWithLoopingAndTerminalBranchesAndFollowingArgument() {
		new CommandAPICommand("test")
			.withArguments(
				new FlagsArgument("flags")
					.loopingBranch(new LiteralArgument("loop1"), new StringArgument("value"))
					.loopingBranch(new LiteralArgument("loop2"))
					.terminalBranch(new LiteralArgument("end1"), new IntegerArgument("value"))
					.terminalBranch(new LiteralArgument("end2")),
				new StringArgument("argument")
			)
			.executesPlayer(P_EXEC)
			.register();

		// When commands are sent to the player, looping uses redirects
		assertEquals("""
				{
				  "type": "root",
				  "children": {
				    "test": {
				      "type": "literal",
				      "children": {
				        "flags": {
				          "type": "flagsArgumentRootNode",
				          "children": {
				            "loop1": {
				              "type": "literal",
				              "children": {
				                "value": {
				                  "type": "argument",
				                  "argumentType": "com.mojang.brigadier.arguments.StringArgumentType",
				                  "properties": {
				                    "type": "word"
				                  },
				                  "redirect": [
				                    "test",
				                    "flags"
				                  ]
				                }
				              }
				            },
				            "end1": {
				              "type": "literal",
				              "children": {
				                "value": {
				                  "type": "argument",
				                  "argumentType": "com.mojang.brigadier.arguments.IntegerArgumentType",
				                  "children": {
				                    "argument": [
				                      "test",
				                      "flags",
				                      "end2",
				                      "argument"
				                    ]
				                  }
				                }
				              }
				            },
				            "loop2": {
				              "type": "literal",
				              "redirect": [
				                "test",
				                "flags"
				              ]
				            },
				            "end2": {
				              "type": "literal",
				              "children": {
				                "argument": {
				                  "type": "argument",
				                  "argumentType": "com.mojang.brigadier.arguments.StringArgumentType",
				                  "properties": {
				                    "type": "word"
				                  },
				                  "executable": true
				                }
				              }
				            }
				          }
				        }
				      }
				    }
				  }
				}""",
			getDispatcherString()
		);
	}

	@Test
	void executionTestLoopingBranches() {
		Mut<List<Object>> results = Mut.of();
		Player sender = server.addPlayer();

		new CommandAPICommand("test")
			.withArguments(
				new FlagsArgument("flags")
					.loopingBranch(new LiteralArgument("choice", "loop1").setListed(true), new StringArgument("value"))
					.loopingBranch(new LiteralArgument("choice", "loop2").setListed(true), new IntegerArgument("value"))
					.loopingBranch(new MultiLiteralArgument("choice", "loop3"))
					.loopingBranch(new MultiLiteralArgument("choice", "loop4", "loop5"))
			)
			.executes(info -> {
				List<CommandArguments> flags = info.args().getUnchecked("flags");

				List<Object> objects = new ArrayList<>();
				for (CommandArguments branches : flags) {
					objects.add(branches.get("choice"));
					objects.add(branches.get("value"));
				}

				results.set(objects);
			})
			.register();

		// Execute branch by itself
		assertStoresResult(sender, "test flags loop1 alice", results, List.of("loop1", "alice"));
		assertStoresResult(sender, "test flags loop2 0", results, List.of("loop2", 0));
		// Arrays.asList is necessary for null elements
		assertStoresResult(sender, "test flags loop3", results, Arrays.asList("loop3", null));
		assertStoresResult(sender, "test flags loop4", results, Arrays.asList("loop4", null));
		assertStoresResult(sender, "test flags loop5", results, Arrays.asList("loop5", null));

		// Go back to self
		assertStoresResult(sender, "test flags loop1 alice loop1 bob", results, List.of("loop1", "alice", "loop1", "bob"));
		assertStoresResult(sender, "test flags loop2 0 loop2 10", results, List.of("loop2", 0, "loop2", 10));

		// Go to other branches
		assertStoresResult(sender, "test flags loop1 bob loop2 0", results, List.of("loop1", "bob", "loop2", 0));
		assertStoresResult(sender, "test flags loop2 10 loop1 alice", results, List.of("loop2", 10, "loop1", "alice"));

		assertStoresResult(sender, "test flags loop3 loop4 loop5 loop3", results, Arrays.asList("loop3", null, "loop4", null, "loop5", null, "loop3", null));

		// Incomplete branch fails
		assertCommandFailsWith(
			sender, 
			"test flags", 
			"Unknown or incomplete command, see below for error at position 10: test flags<--[HERE]"
		);
		assertCommandFailsWith(
			sender, 
			"test flags loop1 alice loop1", 
			"Unknown or incomplete command, see below for error at position 28: ...lice loop1<--[HERE]"
		);
		assertCommandFailsWith(
			sender, 
			"test flags loop2 0 loop2", 
			"Unknown or incomplete command, see below for error at position 24: ...p2 0 loop2<--[HERE]"
		);

		// Argument parse failure
		assertCommandFailsWith(
			sender, 
			"test flags lo", 
			"Incorrect argument for command at position 11: ...est flags <--[HERE]"
		);
		assertCommandFailsWith(
			sender, 
			"test flags loop2 0 loop2 NaN", 
			"Expected integer at position 25: ...2 0 loop2 <--[HERE]"
		);

		assertNoMoreResults(results);
	}

	@Test
	void exceptionTestLoopingBranchesWithFollowingArgument() {
		// One branch
		CommandAPICommand oneBranch = new CommandAPICommand("oneBranch")
			.withArguments(
				new FlagsArgument("flags")
					.loopingBranch(new LiteralArgument("branch1")),
				new StringArgument("argument")
			)
			.executesPlayer(P_EXEC);

		assertThrowsWithMessage(
			GreedyArgumentException.class,
			"A greedy argument can only be declared at the end of a command. " + 
			"Going down the [oneBranch<MultiLiteralArgument>] branch, found the greedy argument flags<FlagsArgument> followed by argument<StringArgument>",
			oneBranch::register
		);

		// Two branches
		CommandAPICommand twoBranches = new CommandAPICommand("twoBranches")
			.withArguments(
				new FlagsArgument("flags")
					.loopingBranch(new LiteralArgument("branch1"))
					.loopingBranch(new LiteralArgument("branch2")),
				new StringArgument("argument")
			)
			.executesPlayer(P_EXEC);

		assertThrowsWithMessage(
			GreedyArgumentException.class, 
			"A greedy argument can only be declared at the end of a command. " + 
			"Going down the [twoBranches<MultiLiteralArgument>] branch, found the greedy argument flags<FlagsArgument> followed by argument<StringArgument>",
			twoBranches::register
		);

		// Deep branch
		CommandAPICommand deepBranch = new CommandAPICommand("deepBranch")
			.withArguments(
				new FlagsArgument("flags")
					.loopingBranch(new LiteralArgument("branch1"), new StringArgument("branchArgument")),
				new StringArgument("argument")
			)
			.executesPlayer(P_EXEC);
		
		assertThrowsWithMessage(
			GreedyArgumentException.class, 
			"A greedy argument can only be declared at the end of a command. " + 
			"Going down the [deepBranch<MultiLiteralArgument>] branch, found the greedy argument flags<FlagsArgument> followed by argument<StringArgument>",
			deepBranch::register
		);

		// Combined argument
		CommandAPICommand combinedArgument = new CommandAPICommand("combinedArgument")
			.withArguments(
				new FlagsArgument("flags")
					.loopingBranch(new LiteralArgument("branch1"))
					.combineWith(new StringArgument("argument"))
			)
			.executesPlayer(P_EXEC);

		assertThrowsWithMessage(
			GreedyArgumentException.class, 
			"A greedy argument can only be declared at the end of a command. Going down the [combinedArgument<MultiLiteralArgument>] branch, found the greedy argument flags<FlagsArgument> followed by argument<StringArgument>", 
			combinedArgument::register
		);
	}

	@Test
	void executionTestTerminalBranches() {
		Mut<List<Object>> results = Mut.of();
		Player sender = server.addPlayer();

		new CommandAPICommand("test")
			.withArguments(
				new FlagsArgument("flags")
					.terminalBranch(new LiteralArgument("choice", "end1").setListed(true), new StringArgument("value"))
					.terminalBranch(new LiteralArgument("choice", "end2").setListed(true), new IntegerArgument("value"))
					.terminalBranch(new MultiLiteralArgument("choice", "end3"))
					.terminalBranch(new MultiLiteralArgument("choice", "end4", "end5"))
			)
			.executes(info -> {
				List<CommandArguments> flags = info.args().getUnchecked("flags");

				List<Object> objects = new ArrayList<>();
				for (CommandArguments branches : flags) {
					objects.add(branches.get("choice"));
					objects.add(branches.get("value"));
				}

				results.set(objects);
			})
			.register();

		// Execute branch by itself
		assertStoresResult(sender, "test flags end1 alice", results, List.of("end1", "alice"));
		assertStoresResult(sender, "test flags end2 0", results, List.of("end2", 0));
		// Arrays.asList is necessary for null elements
		assertStoresResult(sender, "test flags end3", results, Arrays.asList("end3", null));
		assertStoresResult(sender, "test flags end4", results, Arrays.asList("end4", null));
		assertStoresResult(sender, "test flags end5", results, Arrays.asList("end5", null));

		// Branches do not go back to themselves
		assertCommandFailsWith(
			sender, 
			"test flags end1 alice end1 bob", 
			"Incorrect argument for command at position 22: ...nd1 alice <--[HERE]"
		);
		assertCommandFailsWith(
			sender, 
			"test flags end2 0 end2 10", 
			"Incorrect argument for command at position 18: ...gs end2 0 <--[HERE]"
		);
		assertCommandFailsWith(
			sender, 
			"test flags end3 end3", 
			"Incorrect argument for command at position 16: ...lags end3 <--[HERE]"
		);
		assertCommandFailsWith(
			sender, 
			"test flags end4 end4", 
			"Incorrect argument for command at position 16: ...lags end4 <--[HERE]"
		);
		assertCommandFailsWith(
			sender, 
			"test flags end5 end5", 
			"Incorrect argument for command at position 16: ...lags end5 <--[HERE]"
		);

		// Branches do not go back to other branches
		assertCommandFailsWith(
			sender, 
			"test flags end1 alice end3", 
			"Incorrect argument for command at position 22: ...nd1 alice <--[HERE]"
		);
		assertCommandFailsWith(
			sender, 
			"test flags end2 0 end1 alice", 
			"Incorrect argument for command at position 18: ...gs end2 0 <--[HERE]"
		);
		assertCommandFailsWith(
			sender, 
			"test flags end4 end5", 
			"Incorrect argument for command at position 16: ...lags end4 <--[HERE]"
		);

		// Incomplete branch fails
		assertCommandFailsWith(
			sender, 
			"test flags", 
			"Unknown or incomplete command, see below for error at position 10: test flags<--[HERE]"
		);
		assertCommandFailsWith(
			sender, 
			"test flags end1", 
			"Unknown or incomplete command, see below for error at position 15: ...flags end1<--[HERE]"
		);
		assertCommandFailsWith(
			sender, 
			"test flags end2", 
			"Unknown or incomplete command, see below for error at position 15: ...flags end2<--[HERE]"
		);

		// Argument parse failure
		assertCommandFailsWith(
			sender, 
			"test flags en", 
			"Incorrect argument for command at position 11: ...est flags <--[HERE]"
		);
		assertCommandFailsWith(
			sender, 
			"test flags end2 NaN", 
			"Expected integer at position 16: ...lags end2 <--[HERE]"
		);

		assertNoMoreResults(results);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void executionTestTerminalBranchesWithFollowingArgument(boolean combineArgument) {
		Mut<List<Object>> results = Mut.of();
		Player sender = server.addPlayer();

		// Arguments
		FlagsArgument flags = new FlagsArgument("flags")
			.terminalBranch(new LiteralArgument("string"), new StringArgument("value"))
			.terminalBranch(new LiteralArgument("int"), new IntegerArgument("value"));
		StringArgument following = new StringArgument("argument");

		// Command
		CommandAPICommand test = new CommandAPICommand("test");

		test.withArguments(flags);

		if (combineArgument) {
			flags.combineWith(following);
		} else {
			test.withArguments(following);
		}
		
		test.executes(info -> {
				CommandArguments args = info.args();

				List<CommandArguments> branches = args.getUnchecked("flags");
				assertEquals(1, branches.size());
				CommandArguments branch = branches.get(0);

				results.set(List.of(branch.get("value"), args.get("argument")));
			});
		test.register();

		// Branches converge on same argument
		assertStoresResult(sender, "test flags string alice argument", results, List.of("alice", "argument"));
		assertStoresResult(sender, "test flags int 10 argument", results, List.of(10, "argument"));

		// Incomplete branches fail
		assertCommandFailsWith(
			sender, 
			"test flags", 
			"Unknown or incomplete command, see below for error at position 10: test flags<--[HERE]"
		);

		assertCommandFailsWith(
			sender, 
			"test flags string", 
			"Unknown or incomplete command, see below for error at position 17: ...ags string<--[HERE]"
		);
		assertCommandFailsWith(
			sender, 
			"test flags int", 
			"Unknown or incomplete command, see below for error at position 14: ... flags int<--[HERE]"
		);

		assertCommandFailsWith(
			sender, 
			"test flags string alice", 
			"Unknown or incomplete command, see below for error at position 23: ...ring alice<--[HERE]"
		);
		assertCommandFailsWith(
			sender, 
			"test flags int 10", 
			"Unknown or incomplete command, see below for error at position 17: ...ags int 10<--[HERE]"
		);

		assertNoMoreResults(results);
	}

	@Test
	void executionTestLoopingAndTerminalBranches() {
		Mut<List<Object>> results = Mut.of();
		Player sender = server.addPlayer();

		new CommandAPICommand("test")
			.withArguments(
				new FlagsArgument("flags")
					.loopingBranch(new LiteralArgument("choice", "loop1").setListed(true), new StringArgument("value"))
					.loopingBranch(new LiteralArgument("choice", "loop2").setListed(true), new IntegerArgument("value"))
					.terminalBranch(new LiteralArgument("choice", "end1").setListed(true), new StringArgument("value"))
					.terminalBranch(new LiteralArgument("choice", "end2").setListed(true), new IntegerArgument("value"))
			)
			.executes(info -> {
				List<CommandArguments> flags = info.args().getUnchecked("flags");

				List<Object> objects = new ArrayList<>();
				for (CommandArguments branches : flags) {
					objects.add(branches.get("choice"));
					objects.add(branches.get("value"));
				}

				results.set(objects);
			})
			.register();

		// One-branch options
		assertCommandFailsWith(
			sender, 
			"test flags loop1 alice", 
			"Unknown or incomplete command, see below for error at position 22: ...oop1 alice<--[HERE]"
		);
		assertCommandFailsWith(
			sender, 
			"test flags loop2 0", 
			"Unknown or incomplete command, see below for error at position 18: ...gs loop2 0<--[HERE]"
		);
		assertStoresResult(sender, "test flags end1 alice", results, List.of("end1", "alice"));
		assertStoresResult(sender, "test flags end2 0", results, List.of("end2", 0));

		// Two-branch options
		assertCommandFailsWith(
			sender, 
			"test flags loop1 alice loop2 0", 
			"Unknown or incomplete command, see below for error at position 30: ...ce loop2 0<--[HERE]"
		);
		assertCommandFailsWith(
			sender, 
			"test flags loop2 10 loop1 bob", 
			"Unknown or incomplete command, see below for error at position 29: ... loop1 bob<--[HERE]"
		);
		assertStoresResult(sender, "test flags loop1 alice end2 10", results, List.of("loop1", "alice", "end2", 10));
		assertStoresResult(sender, "test flags loop2 0 end1 bob", results, List.of("loop2", 0, "end1", "bob"));
		assertCommandFailsWith(
			sender, 
			"test flags end1 alice loop1 bob", 
			"Incorrect argument for command at position 22: ...nd1 alice <--[HERE]"
		);
		assertCommandFailsWith(
			sender, 
			"test flags end2 0 loop2 10", 
			"Incorrect argument for command at position 18: ...gs end2 0 <--[HERE]"
		);

		// Three-branch options
		assertStoresResult(sender, "test flags loop1 alice loop2 10 end1 bob", results, List.of("loop1", "alice", "loop2", 10, "end1", "bob"));
		assertStoresResult(sender, "test flags loop2 0 loop1 bob end2 10", results, List.of("loop2", 0, "loop1", "bob", "end2", 10));

		assertNoMoreResults(results);
	}

	private record Color(int red, int green, int blue) {}

	@Test
	void suggestionsAndExecutionTestWithColorArgument() {
		Mut<Color> results = Mut.of();
		Player sender = server.addPlayer();

		new CommandAPICommand("test")
			.withArguments(
				new CustomArgument<>(
					new FlagsArgument("color")
						.loopingBranch(
							new StringArgument("channel").replaceSuggestions(ArgumentSuggestions.strings(info -> {
								Set<String> channelsLeft = new HashSet<>(Set.of("-r", "-g", "-b"));
								for(CommandArguments previousChannels : info.previousArgs().<List<CommandArguments>>getUnchecked("color")) {
									// Yes, you can reference previous versions of yourself
									channelsLeft.remove(previousChannels.<String>getUnchecked("channel"));
								}
								return channelsLeft.toArray(String[]::new);
							})),
							new IntegerArgument("value", 0, 255)
						),
					info -> {
						int red = 0, green = 0, blue = 0;
						for (CommandArguments channels : (List<CommandArguments>) info.currentInput()) {
							int value = channels.getUnchecked("value");

							String channel = channels.getUnchecked("channel");
							switch (channel) {
								case "-r" -> red = value;
								case "-g" -> green = value;
								case "-b" -> blue = value;
								default -> throw CustomArgument.CustomArgumentException.fromString("Unknown channel \"" + channel + "\"");
							}
						}
						return new Color(red, green, blue);
					}
				)
			)
			.executes(info -> {
				results.set(info.args().getUnchecked("color"));
			})
			.register();

		// Initial suggestions
		assertCommandSuggests(sender, "test color ", "-b", "-g", "-r");

		// First iteration
		assertCommandSuggests(sender, "test color -r 50 ", "-b", "-g");
		assertCommandSuggests(sender, "test color -g 50 ", "-b", "-r");
		assertCommandSuggests(sender, "test color -b 50 ", "-g", "-r");

		// Third iteration
		assertCommandSuggests(sender, "test color -r 50 -g 50 ", "-b");
		assertCommandSuggests(sender, "test color -g 50 -b 50 ", "-r");
		assertCommandSuggests(sender, "test color -b 50 -r 50 ", "-g");

		// Test execution
		assertStoresResult(sender, "test color -r 50", results, new Color(50, 0, 0));
		assertStoresResult(sender, "test color -g 100 -b 255", results, new Color(0, 100, 255));
		assertStoresResult(sender, "test color -g 10 -r 100 -g 200 -b 5", results, new Color(100, 200, 5));

		// Test parse errors
		assertCommandFailsWith(
			sender, 
			"test color -f 50", 
			"Unknown channel \"-f\""
		);
		assertCommandFailsWith(
			sender, 
			"test color -g 300", 
			"Integer must not be more than 255, found 300 at position 14: ... color -g <--[HERE]"
		);

		assertNoMoreResults(results);
	}
}
