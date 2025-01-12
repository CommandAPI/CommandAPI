package dev.jorel.commandapi.examples.java;
/*******************************************************************************
 * Copyright 2018, 2021 Jorel Ali (Skepter) - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import dev.jorel.commandapi.*;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentException;
import dev.jorel.commandapi.arguments.CustomArgument.MessageBuilder;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.ExecutorType;
import dev.jorel.commandapi.wrappers.Rotation;
import dev.jorel.commandapi.wrappers.*;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.help.HelpTopic;
import org.bukkit.inventory.ComplexRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.EulerAngle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

public class Examples extends JavaPlugin {

/**
 * The list of all examples that are present in the CommandAPI's
 * documentation. The indentation SHOULD NOT be changed - any
 * indentation that appears in here will be reflected in the
 * documentation and that would look terrible!
 * 
 * To manage scope between each example, these should be encased
 * in curly braces {}.
 */

/**
 * For 9.0.0, the examples have been reordered. They now are in alphabetical order.
 */

/*************
 * Arguments *
 *************/

void advancementArgument() {
/* ANCHOR: advancementArgument1 */
new CommandAPICommand("award")
    .withArguments(new PlayerArgument("player"))
    .withArguments(new AdvancementArgument("advancement"))
    .executes((sender, args) -> {
        Player target = (Player) args.get("player");
        Advancement advancement = (Advancement) args.get("advancement");
        
        // Award all criteria for the advancement
        AdvancementProgress progress = target.getAdvancementProgress(advancement);
        for (String criteria : advancement.getCriteria()) {
            progress.awardCriteria(criteria);
        }
    })
    .register();
/* ANCHOR_END: advancementArgument1 */
}

void aliases() {
/* ANCHOR: aliases1 */
new CommandAPICommand("getpos")
    // Declare your aliases
    .withAliases("getposition", "getloc", "getlocation", "whereami")
      
    // Declare your implementation
    .executesEntity((entity, args) -> {
        entity.sendMessage(String.format("You are at %d, %d, %d", 
            entity.getLocation().getBlockX(), 
            entity.getLocation().getBlockY(), 
            entity.getLocation().getBlockZ())
        );
    })
    .executesCommandBlock((block, args) -> {
        block.sendMessage(String.format("You are at %d, %d, %d", 
            block.getBlock().getLocation().getBlockX(), 
            block.getBlock().getLocation().getBlockY(), 
            block.getBlock().getLocation().getBlockZ())
        );
    })
      
    // Register the command
    .register();
/* ANCHOR_END: aliases1 */
}

void argument_angle() {
    // NOTE: This example isn't used!
/* ANCHOR: argumentAngle1 */
new CommandAPICommand("yaw")
    .withArguments(new AngleArgument("amount"))
    .executesPlayer((player, args) -> {
        Location newLocation = player.getLocation();
        newLocation.setYaw((float) args.get("amount"));
        player.teleport(newLocation);
    })
    .register();
/* ANCHOR_END: argumentAngle2 */
}

void argument_asyncOfflinePlayer() {
/* ANCHOR: argumentAsyncOfflinePlayer1 */
new CommandAPICommand("playedbefore")
    .withArguments(new AsyncOfflinePlayerArgument("player"))
    .executes((sender, args) -> {
        CompletableFuture<OfflinePlayer> player = (CompletableFuture<OfflinePlayer>) args.get("player");

        // Directly sends a message to the sender, indicating that the command is running to prevent confusion
        sender.sendMessage("Checking if the player has played before...");

        player.thenAccept(offlinePlayer -> {
            if (offlinePlayer.hasPlayedBefore()) {
                sender.sendMessage("Player has played before");
            } else {
                sender.sendMessage("Player has never played before");
            }
        }).exceptionally(throwable -> {
            // We have to partly handle exceptions ourselves, since we are using a CompletableFuture
            Throwable cause = throwable.getCause();
            Throwable rootCause = cause instanceof RuntimeException ? cause.getCause() : cause;

            sender.sendMessage(Component.text(rootCause.getMessage(), NamedTextColor.RED));
            return null;
        });
    })
    .register();
/* ANCHOR_END: argumentAsyncOfflinePlayer1 */
}

void argument_biome() {
/* ANCHOR: argumentBiome1 */
new CommandAPICommand("setbiome")
    .withArguments(new BiomeArgument("biome"))
    .executesPlayer((player, args) -> {
        Biome biome = (Biome) args.get("biome");

        Chunk chunk = player.getLocation().getChunk();
        player.getWorld().setBiome(chunk.getX(), player.getLocation().getBlockY(), chunk.getZ(), biome);
    })
    .register();
/* ANCHOR_END: argumentBiome1 */
}

void argument_blockPredicate() {
/* ANCHOR: argumentBlockPredicate1 */
Argument<?>[] arguments = new Argument<?>[] {
    new IntegerArgument("radius"),
    new BlockPredicateArgument("fromBlock"),
    new BlockStateArgument("toBlock"),
};
/* ANCHOR_END: argumentBlockPredicate1 */

/* ANCHOR: argumentBlockPredicate2 */
new CommandAPICommand("replace")
    .withArguments(arguments)
    .executesPlayer((player, args) -> {
        
        // Parse the arguments
        int radius = (int) args.get("radius");
        @SuppressWarnings("unchecked")
        Predicate<Block> predicate = (Predicate<Block>) args.get("fromBlock");
        BlockData blockData = (BlockData) args.get("toBlock");
        
        // Find a (solid) sphere of blocks around the player with a given radius
        Location center = player.getLocation();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.sqrt(((x * x) + (y * y) + (z * z))) <= radius) {
                        Block block = center.getWorld().getBlockAt(x + center.getBlockX(), y + center.getBlockY(), z + center.getBlockZ());
                        
                        // If that block matches a block from the predicate, set it
                        if (predicate.test(block)) {
                            block.setType(blockData.getMaterial());
                            block.setBlockData(blockData);
                        }
                    }
                }
            }
        }
        return;
    })
    .register();
/* ANCHOR_END: argumentBlockPredicate2 */
}

void argument_blockState() {
/* ANCHOR: argumentBlockState1 */
new CommandAPICommand("set")
    .withArguments(new BlockStateArgument("block"))
    .executesPlayer((player, args) -> {
        BlockData blockdata = (BlockData) args.get("block");
        Block targetBlock = player.getTargetBlockExact(256);
        
        // Set the block, along with its data
        targetBlock.setType(blockdata.getMaterial());
        targetBlock.getState().setBlockData(blockdata);
    })
    .register();
/* ANCHOR_END: argumentBlockState1 */
}

void argument_chatAdventure() {
/* ANCHOR: argumentChatAdventure1 */
new CommandAPICommand("namecolor")
    .withArguments(new AdventureChatColorArgument("chatcolor"))
    .executesPlayer((player, args) -> {
        NamedTextColor color = (NamedTextColor) args.get("chatcolor");
        player.displayName(Component.text().color(color).append(Component.text(player.getName())).build());
    })
    .register();
/* ANCHOR_END: argumentChatAdventure1 */

/* ANCHOR: argumentChatAdventure2 */
new CommandAPICommand("showbook")
    .withArguments(new PlayerArgument("target"))
    .withArguments(new TextArgument("title"))
    .withArguments(new StringArgument("author"))
    .withArguments(new AdventureChatComponentArgument("contents"))
    .executes((sender, args) -> {
        Player target = (Player) args.get("target");
        String title = (String) args.get("title");
        String author = (String) args.get("author");
        Component content = (Component) args.get("contents");
        
        // Create a book and show it to the user (Requires Paper)
        Book mybook = Book.book(Component.text(title), Component.text(author), content);
        target.openBook(mybook);
    })
    .register();
/* ANCHOR_END: argumentChatAdventure2 */

/* ANCHOR: argumentChatAdventure3 */
new CommandAPICommand("pbroadcast")
    .withArguments(new AdventureChatArgument("message"))
    .executes((sender, args) -> {
        Component message = (Component) args.get("message");
        
        // Broadcast the message to everyone with broadcast permissions.
        Bukkit.getServer().broadcast(message, Server.BROADCAST_CHANNEL_USERS);
        Bukkit.getServer().broadcast(message);
    })
    .register();
/* ANCHOR_END: argumentChatAdventure3 */
}

void argument_chatSpigot() {
/* ANCHOR: argumentChatSpigot1 */
new CommandAPICommand("namecolor")
    .withArguments(new ChatColorArgument("chatcolor"))
    .executesPlayer((player, args) -> {
        ChatColor color = (ChatColor) args.get("chatcolor");
        player.setDisplayName(color + player.getName());
    })
    .register();
/* ANCHOR_END: argumentChatSpigot1 */

/* ANCHOR: argumentChatSpigot2 */
new CommandAPICommand("makebook")
    .withArguments(new PlayerArgument("player"))
    .withArguments(new ChatComponentArgument("contents"))
    .executes((sender, args) -> {
        Player player = (Player) args.get("player");
        BaseComponent[] arr = (BaseComponent[]) args.get("contents");
        
        // Create book
        ItemStack is = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) is.getItemMeta(); 
        meta.setTitle("Custom Book");
        meta.setAuthor(player.getName());
        meta.spigot().setPages(arr);
        is.setItemMeta(meta);
        
        // Give player the book
        player.getInventory().addItem(is);
    })
    .register();
/* ANCHOR_END: argumentChatSpigot2 */

/* ANCHOR: argumentChatSpigot3 */
new CommandAPICommand("pbroadcast")
    .withArguments(new ChatArgument("message"))
    .executes((sender, args) -> {
        BaseComponent[] message = (BaseComponent[]) args.get("message");
    
        // Broadcast the message to everyone on the server
        Bukkit.getServer().spigot().broadcast(message);
    })
    .register();
/* ANCHOR_END: argumentChatSpigot3 */
}

void argument_command() {
/* ANCHOR: argumentCommand1 */
new CommandAPICommand("sudo")
    .withArguments(new PlayerArgument("target"))
    .withArguments(new CommandArgument("command"))
    .executes((sender, args) -> {
        Player target = (Player) args.get("target");
        CommandResult command = (CommandResult) args.get("command");

        command.execute(target);
    })
    .register();
/* ANCHOR_END: argumentCommand1 */

/* ANCHOR: argumentCommand2 */
SuggestionsBranch.suggest(
    ArgumentSuggestions.strings("tp"),
    ArgumentSuggestions.strings(info -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new)),
    ArgumentSuggestions.strings(info -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new))
)
/* ANCHOR_END: argumentCommand2 */
;

/* ANCHOR: argumentCommand3 */
SuggestionsBranch.suggest(
    ArgumentSuggestions.strings("give"),
    ArgumentSuggestions.strings(info -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new))
).branch(
    SuggestionsBranch.suggest(
        ArgumentSuggestions.strings("diamond", "minecraft:diamond"),
        ArgumentSuggestions.empty()
    ),
    SuggestionsBranch.suggest(
        ArgumentSuggestions.strings("dirt", "minecraft:dirt"),
        null,
        ArgumentSuggestions.empty()
    )
)
/* ANCHOR_END: argumentCommand3 */
;

