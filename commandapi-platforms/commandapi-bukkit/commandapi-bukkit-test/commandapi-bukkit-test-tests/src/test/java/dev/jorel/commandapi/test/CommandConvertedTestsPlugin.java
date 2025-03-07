package dev.jorel.commandapi.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

public class CommandConvertedTestsPlugin extends JavaPlugin {
	public static CommandConvertedTestsPlugin load() {
		return MockBukkit.loadWith(CommandConvertedTestsPlugin.class, CommandConvertedTestsPlugin.pluginYaml());
	}

	@Override
	public void onEnable() {
	}

	// Additional constructors required for MockBukkit
	public CommandConvertedTestsPlugin() {
		super();
	}

	public CommandConvertedTestsPlugin(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
		super(loader, description, dataFolder, file);
	}

	private final Mut<String[]> captureArgsResults = Mut.of();
	private int allDifferentRunCount = 0;

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return switch (label) {
			case "mycommand" -> {
				sender.sendMessage("hello");
				yield true;
			}
			case "captureargs" -> {
				captureArgsResults.set(args);
				yield true;
			}
			case "alldifferent" -> {
				allDifferentRunCount++;
				yield new HashSet<>(List.of(args)).size() == args.length;
			}
			default -> false;
		};
	}

	public Mut<String[]> getCaptureArgsResults() {
		return captureArgsResults;
	}

	public int resetAllDifferentRunCount() {
		int count = allDifferentRunCount;
		allDifferentRunCount = 0;
		return count;
	}

	public static InputStream pluginYaml() {
		return new ByteArrayInputStream("""
			name: MyPlugin
			main: dev.jorel.commandapi.test.CommandConvertedTestsPlugin
			version: 0.0.1
			description: A mock Bukkit plugin for CommandAPI testing
			author: Skepter
			website: https://www.jorel.dev/CommandAPI/
			api-version: 1.13
			commands:
			  mycommand:
			  captureargs:
			  alldifferent:
			""".getBytes());
	}
}