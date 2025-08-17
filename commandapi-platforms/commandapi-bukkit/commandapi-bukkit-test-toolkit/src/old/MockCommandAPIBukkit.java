package dev.jorel.commandapi;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.commandsenders.AbstractCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitCommandSender;
import dev.jorel.commandapi.spying.CommandAPIHandlerSpy;
import dev.jorel.commandapi.wrappers.Rotation;
import dev.jorel.commandapi.wrappers.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.loot.LootTable;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An implementation of {@link CommandAPIBukkit} that is compatible with a MockBukkit testing environment.
 * Does not rely on any version-specific Minecraft code to (ideally) support testing in any version.
 */
public class MockCommandAPIBukkit extends CommandAPIBukkit<MockCommandSource> {

	// Miscellaneous methods
	/**
	 * A global toggle for whether the default logger returned by {@link #getLogger()} should print messages to the
	 * console. This is {@code false} by default, so not messages will appear. If you don't provide your own logger
	 * using {@link CommandAPI#setLogger(CommandAPILogger)} and set this to {@code true} before calling
	 * {@link CommandAPI#onLoad(CommandAPIConfig)}, then the CommandAPI will write messages into the test log.
	 */
	public static boolean ENABLE_LOGGING = false;

	@Override
	public CommandAPILogger getLogger() {
		return ENABLE_LOGGING ?
			super.getLogger() :
			CommandAPILogger.bindToMethods(msg -> {}, msg -> {}, msg -> {}, (msg, ex) -> {});
	}
}