/* ANCHOR: argumentCommand4 */
new CommandArgument("command")
    .branchSuggestions(
        SuggestionsBranch.<CommandSender>suggest(
            ArgumentSuggestions.strings("give"),
            ArgumentSuggestions.strings(info -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new))
        ).branch(
            SuggestionsBranch.suggest(
                ArgumentSuggestions.strings("diamond", "minecraft:diamond"),
                ArgumentSuggestions.empty()
            ),
            SuggestionsBranch.suggest(
                ArgumentSuggestions.strings("dirt", "minecraft:dirt"),
                null,
                ArgumentSuggestions.empty()
            )
        ),
        SuggestionsBranch.suggest(
            ArgumentSuggestions.strings("tp"),
            ArgumentSuggestions.strings(info -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new)),
            ArgumentSuggestions.strings(info -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new))
        )
    );
/* ANCHOR_END: argumentCommand4 */
}

class argument_custom {
/* ANCHOR: argumentCustom1 */
// Function that returns our custom argument
public Argument<World> customWorldArgument(String nodeName) {

    // Construct our CustomArgument that takes in a String input and returns a World object
    return new CustomArgument<World, String>(new StringArgument(nodeName), info -> {
        // Parse the world from our input
        World world = Bukkit.getWorld(info.input());

        if (world == null) {
            throw CustomArgumentException.fromMessageBuilder(new MessageBuilder("Unknown world: ").appendArgInput());
        } else {
            return world;
        }
    }).replaceSuggestions(ArgumentSuggestions.strings(info ->
        // List of world names on the server
        Bukkit.getWorlds().stream().map(World::getName).toArray(String[]::new))
    );
}
/* ANCHOR_END: argumentCustom1 */

{
/* ANCHOR: argumentCustom2 */
new CommandAPICommand("tpworld")
    .withArguments(customWorldArgument("world"))
    .executesPlayer((player, args) -> {
        player.teleport(((World) args.get("world")).getSpawnLocation());
    })
    .register();
/* ANCHOR_END: argumentCustom2 */
}
}

void argument_enchantment() {
/* ANCHOR: argumentEnchantment1 */
new CommandAPICommand("enchantitem")
    .withArguments(new EnchantmentArgument("enchantment"))
    .withArguments(new IntegerArgument("level", 1, 5))
    .executesPlayer((player, args) -> {
        Enchantment enchantment = (Enchantment) args.get("enchantment");
        int level = (int) args.get("level");
        
        // Add the enchantment
        player.getInventory().getItemInMainHand().addEnchantment(enchantment, level);
    })
    .register();
/* ANCHOR_END: argumentEnchantment1 */
}

void argument_entities() {
/* ANCHOR: argumentEntities1 */
new CommandAPICommand("remove")
    // Using a collective entity selector to select multiple entities
    .withArguments(new EntitySelectorArgument.ManyEntities("entities"))
    .executes((sender, args) -> {
        // Parse the argument as a collection of entities (as stated above in the documentation)
        @SuppressWarnings("unchecked")
        Collection<Entity> entities = (Collection<Entity>) args.get("entities");
        
        sender.sendMessage("Removed " + entities.size() + " entities");
        for (Entity e : entities) {
            e.remove();
        }
    })
    .register();
/* ANCHOR_END: argumentEntities1 */

/* ANCHOR: argumentEntities2 */
Argument<?> noSelectorSuggestions = new PlayerArgument("target")
    .replaceSafeSuggestions(SafeSuggestions.suggest(info ->
        Bukkit.getOnlinePlayers().toArray(new Player[0])
    ));
/* ANCHOR_END: argumentEntities2 */

/* ANCHOR: argumentEntities3 */
new CommandAPICommand("warp")
    .withArguments(noSelectorSuggestions)
    .executesPlayer((player, args) -> {
        Player target = (Player) args.get("target");
        player.teleport(target);
    })
    .register();
/* ANCHOR_END: argumentEntities3 */

/* ANCHOR: argumentEntities4 */
new CommandAPICommand("spawnmob")
    .withArguments(new EntityTypeArgument("entity"))
    .withArguments(new IntegerArgument("amount", 1, 100)) // Prevent spawning too many entities
    .executesPlayer((Player player, CommandArguments args) -> {
        for (int i = 0; i < (int) args.get("amount"); i++) {
            player.getWorld().spawnEntity(player.getLocation(), (EntityType) args.get("entity"));
        }
    })
    .register();
/* ANCHOR_END: argumentEntities4 */
}

void argument_function() {
/* ANCHOR: argumentFunction1 */
new CommandAPICommand("runfunction")
    .withArguments(new FunctionArgument("function"))
    .executes((sender, args) -> {
        FunctionWrapper[] functions = (FunctionWrapper[]) args.get("function");

        // Run all functions in our FunctionWrapper[]
        for (FunctionWrapper function : functions) {
            function.run();
        }
    })
    .register();
/* ANCHOR_END: argumentFunction1 */
}

void argument_itemStack() {
/* ANCHOR: argumentItemStack1 */
new CommandAPICommand("item")
    .withArguments(new ItemStackArgument("itemStack"))
    .executesPlayer((player, args) -> {
        player.getInventory().addItem((ItemStack) args.get("itemStack"));
    })
    .register();
/* ANCHOR_END: argumentItemStack1 */
}

void argument_itemStackPredicate() {
/* ANCHOR: argumentItemStackPredicate1 */
// Register our command
new CommandAPICommand("rem")
    .withArguments(new ItemStackPredicateArgument("items"))
    .executesPlayer((player, args) -> {
        
        // Get our predicate
        @SuppressWarnings("unchecked")
        Predicate<ItemStack> predicate = (Predicate<ItemStack>) args.get("items");
        
        for (ItemStack item : player.getInventory()) {
            if (predicate.test(item)) {
                player.getInventory().remove(item);
            }
        }
    })
    .register();
/* ANCHOR_END: argumentItemStackPredicate1 */
}

void argument_list() {
/* ANCHOR: argumentList1 */
new CommandAPICommand("multigive")
    .withArguments(new IntegerArgument("amount", 1, 64))
    .withArguments(new ListArgumentBuilder<Material>("materials")
        .withList(List.of(Material.values()))
        .withMapper(material -> material.name().toLowerCase())
        .buildGreedy()
    )
    .executesPlayer((player, args) -> {
        int amount = (int) args.get("amount");
        List<Material> theList = (List<Material>) args.get("materials");
        
        for (Material item : theList) {
            player.getInventory().addItem(new ItemStack(item, amount));
        }
    })
    .register();
/* ANCHOR_END: argumentList1 */
}

void argument_literal() {
/* ANCHOR: argumentLiteral1 */
new CommandAPICommand("mycommand")
    .withArguments(new LiteralArgument("hello"))
    .withArguments(new TextArgument("text"))
    .executes((sender, args) -> {
        // This gives the variable "text" the contents of the TextArgument, and not the literal "hello"
        String text = (String) args.get(0);
    })
    .register();
/* ANCHOR_END: argumentLiteral1 */

/* ANCHOR: argumentLiteral2 */
new CommandAPICommand("mycommand")
    .withArguments(LiteralArgument.of("hello"))
    .withArguments(new TextArgument("text"))
    .executes((sender, args) -> {
        // This gives the variable "text" the contents of the TextArgument, and not the literal "hello"
        String text = (String) args.get(0);
    })
    .register();

new CommandAPICommand("mycommand")
    .withArguments(LiteralArgument.literal("hello"))
    .withArguments(new TextArgument("text"))
    .executes((sender, args) -> {
        // This gives the variable "text" the contents of the TextArgument, and not the literal "hello"
        String text = (String) args.get(0);
    })
    .register();
/* ANCHOR_END: argumentLiteral2 */

/* ANCHOR: argumentLiteral3 */
// Create a map of gamemode names to their respective objects
HashMap<String, GameMode> gamemodes = new HashMap<>();
gamemodes.put("adventure", GameMode.ADVENTURE);
gamemodes.put("creative", GameMode.CREATIVE);
gamemodes.put("spectator", GameMode.SPECTATOR);
gamemodes.put("survival", GameMode.SURVIVAL);

// Iterate over the map
for(Entry<String, GameMode> entry : gamemodes.entrySet()) {

    // Register the command as usual
    new CommandAPICommand("changegamemode")
        .withArguments(new LiteralArgument(entry.getKey()))
        .executesPlayer((player, args) -> {
            // Retrieve the object from the map via the key and NOT the args[]
            player.setGameMode(entry.getValue());
        })
        .register();
}
/* ANCHOR_END: argumentLiteral3 */
}

void argument_locations() {
/* ANCHOR: argumentLocations1 */
new LocationArgument("location", LocationType.PRECISE_POSITION, true);
/* ANCHOR_END: argumentLocations1 */

/* ANCHOR: argumentLocations2 */
new LocationArgument("location", LocationType.PRECISE_POSITION, false);
/* ANCHOR_END: argumentLocations2 */

/* ANCHOR: argumentLocations3 */
new CommandAPICommand("break")
    // We want to target blocks in particular, so use BLOCK_POSITION
    .withArguments(new LocationArgument("block", LocationType.BLOCK_POSITION))
    .executesPlayer((player, args) -> {
        Location location = (Location) args.get("block");
        location.getBlock().setType(Material.AIR);
    })
    .register();
/* ANCHOR_END: argumentLocations3 */
}

void argument_lootTable() {
/* ANCHOR: argumentLootTable1 */
new CommandAPICommand("giveloottable")
    .withArguments(new LootTableArgument("lootTable"))
    .withArguments(new LocationArgument("location", LocationType.BLOCK_POSITION))
    .executes((sender, args) -> {
        LootTable lootTable = (LootTable) args.get("lootTable");
        Location location = (Location) args.get("location");

        BlockState state = location.getBlock().getState();

        // Check if the input block is a container (e.g. chest)
        if (state instanceof Container container && state instanceof Lootable lootable) {
            // Apply the loot table to the chest
            lootable.setLootTable(lootTable);
            container.update();
        }
    })
    .register();
/* ANCHOR_END: argumentLootTable1 */
}

@SuppressWarnings({ "unchecked", "null" })
void argument_map() {
/* ANCHOR: argumentMap1 */
new CommandAPICommand("sendmessage")
    // Parameter 'delimiter' is missing, delimiter will be a colon
    // Parameter 'separator' is missing, separator will be a space
    .withArguments(new MapArgumentBuilder<Player, String>("message")

        // Providing a key mapper to convert a String into a Player
        .withKeyMapper(Bukkit::getPlayer)

        // Providing a value mapper to leave the message how it was sent
        .withValueMapper(s -> s)

        // Providing a list of player names to be used as keys
        .withKeyList(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList())

        // Don't provide a list of values so messages can be chosen without restrictions
        // Allow duplicates in case the same message should be sent to different players
        .withoutValueList(true)

        // Build the MapArgument
        .build()
    )
    .executesPlayer((player, args) -> {
        // The MapArgument returns a LinkedHashMap
        LinkedHashMap<Player, String> map = (LinkedHashMap<Player, String>) args.get("message");

        // Sending the messages to the players
        for (Entry<Player, String> messageRecipients : map.entrySet()) {
            messageRecipients.getKey().sendMessage(messageRecipients.getValue());
        }
    })
    .register();
/* ANCHOR_END: argumentMap1 */
}

@SuppressWarnings("null")
void argument_mathOperation() {
/* ANCHOR: argumentMathOperation1 */
new CommandAPICommand("changelevel")
    .withArguments(new PlayerArgument("player"))
    .withArguments(new MathOperationArgument("operation"))
    .withArguments(new IntegerArgument("value"))
    .executes((sender, args) -> {
        Player target = (Player) args.get("player");
        MathOperation op = (MathOperation) args.get("operation");
        int value = (int) args.get("value");

        target.setLevel(op.apply(target.getLevel(), value));
    })
    .register();
/* ANCHOR_END: argumentMathOperation1 */
}

