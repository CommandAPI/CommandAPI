package dev.jorel.commandapi;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.arguments.parser.Parser;
import dev.jorel.commandapi.arguments.parser.ParserArgument;
import dev.jorel.commandapi.executors.NormalExecutorInfo;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.function.Executable;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class CommandTestBase extends CommandAPITestUtilities {
	// Useful objects
	public static NormalExecutorInfo<CommandSender, ?> DEFAULT_EXECUTOR = info -> {};

	// Setup
	protected ServerMock server;
	protected MockCommandAPIPlugin plugin;

	public void setUp() {
		server = MockBukkit.mock();
		plugin = MockBukkit.load(MockCommandAPIPlugin.class);
	}

	public void tearDown() {
		MockBukkit.unmock();
	}

	// Additional test utilities
	@CanIgnoreReturnValue
	public <T extends Throwable> T assertThrowsWithMessage(Class<T> exceptionClass, Executable executable, String expectedMessage) {
		T exception = assertThrows(exceptionClass, executable);
		String actualMessage = exception.getMessage();
		if (!expectedMessage.equals(actualMessage)) {
			throw new AssertionFailedError(
				"Exception messages did not match. Expected <" + expectedMessage + "> but was <" + actualMessage + ">.",
				expectedMessage, actualMessage, exception
			);
		}
		return exception;
	}

	@CanIgnoreReturnValue
	public AssertionFailedError assertAssertionFails(Executable executable, String expectedMessage) {
		return assertThrowsWithMessage(AssertionFailedError.class, executable, expectedMessage);
	}

	public <T> CommandContext<MockCommandSource> createContextWithParser(
		CommandSender source, String key, Parser<T> parser, String input
	) throws CommandSyntaxException {
		CommandDispatcher<MockCommandSource> dispatcher = getCommandAPIPlatform().getBrigadierDispatcher();
		CommandContextBuilder<MockCommandSource> contextBuilder = new CommandContextBuilder<>(
			dispatcher,
			new MockCommandSource(source),
			dispatcher.getRoot(),
			0
		);

		StringReader reader = new StringReader(input);
		T result = parser.parse(reader);
		contextBuilder.withArgument(key, new ParsedArgument<>(0, reader.getCursor(), result));

		return contextBuilder.build(input);
	}

	public <T> CommandContext<MockCommandSource> createContextWithParser(
		CommandSender source, String key, ParserArgument<T> parser, String input
	) throws CommandSyntaxException {
		return createContextWithParser(source, key, (Parser<T>) parser, input);
	}
}
