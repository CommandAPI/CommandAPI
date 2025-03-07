package dev.jorel.commandapi.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.RegisteredCommand;
import dev.jorel.commandapi.RegisteredCommand.Node;
import dev.jorel.commandapi.help.EditableHelpTopic;

public abstract class RegisteredCommandTestBase extends TestBase {

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

	/*******************
	 * Utility methods *
	 *******************/

	@SafeVarargs
	public final void assertCreatedSimpleRegisteredCommand(String name, NodeBuilder args, List<String>... argsAsStr) {
		RegisteredCommand<CommandSender> expectedCommand = simpleRegisteredCommand(name, "minecraft", args);

		assertCreatedRegisteredCommands(expectedCommand.copyWithEmptyNamespace(), expectedCommand);

		// `.get(0)` makes sense since we just verified `CommandAPI#getRegisteredCommands` has elements
		assertEquals(Arrays.asList(argsAsStr), CommandAPI.getRegisteredCommands().get(0).rootNode().argsAsStr());
	}

	public RegisteredCommand<CommandSender> simpleRegisteredCommand(String name, String namespace, NodeBuilder args, String... aliases) {
		return new RegisteredCommand<>(
			name, aliases, namespace,
			new EditableHelpTopic<>(),
			args.build()
		);
	}

	public static class NodeBuilder {
		public static NodeBuilder node(String name, Class<?> clazz, boolean executable) {
			return new NodeBuilder(name, clazz, executable);
		}

		public static List<Node<CommandSender>> children(NodeBuilder... children) {
			List<Node<CommandSender>> result = new ArrayList<>(children.length);
			for (NodeBuilder child : children) {
				result.add(child.build());
			}
			return result;
		}

		private final String nodeName;
		private final String className;
		private final boolean executable;

		private String helpString;
		private CommandPermission permission = CommandPermission.NONE;
		private Predicate<CommandSender> requirements = CommandPermission.TRUE();

		private final List<Node<CommandSender>> children;

		public NodeBuilder(String nodeName, Class<?> clazz, boolean executable) {
			this.nodeName = nodeName;
			this.className = clazz.getSimpleName();
			this.executable = executable;

			this.helpString = "<" + nodeName + ">";
			this.children = new ArrayList<>();
		}

		public NodeBuilder helpString(String helpString) {
			this.helpString = helpString;
			return this;
		}

		public NodeBuilder permission(CommandPermission permission) {
			this.permission = permission;
			return this;
		}

		public NodeBuilder requirements(Predicate<CommandSender> requirements) {
			this.requirements = requirements;
			return this;
		}

		public NodeBuilder withChildren(NodeBuilder... children) {
			for (NodeBuilder child : children) {
				this.children.add(child.build());
			}
			return this;
		}

		@SafeVarargs
		public final NodeBuilder withChildren(Node<CommandSender>... children) {
			return withChildren(Arrays.asList(children));
		}

		public NodeBuilder withChildren(List<Node<CommandSender>> children) {
			this.children.addAll(children);
			return this;
		}

		public Node<CommandSender> build() {
			return new Node<>(nodeName, className, helpString, executable, permission, requirements, children);
		}
	}

	@SafeVarargs
	public final void assertCreatedRegisteredCommands(RegisteredCommand<CommandSender>... commands) {
		List<RegisteredCommand<CommandSender>> expectedCommands = Arrays.asList(commands);
		List<RegisteredCommand<CommandSender>> actualCommands = CommandAPI.getRegisteredCommands();

		if (expectedCommands.size() != actualCommands.size()) {
			StringBuilder builder = new StringBuilder();
			builder.append("Expected ").append(expectedCommands.size()).append(" command(s), found ").append(actualCommands.size()).append(" command(s).");

			builder.append("\nExpected: ");
			addRegisteredCommandList(builder, expectedCommands);
			builder.append("\nActual: ");
			addRegisteredCommandList(builder, actualCommands);

			fail(builder.toString());
		}

		for (int i = 0; i < expectedCommands.size(); i++) {
			RegisteredCommand<CommandSender> expectedCommand = expectedCommands.get(i);
			RegisteredCommand<CommandSender> actualCommand = actualCommands.get(i);

			if (!Objects.equals(expectedCommand, actualCommand)) {
				StringBuilder builder = new StringBuilder();
				builder.append("Command #").append(i + 1).append(" differed. Expected:\n");
				builder.append(expectedCommand);
				builder.append("\nActual:\n");
				builder.append(actualCommand);

				builder.append("\nExpected list: ");
				addRegisteredCommandList(builder, expectedCommands);
				builder.append("\nActual list: ");
				addRegisteredCommandList(builder, actualCommands);

				fail(builder.toString());
			}
		}
	}

	private void addRegisteredCommandList(StringBuilder builder, List<RegisteredCommand<CommandSender>> commands) {
		if (commands.isEmpty()) {
			builder.append("[]");
			return;
		}

		builder.append("[\n");
		for (RegisteredCommand<CommandSender> command : commands) {
			builder.append("\t");
			builder.append(command);
			builder.append("\n");
		}
		builder.append("]");
	}
}