@SuppressWarnings("null")
void argument_multiLiteral() {
/* ANCHOR: argumentMultiLiteral1 */
new CommandAPICommand("gamemode")
    .withArguments(new MultiLiteralArgument("gamemodes", "adventure", "creative", "spectator", "survival"))
    .executesPlayer((player, args) -> {
        // The literal string that the player enters IS available in the args[]
        switch ((String) args.get("gamemodes")) {
            case "adventure":
                player.setGameMode(GameMode.ADVENTURE);
                break;
            case "creative":
                player.setGameMode(GameMode.CREATIVE);
                break;
            case "spectator":
                player.setGameMode(GameMode.SPECTATOR);
                break;
            case "survival":
                player.setGameMode(GameMode.SURVIVAL);
                break;
            default:
                player.sendMessage("Invalid gamemode: " + args.get("gamemodes"));
                break;
        }
    }) 
    .register();
/* ANCHOR_END: argumentMultiLiteral1 */
}

class argument_nbt extends JavaPlugin {
/* ANCHOR: argumentNBT1 */
@Override
public void onLoad() {
    CommandAPI.onLoad(new CommandAPIBukkitConfig(this)
        .initializeNBTAPI(NBTContainer.class, NBTContainer::new)
    );
}
/* ANCHOR_END: argumentNBT1 */

void argument_nbt2() {
/* ANCHOR: argumentNBT2 */
new CommandAPICommand("award")
    .withArguments(new NBTCompoundArgument<NBTContainer>("nbt"))
    .executes((sender, args) -> {
        NBTContainer nbt = (NBTContainer) args.get("nbt");
        
        // Do something with "nbt" here...
    })
    .register();
/* ANCHOR_END: argumentNBT2 */
}
}

void argument_objectives() {
/* ANCHOR: argumentObjectives1 */
new CommandAPICommand("sidebar")
    .withArguments(new ObjectiveArgument("objective"))
    .executes((sender, args) -> {
        Objective objective = (Objective) args.get("objective");
        
        // Set display slot
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    })
    .register();
/* ANCHOR_END: argumentObjectives1 */

/* ANCHOR: argumentObjectives2 */
new CommandAPICommand("unregisterall")
    .withArguments(new ObjectiveCriteriaArgument("objective criteria"))
    .executes((sender, args) -> {
        String objectiveCriteria = (String) args.get("objective criteria");
        Set<Objective> objectives = Bukkit.getScoreboardManager().getMainScoreboard().getObjectivesByCriteria(objectiveCriteria);
        
        // Unregister the objectives
        for (Objective objective : objectives) {
            objective.unregister();
        }
    })
    .register();
/* ANCHOR_END: argumentObjectives2 */
}

@SuppressWarnings("null")
void argument_particle() {
/* ANCHOR: argumentParticle1 */
new CommandAPICommand("showparticle")
    .withArguments(new ParticleArgument("particle"))
    .executesPlayer((player, args) -> {
        ParticleData<?> particleData = (ParticleData<?>) args.get("particle");
        player.getWorld().spawnParticle(particleData.particle(), player.getLocation(), 1);
    })
    .register();
/* ANCHOR_END: argumentParticle1 */

/* ANCHOR: argumentParticle2 */
new CommandAPICommand("showparticle")
    .withArguments(new ParticleArgument("particle"))
    .executesPlayer((player, args) -> {
        ParticleData<?> particleData = (ParticleData<?>) args.get("particle");
        player.getWorld().spawnParticle(particleData.particle(), player.getLocation(), 1, particleData.data());
    })
    .register();
/* ANCHOR_END: argumentParticle2 */
}

@SuppressWarnings("null")
void argument_potion() {
/* ANCHOR: argumentPotion1 */
new CommandAPICommand("potion")
    .withArguments(new PlayerArgument("target"))
    .withArguments(new PotionEffectArgument("potion"))
    .withArguments(new TimeArgument("duration"))
    .withArguments(new IntegerArgument("strength"))
    .executes((sender, args) -> {
        Player target = (Player) args.get("target");
        PotionEffectType potion = (PotionEffectType) args.get("potion");
        int duration = (int) args.get("duration");
        int strength = (int) args.get("strength");
        
        // Add the potion effect to the target player
        target.addPotionEffect(new PotionEffect(potion, duration, strength));
    })
    .register();
/* ANCHOR_END: argumentPotion1 */
/* ANCHOR: argumentPotion2 */
new CommandAPICommand("potion")
    .withArguments(new PlayerArgument("target"))
    .withArguments(new PotionEffectArgument.NamespacedKey("potion"))
    .withArguments(new TimeArgument("duration"))
    .withArguments(new IntegerArgument("strength"))
    .executes((sender, args) -> {
        Player target = (Player) args.get("target");
        NamespacedKey potionKey = (NamespacedKey) args.get("potion");
        int duration = (int) args.get("duration");
        int strength = (int) args.get("strength");

        PotionEffectType potion = PotionEffectType.getByKey(potionKey);

        // Add the potion effect to the target player
        target.addPotionEffect(new PotionEffect(potion, duration, strength));
    })
    .register();
/* ANCHOR_END: argumentPotion2 */
}

@SuppressWarnings("null")
void argument_primitives() {
/* ANCHOR: argumentPrimitives1 */
// Load keys from config file
String[] configKeys = getConfig().getKeys(true).toArray(new String[0]);

// Register our command
new CommandAPICommand("editconfig")
    .withArguments(new TextArgument("config-key").replaceSuggestions(ArgumentSuggestions.strings(info -> configKeys)))
    .withArguments(new BooleanArgument("value"))
    .executes((sender, args) -> {
        // Update the config with the boolean argument
        getConfig().set((String) args.get("config-key"), (boolean) args.get("value"));
    })
    .register();
/* ANCHOR_END: argumentPrimitives1 */
}

void argument_range() {
/* ANCHOR: argumentRange1 */
new CommandAPICommand("searchrange")
    .withArguments(new IntegerRangeArgument("range")) // Range argument
    .withArguments(new ItemStackArgument("item"))     // The item to search for
    .executesPlayer((player, args) -> {
        // Retrieve the range from the arguments
        IntegerRange range = (IntegerRange) args.get("range");
        ItemStack itemStack = (ItemStack) args.get("item");

        // Store the locations of chests with certain items
        List<Location> locations = new ArrayList<>();

        // Iterate through all chunks, and then all tile entities within each chunk
        for (Chunk chunk : player.getWorld().getLoadedChunks()) {
            for (BlockState blockState : chunk.getTileEntities()) {

                // The distance between the block and the player
                int distance = (int) blockState.getLocation().distance(player.getLocation());

                // Check if the distance is within the specified range 
                if (range.isInRange(distance)) {

                    // Check if the tile entity is a chest
                    if (blockState instanceof Chest chest) {
                        
                        // Check if the chest contains the item specified by the player
                        if (chest.getInventory().contains(itemStack.getType())) {
                            locations.add(chest.getLocation());
                        }
                    }
                }

            }
        }

        // Output the locations of the chests, or whether no chests were found
        if (locations.isEmpty()) {
            player.sendMessage("No chests were found");
        } else {
            player.sendMessage("Found " + locations.size() + " chests:");
            locations.forEach(location -> {
                player.sendMessage("  Found at: " 
                        + location.getX() + ", " 
                        + location.getY() + ", " 
                        + location.getZ());
            });
        }
    })
    .register();
/* ANCHOR_END: argumentRange1 */
}

void argument_recipe() {
/* ANCHOR: argumentRecipe1 */
new CommandAPICommand("giverecipe")
    .withArguments(new RecipeArgument("recipe"))
    .executesPlayer((player, args) -> {
        ComplexRecipe recipe = (ComplexRecipe) args.get("recipe");
        player.getInventory().addItem(recipe.getResult());
    })
    .register();
/* ANCHOR_END: argumentRecipe1 */

/* ANCHOR: argumentRecipe2 */
new CommandAPICommand("unlockrecipe")
    .withArguments(new PlayerArgument("player"))
    .withArguments(new RecipeArgument("recipe"))
    .executes((sender, args) -> {
        Player target = (Player) args.get("player");
        ComplexRecipe recipe = (ComplexRecipe) args.get("recipe");

        target.discoverRecipe(recipe.getKey());
    })
    .register();
/* ANCHOR_END: argumentRecipe2 */
}

void argument_rotation() {
/* ANCHOR: argumentRotation1 */
new CommandAPICommand("rotate")
    .withArguments(new RotationArgument("rotation"))
    .withArguments(new EntitySelectorArgument.OneEntity("target"))
    .executes((sender, args) -> {
        Rotation rotation = (Rotation) args.get("rotation");
        Entity target = (Entity) args.get("target");

        if (target instanceof ArmorStand armorStand) {
            armorStand.setHeadPose(new EulerAngle(Math.toRadians(rotation.getPitch()), Math.toRadians(rotation.getYaw() - 90), 0));
        }
    })
    .register();
/* ANCHOR_END: argumentRotation1 */
}

void argument_scoreboards() {
/* ANCHOR: argumentScoreboards1 */
new CommandAPICommand("reward")
    // We want multiple players, so we use ScoreHolderType.MULTIPLE in the constructor
    .withArguments(new ScoreHolderArgument.Multiple("players"))
    .executes((sender, args) -> {
        // Get player names by casting to Collection<String>
        @SuppressWarnings("unchecked")
        Collection<String> players = (Collection<String>) args.get("players");
        
        for (String playerName : players) {
            Bukkit.getPlayer(playerName).getInventory().addItem(new ItemStack(Material.DIAMOND, 3));
        }
    })
    .register();
/* ANCHOR_END: argumentScoreboards1 */

/* ANCHOR: argumentScoreboards2 */
new CommandAPICommand("clearobjectives")
    .withArguments(new ScoreboardSlotArgument("slot"))
    .executes((sender, args) -> {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        DisplaySlot slot = ((ScoreboardSlot) args.get("slot")).getDisplaySlot();
        scoreboard.clearSlot(slot);
    })
    .register();
/* ANCHOR_END: argumentScoreboards2 */
}

void argument_sound() {
/* ANCHOR: argumentSound1 */
new CommandAPICommand("sound")
    .withArguments(new SoundArgument("sound"))
    .executesPlayer((player, args) -> {
        player.getWorld().playSound(player.getLocation(), (Sound) args.get("sound"), 100.0f, 1.0f);
    })
    .register();
/* ANCHOR_END: argumentSound1 */

/* ANCHOR: argumentSound2 */
new CommandAPICommand("sound")
    .withArguments(new SoundArgument.NamespacedKey("sound"))
    .executesPlayer((player, args) -> {
        player.getWorld().playSound(player.getLocation(), ((NamespacedKey) args.get("sound")).asString(), 100.0f, 1.0f);
    })
    .register();
/* ANCHOR_END: argumentSound2 */
}

void argument_strings() {
/* ANCHOR: argumentStrings1 */
new CommandAPICommand("message")
    .withArguments(new PlayerArgument("target"))
    .withArguments(new GreedyStringArgument("message"))
    .executes((sender, args) -> {
        ((Player) args.get("target")).sendMessage((String) args.get("message"));
    })
    .register();
/* ANCHOR_END: argumentStrings1 */
}

