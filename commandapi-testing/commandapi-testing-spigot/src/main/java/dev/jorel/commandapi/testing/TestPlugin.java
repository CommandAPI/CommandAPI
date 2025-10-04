package dev.jorel.commandapi.testing;

import com.mojang.brigadier.tree.CommandNode;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandAPISpigot;
import dev.jorel.commandapi.CommandAPISpigotConfig;
import dev.jorel.commandapi.arguments.AdvancementArgument;
import dev.jorel.commandapi.arguments.AngleArgument;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.AxisArgument;
import dev.jorel.commandapi.arguments.BiomeArgument;
import dev.jorel.commandapi.arguments.BlockPredicateArgument;
import dev.jorel.commandapi.arguments.BlockStateArgument;
import dev.jorel.commandapi.arguments.ChatArgument;
import dev.jorel.commandapi.arguments.ChatColorArgument;
import dev.jorel.commandapi.arguments.ChatComponentArgument;
import dev.jorel.commandapi.arguments.DoubleRangeArgument;
import dev.jorel.commandapi.arguments.EnchantmentArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.EntityTypeArgument;
import dev.jorel.commandapi.arguments.FunctionArgument;
import dev.jorel.commandapi.arguments.IntegerRangeArgument;
import dev.jorel.commandapi.arguments.ItemStackArgument;
import dev.jorel.commandapi.arguments.ItemStackPredicateArgument;
import dev.jorel.commandapi.arguments.Location2DArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.arguments.LootTableArgument;
import dev.jorel.commandapi.arguments.MathOperationArgument;
import dev.jorel.commandapi.arguments.NamespacedKeyArgument;
import dev.jorel.commandapi.arguments.ObjectiveArgument;
import dev.jorel.commandapi.arguments.ObjectiveCriteriaArgument;
import dev.jorel.commandapi.arguments.ParticleArgument;
import dev.jorel.commandapi.arguments.PlayerProfileArgument;
import dev.jorel.commandapi.arguments.PotionEffectArgument;
import dev.jorel.commandapi.arguments.RecipeArgument;
import dev.jorel.commandapi.arguments.RotationArgument;
import dev.jorel.commandapi.arguments.ScoreHolderArgument;
import dev.jorel.commandapi.arguments.ScoreboardSlotArgument;
import dev.jorel.commandapi.arguments.SoundArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.TeamArgument;
import dev.jorel.commandapi.arguments.TimeArgument;
import dev.jorel.commandapi.arguments.UUIDArgument;
import dev.jorel.commandapi.arguments.WorldArgument;
import dev.jorel.commandapi.wrappers.DoubleRange;
import dev.jorel.commandapi.wrappers.FunctionWrapper;
import dev.jorel.commandapi.wrappers.IntegerRange;
import dev.jorel.commandapi.wrappers.Location2D;
import dev.jorel.commandapi.wrappers.MathOperation;
import dev.jorel.commandapi.wrappers.ParticleData;
import dev.jorel.commandapi.wrappers.Rotation;
import dev.jorel.commandapi.wrappers.ScoreboardSlot;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.loot.LootTable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class TestPlugin extends JavaPlugin {

	@Override
	public void onLoad() {
		CommandAPI.onLoad(new CommandAPISpigotConfig(this).verboseOutput(true));

		register(new AdvancementArgument("advancementtype"), Advancement.class, advancement -> advancement.getKey().toString());
		register(new AngleArgument("angletype"), float.class, Object::toString);
		register(new AxisArgument("axistype"), EnumSet.class, Object::toString);
		register(new BiomeArgument("biome_biometype"), Biome.class, biome -> biome.getKey().toString());
		register(new BiomeArgument.NamespacedKey("biome_keytype"), NamespacedKey.class, Object::toString);
		register(new BlockPredicateArgument("blockpredicatetype"), Predicate.class, Object::toString);
		register(new BlockStateArgument("blockstatetype"), BlockState.class, Object::toString);
		register(new ChatArgument("chattype"), BaseComponent[].class, (message, sender) -> sender.spigot().sendMessage(message));
		register(new ChatColorArgument("chatcolortype"), ChatColor.class, color -> color + "Test");
		register(new ChatComponentArgument("chatcomponenttype"), Function.identity(), BaseComponent[].class);
		register(new EnchantmentArgument("enchantmenttype"), Enchantment.class, enchantment -> enchantment.getKey().toString());
		register(new EntitySelectorArgument.OnePlayer("oneplayertype"), Player.class, Player::getName);
		register(new EntitySelectorArgument.OneEntity("onenentitytype"), Entity.class, Entity::getName);
		register(new EntitySelectorArgument.ManyPlayers("manyplayerstype"), Collection.class, Object::toString);
		register(new EntitySelectorArgument.ManyEntities("manyentitiestype"), Collection.class, Object::toString);
		register(new EntityTypeArgument("entitytype"), EntityType.class, Object::toString);
		register(new DoubleRangeArgument("floatrangetype"), DoubleRange.class, range -> range.getLowerBound() + ".." + range.getUpperBound());
		register(new FunctionArgument("functionstype"), FunctionWrapper[].class, Arrays::toString);
		register(new IntegerRangeArgument("integerrangetype"), IntegerRange.class, range -> range.getLowerBound() + ".." + range.getUpperBound());
		register(new ItemStackArgument("itemstacktype"), ItemStack.class, stack -> stack.getType().getItemTranslationKey());
		register(new ItemStackPredicateArgument("itemstackpredicatetype"), Predicate.class, Object::toString);
		register(new Location2DArgument("location2dblocktype", LocationType.BLOCK_POSITION), Location2D.class, loc -> loc.getX() + ", " + loc.getZ());
		register(new Location2DArgument("location2dprecisetype", LocationType.PRECISE_POSITION, true), Location2D.class, loc -> loc.getX() + ", " + loc.getZ());
		register(new LocationArgument("locationblocktype", LocationType.BLOCK_POSITION), Location.class, loc -> loc.getX() + ", " + loc.getY() + ", " + loc.getZ());
		register(new LocationArgument("locationprecisetype", LocationType.PRECISE_POSITION, true), Location.class, loc -> loc.getX() + ", " + loc.getY() + ", " + loc.getZ());
		register(new LootTableArgument("loottabletype"), LootTable.class, loot -> loot.getKey().toString());
		register(new MathOperationArgument("mathoperationtype"), MathOperation.class, MathOperation::toString);
		register(new NamespacedKeyArgument("namespacedkeytype"), NamespacedKey.class, NamespacedKey::toString);
		register(new ObjectiveArgument("objectivetype"), Objective.class, Objective::getName);
		register(new ObjectiveCriteriaArgument("objectivecriteriatype"), String.class, Function.identity());
		register(new ParticleArgument("particletype"), ParticleData.class, data -> data.particle().toString());
		register(new PlayerProfileArgument("playertype"), List.class, Object::toString);
		register(new PotionEffectArgument("potioneffect_potioneffecttype"), PotionEffectType.class, PotionEffectType::toString);
		register(new PotionEffectArgument.NamespacedKey("potioneffect_keytype"), NamespacedKey.class, NamespacedKey::toString);
		register(new RecipeArgument("recipetype"), Recipe.class, recipe -> recipe.getResult().getType().getKey().toString());
		register(new RotationArgument("rotationtype"), Rotation.class, rotation -> rotation.getYaw() + ", " + rotation.getPitch());
		register(new ScoreboardSlotArgument("scoreboardslottype"), ScoreboardSlot.class, ScoreboardSlot::toString);
		register(new ScoreHolderArgument.Single("scoreholder_singletype"), String.class, Function.identity());
		register(new ScoreHolderArgument.Multiple("scoreholder_multipletype"), Collection.class, coll -> String.join(", ", coll));
		register(new SoundArgument("sound_soundtype"), Sound.class, s -> s.getKey().toString());
		register(new SoundArgument.NamespacedKey("sound_keytype"), NamespacedKey.class, NamespacedKey::toString);
		register(new TeamArgument("teamtype"), Team.class, Team::getName);
		register(new TimeArgument("timetype"), int.class, Object::toString);
		register(new UUIDArgument("uuidtype"), UUID.class, UUID::toString);
		register(new WorldArgument("worldtype"), World.class, World::getName);

		// Unregistration/Registration test commands
		registerExecutor(new StringArgument("register"), String.class, (name, sender) -> {
			new CommandAPICommand(name)
				.executes(info -> {
					info.sender().sendMessage("Command /" + name + " was registered and executed successfully!");
				})
				.register();
		});
		registerExecutor(new StringArgument("unregister")
				.replaceSuggestions(ArgumentSuggestions.strings(info -> CommandAPISpigot.getSpigot()
					.getBrigadierDispatcher()
					.getRoot()
					.getChildren()
					.stream()
					.map(CommandNode::getName)
					.filter(name -> !fixedCommandNames.contains(name))
					.toArray(String[]::new)
				)),
			String.class,
			(name, sender) -> {
				if (fixedCommandNames.contains(name)) {
					sender.sendMessage("§cSorry, the command /" + name + " cannot be unregistered!");
					return;
				}
				CommandAPI.unregister(name, true);
				sender.sendMessage("§aCommand " + name + " was unregistered successfully!");
			}
		);
	}

	@Override
	public void onEnable() {
		CommandAPI.onEnable();
	}

	private final List<String> fixedCommandNames = new ArrayList<>();

	private <T> void register(Argument<T> argument, Class<T> castType, Function<T, String> toString) {
		fixedCommandNames.add(argument.getNodeName());
		new CommandAPICommand(argument.getNodeName())
			.withArguments(argument)
			.executes(info -> {
				info.sender().sendMessage(toString.apply(info.args().getByClass(argument.getNodeName(), castType)));
			})
			.register();
	}

	private <T> void register(Argument<T> argument, Function<T, BaseComponent[]> toString, Class<T> castType) {
		fixedCommandNames.add(argument.getNodeName());
		new CommandAPICommand(argument.getNodeName())
			.withArguments(argument)
			.executes(info -> {
				info.sender().spigot().sendMessage(toString.apply(info.args().getByClass(argument.getNodeName(), castType)));
			})
			.register();
	}

	private <T> void register(Argument<T> argument, Class<T> castType, BiConsumer<T, CommandSender> action) {
		fixedCommandNames.add(argument.getNodeName());
		new CommandAPICommand(argument.getNodeName())
			.withArguments(argument)
			.executes(info -> {
				T argumentType = info.args().getByClass(argument.getNodeName(), castType);
				action.accept(argumentType, info.sender());
			})
			.register();
	}

	private <T> void registerExecutor(Argument<T> argument, Class<T> castType, BiConsumer<T, CommandSender> executor) {
		fixedCommandNames.add(argument.getNodeName());
		new CommandAPICommand(argument.getNodeName())
			.withArguments(argument)
			.executes(info -> {
				executor.accept(info.args().getByClass(argument.getNodeName(), castType), info.sender());
			})
			.register();
	}

}
