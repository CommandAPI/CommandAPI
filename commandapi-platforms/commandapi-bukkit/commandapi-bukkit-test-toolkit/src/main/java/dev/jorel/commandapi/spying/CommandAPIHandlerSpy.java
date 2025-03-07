package dev.jorel.commandapi.spying;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.CommandAPIExecutor;
import dev.jorel.commandapi.CommandAPIHandler;
import dev.jorel.commandapi.MockCommandSource;
import dev.jorel.commandapi.arguments.Argument;
import org.bukkit.command.CommandSender;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;

public class CommandAPIHandlerSpy {
	// Handler instances
	private final CommandAPIHandler<Argument<?>, CommandSender, MockCommandSource> handler;
	private final CommandAPIHandler<Argument<?>, CommandSender, MockCommandSource> spyHandler;

	public CommandAPIHandler<Argument<?>, CommandSender, MockCommandSource> spyHandler() {
		return spyHandler;
	}

	// Methods for handling intercepts
	ExecutionQueue executionQueue = new ExecutionQueue();

	public ExecutionQueue getExecutionQueue() {
		return executionQueue;
	}

	// Setup
	public CommandAPIHandlerSpy(CommandAPIHandler<?, ?, ?> commandAPIHandler) {
		handler = (CommandAPIHandler<Argument<?>, CommandSender, MockCommandSource>) commandAPIHandler;
		spyHandler = Mockito.spy(handler);

		Mockito.doAnswer(i -> generateBrigadierCommand(i.getArgument(0), i.getArgument(1)))
				.when(spyHandler).generateBrigadierCommand(any(), any());
	}

	// Intercepted methods
	private Command<MockCommandSource> generateBrigadierCommand(List<Argument<?>> arguments, CommandAPIExecutor<CommandSender> executor) {
		CommandAPIExecutor<CommandSender> spyExecutor = Mockito.spy(executor);

		try {
			// Not using Mockito.when to avoid calling real executes method
			Mockito.doAnswer(i -> {
				executionQueue.add(i.getArgument(0));
				return i.callRealMethod();
			}).when(spyExecutor).execute(any());
		} catch (CommandSyntaxException ignored) {
			// `spyExecutor#execute` will never actually throw an exception
		}

		return handler.generateBrigadierCommand(arguments, spyExecutor);
	}
}