void argument_team() {
/* ANCHOR: argumentTeam1 */
new CommandAPICommand("togglepvp")
    .withArguments(new TeamArgument("team"))
    .executes((sender, args) -> {
        Team team = (Team) args.get("team");
        
        // Toggle pvp
        team.setAllowFriendlyFire(team.allowFriendlyFire());
    })
    .register();
/* ANCHOR_END: argumentTeam1 */
}

@SuppressWarnings("deprecation")
void argument_time() {
/* ANCHOR: argumentTime1 */
new CommandAPICommand("bigmsg")
    .withArguments(new TimeArgument("duration"))
    .withArguments(new GreedyStringArgument("message"))
    .executes((sender, args) -> {
        // Duration in ticks
        int duration = (int) args.get("duration");
        String message = (String) args.get("message");

        for (Player player : Bukkit.getOnlinePlayers()) {
            // Display the message to all players, with the default fade in/out times (10 and 20).
            player.sendTitle(message, "", 10, duration, 20);
        }
    })
    .register();
/* ANCHOR_END: argumentTime1 */
}

void argument_world() {
/* ANCHOR: argumentWorld1 */
new CommandAPICommand("unloadworld")
    .withArguments(new WorldArgument("world"))
    .executes((sender, args) -> {
        World world = (World) args.get("world");

        // Unload the world (and save the world's chunks)
        Bukkit.getServer().unloadWorld(world, true);
    })
    .register();
/* ANCHOR_END: argumentWorld1 */
}

void arguments() {
/* ANCHOR: arguments1 */
new CommandAPICommand("mycommand")
    .withArguments(new StringArgument("arg0"))
    .withArguments(new StringArgument("arg1"))
    .withArguments(new StringArgument("arg2"))
    // And so on
/* ANCHOR_END: arguments1 */
    ;

/* ANCHOR: arguments2 */
new CommandAPICommand("mycommand")
    .withArguments(new StringArgument("arg0"), new StringArgument("arg1"), new StringArgument("arg2"))
    // And so on
/* ANCHOR_END: arguments2 */
    ;

/* ANCHOR: arguments3 */
List<Argument<?>> arguments = new ArrayList<>();
arguments.add(new StringArgument("arg0"));
arguments.add(new StringArgument("arg1"));
arguments.add(new StringArgument("arg2"));

new CommandAPICommand("mycommand")
    .withArguments(arguments)
    // And so on
/* ANCHOR_END: arguments3 */
    ;

/* ANCHOR: arguments4 */
List<Argument<?>> commandArguments = new ArrayList<>();
commandArguments.add(new StringArgument("arg0"));
commandArguments.add(new PotionEffectArgument("arg1"));
commandArguments.add(new LocationArgument("arg2"));

new CommandAPICommand("cmd")
    .withArguments(commandArguments)
    .executes((sender, args) -> {
        String stringArg = (String) args.get("arg0");
        PotionEffectType potionArg = (PotionEffectType) args.get("arg1");
        Location locationArg = (Location) args.get("arg2");
    })
    .register();
/* ANCHOR_END: arguments4 */
}

void asyncSuggestions() {
JavaPlugin plugin = new JavaPlugin() {};
/* ANCHOR: asyncSuggestions1 */
new CommandAPICommand("setconfig")
    .withArguments(new StringArgument("key").replaceSuggestions(ArgumentSuggestions.stringsAsync(info -> {
        return CompletableFuture.supplyAsync(() -> {
            return plugin.getConfig().getKeys(false).toArray(new String[0]);
        });
    })))
    .withArguments(new TextArgument("value"))
    .executes((sender, args) -> {
        String key = (String) args.get("key");
        String value = (String) args.get("value");
        plugin.getConfig().set(key, value);
    })
    .register();
/* ANCHOR_END: asyncSuggestions1 */
}

@SuppressWarnings({ "rawtypes", "unchecked" })
void brigadier(){
/* ANCHOR: brigadier7 */
/* ANCHOR: brigadier1 */
// Register literal "randomchance"
LiteralCommandNode randomChance = Brigadier.fromLiteralArgument(new LiteralArgument("randomchance")).build();
/* ANCHOR_END: brigadier1 */

/* ANCHOR: brigadier2 */
// Declare arguments like normal
Argument<Integer> numeratorArgument = new IntegerArgument("numerator", 0);
Argument<Integer> denominatorArgument = new IntegerArgument("denominator", 1);

List<Argument> arguments = new ArrayList<>();
arguments.add(numeratorArgument);
arguments.add(denominatorArgument);
/* ANCHOR_END: brigadier2 */

// Get brigadier argument objects
/* ANCHOR: brigadier3 */
ArgumentBuilder numerator = Brigadier.fromArgument(numeratorArgument);
/* ANCHOR: brigadier4 */
ArgumentBuilder denominator = Brigadier.fromArgument(denominatorArgument)
/* ANCHOR_END: brigadier3 */
    // Fork redirecting to "execute" and state our predicate
    .fork(Brigadier.getRootNode().getChild("execute"), Brigadier.fromPredicate((sender, args) -> {
        // Parse arguments like normal
        int num = (int) args[0];
        int denom = (int) args[1];
        
        // Return boolean with a num/denom chance
        return Math.ceil(Math.random() * denom) <= num;
    }, arguments));
/* ANCHOR_END: brigadier4 */

/* ANCHOR: brigadier5 */
// Add <numerator> <denominator> as a child of randomchance
randomChance.addChild(numerator.then(denominator).build());
/* ANCHOR_END: brigadier5 */

/* ANCHOR: brigadier6 */
// Add (randomchance <numerator> <denominator>) as a child of (execute -> if)
Brigadier.getRootNode().getChild("execute").getChild("if").addChild(randomChance);
/* ANCHOR_END: brigadier6 */
/* ANCHOR_END: brigadier7 */
}

void brigadierSuggestions() {
/* ANCHOR: brigadierSuggestions1 */
Map<String, String> emojis = new HashMap<>();
emojis.put("☻", "smile");
emojis.put("❤", "heart");
emojis.put("🔥", "fire");
emojis.put("★", "star");
emojis.put("☠", "death");
emojis.put("⚠", "warning");
emojis.put("☀", "sun");
emojis.put("☺", "smile");
emojis.put("☹", "frown");
emojis.put("✉", "mail");
emojis.put("☂", "umbrella");
emojis.put("✘", "cross");
emojis.put("♪", "music note (eighth)");
emojis.put("♬", "music note (beamed sixteenth)");
emojis.put("♩", "music note (quarter)");
emojis.put("♫", "music note (beamed eighth)");
emojis.put("☄", "comet");
emojis.put("✦", "star");
emojis.put("🗡", "sword");
emojis.put("🪓", "axe");
emojis.put("🔱", "trident");
emojis.put("🎣", "fishing rod");
emojis.put("🏹", "bow");
emojis.put("⛏", "pickaxe");
emojis.put("🍖", "food");

Argument<String> messageArgument = new GreedyStringArgument("message")
    .replaceSuggestions((info, builder) -> {
        // Only display suggestions at the very end character
        builder = builder.createOffset(builder.getStart() + info.currentArg().length());

        // Suggest all the emojis!
        for (Entry<String, String> str : emojis.entrySet()) {
            builder.suggest(str.getKey(), new LiteralMessage(str.getValue()));
        }

        return builder.buildFuture();
    });

new CommandAPICommand("emoji")
    .withArguments(messageArgument)
    .executes((sender, args) -> {
        Bukkit.broadcastMessage((String) args.get("message"));
    })
    .register();
/* ANCHOR_END: brigadierSuggestions1 */

/* ANCHOR: brigadierSuggestions2 */
ArgumentSuggestions<CommandSender> commandSuggestions = (info, builder) -> {
    // The current argument, which is a full command
    String arg = info.currentArg();

    // Identify the position of the current argument
    int start;
    if (arg.contains(" ")) {
        // Current argument contains spaces - it starts after the last space and after the start of this argument.
        start = builder.getStart() + arg.lastIndexOf(' ') + 1;
    } else {
        // Input starts at the start of this argument
        start = builder.getStart();
    }
    
    // Parse command using brigadier
    ParseResults<?> parseResults = Brigadier.getCommandDispatcher()
        .parse(info.currentArg(), Brigadier.getBrigadierSourceFromCommandSender(info.sender()));
    
    // Intercept any parsing errors indicating an invalid command
    if(!parseResults.getExceptions().isEmpty()) {
        CommandSyntaxException exception = parseResults.getExceptions().values().iterator().next();
        // Raise the error, with the cursor offset to line up with the argument
        throw new CommandSyntaxException(exception.getType(), exception.getRawMessage(), exception.getInput(), exception.getCursor() + start);
    }

    return Brigadier
        .getCommandDispatcher()
        .getCompletionSuggestions(parseResults)
        .thenApply(suggestionsObject -> {
            // Brigadier's suggestions
            Suggestions suggestions = (Suggestions) suggestionsObject;

            return new Suggestions(
                // Offset the index range of the suggestions by the start of the current argument
                new StringRange(start, start + suggestions.getRange().getLength()),
                // Copy the suggestions
                suggestions.getList()
            );
        });
};
/* ANCHOR_END: brigadierSuggestions2 */

/* ANCHOR: brigadierSuggestions3 */
new CommandAPICommand("commandargument")
    .withArguments(new GreedyStringArgument("command").replaceSuggestions(commandSuggestions))
    .executes((sender, args) -> {
        // Run the command using Bukkit.dispatchCommand()
        Bukkit.dispatchCommand(sender, (String) args.get("command"));
    }).register();
/* ANCHOR_END: brigadierSuggestions3 */
}

