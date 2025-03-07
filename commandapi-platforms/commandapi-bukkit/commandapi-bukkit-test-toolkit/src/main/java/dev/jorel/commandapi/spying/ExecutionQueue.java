package dev.jorel.commandapi.spying;

import dev.jorel.commandapi.MockCommandSource;
import dev.jorel.commandapi.executors.ExecutionInfo;
import org.bukkit.command.CommandSender;
import org.opentest4j.AssertionFailedError;

import java.util.LinkedList;
import java.util.Queue;

public class ExecutionQueue {
	Queue<ExecutionInfo<CommandSender, MockCommandSource>> queue = new LinkedList<>();

	public void clear() {
		queue.clear();
	}

	public void add(ExecutionInfo<CommandSender, MockCommandSource> info) {
		queue.add(info);
	}

	public ExecutionInfo<CommandSender, MockCommandSource> poll() {
		return queue.poll();
	}

	public void assertNoMoreCommandsWereRun() {
		if (!queue.isEmpty()) {
			throw new AssertionFailedError("Expected no more commands to be run, but found " + queue.size() + " command(s) left");
		}
	}
}