void chatPreview() {
/* ANCHOR: chatPreview1 */
new CommandAPICommand("broadcast")
    .withArguments(new ChatArgument("message").withPreview(info -> {
        // Convert parsed BaseComponent[] to plain text
        String plainText = BaseComponent.toPlainText(info.parsedInput());

        // Translate the & in plain text and generate a new BaseComponent[]
        return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', plainText));
    }))
    .executesPlayer((player, args) -> {
        // The user still entered legacy text. We need to properly convert this
        // to a BaseComponent[] by converting to plain text then to BaseComponent[]
        String plainText = BaseComponent.toPlainText((BaseComponent[]) args.get("message"));
        Bukkit.spigot().broadcast(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', plainText)));
    })
    .register();
/* ANCHOR_END: chatPreview1 */

/* ANCHOR: chatPreview2 */
new CommandAPICommand("broadcast")
    .withArguments(new AdventureChatArgument("message").withPreview(info -> {
        // Convert parsed Component to plain text
        String plainText = PlainTextComponentSerializer.plainText().serialize(info.parsedInput());

        // Translate the & in plain text and generate a new Component
        return LegacyComponentSerializer.legacyAmpersand().deserialize(plainText);
    }))
    .executesPlayer((player, args) -> {
        // The user still entered legacy text. We need to properly convert this
        // to a Component by converting to plain text then to Component
        String plainText = PlainTextComponentSerializer.plainText().serialize((Component) args.get("broadcast"));
        Bukkit.broadcast(LegacyComponentSerializer.legacyAmpersand().deserialize(plainText));
    })
    .register();
/* ANCHOR_END: chatPreview2 */

/* ANCHOR: chatPreview3 */
new CommandAPICommand("broadcast")
    .withArguments(new ChatArgument("message").usePreview(true).withPreview(info -> {
        // Convert parsed BaseComponent[] to plain text
        String plainText = BaseComponent.toPlainText(info.parsedInput());

        // Translate the & in plain text and generate a new BaseComponent[]
        return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', plainText));
    }))
    .executesPlayer((player, args) -> {
        Bukkit.spigot().broadcast((BaseComponent[]) args.get("message"));
    })
    .register();
/* ANCHOR_END: chatPreview3 */

/* ANCHOR: chatPreview4 */
new CommandAPICommand("broadcast")
    .withArguments(new AdventureChatArgument("message").usePreview(true).withPreview(info -> {
        // Convert parsed Component to plain text
        String plainText = PlainTextComponentSerializer.plainText().serialize(info.parsedInput());

        // Translate the & in plain text and generate a new Component
        return LegacyComponentSerializer.legacyAmpersand().deserialize(plainText);
    }))
    .executesPlayer((player, args) -> {
        Bukkit.broadcast((Component) args.get("message"));
    })
    .register();
/* ANCHOR_END: chatPreview4 */
}

void commandArguments() {
/* ANCHOR: commandArguments1 */
new CommandAPICommand("mycommand")
    .withArguments(new StringArgument("name"))
    .withArguments(new IntegerArgument("amount"))
    .withOptionalArguments(new PlayerArgument("player"))
    .withOptionalArguments(new PlayerArgument("target"))
    .withOptionalArguments(new GreedyStringArgument("message"))
    .executesPlayer((player, args) -> {
        String name = (String) args.get(0); // Access arguments by index
        int amount = (int) args.get("amount"); // Access arguments by node name
        Player p = (Player) args.getOrDefault("player", player); // Access arguments using the getOrDefault(String, Object) method
        Player target = (Player) args.getOrDefault("target", () -> player); // Access arguments using the getOrDefault(String, Supplier<?>) method
        String message = (String) args.getOptional("message").orElse("Hello!"); // Access arguments using the getOptional(String) method

        // Do whatever with these values
    })
    .register();
/* ANCHOR_END: commandArguments1 */

/* ANCHOR: commandArguments2 */
new CommandAPICommand("mycommand")
    .withArguments(new EntitySelectorArgument.ManyEntities("entities"))
    .executesPlayer((player, args) -> {
        String entitySelector = args.getRaw("entities"); // Access the raw argument with getRaw(String)

        // Do whatever with the entity selector
    })
    .register();
/* ANCHOR_END: commandArguments2 */

/* ANCHOR: commandArguments3 */
new CommandAPICommand("mycommand")
    .withArguments(new PlayerArgument("player"))
    .executesPlayer((player, args) -> {
        Player p = args.getUnchecked("player");

        // Do whatever with the player
    })
    .register();
/* ANCHOR_END: commandArguments3 */

/* ANCHOR: commandArguments4 */
StringArgument nameArgument = new StringArgument("name");
IntegerArgument amountArgument = new IntegerArgument("amount");
PlayerArgument playerArgument = new PlayerArgument("player");
PlayerArgument targetArgument = new PlayerArgument("target");
GreedyStringArgument messageArgument = new GreedyStringArgument("message");

new CommandAPICommand("mycommand")
    .withArguments(nameArgument)
    .withArguments(amountArgument)
    .withOptionalArguments(playerArgument)
    .withOptionalArguments(targetArgument)
    .withOptionalArguments(messageArgument)
    .executesPlayer((player, args) -> {
        String name = args.getByArgument(nameArgument);
        int amount = args.getByArgument(amountArgument);
        Player p = args.getByArgumentOrDefault(playerArgument, player);
        Player target = args.getByArgumentOrDefault(targetArgument, player);
        String message = args.getOptionalByArgument(messageArgument).orElse("Hello!");

        // Do whatever with these values
    })
    .register();
/* ANCHOR_END: commandArguments4 */
}

void commandFailures() {
/* ANCHOR: commandFailures1 */
// Array of fruit
String[] fruit = new String[] {"banana", "apple", "orange"};

// Register the command
new CommandAPICommand("getfruit")
    .withArguments(new StringArgument("item").replaceSuggestions(ArgumentSuggestions.strings(fruit)))
    .executes((sender, args) -> {
        String inputFruit = (String) args.get("item");
        
        if (Arrays.stream(fruit).anyMatch(inputFruit::equals)) {
            // Do something with inputFruit
        } else {
            // The sender's input is not in the list of fruit
            throw CommandAPI.failWithString("That fruit doesn't exist!");
        }
    })
    .register();
/* ANCHOR_END: commandFailures1 */
}

void commandRegistration() {
/* ANCHOR: commandRegistration1 */
// Create our command
new CommandAPICommand("broadcastmsg")
    .withArguments(new GreedyStringArgument("message")) // The arguments
    .withAliases("broadcast", "broadcastmessage")       // Command aliases
    .withPermission(CommandPermission.OP)               // Required permissions
    .executes((sender, args) -> {
        String message = (String) args.get("message");
        Bukkit.getServer().broadcastMessage(message);
    })
    .register();
/* ANCHOR_END: commandRegistration1 */
}

class commandTrees extends JavaPlugin {
{
/* ANCHOR: commandTrees1 */
new CommandTree("sayhi")
    .executes((sender, args) -> {
        sender.sendMessage("Hi!");
    })
    .then(new PlayerArgument("target")
        .executes((sender, args) -> {
            Player target = (Player) args.get("target");
            target.sendMessage("Hi");
        }))
    .register();
/* ANCHOR_END: commandTrees1 */

/* ANCHOR: commandTrees2 */
new CommandTree("signedit")
    .then(new LiteralArgument("set")
        .then(new IntegerArgument("line_number", 1, 4)
            .then(new GreedyStringArgument("text")
                .executesPlayer((player, args) -> {
                    // /signedit set <line_number> <text>
                    Sign sign = getTargetSign(player);
                    int lineNumber = (int) args.get("line_number");
                    String text = (String) args.get("text");
                    sign.setLine(lineNumber - 1, text);
                    sign.update(true);
                 }))))
    .then(new LiteralArgument("clear")
        .then(new IntegerArgument("line_number", 1, 4)
            .executesPlayer((player, args) -> {
                // /signedit clear <line_number>
                Sign sign = getTargetSign(player);
                int lineNumber = (int) args.get("line_number");
                sign.setLine(lineNumber - 1, "");
                sign.update(true);
            })))
    .then(new LiteralArgument("copy")
        .then(new IntegerArgument("line_number", 1, 4)
            .executesPlayer((player, args) -> {
                // /signedit copy <line_number>
                Sign sign = getTargetSign(player);
                int lineNumber = (int) args.get("line_number");
                player.setMetadata("copied_sign_text", new FixedMetadataValue(this, sign.getLine(lineNumber - 1)));
            })))
    .then(new LiteralArgument("paste")
        .then(new IntegerArgument("line_number", 1, 4)
            .executesPlayer((player, args) -> {
                // /signedit copy <line_number>
                Sign sign = getTargetSign(player);
                int lineNumber = (int) args.get("line_number");
                sign.setLine(lineNumber - 1, player.getMetadata("copied_sign_text").get(0).asString());
                sign.update(true);
            })))
    .register();
/* ANCHOR_END: commandTrees2 */
}

public Sign getTargetSign(Player player) throws WrapperCommandSyntaxException {
    Block block = player.getTargetBlock(null, 256);
    if (block.getState() instanceof Sign sign) {
        return sign;
    } else {
        throw CommandAPI.failWithString("You're not looking at a sign!");
    }
}
}

class CommandUnregistration {
class UnregistrationBukkit extends JavaPlugin {
/* ANCHOR: commandUnregistration1 */
@Override
public void onLoad() {
    CommandAPIBukkit.unregister("version", true, true);
}
/* ANCHOR_END: commandUnregistration1 */
}

class UnregistrationVanilla extends JavaPlugin {
/* ANCHOR: commandUnregistration2 */
@Override
public void onEnable() {
    CommandAPI.unregister("gamemode");
}
/* ANCHOR_END: commandUnregistration2 */
}

class UnregistrationReplaceVanilla extends JavaPlugin {
/* ANCHOR: commandUnregistration3 */
@Override
public void onEnable() {
    CommandAPI.unregister("gamemode");

    // Register our new /gamemode, with survival, creative, adventure and spectator
    new CommandAPICommand("gamemode")
        .withArguments(new MultiLiteralArgument("gamemodes", "survival", "creative", "adventure", "spectator"))
        .executes((sender, args) -> {
            // Implementation of our /gamemode command
        })
        .register();
}
/* ANCHOR_END: commandUnregistration3 */
}

class UnregistrationPlugin extends JavaPlugin {
/* ANCHOR: commandUnregistration4 */
@Override
public void onEnable() {
    CommandAPIBukkit.unregister("luckperms:luckperms", false, true);
}
/* ANCHOR_END: commandUnregistration4 */
}

class UnregistrationCommandAPI extends JavaPlugin {
/* ANCHOR: commandUnregistration5 */
@Override
public void onEnable() {
    CommandAPI.unregister("break");
}
/* ANCHOR_END: commandUnregistration5 */
}

class UnregistrationBukkitHelp extends JavaPlugin {
/* ANCHOR: commandUnregistration6 */
@Override
public void onEnable() {
    new BukkitRunnable() {
        @Override
        public void run() {
            CommandAPIBukkit.unregister("help", false, true);
        }
    }.runTaskLater(this, 0);
}
/* ANCHOR_END: commandUnregistration6 */
}

class UnregistrationOnlyVanillaNamespace extends JavaPlugin {
/* ANCHOR: commandUnregistration7 */
@Override
public void onEnable() {
    new BukkitRunnable() {
        @Override
        public void run() {
            CommandAPI.unregister("minecraft:gamemode");
        }
    }.runTaskLater(this, 0);
}
/* ANCHOR_END: commandUnregistration7 */
}

class UnregistrationDelayedVanillaBad extends JavaPlugin {
/* ANCHOR: commandUnregistration8 */
// NOT RECOMMENDED
@Override
public void onEnable() {
    new BukkitRunnable() {
        @Override
        public void run() {
            CommandAPI.unregister("gamemode");
        }
    }.runTaskLater(this, 0);
}
/* ANCHOR_END: commandUnregistration8 */
}

class UnregistrationDelayedVanillaBetter extends JavaPlugin {
/* ANCHOR: commandUnregistration9 */
@Override
public void onEnable() {
    new BukkitRunnable() {
        @Override
        public void run() {
            CommandAPI.unregister("gamemode", true);
        }
    }.runTaskLater(this, 0);
}
/* ANCHOR_END: commandUnregistration9 */
}
}

class conversion {
/* ANCHOR: conversion1 */
class YourPlugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        Converter.convert((JavaPlugin) Bukkit.getPluginManager().getPlugin("TargetPlugin"));
        // Other code goes here...
    }
    
}
/* ANCHOR_END: conversion1 */

{
/* ANCHOR: conversion2 */
JavaPlugin essentials = (JavaPlugin) Bukkit.getPluginManager().getPlugin("Essentials");

// /speed <speed>
Converter.convert(essentials, "speed", new IntegerArgument("speed", 0, 10));

// /speed <target>
Converter.convert(essentials, "speed", new PlayerArgument("target"));

// /speed <walk/fly> <speed>
Converter.convert(essentials, "speed", 
    new MultiLiteralArgument("modes", "walk", "fly"),
    new IntegerArgument("speed", 0, 10)
);

// /speed <walk/fly> <speed> <target>
Converter.convert(essentials, "speed", 
    new MultiLiteralArgument("modes", "walk", "fly"),
    new IntegerArgument("speed", 0, 10), 
    new PlayerArgument("target")
);
/* ANCHOR_END: conversion2 */
}
}

class functions {
/* ANCHOR: functions1 */
class Main extends JavaPlugin {

    @Override
    public void onLoad() {
        // Commands which will be used in Minecraft functions are registered here

        new CommandAPICommand("killall")
            .executes((sender, args) -> {
                // Kills all enemies in all worlds
                Bukkit.getWorlds().forEach(w -> w.getLivingEntities().forEach(e -> e.setHealth(0)));
            })
            .register();
    }
    
    @Override
    public void onEnable() {
        // Register all other commands here
    } 
}
/* ANCHOR_END: functions1 */
}

void functionWrapper() {
/* ANCHOR: functionWrapper1 */
new CommandAPICommand("runfunc")
    .withArguments(new FunctionArgument("function"))
    .executes((sender, args) -> {
        FunctionWrapper[] functions = (FunctionWrapper[]) args.get("function");
        for (FunctionWrapper function : functions) {
            function.run(); // The command executor in this case is 'sender'
        }
    })
    .register();
/* ANCHOR_END: functionWrapper1 */
}

void help() {
/* ANCHOR: help1 */
new CommandAPICommand("mycmd")
    .withShortDescription("Says hi")
    .withFullDescription("Broadcasts hi to everyone on the server")
    .executes((sender, args) -> {
        Bukkit.broadcastMessage("Hi!");
    })
    .register();
/* ANCHOR_END: help1 */

/* ANCHOR: help2 */
new CommandAPICommand("mycmd")
    .withHelp("Says hi", "Broadcasts hi to everyone on the server")
    .executes((sender, args) -> {
        Bukkit.broadcastMessage("Hi!");
    })
    .register();
/* ANCHOR_END: help2 */
}

/* ANCHOR: help3 */
public HelpTopic makeHelp(String command) {
    return new HelpTopic() {

        @Override
        public String getShortText() {
            return "Says hi";
        }

        @Override
        public String getFullText(CommandSender forWho) {
            String helpText = "";
            if (forWho instanceof Player player) {
                // Make use of the player's locale to make language-specific help!
                Locale playerLocale = player.locale();
                if (playerLocale.getLanguage().equals("en")) {
                    helpText = "Broadcasts \"Hi!\" to everyone on the server";
                } else if (playerLocale.getLanguage().equals("de")) {
                    helpText = "Sendet \"Hi!\" an alle auf dem Server";
                }
            } else {
                helpText = "Broadcasts \"Hi!\" to everyone on the server";
            }
            return helpText;
        }

        // Allow anyone to see this help topic
        @Override
        public boolean canSee(CommandSender player) {
            return true;
        }
    };
}
/* ANCHOR_END: help3 */

void help2() {
/* ANCHOR: help4 */
new CommandAPICommand("mycmd")
    .withHelp(makeHelp("mycmd"))
    .executes((sender, args) -> {
        Bukkit.broadcastMessage("Hi!");
    })
    .register();
/* ANCHOR_END: help4 */
}

void listed() {
/* ANCHOR: listed1 */
new CommandAPICommand("mycommand")
    .withArguments(new PlayerArgument("player"))
    .withArguments(new IntegerArgument("value").setListed(false))
    .withArguments(new GreedyStringArgument("message"))
    .executes((sender, args) -> {
        // args == [player, message]
        Player player = (Player) args.get("player");
        String message = (String) args.get("message"); // Note that the IntegerArgument is not available in the CommandArguments
        player.sendMessage(message);
    })
    .register();
/* ANCHOR_END: listed1 */
}

void nativeSender() {
/* ANCHOR: native1 */
new CommandAPICommand("break")
    .executesNative((sender, args) -> {
        Location location = sender.getLocation();
        if (location != null) {
            location.getBlock().breakNaturally();
        }
    })
    .register();
/* ANCHOR_END: native1 */
}

void normalExecutors() {
/* ANCHOR: normalExecutors1 */
// Create our command
new CommandAPICommand("broadcastmsg")
    .withArguments(new GreedyStringArgument("message")) // The arguments
    .withAliases("broadcast", "broadcastmessage")       // Command aliases
    .withPermission(CommandPermission.OP)               // Required permissions
    .executes((sender, args) -> {
        String message = (String) args.get("message");
        Bukkit.getServer().broadcastMessage(message);
    })
    .register();
/* ANCHOR_END: normalExecutors1 */

/* ANCHOR: normalExecutors2 */
new CommandAPICommand("suicide")
    .executesPlayer((player, args) -> {
        player.setHealth(0);
    })
    .register();
/* ANCHOR_END: normalExecutors2 */

/* ANCHOR: normalExecutors3 */
new CommandAPICommand("suicide")
    .executesPlayer((player, args) -> {
        player.setHealth(0);
    })
    .executesEntity((entity, args) -> {
        entity.getWorld().createExplosion(entity.getLocation(), 4);
        entity.remove();
    })
    .register();
/* ANCHOR_END: normalExecutors3 */

/* ANCHOR: normalExecutors4 */
new CommandAPICommand("suicide")
    .executes((sender, args) -> {
        LivingEntity entity;
        if (sender instanceof ProxiedCommandSender proxy) {
            entity = (LivingEntity) proxy.getCallee();
        } else {
            entity = (LivingEntity) sender;
        }
        entity.setHealth(0);
    }, ExecutorType.PLAYER, ExecutorType.PROXY)
    .register();
/* ANCHOR_END: normalExecutors4 */
}

void optionalArguments() {
/* ANCHOR: optionalArguments1 */
new CommandAPICommand("sayhi")
    .withOptionalArguments(new PlayerArgument("target"))
    .executesPlayer((player, args) -> {
        Player target = (Player) args.get("target");
        if (target != null) {
            target.sendMessage("Hi!");
        } else {
            player.sendMessage("Hi!");
        }
    })
    .register();
/* ANCHOR_END: optionalArguments1 */

/* ANCHOR: optionalArguments2 */
new CommandAPICommand("sayhi")
    .withOptionalArguments(new PlayerArgument("target"))
    .executesPlayer((player, args) -> {
        Player target = (Player) args.getOptional("target").orElse(player);
        target.sendMessage("Hi!");
    })
    .register();
/* ANCHOR_END: optionalArguments2 */

/* ANCHOR: optionalArguments3 */
new CommandAPICommand("rate")
    .withOptionalArguments(new StringArgument("topic").combineWith(new IntegerArgument("rating", 0, 10)))
    .withOptionalArguments(new PlayerArgument("target"))
    .executes((sender, args) -> {
        String topic = (String) args.get("topic");
        if(topic == null) {
            sender.sendMessage(
                "Usage: /rate <topic> <rating> <player>(optional)",
                "Select a topic to rate, then give a rating between 0 and 10",
                "You can optionally add a player at the end to give the rating to"
            );
            return;
        }

        // We know this is not null because rating is required if topic is given
        int rating = (int) args.get("rating");

        // The target player is optional, so give it a default here
        CommandSender target = (CommandSender) args.getOptional("target").orElse(sender);

        target.sendMessage("Your " + topic + " was rated: " + rating + "/10");
    })
    .register();
/* ANCHOR_END: optionalArguments3 */
}

void permissions() {
/* ANCHOR: permissions1 */
// Register the /god command with the permission node "command.god"
new CommandAPICommand("god")
    .withPermission(CommandPermission.fromString("command.god"))
    .executesPlayer((player, args) -> {
        player.setInvulnerable(true);
        player.sendMessage("God mode enabled");
    })
    .register();
/* ANCHOR_END: permissions1 */

/* ANCHOR: permissions2 */
// Register the /god command with the permission node "command.god", without creating a CommandPermission
new CommandAPICommand("god")
    .withPermission("command.god")
    .executesPlayer((player, args) -> {
        player.setInvulnerable(true);
        player.sendMessage("God mode enabled");
    })
    .register();
/* ANCHOR_END: permissions2 */

/* ANCHOR: permissions3 */
// Register /kill command normally. Since no permissions are applied, anyone can run this command
new CommandAPICommand("kill")
    .executesPlayer((player, args) -> {
        player.setHealth(0);
    })
    .register();
/* ANCHOR_END: permissions3 */

/* ANCHOR: permissions4 */
// Adds the OP permission to the "target" argument. The sender requires OP to execute /kill <target>
new CommandAPICommand("kill")
    .withArguments(new PlayerArgument("target").withPermission(CommandPermission.OP))
    .executesPlayer((player, args) -> {
        ((Player) args.get("target")).setHealth(0);
    })
    .register();
/* ANCHOR_END: permissions4 */

/* ANCHOR: permissions5 */
// /economy - requires the permission "economy.self" to execute
new CommandAPICommand("economy")
    .withPermission("economy.self") // The important part of this example
    .executesPlayer((player, args) -> {
        // send the executor their own balance here.
    })
    .register();

// /economy <target> - requires the permission "economy.other" to execute
new CommandAPICommand("economy")
    .withPermission("economy.other") // The important part of this example
    .withArguments(new PlayerArgument("target"))
    .executesPlayer((player, args) -> {
        Player target = (Player) args.get("target");

        // Send a message to the executor with the target's balance
        player.sendMessage(target.getName() + "'s balance: " + Economy.getBalance(target));
    })
    .register();

// /economy give <target> <amount> - requires the permission "economy.admin.give" to execute
new CommandAPICommand("economy")
    .withPermission("economy.admin.give") // The important part of this example
    .withArguments(new PlayerArgument("target"))
    .withArguments(new DoubleArgument("amount"))
    .executesPlayer((player, args) -> {
        Player target = (Player) args.get("target");
        double amount = (Double) args.get("amount");

        // Update the target player's balance
        Economy.updateBalance(target, amount);
    })
    .register();

// /economy reset <target> - requires the permission "economy.admin.reset" to execute
new CommandAPICommand("economy")
    .withPermission("economy.admin.reset") // The important part of this example
    .withArguments(new PlayerArgument("target"))
    .executesPlayer((player, args) -> {
        Player target = (Player) args.get("target");

        // Reset target balance here
        Economy.resetBalance(target);
    })
    .register();
/* ANCHOR_END: permissions5 */
}

void predicateTips() {
Map<UUID, String> partyMembers = new HashMap<>();
/* ANCHOR: predicateTips1 */
List<Argument<?>> arguments = new ArrayList<>();

// The "create" literal, with a requirement that a player must have a party
arguments.add(new LiteralArgument("create")
    .withRequirement(sender -> !partyMembers.containsKey(((Player) sender).getUniqueId()))
);

arguments.add(new StringArgument("partyName"));
/* ANCHOR_END: predicateTips1 */

/* ANCHOR: predicateTips2 */
arguments = new ArrayList<>();
arguments.add(new LiteralArgument("tp")
    .withRequirement(sender -> partyMembers.containsKey(((Player) sender).getUniqueId()))
);
/* ANCHOR_END: predicateTips2 */

/* ANCHOR: predicateTips3 */
Predicate<CommandSender> testIfPlayerHasParty = sender -> {
    return partyMembers.containsKey(((Player) sender).getUniqueId());
};
/* ANCHOR_END: predicateTips3 */

/* ANCHOR: predicateTips4 */
List<Argument<?>> args = new ArrayList<>();
args.add(new LiteralArgument("create").withRequirement(testIfPlayerHasParty.negate()));
args.add(new StringArgument("partyName"));
/* ANCHOR_END: predicateTips4 */

/* ANCHOR: predicateTips5 */
args = new ArrayList<>();
args.add(new LiteralArgument("tp").withRequirement(testIfPlayerHasParty));
/* ANCHOR_END: predicateTips5 */
}

void proxySender() {
/* ANCHOR: proxySender1 */
new CommandAPICommand("killme")
    .executesPlayer((player, args) -> {
        player.setHealth(0);
    })
    .register();
/* ANCHOR_END: proxySender1 */

/* ANCHOR: proxySender2 */
new CommandAPICommand("killme")
    .executesPlayer((player, args) -> {
        player.setHealth(0);
    })
    .executesProxy((proxy, args) -> {
        // Check if the callee (target) is an Entity and kill it
        if (proxy.getCallee() instanceof LivingEntity target) {
            target.setHealth(0);
        }
    })
    .register();
/* ANCHOR_END: proxySender2 */
}

void requirements() {
/* ANCHOR: requirements1 */
new CommandAPICommand("repair")
    .withRequirement(sender -> ((Player) sender).getLevel() >= 30)
    .executesPlayer((player, args) -> {
        
        // Repair the item back to full durability
        ItemStack is = player.getInventory().getItemInMainHand();
        ItemMeta itemMeta = is.getItemMeta();
        if (itemMeta instanceof Damageable damageable) {
            damageable.setDamage(0);
            is.setItemMeta(itemMeta);
        }
        
        // Subtract 30 levels
        player.setLevel(player.getLevel() - 30);
    })
    .register();
/* ANCHOR_END: requirements1 */

/* ANCHOR: requirements2 */
Map<UUID, String> partyMembers = new HashMap<>();
/* ANCHOR_END: requirements2 */

/* ANCHOR: requirements3 */
List<Argument<?>> arguments = new ArrayList<>();

// The "create" literal, with a requirement that a player must have a party
arguments.add(new LiteralArgument("create")
    .withRequirement(sender -> !partyMembers.containsKey(((Player) sender).getUniqueId()))
);

arguments.add(new StringArgument("partyName"));
/* ANCHOR_END: requirements3 */

/* ANCHOR: requirements4 */
new CommandAPICommand("party")
    .withArguments(arguments)
    .executesPlayer((player, args) -> {
        
        // Get the name of the party to create
        String partyName = (String) args.get("partyName");
        
        partyMembers.put(player.getUniqueId(), partyName);
    })
    .register();
/* ANCHOR_END: requirements4 */

/* ANCHOR: requirements5 */
arguments = new ArrayList<>();
arguments.add(new LiteralArgument("tp")
    .withRequirement(sender -> partyMembers.containsKey(((Player) sender).getUniqueId()))
);

arguments.add(new PlayerArgument("player")
    .replaceSafeSuggestions(SafeSuggestions.suggest(info -> {
        
        // Store the list of party members to teleport to
        List<Player> playersToTeleportTo = new ArrayList<>();
        
        String partyName = partyMembers.get(((Player) info.sender()).getUniqueId());
        
        // Find the party members
        for (Entry<UUID, String> entry : partyMembers.entrySet()) {
            
            // Ignore yourself
            if (entry.getKey().equals(((Player) info.sender()).getUniqueId())) {
                continue;
            } else {
                // If the party member is in the same party as you
                if (entry.getValue().equals(partyName)) {
                    Player target = Bukkit.getPlayer(entry.getKey());
                    if (target.isOnline()) {
                        // Add them if they are online
                        playersToTeleportTo.add(target);
                    }
                }
            }
        }
        
        return playersToTeleportTo.toArray(new Player[0]);
    })));
/* ANCHOR_END: requirements5 */

/* ANCHOR: requirements6 */
new CommandAPICommand("party")
    .withArguments(arguments)
    .executesPlayer((player, args) -> {
        Player target = (Player) args.get("player");
        player.teleport(target);
    })
    .register();
/* ANCHOR_END: requirements6 */

/* ANCHOR: requirements7 */
new CommandAPICommand("party")
    .withArguments(arguments)
    .executesPlayer((player, args) -> {
        
        // Get the name of the party to create
        String partyName = (String) args.get("partyName");
        
        partyMembers.put(player.getUniqueId(), partyName);
        
        CommandAPI.updateRequirements(player);
    })
    .register();
/* ANCHOR_END: requirements7 */

/* ANCHOR: requirements8 */
new CommandAPICommand("someCommand")
    .withRequirement(sender -> ((Player) sender).getLevel() >= 30)
    .withRequirement(sender -> ((Player) sender).getInventory().contains(Material.DIAMOND_PICKAXE))
    .withRequirement(sender -> ((Player) sender).isInvulnerable())
    .executesPlayer((player, args) -> {
        // Code goes here
    })
    .register();
/* ANCHOR_END: requirements8 */
}

void resultingCommandExecutors() {
/* ANCHOR: resultingCommandExecutors1 */
new CommandAPICommand("randnum")
    .executes((sender, args) -> {
        return ThreadLocalRandom.current().nextInt();
    })
    .register();
/* ANCHOR_END: resultingCommandExecutors1 */

/* ANCHOR: resultingCommandExecutors2 */
// Register random number generator command from 1 to 99 (inclusive)
new CommandAPICommand("randomnumber")
    .executes((sender, args) -> {
        return ThreadLocalRandom.current().nextInt(1, 100); // Returns random number from 1 <= x < 100
    })
    .register();
/* ANCHOR_END: resultingCommandExecutors2 */

/* ANCHOR: resultingCommandExecutors3 */
// Register reward giving system for a target player
new CommandAPICommand("givereward")
    .withArguments(new EntitySelectorArgument.OnePlayer("target"))
    .executes((sender, args) -> {
        Player player = (Player) args.get("target");
        player.getInventory().addItem(new ItemStack(Material.DIAMOND, 64));
        Bukkit.broadcastMessage(player.getName() + " won a rare 64 diamonds from a loot box!");
    })
    .register();
/* ANCHOR_END: resultingCommandExecutors3 */
}

void safeArgumentSuggestions() {
/* ANCHOR: safeArgumentSuggestions1 */
// Create our itemstack
ItemStack emeraldSword = new ItemStack(Material.DIAMOND_SWORD);
ItemMeta meta = emeraldSword.getItemMeta();
meta.setDisplayName("Emerald Sword");
meta.setUnbreakable(true);
emeraldSword.setItemMeta(meta);

// Create and register our recipe
ShapedRecipe emeraldSwordRecipe = new ShapedRecipe(new NamespacedKey(this, "emerald_sword"), emeraldSword);
emeraldSwordRecipe.shape(
    "AEA", 
    "AEA", 
    "ABA"
);
emeraldSwordRecipe.setIngredient('A', Material.AIR);
emeraldSwordRecipe.setIngredient('E', Material.EMERALD);
emeraldSwordRecipe.setIngredient('B', Material.BLAZE_ROD);
getServer().addRecipe(emeraldSwordRecipe);

// Omitted, more itemstacks and recipes
/* ANCHOR_END: safeArgumentSuggestions1 */

/* ANCHOR: safeArgumentSuggestions2 */
// Safely override with the recipe we've defined
List<Argument<?>> arguments = new ArrayList<>();
arguments.add(new RecipeArgument("recipe").replaceSafeSuggestions(SafeSuggestions.suggest(info -> 
    new Recipe[] { emeraldSwordRecipe, /* Other recipes here */ }
)));

// Register our command
new CommandAPICommand("giverecipe")
    .withArguments(arguments)
    .executesPlayer((player, args) -> {
        Recipe recipe = (Recipe) args.get("recipe");
        player.getInventory().addItem(recipe.getResult());
    })
    .register();
/* ANCHOR_END: safeArgumentSuggestions2 */

/* ANCHOR: safeArgumentSuggestions3 */
EntityType[] forbiddenMobs = new EntityType[] {EntityType.ENDER_DRAGON, EntityType.WITHER};
List<EntityType> allowedMobs = new ArrayList<>(Arrays.asList(EntityType.values()));
allowedMobs.removeAll(Arrays.asList(forbiddenMobs)); // Now contains everything except enderdragon and wither
/* ANCHOR_END: safeArgumentSuggestions3 */

/* ANCHOR: safeArgumentSuggestions4 */
List<Argument<?>> safeArguments = new ArrayList<>();
safeArguments.add(new EntityTypeArgument("mob").replaceSafeSuggestions(SafeSuggestions.suggest(
    info -> {
        if (info.sender().isOp()) {
            // All entity types
            return EntityType.values();
        } else {
            // Only allowedMobs
            return allowedMobs.toArray(new EntityType[0]);
        }
    })
));
/* ANCHOR_END: safeArgumentSuggestions4 */

/* ANCHOR: safeArgumentSuggestions5 */
new CommandAPICommand("spawnmob")
    .withArguments(safeArguments)
    .executesPlayer((player, args) -> {
        EntityType entityType = (EntityType) args.get("mob");
        player.getWorld().spawnEntity(player.getLocation(), entityType);
    })
    .register();
/* ANCHOR_END: safeArgumentSuggestions5 */

/* ANCHOR: safeArgumentSuggestions6 */
List<Argument<?>> safeArgs = new ArrayList<>();
safeArgs.add(new EntitySelectorArgument.OnePlayer("target"));
safeArgs.add(new PotionEffectArgument("potioneffect").replaceSafeSuggestions(SafeSuggestions.suggest(
    info -> {
        Player target = (Player) info.previousArgs().get(0);
        
        // Convert PotionEffect[] into PotionEffectType[]
        return target.getActivePotionEffects().stream()
            .map(PotionEffect::getType)
            .toArray(PotionEffectType[]::new);
    })
));
/* ANCHOR_END: safeArgumentSuggestions6 */

/* ANCHOR: safeArgumentSuggestions7 */
new CommandAPICommand("removeeffect")
    .withArguments(safeArgs)
    .executesPlayer((player, args) -> {
        Player target = (Player) args.get("target");
        PotionEffectType potionEffect = (PotionEffectType) args.get("potioneffect");
        target.removePotionEffect(potionEffect);
    })
    .register();
/* ANCHOR_END: safeArgumentSuggestions7 */
}

class setupShading {
JavaPlugin plugin = new JavaPlugin() {};

{
/* ANCHOR: setupShading1 */
CommandAPI.onLoad(new CommandAPIBukkitConfig(plugin).silentLogs(true));
/* ANCHOR_END: setupShading1 */
}

/* ANCHOR: setupShading2 */
class MyPlugin extends JavaPlugin {

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(true)); // Load with verbose output
        
        new CommandAPICommand("ping")
            .executes((sender, args) -> {
                sender.sendMessage("pong!");
            })
            .register();
    }
    
    @Override
    public void onEnable() {
        CommandAPI.onEnable();
        
        // Register commands, listeners etc.
    }
    
    @Override
    public void onDisable() {
        CommandAPI.onDisable();
    }

}
/* ANCHOR_END: setupShading2 */
}

class stringArgumentSuggestions {
{
Map<String, Location> warps = new HashMap<>();
/* ANCHOR: stringArgumentSuggestions1 */
List<Argument<?>> arguments = new ArrayList<>();
arguments.add(new StringArgument("world").replaceSuggestions(ArgumentSuggestions.strings( 
    "northland", "eastland", "southland", "westland"
)));

new CommandAPICommand("warp")
    .withArguments(arguments)
    .executesPlayer((player, args) -> {
        String warp = (String) args.get("world");
        player.teleport(warps.get(warp)); // Look up the warp in a map, for example
    })
    .register();
/* ANCHOR_END: stringArgumentSuggestions1 */
}

/* ANCHOR: stringArgumentSuggestions2 */
class Friends {
    
    static Map<UUID, String[]> friendsMap = new HashMap<>();
    
    public static String[] getFriends(CommandSender sender) {
        if (sender instanceof Player player) {
            // Look up friends in a database or file
            return friendsMap.get(player.getUniqueId());
        } else {
            return new String[0];
        }
    }
}
/* ANCHOR_END: stringArgumentSuggestions2 */

{
/* ANCHOR: stringArgumentSuggestions3 */
List<Argument<?>> arguments = new ArrayList<>();
arguments.add(new PlayerArgument("friend").replaceSuggestions(ArgumentSuggestions.strings(info ->
    Friends.getFriends(info.sender())
)));

new CommandAPICommand("friendtp")
    .withArguments(arguments)
    .executesPlayer((player, args) -> {
        Player target = (Player) args.get("friend");
        player.teleport(target);
    })
    .register();
/* ANCHOR_END: stringArgumentSuggestions3 */

/* ANCHOR: stringArgumentSuggestions4 */
// Declare our arguments as normal
List<Argument<?>> commandArgs = new ArrayList<>();
commandArgs.add(new IntegerArgument("radius"));

// Replace the suggestions for the PlayerArgument.
// info.sender() refers to the command sender that is running this command
// info.previousArgs() refers to the Object[] of previously declared arguments (in this case, the IntegerArgument radius)
commandArgs.add(new PlayerArgument("target").replaceSuggestions(ArgumentSuggestions.strings(info -> {

    // Cast the first argument (radius, which is an IntegerArgument) to get its value
    int radius = (int) info.previousArgs().get("radius");
    
    // Get nearby entities within the provided radius
    Player player = (Player) info.sender();
    Collection<Entity> entities = player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius);
    
    // Get player names within that radius
    return entities.stream()
        .filter(e -> e.getType() == EntityType.PLAYER)
        .map(Entity::getName)
        .toArray(String[]::new);
})));
commandArgs.add(new GreedyStringArgument("message"));

// Declare our command as normal
new CommandAPICommand("localmsg")
    .withArguments(commandArgs)
    .executesPlayer((player, args) -> {
        Player target = (Player) args.get("target");
        String message = (String) args.get("message");
        target.sendMessage(message);
    })
    .register();
/* ANCHOR_END: stringArgumentSuggestions4 */
}
}

void subcommands() {
/* ANCHOR: subcommands1 */
CommandAPICommand groupAdd = new CommandAPICommand("add")
    .withArguments(new StringArgument("permission"))
    .withArguments(new StringArgument("groupName"))
    .executes((sender, args) -> {
        // perm group add code
    });
/* ANCHOR_END: subcommands1 */

/* ANCHOR: subcommands2 */
CommandAPICommand groupRemove = new CommandAPICommand("remove")
    .withArguments(new StringArgument("permission"))
    .withArguments(new StringArgument("groupName"))
    .executes((sender, args) -> {
        // perm group remove code
    });

CommandAPICommand group = new CommandAPICommand("group")
    .withSubcommand(groupAdd)
    .withSubcommand(groupRemove);
/* ANCHOR_END: subcommands2 */

/* ANCHOR: subcommands3 */
new CommandAPICommand("perm")
    .withSubcommand(group)
    .register();
/* ANCHOR_END: subcommands3 */

/* ANCHOR: subcommands4 */
new CommandAPICommand("perm")
    .withSubcommand(new CommandAPICommand("group")
        .withSubcommand(new CommandAPICommand("add")
            .withArguments(new StringArgument("permission"))
            .withArguments(new StringArgument("groupName"))
            .executes((sender, args) -> {
                // perm group add code
            })
        )
        .withSubcommand(new CommandAPICommand("remove")
            .withArguments(new StringArgument("permission"))
            .withArguments(new StringArgument("groupName"))
            .executes((sender, args) -> {
                // perm group remove code
            })
        )
    )
    .withSubcommand(new CommandAPICommand("user")
        .withSubcommand(new CommandAPICommand("add")
            .withArguments(new StringArgument("permission"))
            .withArguments(new StringArgument("userName"))
            .executes((sender, args) -> {
                // perm user add code
            })
        )
        .withSubcommand(new CommandAPICommand("remove")
            .withArguments(new StringArgument("permission"))
            .withArguments(new StringArgument("userName"))
            .executes((sender, args) -> {
                // perm user remove code
            })
        )
    )
    .register();
/* ANCHOR_END: subcommands4 */
}

class test {
class Main extends JavaPlugin {

}

class example1 {
/* ANCHOR: testLoadMockCommandAPI1 */
@BeforeEach
public void setUp() {
    // Set up MockBukkit server
    ServerMock server = MockBukkit.mock();

    // Load the CommandAPI plugin
    MockCommandAPIPlugin.load(config -> config
        .missingExecutorImplementationMessage("This command cannot be run by %S")
    );

    // Load our plugin
    MockBukkit.load(Main.class);
}

@AfterEach
public void tearDown() {
    // Reset for a clean slate next test
    MockBukkit.unmock();
}
/* ANCHOR_END: testLoadMockCommandAPI1 */
}

class example2 {
/* ANCHOR: testLoadMockCommandAPI2 */
public class CustomMockCommandAPIBukkit extends MockCommandAPIBukkit {
    // Implement a method that usually throws `UnimplementedMethodException`
    @Override
    public void reloadDataPacks() {
        CommandAPI.logInfo("Simulating data pack reload");
        // Further logic
    }
}

@BeforeEach
public void setUp() {
    // Set up MockBukkit server
    MockBukkit.mock();

    // Tell the CommandAPI to use your custom platform implementation
    CommandAPIVersionHandler.usePlatformImplementation(new CustomMockCommandAPIBukkit());

    // Load CommandAPI and your plugin as mentioned above...
}
/* ANCHOR_END: testLoadMockCommandAPI2 */
}
}

class tooltips {
{
/* ANCHOR: tooltips1 */
List<Argument<?>> arguments = new ArrayList<>();
arguments.add(new StringArgument("emote")
    .replaceSuggestions(ArgumentSuggestions.stringsWithTooltips(info ->
        new IStringTooltip[] {
            StringTooltip.ofString("wave", "Waves at a player"),
            StringTooltip.ofString("hug", "Gives a player a hug"),
            StringTooltip.ofString("glare", "Gives a player the death glare")
        }
    ))
);
arguments.add(new PlayerArgument("target"));
/* ANCHOR_END: tooltips1 */

/* ANCHOR: tooltips2 */
new CommandAPICommand("emote")
    .withArguments(arguments)
    .executesPlayer((player, args) -> {
        String emote = (String) args.get("emote");
        Player target = (Player) args.get("target");
        
        switch (emote) {
        case "wave":
            target.sendMessage(player.getName() + " waves at you!");
            break;
        case "hug":
            target.sendMessage(player.getName() + " hugs you!");
            break;
        case "glare":
            target.sendMessage(player.getName() + " gives you the death glare...");
            break;
        default:
            player.sendMessage("Invalid emote '" + emote + "'!");
            break;
        }
    })
    .register();
/* ANCHOR_END: tooltips2 */
}

/* ANCHOR: tooltips3 */
@SuppressWarnings("deprecation")
class CustomItem implements IStringTooltip {

    private ItemStack itemstack;
    private String name;
    
    public CustomItem(ItemStack itemstack, String name, String lore) {
        ItemMeta meta = itemstack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        itemstack.setItemMeta(meta);
        this.itemstack = itemstack;
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public ItemStack getItem() {
        return this.itemstack;
    }
    
    @Override
    public String getSuggestion() {
        return this.itemstack.getItemMeta().getDisplayName();
    }

    @Override
    public Message getTooltip() {
        return BukkitTooltip.messageFromString(this.itemstack.getItemMeta().getLore().get(0));
    }
    
}
/* ANCHOR_END: tooltips3 */

{
/* ANCHOR: tooltips4 */
CustomItem[] customItems = new CustomItem[] {
    new CustomItem(new ItemStack(Material.DIAMOND_SWORD), "God sword", "A sword from the heavens"),
    new CustomItem(new ItemStack(Material.PUMPKIN_PIE), "Sweet pie", "Just like grandma used to make")
};
    
new CommandAPICommand("giveitem")
    .withArguments(new StringArgument("item").replaceSuggestions(ArgumentSuggestions.stringsWithTooltips(customItems))) // We use customItems[] as the input for our suggestions with tooltips
    .executesPlayer((player, args) -> {
        String itemName = (String) args.get("item");
        
        // Give them the item
        for (CustomItem item : customItems) {
            if (item.getName().equals(itemName)) {
                player.getInventory().addItem(item.getItem());
                break;
            }
        }
    })
    .register();
/* ANCHOR_END: tooltips4 */

/* ANCHOR: tooltips5 */
List<Argument<?>> arguments = new ArrayList<>();
arguments.add(new LocationArgument("location")
    .replaceSafeSuggestions(SafeSuggestions.tooltips(info -> {
        // We know the sender is a player if we use .executesPlayer()
        Player player = (Player) info.sender();
        return BukkitTooltip.arrayOf(
            BukkitTooltip.ofString(player.getWorld().getSpawnLocation(), "World spawn"),
            BukkitTooltip.ofString(player.getBedSpawnLocation(), "Your bed"),
            BukkitTooltip.ofString(player.getTargetBlockExact(256).getLocation(), "Target block")
        );
    })));
/* ANCHOR_END: tooltips5 */

/* ANCHOR: tooltips6 */
new CommandAPICommand("warp")
    .withArguments(arguments)
    .executesPlayer((player, args) -> {
        player.teleport((Location) args.get("location"));
    })
    .register();
/* ANCHOR_END: tooltips6 */
}
}

/*****************
 * Anything else *
 *****************/
 
// TODO: Can we delete this? This isn't used!

void commandtree() {
new CommandTree("treeexample")
    // Set the aliases as you normally would 
    .withAliases("treealias")
    // Set an executor on the command itself
    .executes((sender, args) -> {
        sender.sendMessage("Root with no arguments");
    })
    // Create a new branch starting with a the literal 'integer'
    .then(new LiteralArgument("integer")
        // Execute on the literal itself
        .executes((sender, args) -> {
            sender.sendMessage("Integer Branch with no arguments");
        })
        // Create a further branch starting with an integer argument, which executes a command
        .then(new IntegerArgument("integer").executes((sender, args) -> {
            sender.sendMessage("Integer Branch with integer argument: " + args.get(0));
        })))
    .then(new LiteralArgument("biome")
        .executes((sender, args) -> {
            sender.sendMessage("Biome Branch with no arguments");
        })
        .then(new BiomeArgument("biome").executes((sender, args) -> {
            sender.sendMessage("Biome Branch with biome argument: " + args.get(0));
        })))
    .then(new LiteralArgument("string")
        .executes((sender, args) -> {
            sender.sendMessage("String Branch with no arguments");
        })
        .then(new StringArgument("string").executes((sender, args) -> {
            sender.sendMessage("String Branch with string argument: " + args.get(0));
        })))
    // Call register to finish as you normally would
    .register();
}

} // Examples class end // /// /// /// /// /// /// /// /// /// /// /// /// /// /// /// /// /// /// /// /// /// ///

class Economy {
    static void updateBalance(Player player, double amount) {
        throw new UnsupportedOperationException();
    }

    static String getBalance(Player player) {
        throw new UnsupportedOperationException();
    }

    static void resetBalance(Player target) {
        throw new UnsupportedOperationException();
    }
}