package dev.jorel.commandapi.examples.kotlin

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.Message
import com.mojang.brigadier.ParseResults
import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.tree.LiteralCommandNode
import de.tr7zw.changeme.nbtapi.NBTContainer
import dev.jorel.commandapi.*
import dev.jorel.commandapi.arguments.*
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentException
import dev.jorel.commandapi.arguments.CustomArgument.MessageBuilder
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException
import dev.jorel.commandapi.executors.*
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.playerArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.stringArgument
import dev.jorel.commandapi.wrappers.*
import dev.jorel.commandapi.wrappers.Rotation
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.*
import org.bukkit.advancement.Advancement
import org.bukkit.block.*
import org.bukkit.block.data.BlockData
import org.bukkit.command.CommandSender
import org.bukkit.command.ProxiedCommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.help.HelpTopic
import org.bukkit.inventory.ComplexRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.Damageable
import org.bukkit.loot.LootTable
import org.bukkit.loot.Lootable
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Team
import org.bukkit.util.EulerAngle
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Predicate
import java.util.function.Supplier
import kotlin.collections.LinkedHashMap
import kotlin.random.Random

class Examples : JavaPlugin() {

fun advancementArgument() {
/* ANCHOR: advancementArgument1 */
CommandAPICommand("award")
    .withArguments(PlayerArgument("player"))
    .withArguments(AdvancementArgument("advancement"))
    .executes(CommandExecutor { _, args ->
        val target = args["player"] as Player
        val advancement = args["advancement"] as Advancement

        // Award all criteria for the advancement
        val progress = target.getAdvancementProgress(advancement)
        for (criteria in advancement.criteria) {
            progress.awardCriteria(criteria)
        }
    })
    .register()
/* ANCHOR_END: advancementArgument1 */
}

fun aliases() {
/* ANCHOR: aliases1 */
CommandAPICommand("getpos")
    // Declare your aliases
    .withAliases("getposition", "getloc", "getlocation", "whereami")

    // Declare your implementation
    .executesEntity(EntityCommandExecutor { entity, _ ->
        val loc = entity.location
        entity.sendMessage("You are at ${loc.blockX}, ${loc.blockY}, ${loc.blockZ}")
    })
    .executesCommandBlock(CommandBlockCommandExecutor { block, _ ->
        val loc = block.block.location
        block.sendMessage("You are at ${loc.blockX}, ${loc.blockY}, ${loc.blockZ}")
    })

    // Register the command
    .register()
/* ANCHOR_END: aliases1 */
}

fun argument_angle() {
// TODO: This example isn't used!
/* ANCHOR: argumentAngle1 */
CommandAPICommand("yaw")
    .withArguments(AngleArgument("amount"))
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val newLocation = player.location
        newLocation.yaw = args["amount"] as Float
        player.teleport(newLocation)
    })
    .register()
/* ANCHOR_END: argumentAngle1 */
}

fun argument_asyncOfflinePlayer() {
/* ANCHOR: argumentAsyncOfflinePlayer1 */
CommandAPICommand("playedbefore")
    .withArguments(AsyncOfflinePlayerArgument("player"))
    .executes(CommandExecutor { sender, args ->
        val player = args["player"] as CompletableFuture<OfflinePlayer>

        // Directly sends a message to the sender, indicating that the command is running to prevent confusion
        sender.sendMessage("Checking if the player has played before...")

        player.thenAccept { offlinePlayer ->
            if (offlinePlayer.hasPlayedBefore()) {
                sender.sendMessage("Player has played before")
            } else {
                sender.sendMessage("Player has never played before")
            }
        }.exceptionally { throwable ->
            // We have to partly handle exceptions ourselves, since we are using a CompletableFuture
            val cause = throwable.cause
            val rootCause = if (cause is RuntimeException) cause.cause else cause

            sender.sendMessage(Component.text(rootCause?.message ?: "An error occurred", NamedTextColor.RED))
            null
        }
    })
    .register()
/* ANCHOR_END: argumentAsyncOfflinePlayer1 */
}

fun argument_biome() {
/* ANCHOR: argumentBiome1 */
CommandAPICommand("setbiome")
    .withArguments(BiomeArgument("biome"))
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val biome = args["biome"] as Biome

        val chunk = player.location.chunk
        player.world.setBiome(chunk.x, player.location.blockY, chunk.z, biome)
    })
    .register()
/* ANCHOR_END: argumentBiome1 */
}

fun argument_blockPredicate() {
/* ANCHOR: argumentBlockPredicate1 */
val arguments = arrayOf<Argument<*>>(
    IntegerArgument("radius"),
    BlockPredicateArgument("fromBlock"),
    BlockStateArgument("toBlock"),
)
/* ANCHOR_END: argumentBlockPredicate1 */

/* ANCHOR: argumentBlockPredicate2 */
CommandAPICommand("replace")
    .withArguments(*arguments)
    .executesPlayer(PlayerCommandExecutor { player, args ->

        // Parse the arguments
        val radius = args["radius"] as Int
        val predicate = args["fromBlock"] as Predicate<Block>
        val blockData = args["toBlock"] as BlockData

        // Find a (solid) sphere of blocks around the player with a given radius
        val center = player.location // for (i in 1 until 11) { }
        for (x in -radius until radius + 1) {
            for (y in -radius until radius + 1) {
                for (z in -radius until radius + 1) {
                    if (Math.sqrt((x * x + y * y + z * z).toDouble()) <= radius) {
                        val block = center.world.getBlockAt(x + center.blockX, y + center.blockY, z + center.blockZ)

                        // If that block matches a block from the predicate, set it
                        if (predicate.test(block)) {
                            block.type = blockData.material
                            block.blockData = blockData
                        }
                    }
                }
            }
        }
    })
    .register()
/* ANCHOR_END: argumentBlockPredicate2 */
}

fun argument_blockState() {
/* ANCHOR: argumentBlockState1 */
CommandAPICommand("set")
    .withArguments(BlockStateArgument("block"))
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val blockdata = args["block"] as BlockData
        val targetBlock = player.getTargetBlockExact(256)

        // Set the block, along with its data
        targetBlock?.type = blockdata.material
        targetBlock?.state?.blockData = blockdata
    })
    .register()
/* ANCHOR_END: argumentBlockState1 */
}

fun argument_chatAdventure() {
/* ANCHOR: argumentChatAdventure1 */
CommandAPICommand("namecolor")
    .withArguments(AdventureChatColorArgument("chatcolor"))
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val color = args["chatcolor"] as NamedTextColor
        player.displayName(Component.text().color(color).append(Component.text(player.name)).build())
    })
    .register()
/* ANCHOR_END: argumentChatAdventure1 */

/* ANCHOR: argumentChatAdventure2 */
CommandAPICommand("showbook")
    .withArguments(PlayerArgument("target"))
    .withArguments(TextArgument("title"))
    .withArguments(StringArgument("author"))
    .withArguments(AdventureChatComponentArgument("contents"))
    .executes(CommandExecutor { _, args ->
        val target = args["target"] as Player
        val title = args["title"] as String
        val author = args["author"] as String
        val content = args["contents"] as Component

        // Create a book and show it to the user (Requires Paper)
        val mybook = Book.book(Component.text(title), Component.text(author), content)
        target.openBook(mybook)
    })
    .register()
/* ANCHOR_END: argumentChatAdventure2 */

/* ANCHOR: argumentChatAdventure3 */
CommandAPICommand("pbroadcast")
    .withArguments(AdventureChatArgument("message"))
    .executes(CommandExecutor { _, args ->
        val message = args["message"] as Component

        // Broadcast the message to everyone with broadcast permissions.
        Bukkit.getServer().broadcast(message, Server.BROADCAST_CHANNEL_USERS)
        Bukkit.getServer().broadcast(message)
    })
    .register()
/* ANCHOR_END: argumentChatAdventure3 */
}

fun argument_chatSpigot() {
/* ANCHOR: argumentChatSpigot1 */
CommandAPICommand("namecolor")
    .withArguments(ChatColorArgument("chatColor"))
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val color = args["chatColor"] as ChatColor
        player.setDisplayName("$color${player.name}")
    })
    .register()
/* ANCHOR_END: argumentChatSpigot1 */

/* ANCHOR: argumentChatSpigot2 */
CommandAPICommand("makebook")
    .withArguments(PlayerArgument("player"))
    .withArguments(ChatComponentArgument("contents"))
    .executes(CommandExecutor { _, args ->
        val player = args["player"] as Player
        val arr = args["contents"] as Array<BaseComponent>

        // Create book
        val item = ItemStack(Material.WRITTEN_BOOK)
        val meta = item.itemMeta as BookMeta
        meta.title = "Custom Book"
        meta.author = player.name
        meta.spigot().setPages(arr)
        item.itemMeta = meta

        // Give player the book
        player.inventory.addItem(item)
    })
    .register()
/* ANCHOR_END: argumentChatSpigot2 */

/* ANCHOR: argumentChatSpigot3 */
CommandAPICommand("pbroadcast")
    .withArguments(ChatArgument("message"))
    .executes(CommandExecutor { _, args ->
        val message = args["message"] as Array<BaseComponent>

        // Broadcast the message to everyone on the server
        Bukkit.getServer().spigot().broadcast(*message)
    })
    .register()
/* ANCHOR_END: argumentChatSpigot3 */
}

fun argument_command() {
/* ANCHOR: argumentCommand1 */
CommandAPICommand("sudo")
    .withArguments(PlayerArgument("target"))
    .withArguments(CommandArgument("command"))
    .executes(CommandExecutor { _, args ->
        val target = args["target"] as Player
        val command = args["command"] as CommandResult

        command.execute(target)
    })
    .register()
/* ANCHOR_END: argumentCommand1 */

/* ANCHOR: argumentCommand2 */
SuggestionsBranch.suggest<CommandSender>(
    ArgumentSuggestions.strings("tp"),
    ArgumentSuggestions.strings { _ -> Bukkit.getOnlinePlayers().map{ it.name }.toTypedArray() },
    ArgumentSuggestions.strings { _ -> Bukkit.getOnlinePlayers().map{ it.name }.toTypedArray() }
)
/* ANCHOR_END: argumentCommand2 */

/* ANCHOR: argumentCommand3 */
SuggestionsBranch.suggest<CommandSender>(
    ArgumentSuggestions.strings("give"),
    ArgumentSuggestions.strings { _ -> Bukkit.getOnlinePlayers().map{ it.name }.toTypedArray() }
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

/* ANCHOR: argumentCommand4 */
CommandArgument("command")
    .branchSuggestions(
        SuggestionsBranch.suggest<CommandSender>(
            ArgumentSuggestions.strings("give"),
            ArgumentSuggestions.strings { _ -> Bukkit.getOnlinePlayers().map{ it.name }.toTypedArray() }
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
            ArgumentSuggestions.strings { _ -> Bukkit.getOnlinePlayers().map{ it.name }.toTypedArray() },
            ArgumentSuggestions.strings { _ -> Bukkit.getOnlinePlayers().map{ it.name }.toTypedArray() }
        )
    )
/* ANCHOR_END: argumentCommand4 */
}

class argument_custom {
/* ANCHOR: argumentCustom1 */
// Function that returns our custom argument
fun worldArgument(nodeName: String): Argument<World> {

    // Construct our CustomArgument that takes in a String input and returns a World object
    return CustomArgument<World, String>(StringArgument(nodeName)) { info ->
        // Parse the world from our input
        val world = Bukkit.getWorld(info.input())

        if (world == null) {
            throw CustomArgumentException.fromMessageBuilder(MessageBuilder("Unknown world: ").appendArgInput())
        } else {
            world
        }
    }.replaceSuggestions(ArgumentSuggestions.strings { _ ->
        // List of world names on the server
        Bukkit.getWorlds().map{ it.name }.toTypedArray()
    })
}
/* ANCHOR_END: argumentCustom1 */

fun argumentCustom2() {
/* ANCHOR: argumentCustom2 */
CommandAPICommand("tpworld")
    .withArguments(worldArgument("world"))
    .executesPlayer(PlayerCommandExecutor { player, args ->
        player.teleport((args["world"] as World).spawnLocation)
    })
    .register()
/* ANCHOR_END: argumentCustom2 */
}
}

fun argument_enchantment() {
/* ANCHOR: argumentEnchantment1 */
CommandAPICommand("enchantitem")
    .withArguments(EnchantmentArgument("enchantment"))
    .withArguments(IntegerArgument("level", 1, 5))
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val enchantment = args["enchantment"] as Enchantment
        val level = args["level"] as Int

        // Add the enchantment
        player.inventory.itemInMainHand.addEnchantment(enchantment, level)
    })
    .register()
/* ANCHOR_END: argumentEnchantment1 */
}

fun argument_entities() {
/* ANCHOR: argumentEntities1 */
CommandAPICommand("remove")
    // Using a collective entity selector to select multiple entities
    .withArguments(EntitySelectorArgument.ManyEntities("entities"))
    .executes(CommandExecutor { sender, args ->
        // Parse the argument as a collection of entities (as stated above in the documentation)
        val entities = args["entities"] as Collection<Entity>

        sender.sendMessage("Removed ${entities.size} entities")
        for (e in entities) {
            e.remove()
        }
    })
    .register()
/* ANCHOR_END: argumentEntities1 */

/* ANCHOR: argumentEntities2 */
val noSelectorSuggestions = PlayerArgument("target")
    .replaceSafeSuggestions(SafeSuggestions.suggest {
        Bukkit.getOnlinePlayers().toTypedArray()
    })
/* ANCHOR_END: argumentEntities2 */

/* ANCHOR: argumentEntities3 */
CommandAPICommand("warp")
    .withArguments(noSelectorSuggestions)
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val target = args["target"] as Player
        player.teleport(target)
    })
    .register()
/* ANCHOR_END: argumentEntities3 */

/* ANCHOR: argumentEntities4 */
CommandAPICommand("spawnmob")
    .withArguments(EntityTypeArgument("entity"))
    .withArguments(IntegerArgument("amount", 1, 100)) // Prevent spawning too many entities
    .executesPlayer(PlayerCommandExecutor { player, args ->
        for (i in 0 until args["amount"] as Int) {
            player.world.spawnEntity(player.location, args["entity"] as EntityType)
        }
    })
    .register()
/* ANCHOR_END: argumentEntities4 */
}

fun argument_function() {
/* ANCHOR: argumentFunction1 */
CommandAPICommand("runfunction")
    .withArguments(FunctionArgument("function"))
    .executes(CommandExecutor { _, args ->
        val functions = args["function"] as Array<FunctionWrapper>

        // Run all functions in our FunctionWrapper[]
        for (function in functions) {
            function.run()
        }
    })
    .register()
/* ANCHOR_END: argumentFunction1 */
}

fun argument_itemStack() {
/* ANCHOR: argumentItemStack1 */
CommandAPICommand("item")
    .withArguments(ItemStackArgument("itemStack"))
    .executesPlayer(PlayerCommandExecutor { player, args ->
        player.inventory.addItem(args["itemStack"] as ItemStack)
    })
    .register()
/* ANCHOR_END: argumentItemStack1 */
}

fun argument_itemStackPredicate() {
/* ANCHOR: argumentItemStackPredicate1 */
// Register our command
CommandAPICommand("rem")
    .withArguments(ItemStackPredicateArgument("items"))
    .executesPlayer(PlayerCommandExecutor { player, args ->

        // Get our predicate
        val predicate = args["items"] as Predicate<ItemStack>

        for (item in player.inventory) {
            if (predicate.test(item)) {
                player.inventory.remove(item)
            }
        }
    })
    .register()
/* ANCHOR_END: argumentItemStackPredicate1 */
}

fun argument_list() {
/* ANCHOR: argumentList1 */
CommandAPICommand("multigive")
    .withArguments(IntegerArgument("amount", 1, 64))
    .withArguments(ListArgumentBuilder<Material>("materials")
        .withList(Material.values().toList())
        .withMapper { material -> material.name.lowercase() }
        .buildGreedy()
    )
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val amount = args["amount"] as Int
        val theList = args["materials"] as List<Material>

        for (item in theList) {
            player.inventory.addItem(ItemStack(item, amount))
        }
    })
    .register()
/* ANCHOR_END: argumentList1 */
}

fun argument_literal() {
/* ANCHOR: argumentLiteral1 */
CommandAPICommand("mycommand")
    .withArguments(LiteralArgument("hello"))
    .withArguments(TextArgument("text"))
    .executes(CommandExecutor { _, args ->
        // This gives the variable "text" the contents of the TextArgument, and not the literal "hello"
        val text = args[0] as String
    })
    .register()
/* ANCHOR_END: argumentLiteral1 */

/* ANCHOR: argumentLiteral2 */
CommandAPICommand("mycommand")
    .withArguments(LiteralArgument.of("hello"))
    .withArguments(TextArgument("text"))
    .executes(CommandExecutor { _, args ->
        val text = args[0] as String
    })
    .register()

CommandAPICommand("mycommand")
    .withArguments(LiteralArgument.literal("hello"))
    .withArguments(TextArgument("text"))
    .executes(CommandExecutor { _, args ->
        val text = args[0] as String
    })
    .register()
/* ANCHOR_END: argumentLiteral2 */

/* ANCHOR: argumentLiteral3 */
// Create a map of gamemode names to their respective objects
val gamemodes = mapOf(
    "adventure" to GameMode.ADVENTURE,
    "creative" to GameMode.CREATIVE,
    "spectator" to GameMode.SPECTATOR,
    "survival" to GameMode.SURVIVAL
)

// Iterate over the map
for ((key, _) in gamemodes) {

    // Register the command as usual
    CommandAPICommand("changegamemode")
        .withArguments(LiteralArgument(key))
        .executesPlayer(PlayerCommandExecutor { player, _ ->
            // Retrieve the object from the map via the key and NOT the args[]
            player.gameMode = gamemodes[key]!!
        })
        .register()
}
/* ANCHOR_END: argumentLiteral3 */
}

fun argument_locations() {
/* ANCHOR: argumentLocations1 */
LocationArgument("location", LocationType.PRECISE_POSITION, true)
/* ANCHOR_END: argumentLocations1 */

/* ANCHOR: argumentLocations2 */
LocationArgument("location", LocationType.PRECISE_POSITION, false)
/* ANCHOR_END: argumentLocations2 */

/* ANCHOR: argumentLocations3 */
CommandAPICommand("break")
    // We want to target blocks in particular, so use BLOCK_POSITION
    .withArguments(LocationArgument("block", LocationType.BLOCK_POSITION))
    .executesPlayer(PlayerCommandExecutor { _, args ->
        (args["block"] as Location).block.type = Material.AIR
    })
    .register()
/* ANCHOR_END: argumentLocations3 */
}

fun argument_lootTable() {
/* ANCHOR: argumentLootTable1 */
CommandAPICommand("giveloottable")
    .withArguments(LootTableArgument("lootTable"))
    .withArguments(LocationArgument("location", LocationType.BLOCK_POSITION))
    .executes(CommandExecutor { _, args ->
        val lootTable = args["lootTable"] as LootTable
        val location = args["location"] as Location

        val state = location.block.state

        // Check if the input block is a container (e.g. chest)
        if (state is Container && state is Lootable) {
            // Apply the loot table to the chest
            state.lootTable = lootTable
            state.update()
        }
    })
    .register()
/* ANCHOR_END: argumentLootTable1 */
}

fun argument_map() {
/* ANCHOR: argumentMap1 */
CommandAPICommand("sendmessage")
    // Parameter 'delimiter' is missing, delimiter will be a colon
    // Parameter 'separator' is missing, separator will be a space
    .withArguments(MapArgumentBuilder<Player, String>("message")

        // Providing a key mapper to convert a String into a Player
        .withKeyMapper { s: String -> Bukkit.getPlayer(s) }

        // Providing a value mapper to leave the message how it was sent
        .withValueMapper { s: String -> s }

        // Providing a list of player names to be used as keys
        .withKeyList(Bukkit.getOnlinePlayers().map { player: Player -> player.name }.toList())

        // Don't provide a list of values so messages can be chosen without restrictions
        // Allow duplicates in case the same message should be sent to different players
        .withoutValueList(true)

        // Build the MapArgument
        .build()
    )
    .executesPlayer(PlayerCommandExecutor { _, args ->
        // The MapArgument returns a LinkedHashMap
        val map: LinkedHashMap<Player, String> = args["message"] as LinkedHashMap<Player, String>

        // Sending the messages to the players
        for (messageRecipient in map.keys) {
            messageRecipient.sendMessage(map[messageRecipient]!!)
        }
    })
    .register()
/* ANCHOR_END: argumentMap1 */
}

fun argument_mathOperation() {
/* ANCHOR: argumentMathOperation1 */
CommandAPICommand("changelevel")
    .withArguments(PlayerArgument("player"))
    .withArguments(MathOperationArgument("operation"))
    .withArguments(IntegerArgument("value"))
    .executes(CommandExecutor { _, args ->
        val target = args["player"] as Player
        val op = args["operation"] as MathOperation
        val value = args["value"] as Int

        target.level = op.apply(target.level, value)
    })
    .register()
/* ANCHOR_END: argumentMathOperation1 */
}

fun argument_multiLiteral() {
/* ANCHOR: argumentMultiLiteral1 */
CommandAPICommand("gamemode")
    .withArguments(MultiLiteralArgument("gamemodes", "adventure", "creative", "spectator", "survival"))
    .executesPlayer(PlayerCommandExecutor { player, args ->
        // The literal string that the player enters IS available in the args[]
        when (args["gamemodes"] as String) {
            "adventure" -> player.gameMode = GameMode.ADVENTURE
            "creative" -> player.gameMode = GameMode.CREATIVE
            "spectator" -> player.gameMode = GameMode.SPECTATOR
            "survival" -> player.gameMode = GameMode.SURVIVAL
        }
    })
    .register()
/* ANCHOR_END: argumentMultiLiteral1 */
}

class argument_nbt : JavaPlugin() {

/* ANCHOR: argumentNBT1 */
override fun onLoad() {
    CommandAPI.onLoad(CommandAPIBukkitConfig(this)
        .initializeNBTAPI(NBTContainer::class.java, ::NBTContainer)
    )
}
/* ANCHOR_END: argumentNBT1 */

fun argument_nbt2() {
/* ANCHOR: argumentNBT2 */
CommandAPICommand("award")
    .withArguments(NBTCompoundArgument<NBTContainer>("nbt"))
    .executes(CommandExecutor { _, args ->
        val nbt = args["nbt"] as NBTContainer

        // Do something with "nbt" here...
    })
    .register()
/* ANCHOR_END: argumentNBT2 */
}

}

fun argument_objectives() {
/* ANCHOR: argumentObjectives1 */
CommandAPICommand("sidebar")
    .withArguments(ObjectiveArgument("objective"))
    .executes(CommandExecutor { _, args ->
        val objective = args["objective"] as Objective

        // Set display slot
        objective.displaySlot = DisplaySlot.SIDEBAR
    })
    .register()
/* ANCHOR_END: argumentObjectives1 */

/* ANCHOR: argumentObjectives2 */
CommandAPICommand("unregisterall")
    .withArguments(ObjectiveCriteriaArgument("objective criteria"))
    .executes(CommandExecutor { _, args ->
        val objectiveCriteria = args["objective criteria"] as String
        val objectives = Bukkit.getScoreboardManager().mainScoreboard.getObjectivesByCriteria(objectiveCriteria)

        // Unregister the objectives
        for (objective in objectives) {
            objective.unregister()
        }
    })
    .register()
/* ANCHOR_END: argumentObjectives2 */
}

fun argument_particle() {
/* ANCHOR: argumentParticle1 */
CommandAPICommand("showparticle")
    .withArguments(ParticleArgument("particle"))
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val particleData = args["particle"] as ParticleData<Any>
        player.world.spawnParticle(particleData.particle(), player.location, 1)
    })
    .register()
/* ANCHOR_END: argumentParticle1 */

/* ANCHOR: argumentParticle2 */
CommandAPICommand("showparticle")
    .withArguments(ParticleArgument("particle"))
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val particleData = args["particle"] as ParticleData<Any>
        player.world.spawnParticle(particleData.particle(), player.location, 1, particleData.data())
    })
    .register()
/* ANCHOR_END: argumentParticle2 */
}

fun argument_potion() {
/* ANCHOR: argumentPotion1 */
CommandAPICommand("potion")
    .withArguments(PlayerArgument("target"))
    .withArguments(PotionEffectArgument("potion"))
    .withArguments(TimeArgument("duration"))
    .withArguments(IntegerArgument("strength"))
    .executes(CommandExecutor { _, args ->
        val target = args["target"] as Player
        val potion = args["potion"] as PotionEffectType
        val duration = args["duration"] as Int
        val strength = args["strength"] as Int

        // Add the potion effect to the target player
        target.addPotionEffect(PotionEffect(potion, duration, strength))
    })
    .register()
/* ANCHOR_END: argumentPotion1 */
/* ANCHOR: argumentPotion2 */
CommandAPICommand("potion")
    .withArguments(PlayerArgument("target"))
    .withArguments(PotionEffectArgument.NamespacedKey("potion"))
    .withArguments(TimeArgument("duration"))
    .withArguments(IntegerArgument("strength"))
    .executes(CommandExecutor { _, args ->
        val target = args["target"] as Player
        val potionKey = args["potion"] as NamespacedKey
        val duration = args["duration"] as Int
        val strength = args["strength"] as Int

        val potion = PotionEffectType.getByKey(potionKey)!!

        // Add the potion effect to the target player
        target.addPotionEffect(PotionEffect(potion, duration, strength))
    })
    .register()
/* ANCHOR_END: argumentPotion2 */
}

fun argument_primitives() {
/* ANCHOR: argumentPrimitives1 */
// Load keys from config file
val configKeys: Array<String> = config.getKeys(true).toTypedArray()

// Register our command
CommandAPICommand("editconfig")
    .withArguments(TextArgument("config-key").replaceSuggestions(ArgumentSuggestions.strings { _ -> configKeys }))
    .withArguments(BooleanArgument("value"))
    .executes(CommandExecutor { _, args ->
        // Update the config with the boolean argument
        config.set(args["config-key"] as String, args["value"] as Boolean)
    })
    .register()
/* ANCHOR_END: argumentPrimitives1 */
}

fun argument_range() {
/* ANCHOR: argumentRange1 */
CommandAPICommand("searchrange")
    .withArguments(IntegerRangeArgument("range")) // Range argument
    .withArguments(ItemStackArgument("item"))     // The item to search for
    .executesPlayer(PlayerCommandExecutor { player, args ->
        // Retrieve the range from the arguments
        val range = args["range"] as IntegerRange
        val itemStack = args["item"] as ItemStack

        // Store the locations of chests with certain items
        val locations = mutableListOf<Location>()

        // Iterate through all chunks, and then all tile entities within each chunk
        for (chunk in player.world.loadedChunks) {
            for (blockState in chunk.tileEntities) {

                // The distance between the block and the player
                val distance = blockState.location.distance(player.location).toInt()

                // Check if the distance is within the specified range
                if (range.isInRange(distance)) {

                    // Check if the tile entity is a chest
                    if (blockState is Chest) {

                        // Check if the chest contains the item specified by the player
                        if (blockState.inventory.contains(itemStack.type)) {
                            locations.add(blockState.location)
                        }
                    }
                }

            }
        }

        // Output the locations of the chests, or whether no chests were found
        if (locations.isEmpty()) {
            player.sendMessage("No chests were found")
        } else {
            player.sendMessage("Found ${locations.size} chests:")
            locations.forEach {
                player.sendMessage("  Found at: ${it.x}, ${it.y}, ${it.z}")
            }
        }
    })
    .register()
/* ANCHOR_END: argumentRange1 */
}

fun argument_recipe() {
/* ANCHOR: argumentRecipe1 */
CommandAPICommand("giverecipe")
    .withArguments(RecipeArgument("recipe"))
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val recipe = args["recipe"] as ComplexRecipe
        player.inventory.addItem(recipe.result)
    })
    .register()
/* ANCHOR_END: argumentRecipe1 */

/* ANCHOR: argumentRecipe2 */
CommandAPICommand("unlockrecipe")
    .withArguments(PlayerArgument("player"))
    .withArguments(RecipeArgument("recipe"))
    .executes(CommandExecutor { _, args ->
        val target = args["player"] as Player
        val recipe = args["recipe"] as ComplexRecipe

        target.discoverRecipe(recipe.key)
    })
    .register()
/* ANCHOR_END: argumentRecipe2 */
}

fun argument_rotation() {
/* ANCHOR: argumentRotation1 */
CommandAPICommand("rotate")
    .withArguments(RotationArgument("rotation"))
    .withArguments(EntitySelectorArgument.OneEntity("target"))
    .executes(CommandExecutor { _, args ->
        val rotation = args["rotation"] as Rotation
        val target = args["target"] as Entity

        if (target is ArmorStand) {
            target.headPose = EulerAngle(Math.toRadians(rotation.pitch.toDouble()), Math.toRadians(rotation.yaw.toDouble() - 90), 0.0)
        }
    })
    .register()
/* ANCHOR_END: argumentRotation1 */
}

fun argument_scoreboards() {
/* ANCHOR: argumentScoreboards1 */
CommandAPICommand("reward")
    // We want multiple players, so we use the ScoreHolderArgument.Multiple constructor
    .withArguments(ScoreHolderArgument.Multiple("players"))
    .executes(CommandExecutor { _, args ->
        // Get player names by casting to Collection<String>
        val players = args["players"] as Collection<String>

        for (playerName in players) {
            Bukkit.getPlayer(playerName)?.inventory!!.addItem(ItemStack(Material.DIAMOND, 3))
        }
    })
    .register()
/* ANCHOR_END: argumentScoreboards1 */

/* ANCHOR: argumentScoreboards2 */
CommandAPICommand("clearobjectives")
    .withArguments(ScoreboardSlotArgument("slot"))
    .executes(CommandExecutor { _, args ->
        val scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        val slot = (args["slot"] as ScoreboardSlot).displaySlot
        scoreboard.clearSlot(slot)
    })
    .register()
/* ANCHOR_END: argumentScoreboards2 */
}

fun argument_sound() {
/* ANCHOR: argumentSound1 */
CommandAPICommand("sound")
    .withArguments(SoundArgument("sound"))
    .executesPlayer(PlayerCommandExecutor { player, args ->
        player.world.playSound(player.location, args["sound"] as Sound, 100.0f, 1.0f)
    })
    .register()
/* ANCHOR_END: argumentSound1 */

/* ANCHOR: argumentSound2 */
CommandAPICommand("sound")
    .withArguments(SoundArgument.NamespacedKey("sound"))
    .executesPlayer(PlayerCommandExecutor { player, args ->
        player.world.playSound(player.location, (args["sound"] as NamespacedKey).asString(), 100.0f, 1.0f)
    })
    .register()
/* ANCHOR_END: argumentSound2 */
}

fun argument_strings() {
/* ANCHOR: argumentStrings1 */
CommandAPICommand("message")
    .withArguments(PlayerArgument("target"))
    .withArguments(GreedyStringArgument("message"))
    .executes(CommandExecutor { _, args ->
        (args["target"] as Player).sendMessage(args["message"] as String)
    })
    .register()
/* ANCHOR_END: argumentStrings1 */
}

fun argument_team() {
/* ANCHOR: argumentTeam1 */
CommandAPICommand("togglepvp")
    .withArguments(TeamArgument("team"))
    .executes(CommandExecutor { _, args ->
        val team = args["team"] as Team

        // Toggle pvp
        team.setAllowFriendlyFire(team.allowFriendlyFire())
    })
    .register()
/* ANCHOR_END: argumentTeam1 */
}

fun argument_time() {
/* ANCHOR: argumentTime1 */
CommandAPICommand("bigmsg")
    .withArguments(TimeArgument("duration"))
    .withArguments(GreedyStringArgument("message"))
    .executes(CommandExecutor { _, args ->
        // Duration in ticks
        val duration = args["duration"] as Int
        val message = args["message"] as String

        for (player in Bukkit.getOnlinePlayers()) {
            // Display the message to all players, with the default fade in/out times (10 and 20).
            player.sendTitle(message, "", 10, duration, 20)
        }
    })
    .register()
/* ANCHOR_END: argumentTime1 */
}

fun argument_world() {
/* ANCHOR: argumentWorld1 */
CommandAPICommand("unloadworld")
    .withArguments(WorldArgument("world"))
    .executes(CommandExecutor { sender, args ->
        val world = args["world"] as World

        // Unload the world (and save the world's chunks)
        Bukkit.getServer().unloadWorld(world, true)
    })
    .register()
/* ANCHOR_END: argumentWorld1 */
}

fun arguments() {
/* ANCHOR: arguments1 */
CommandAPICommand("mycommand")
    .withArguments(StringArgument("arg0"))
    .withArguments(StringArgument("arg1"))
    .withArguments(StringArgument("arg2"))
    // And so on
/* ANCHOR_END: arguments1 */


/* ANCHOR: arguments2 */
CommandAPICommand("mycommand")
    .withArguments(StringArgument("arg0"), StringArgument("arg1"), StringArgument("arg2"))
    // And so on
/* ANCHOR_END: arguments2 */


/* ANCHOR: arguments3 */
val arguments = listOf(
    StringArgument("arg0"),
    StringArgument("arg1"),
    StringArgument("arg2")
)

CommandAPICommand("mycommand")
    .withArguments(arguments)
    // And so on
/* ANCHOR_END: arguments3 */

/* ANCHOR: arguments4 */
val commandArguments = listOf(
    StringArgument("arg0"),
    PotionEffectArgument("arg1"),
    LocationArgument("arg2")
)

CommandAPICommand("cmd")
    .withArguments(commandArguments)
    .executes(CommandExecutor { _, args ->
        val stringArg = args["arg0"] as String
        val potionArg = args["arg1"] as PotionEffectType
        val locationArg = args["arg2"] as Location
    })
    .register()
/* ANCHOR_END: arguments4 */
}

fun asyncSuggestions() {
val plugin: JavaPlugin = object: JavaPlugin() {}
/* ANCHOR: asyncSuggestions1 */
CommandAPICommand("setconfig")
    .withArguments(StringArgument("key").replaceSuggestions(ArgumentSuggestions.stringsAsync { _ ->
        CompletableFuture.supplyAsync { plugin.config.getKeys(false).toTypedArray() }
    } ))
    .withArguments(TextArgument("value"))
    .executes(CommandExecutor { _, args ->
        val key = args["key"] as String
        val value = args["value"] as String
        plugin.config.set(key, value)
    })
    .register()
/* ANCHOR_END: asyncSuggestions1 */
}

fun brigadier() {
/* ANCHOR: brigadier7 */
/* ANCHOR: brigadier1 */
// Register literal "randomchance"
val randomChance: LiteralCommandNode<Any> = Brigadier.fromLiteralArgument(LiteralArgument("randomchance")).build()
/* ANCHOR_END: brigadier1 */

/* ANCHOR: brigadier2 */
// Declare arguments like normal
val numeratorArgument = IntegerArgument("numerator", 0)
val denominatorArgument = IntegerArgument("denominator", 1)

val arguments = listOf<Argument<*>>(numeratorArgument, denominatorArgument)
/* ANCHOR_END: brigadier2 */

// Get brigadier argument objects
/* ANCHOR: brigadier3 */
val numerator = Brigadier.fromArgument(numeratorArgument)
/* ANCHOR: brigadier4 */
val denominator = Brigadier.fromArgument(denominatorArgument)
/* ANCHOR_END: brigadier3 */
    // Fork redirecting to "execute" and state our predicate
    .fork(Brigadier.getRootNode().getChild("execute"), Brigadier.fromPredicate( { _: CommandSender, args ->
        // Parse arguments like normal
        val num = (args[0] as Int).toDouble()
        val denom = (args[1] as Int).toDouble()

        // Return boolean with a num/denom chance
        Math.ceil(Math.random() * denom) <= num
    }, arguments))
/* ANCHOR_END: brigadier4 */

/* ANCHOR: brigadier5 */
// Add <numerator> <denominator> as a child of randomchance
randomChance.addChild(numerator.then(denominator).build())
/* ANCHOR_END: brigadier5 */

/* ANCHOR: brigadier6 */
// Add (randomchance <numerator> <denominator>) as a child of (execute -> if)
Brigadier.getRootNode().getChild("execute").getChild("if").addChild(randomChance)
/* ANCHOR_END: brigadier6 */
/* ANCHOR_END: brigadier7 */
}

fun brigadierSuggestions() {
/* ANCHOR: brigadierSuggestions1 */
val emojis = mapOf(
    "☻" to "smile",
    "❤" to "heart",
    "🔥" to "fire",
    "★" to "star",
    "☠" to "death",
    "⚠" to "warning",
    "☀" to "sun",
    "☺" to "smile",
    "☹" to "frown",
    "✉" to "mail",
    "☂" to "umbrella",
    "✘" to "cross",
    "♪" to "music note (eighth)",
    "♬" to "music note (beamed sixteenth)",
    "♩" to "music note (quarter)",
    "♫" to "music note (beamed eighth)",
    "☄" to "comet",
    "✦" to "star",
    "🗡" to "sword",
    "🪓" to "axe",
    "🔱" to "trident",
    "🎣" to "fishing rod",
    "🏹" to "bow",
    "⛏" to "pickaxe",
    "🍖" to "food"
)

val messageArgument = GreedyStringArgument("message")
    .replaceSuggestions { info, builder ->
        // Only display suggestions at the very end character
        val newBuilder = builder.createOffset(builder.start + info.currentArg().length)

        // Suggest all the emojis!
        emojis.forEach { (emoji, description) ->
            newBuilder.suggest(emoji, LiteralMessage(description))
        }

        newBuilder.buildFuture()
    }

CommandAPICommand("emoji")
    .withArguments(messageArgument)
    .executes(CommandExecutor { _, args ->
        Bukkit.broadcastMessage(args["message"] as String)
    })
    .register()
/* ANCHOR_END: brigadierSuggestions1 */

/* ANCHOR: brigadierSuggestions2 */
val commandSuggestions: ArgumentSuggestions<CommandSender> = ArgumentSuggestions { info, builder ->
    // The current argument, which is a full command
    val arg: String = info.currentArg()

    // Identify the position of the current argument
    var start = if (arg.contains(" ")) {
        // Current argument contains spaces - it starts after the last space and after the start of this argument.
        builder.start + arg.lastIndexOf(' ') + 1
    } else {
        // Input starts at the start of this argument
        builder.start
    }

    // Parse command using brigadier
    val parseResults: ParseResults<*> = Brigadier.getCommandDispatcher()
        .parse(info.currentArg(), Brigadier.getBrigadierSourceFromCommandSender(info.sender))

    // Intercept any parsing errors indicating an invalid command
    for ((_, exception) in parseResults.exceptions) {
        // Raise the error, with the cursor offset to line up with the argument
        throw CommandSyntaxException(exception.type, exception.rawMessage, exception.input, exception.cursor + start)
    }

    val completableFutureSuggestions: CompletableFuture<Suggestions> =
        Brigadier.getCommandDispatcher().getCompletionSuggestions(parseResults) as CompletableFuture<Suggestions>

    completableFutureSuggestions.thenApply { suggestions: Suggestions ->
        Suggestions(
            // Offset the index range of the suggestions by the start of the current argument
            StringRange(start, start + suggestions.range.length),
            // Copy the suggestions
            suggestions.list
        )
    }
}
/* ANCHOR_END: brigadierSuggestions2 */

/* ANCHOR: brigadierSuggestions3 */
CommandAPICommand("commandargument")
    .withArguments(GreedyStringArgument("command").replaceSuggestions(commandSuggestions))
    .executes(CommandExecutor { sender, args ->
        // Run the command using Bukkit.dispatchCommand()
        Bukkit.dispatchCommand(sender, args["command"] as String)
    })
    .register()
/* ANCHOR_END: brigadierSuggestions3 */
}

fun chatPreview() {
/* ANCHOR: chatPreview1 */
CommandAPICommand("broadcast")
    .withArguments(ChatArgument("message").withPreview { info ->
        // Convert parsed BaseComponent[] to plain text
        val plainText: String = BaseComponent.toPlainText(*info.parsedInput() as Array<BaseComponent>)

        // Translate the & in plain text and generate a new BaseComponent[]
        TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', plainText))
    } )
    .executesPlayer(PlayerCommandExecutor { _, args ->
        // The user still entered legacy text. We need to properly convert this
        // to a BaseComponent[] by converting to plain text then to BaseComponent[]
        val plainText: String = BaseComponent.toPlainText(*args["message"] as Array<BaseComponent>)
        val baseComponents: Array<BaseComponent> = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', plainText))
        Bukkit.spigot().broadcast(*baseComponents)
    })
    .register()
/* ANCHOR_END: chatPreview1 */

/* ANCHOR: chatPreview2 */
CommandAPICommand("broadcast")
    .withArguments(AdventureChatArgument("message").withPreview { info ->
        // Convert parsed Component to plain text
        val plainText: String = PlainTextComponentSerializer.plainText().serialize(info.parsedInput() as Component)

        // Translate the & in plain text and generate a new Component
        LegacyComponentSerializer.legacyAmpersand().deserialize(plainText)
    } )
    .executesPlayer(PlayerCommandExecutor { _, args ->
        // The user still entered legacy text. We need to properly convert this
        // to a Component by converting to plain text then to Component
        val plainText: String = PlainTextComponentSerializer.plainText().serialize(args["message"] as Component)
        Bukkit.broadcast(LegacyComponentSerializer.legacyAmpersand().deserialize(plainText))
    })
    .register()
/* ANCHOR_END: chatPreview2 */

/* ANCHOR: chatPreview3 */
CommandAPICommand("broadcast")
    .withArguments(ChatArgument("message").usePreview(true).withPreview { info ->
        // Convert parsed BaseComponent[] to plain text
        val plainText = BaseComponent.toPlainText(*info.parsedInput() as Array<BaseComponent>)

        // Translate the & in plain text and generate a new BaseComponent[]
        TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', plainText))
    } )
    .executesPlayer(PlayerCommandExecutor { _, args ->
        Bukkit.spigot().broadcast(*args["message"] as Array<BaseComponent>)
    })
    .register()
/* ANCHOR_END: chatPreview3 */

/* ANCHOR: chatPreview4 */
CommandAPICommand("broadcast")
    .withArguments(AdventureChatArgument("message").usePreview(true).withPreview { info ->
        // Convert parsed Component to plain text
        val plainText = PlainTextComponentSerializer.plainText().serialize(info.parsedInput() as Component)

        // Translate the & in plain text and generate a new Component
        LegacyComponentSerializer.legacyAmpersand().deserialize(plainText)
    } )
    .executesPlayer(PlayerCommandExecutor { _, args ->
        Bukkit.broadcast(args["message"] as Component)
    })
    .register()
/* ANCHOR_END: chatPreview4 */
}

fun commandArguments() {
/* ANCHOR: commandArguments1 */
CommandAPICommand("mycommand")
    .withArguments(StringArgument("name"))
    .withArguments(IntegerArgument("amount"))
    .withOptionalArguments(PlayerArgument("player"))
    .withOptionalArguments(PlayerArgument("target"))
    .withOptionalArguments(GreedyStringArgument("message"))
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val name = args[0] as String // Access arguments by index
        val amount = args["amount"] as Int // Access arguments by node name
        val p = args.getOrDefault("player", player) as Player // Access arguments using the getOrDefault(String, Object) method
        val target = args.getOrDefault("target") { player } as Player // Access arguments using the getOrDefault(String, Supplier<?>) method
        val message = args.getOptional("message").orElse("Hello!") as String // Access arguments using the getOptional(String) method

        // Do whatever with these values
    })
    .register();
/* ANCHOR_END: commandArguments1 */

/* ANCHOR: commandArguments2 */
CommandAPICommand("mycommand")
    .withArguments(EntitySelectorArgument.ManyEntities("entities"))
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val entitySelector = args.getRaw("entities")!! // Access the raw argument with getRaw(String)

        // Do whatever with the entity selector
    })
    .register();
/* ANCHOR_END: commandArguments2 */

/* ANCHOR: commandArguments3 */
CommandAPICommand("mycommand")
    .withArguments(PlayerArgument("player"))
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val p: Player = args.getUnchecked("player")!!

        // Do whatever with the player
    })
    .register();
/* ANCHOR_END: commandArguments3 */

/* ANCHOR: commandArguments4 */
val nameArgument = StringArgument("name")
val amountArgument = IntegerArgument("amount")
val playerArgument = PlayerArgument("player")
val targetArgument = PlayerArgument("target")
val messageArgument = GreedyStringArgument("message")

CommandAPICommand("mycommand")
    .withArguments(nameArgument)
    .withArguments(amountArgument)
    .withOptionalArguments(playerArgument)
    .withOptionalArguments(targetArgument)
    .withOptionalArguments(messageArgument)
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val name: String = args.getByArgument(nameArgument)!!
        val amount: Int = args.getByArgument(amountArgument)!!
        val p: Player = args.getByArgumentOrDefault(playerArgument, player)
        val target: Player = args.getByArgumentOrDefault(targetArgument, player)
        val message: String = args.getOptionalByArgument(messageArgument).orElse("Hello!")

        // Do whatever with these values
    })
    .register();
/* ANCHOR_END: commandArguments4 */
}

fun commandFailures() {
/* ANCHOR: commandFailures1 */
// List of fruit
val fruit = listOf<String>("banana", "apple", "orange")

// Register the command
CommandAPICommand("getfruit")
    .withArguments(StringArgument("item").replaceSuggestions(ArgumentSuggestions.strings(fruit)))
    .executes(CommandExecutor { _, args ->
        val inputFruit = args["item"] as String

        if(fruit.any { it == inputFruit }) {
            // Do something with inputFruit
        } else {
            // The sender's input is not in the list of fruit
            throw CommandAPI.failWithString("That fruit doesn't exist!")
        }
    })
    .register()
/* ANCHOR_END: commandFailures1 */
}

fun commandRegistration() {
/* ANCHOR: commandRegistration1 */
// Create our command
CommandAPICommand("broadcastmsg")
    .withArguments(GreedyStringArgument("message")) // The arguments
    .withAliases("broadcast", "broadcastmessage")   // Command aliases
    .withPermission(CommandPermission.OP)           // Required permissions
    .executes(CommandExecutor { sender, args ->
        val message = args["message"] as String
        Bukkit.getServer().broadcastMessage(message)
    })
    .register()
/* ANCHOR_END: commandRegistration1 */
}

class commandTrees : JavaPlugin() {
fun commandTrees1() {
/* ANCHOR: commandTrees1 */
CommandTree("sayhi")
    .executes(CommandExecutor { sender, _ ->
        sender.sendMessage("Hi!")
    })
    .then(PlayerArgument("target")
        .executes(CommandExecutor { _, args ->
            val target = args["target"] as Player
            target.sendMessage("Hi")
        }))
    .register()
/* ANCHOR_END: commandTrees1 */

/* ANCHOR: commandTrees2 */
CommandTree("signedit")
    .then(LiteralArgument("set")
        .then(IntegerArgument("line_number", 1, 4)
            .then(GreedyStringArgument("text")
                .executesPlayer(PlayerCommandExecutor { player, args ->
                    // /signedit set <line_number> <text>
                    val sign: Sign = getTargetSign(player)
                    val line_number = args["line_number"] as Int
                    val text = args["text"] as String
                    sign.setLine(line_number - 1, text)
                    sign.update(true)
                 }))))
    .then(LiteralArgument("clear")
        .then(IntegerArgument("line_number", 1, 4)
            .executesPlayer(PlayerCommandExecutor { player, args ->
                // /signedit clear <line_number>
                val sign: Sign = getTargetSign(player)
                val line_number = args["line_number"] as Int
                sign.setLine(line_number - 1, "")
                sign.update(true)
            })))
    .then(LiteralArgument("copy")
        .then(IntegerArgument("line_number", 1, 4)
            .executesPlayer(PlayerCommandExecutor { player, args ->
                // /signedit copy <line_number>
                val sign: Sign = getTargetSign(player)
                val line_number = args["line_number"] as Int
                player.setMetadata("copied_sign_text", FixedMetadataValue(this, sign.getLine(line_number - 1)))
            })))
    .then(LiteralArgument("paste")
        .then(IntegerArgument("line_number", 1, 4)
            .executesPlayer(PlayerCommandExecutor { player, args ->
                // /signedit copy <line_number>
                val sign: Sign = getTargetSign(player)
                val line_number = args["line_number"] as Int
                sign.setLine(line_number - 1, player.getMetadata("copied_sign_text")[0].asString())
                sign.update(true)
            })))
    .register()
/* ANCHOR_END: commandTrees2 */
}

@Throws(WrapperCommandSyntaxException::class)
fun getTargetSign(player: Player): Sign {
    val block: Block? = player.getTargetBlock(null, 256)
    if (block != null && block.state is Sign) {
        return block.state as Sign
    } else {
        throw CommandAPI.failWithString("You're not looking at a sign!")
    }
}
}

class CommandUnregistration {
class UnregistrationBukkit : JavaPlugin() {
/* ANCHOR: commandUnregistration1 */
override fun onLoad() {
    CommandAPIBukkit.unregister("version", false, true)
}
/* ANCHOR_END: commandUnregistration1 */
}

class UnregistrationVanilla : JavaPlugin() {
/* ANCHOR: commandUnregistration2 */
override fun onEnable() {
    CommandAPI.unregister("gamemode")
}
/* ANCHOR_END: commandUnregistration2 */
}

class UnregistrationReplaceVanilla : JavaPlugin() {
/* ANCHOR: commandUnregistration3 */
override fun onEnable() {
    CommandAPI.unregister("gamemode");

    // Register our new /gamemode, with survival, creative, adventure and spectator
    CommandAPICommand("gamemode")
        .withArguments(MultiLiteralArgument("gamemodes", "survival", "creative", "adventure", "spectator"))
        .executes(CommandExecutor { sender, args ->
            // Implementation of our /gamemode command
        })
        .register()
}
/* ANCHOR_END: commandUnregistration3 */
}

class UnregistrationPlugin : JavaPlugin() {
/* ANCHOR: commandUnregistration4 */
override fun onEnable() {
    CommandAPIBukkit.unregister("luckperms:luckperms", false, true)
}
/* ANCHOR_END: commandUnregistration4 */
}

class UnregistrationCommandAPI : JavaPlugin() {
/* ANCHOR: commandUnregistration5 */
override fun onEnable() {
    CommandAPI.unregister("break")
}
/* ANCHOR_END: commandUnregistration5 */
}

class UnregistrationBukkitHelp : JavaPlugin() {
/* ANCHOR: commandUnregistration6 */
override fun onEnable() {
    object : BukkitRunnable() {
        override fun run() {
            CommandAPIBukkit.unregister("help", false, true)
        }
    }.runTaskLater(this, 0)
}
/* ANCHOR_END: commandUnregistration6 */
}

class UnregistrationOnlyVanillaNamespace : JavaPlugin() {
/* ANCHOR: commandUnregistration7 */
override fun onEnable() {
    object : BukkitRunnable() {
        override fun run() {
            CommandAPI.unregister("minecraft:gamemode")
        }
    }.runTaskLater(this, 0)
}
/* ANCHOR_END: commandUnregistration7 */
}

class UnregistrationDelayedVanillaBad : JavaPlugin() {
/* ANCHOR: commandUnregistration8 */
// NOT RECOMMENDED
override fun onEnable() {
    object : BukkitRunnable() {
        override fun run() {
            CommandAPI.unregister("gamemode")
        }
    }.runTaskLater(this, 0)
}
/* ANCHOR_END: commandUnregistration8 */
}

class UnregistrationDelayedVanillaBetter : JavaPlugin() {
/* ANCHOR: commandUnregistration9 */
override fun onEnable() {
    object : BukkitRunnable() {
        override fun run() {
            CommandAPI.unregister("gamemode", true)
        }
    }.runTaskLater(this, 0)
}
/* ANCHOR_END: commandUnregistration9 */
}
}

class conversion : JavaPlugin() {
/* ANCHOR: conversion1 */
class YourPlugin : JavaPlugin() {

    override fun onEnable() {
        Converter.convert(Bukkit.getPluginManager().getPlugin("TargetPlugin") as JavaPlugin)
        // Other code goes here...
    }

}
/* ANCHOR_END: conversion1 */

fun conversion2() {
/* ANCHOR: conversion2 */
val essentials = Bukkit.getPluginManager().getPlugin("Essentials") as JavaPlugin

// /speed <speed>
Converter.convert(essentials, "speed", IntegerArgument("speed", 0, 10))

// /speed <target>
Converter.convert(essentials, "speed", PlayerArgument("target"))

// /speed <walk/fly> <speed>
Converter.convert(essentials, "speed",
    MultiLiteralArgument("modes", "walk", "fly"),
    IntegerArgument("speed", 0, 10)
)

// /speed <walk/fly> <speed> <target>
Converter.convert(essentials, "speed",
    MultiLiteralArgument("modes", "walk", "fly"),
    IntegerArgument("speed", 0, 10),
    PlayerArgument("target")
)
/* ANCHOR_END: conversion2 */
}
}

fun delegatedProperties() {
/* ANCHOR: delegatedProperties1 */
CommandAPICommand("mycommand")
    .withArguments(StringArgument("string"))
    .withArguments(PlayerArgument("target"))
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val string: String by args
        val target: Player by args
        // Implementation...
    })
    .register()
/* ANCHOR_END: delegatedProperties1 */
}

class functions {
/* ANCHOR: functions1 */
class Main : JavaPlugin() {

    override fun onLoad() {
        // Commands which will be used in Minecraft functions are registered here

        CommandAPICommand("killall")
            .executes(CommandExecutor { _, _ ->
                // Kills all enemies in all worlds
                Bukkit.getWorlds().forEach { world -> world.livingEntities.forEach { entity -> entity.health = 0.0 } }
            })
            .register()
    }

    override fun onEnable() {
        // Register all other commands here
    }

}
/* ANCHOR_END: functions1 */
}

fun functionWrapper() {
/* ANCHOR: functionWrapper1 */
CommandAPICommand("runfunc")
    .withArguments(FunctionArgument("function"))
    .executes(CommandExecutor { _, args ->
        val functions = args["function"] as Array<FunctionWrapper>
        for (function in functions) {
            function.run() // The command executor in this case is 'sender'
        }
    })
    .register()
/* ANCHOR_END: functionWrapper1 */
}

fun help() {
/* ANCHOR: help1 */
CommandAPICommand("mycmd")
    .withShortDescription("Says hi")
    .withFullDescription("Broadcasts hi to everyone on the server")
    .executes(CommandExecutor { _, _ ->
        Bukkit.broadcastMessage("Hi!")
    })
    .register()
/* ANCHOR_END: help1 */

/* ANCHOR: help2 */
CommandAPICommand("mycmd")
    .withHelp("Says hi", "Broadcasts hi to everyone on the server")
    .executes(CommandExecutor { _, _ ->
        Bukkit.broadcastMessage("Hi!")
    })
    .register()
/* ANCHOR_END: help2 */
}

/* ANCHOR: help3 */
fun makeHelp(command: String): HelpTopic = object: HelpTopic() {
    override fun getShortText(): String = "Says hi"

    override fun getFullText(forWho: CommandSender): String {
        var helpText = ""
        if (forWho is Player) {
            // Make use of the player's locale to make language-specific help!
            val playerLocale = forWho.locale()
            if (playerLocale.getLanguage() == "en") {
                helpText = "Broadcasts \"Hi!\" to everyone on the server"
            } else if (playerLocale.getLanguage() == "de") {
                helpText = "Sendet \"Hi!\" an alle auf dem Server"
            }
        } else {
            helpText = "Broadcasts \"Hi!\" to everyone on the server"
        }
        return helpText
    }

    // Allow anyone to see this help topic
    override fun canSee(player: CommandSender): Boolean = true
}
/* ANCHOR_END: help3 */

fun help2() {
/* ANCHOR: help4 */
return CommandAPICommand("mycmd")
    .withHelp(makeHelp("mycmd"))
    .executes(CommandExecutor { _, _ ->
        Bukkit.broadcastMessage("Hi!")
    })
    .register()
/* ANCHOR_END: help4 */
}

fun listed() {
/* ANCHOR: listed1 */
CommandAPICommand("mycommand")
    .withArguments(PlayerArgument("player"))
    .withArguments(IntegerArgument("value").setListed(false))
    .withArguments(GreedyStringArgument("message"))
    .executes(CommandExecutor { _, args ->
        // args == [player, message]
        val player = args["player"] as Player
        val message = args["message"] as String // Note that the IntegerArgument is not available in the CommandArguments
        player.sendMessage(message)
    })
    .register()
/* ANCHOR_END: listed1 */
}

fun native() {
/* ANCHOR: native1 */
CommandAPICommand("break")
    .executesNative(NativeCommandExecutor { sender, _ ->
        val location = sender.location
        location.block.breakNaturally()
    })
    .register()
/* ANCHOR_END: native1 */
}

fun normalExecutors() {
/* ANCHOR: normalExecutors1 */
// Create our command
CommandAPICommand("broadcastmsg")
    .withArguments(GreedyStringArgument("message")) // The arguments
    .withAliases("broadcast", "broadcastmessage")       // Command aliases
    .withPermission(CommandPermission.OP)               // Required permissions
    .executes(CommandExecutor { _, args ->
        val message = args["message"] as String
        Bukkit.getServer().broadcastMessage(message)
    })
    .register()
/* ANCHOR_END: normalExecutors1 */

/* ANCHOR: normalExecutors2 */
CommandAPICommand("suicide")
    .executesPlayer(PlayerCommandExecutor { player, _ ->
        player.setHealth(0.0)
    })
    .register()
/* ANCHOR_END: normalExecutors2 */

/* ANCHOR: normalExecutors3 */
CommandAPICommand("suicide")
    .executesPlayer(PlayerCommandExecutor { player, _ ->
        player.setHealth(0.0)
    })
    .executesEntity(EntityCommandExecutor { entity, _ ->
        entity.world.createExplosion(entity.location, 4f)
        entity.remove()
    })
    .register()
/* ANCHOR_END: normalExecutors3 */

/* ANCHOR: normalExecutors4 */
CommandAPICommand("suicide")
    .executes(CommandExecutor { sender, _ ->
        val entity = (if (sender is ProxiedCommandSender) sender.callee else sender) as LivingEntity
        entity.setHealth(0.0)
    }, ExecutorType.PLAYER, ExecutorType.PROXY)
    .register()
/* ANCHOR_END: normalExecutors4 */
}

fun optionalArguments() {
/* ANCHOR: optionalArguments1 */
CommandAPICommand("sayhi")
    .withOptionalArguments(PlayerArgument("target"))
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val target: Player? = args["target"] as Player?
        if (target != null) {
            target.sendMessage("Hi!")
        } else {
            player.sendMessage("Hi!")
        }
    })
    .register()
/* ANCHOR_END: optionalArguments1 */

/* ANCHOR: optionalArguments2 */
CommandAPICommand("sayhi")
    .withOptionalArguments(PlayerArgument("target"))
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val target: Player = args.getOptional("target").orElse(player) as Player
        target.sendMessage("Hi!")
    })
    .register()
/* ANCHOR_END: optionalArguments2 */

/* ANCHOR: optionalArguments3 */
CommandAPICommand("rate")
    .withOptionalArguments(StringArgument("topic").combineWith(IntegerArgument("rating", 0, 10)))
    .withOptionalArguments(PlayerArgument("target"))
    .executes(CommandExecutor { sender, args ->
        val topic: String? = args["topic"] as String?
        if (topic == null) {
            sender.sendMessage(
                "Usage: /rate <topic> <rating> <player>(optional)",
                "Select a topic to rate, then give a rating between 0 and 10",
                "You can optionally add a player at the end to give the rating to"
            )
            return@CommandExecutor
        }

        // We know this is not null because rating is required if topic is given
        val rating = args["rating"] as Int

        // The target player is optional, so give it a default here
        val target: CommandSender = args.getOptional("target").orElse(sender) as CommandSender

        target.sendMessage("Your $topic was rated: $rating/10")
    })
    .register()
/* ANCHOR_END: optionalArguments3 */
}

fun permissions() {
/* ANCHOR: permissions1 */
// Register the /god command with the permission node "command.god"
CommandAPICommand("god")
    .withPermission(CommandPermission.fromString("command.god"))
    .executesPlayer(PlayerCommandExecutor { player, _ ->
        player.isInvulnerable = true
    })
    .register()
/* ANCHOR_END: permissions1 */

/* ANCHOR: permissions2 */
// Register the /god command with the permission node "command.god", without creating a CommandPermission
CommandAPICommand("god")
    .withPermission("command.god")
    .executesPlayer(PlayerCommandExecutor { player, _ ->
        player.isInvulnerable = true
    })
    .register()
/* ANCHOR_END: permissions2 */

/* ANCHOR: permissions3 */
// Register /kill command normally. Since no permissions are applied, anyone can run this command
CommandAPICommand("kill")
    .executesPlayer(PlayerCommandExecutor { player, _ ->
        player.health = 0.0
    })
    .register()
/* ANCHOR_END: permissions3 */

/* ANCHOR: permissions4 */
// Adds the OP permission to the "target" argument. The sender requires OP to execute /kill <target>
CommandAPICommand("kill")
    .withArguments(PlayerArgument("target").withPermission(CommandPermission.OP))
    .executesPlayer(PlayerCommandExecutor { _, args ->
        (args["target"] as Player).health = 0.0
    })
    .register()
/* ANCHOR_END: permissions4 */

/* ANCHOR: permissions5 */
// /economy - requires the permission "economy.self" to exectue
CommandAPICommand("economy")
    .withPermission("economy.self")
    .executesPlayer(PlayerCommandExecutor { player, _ ->
        // send the executor their own balance here.
    })
    .register()

// /economy <target> - requires the permission "economy.other" to execute
CommandAPICommand("economy")
    .withPermission("economy.other") // The important part of this example
    .withArguments(PlayerArgument("target"))
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val target = args["target"] as Player
        // send the executor the targets balance here.
    })
    .register()

// /economy give <target> <amount> - requires the permission "economy.admin.give" to execute
CommandAPICommand("economy")
    .withPermission("economy.admin.give") // The important part of this example
    .withArguments(PlayerArgument("target"))
    .withArguments(DoubleArgument("amount"))
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val target = args["target"] as Player
        val amount = args["amount"] as Double
        // update the targets balance here
    })
    .register()

// /economy reset <target> - requires the permission "economy.admin.reset" to execute
CommandAPICommand("economy")
    .withPermission("economy.admin.reset") // The important part of this example
    .withArguments(PlayerArgument("target"))
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val target = args["target"] as Player
        // reset the targets balance here
    })
    .register()
/* ANCHOR_END: permissions5 */
}

fun predicateTips() {
val partyMembers = mutableMapOf<UUID, String>()
/* ANCHOR: predicateTips1 */
var arguments = mutableListOf<Argument<*>>()

// The "create" literal, with a requirement that a player must have a party
arguments.add(LiteralArgument("create")
    .withRequirement { !partyMembers.containsKey((it as Player).uniqueId) }
)

arguments.add(StringArgument("partyName"))
/* ANCHOR_END: predicateTips1 */

/* ANCHOR: predicateTips2 */
arguments = mutableListOf<Argument<*>>()
arguments.add(LiteralArgument("tp")
    .withRequirement { partyMembers.containsKey((it as Player).uniqueId) })
/* ANCHOR_END: predicateTips2 */

/* ANCHOR: predicateTips3 */
val testIfPlayerHasParty = Predicate { sender: CommandSender ->
    partyMembers.containsKey((sender as Player).uniqueId)
}
/* ANCHOR_END: predicateTips3 */

/* ANCHOR: predicateTips4 */
var args = listOf<Argument<*>>(
    LiteralArgument("create").withRequirement(testIfPlayerHasParty.negate()),
    StringArgument("partyName")
)
/* ANCHOR_END: predicateTips4 */

@Suppress("unused")
/* ANCHOR: predicateTips5 */
args = listOf<Argument<*>>(LiteralArgument("tp").withRequirement(testIfPlayerHasParty))
/* ANCHOR_END: predicateTips5 */
}

fun proxySender() {
/* ANCHOR: proxySender1 */
CommandAPICommand("killme")
    .executesPlayer(PlayerCommandExecutor { player, _ ->
        player.setHealth(0.0)
    })
    .register()
/* ANCHOR_END: proxySender1 */

/* ANCHOR: proxySender2 */
CommandAPICommand("killme")
    .executesPlayer(PlayerCommandExecutor { player, _ ->
        player.setHealth(0.0)
    })
    .executesProxy(ProxyCommandExecutor { proxy, _ ->
        // Check if the callee (target) is an Entity and kill it
        if (proxy.callee is LivingEntity) {
            (proxy.callee as LivingEntity).setHealth(0.0)
        }
    })
    .register()
/* ANCHOR_END: proxySender2 */
}

fun requirements() {
/* ANCHOR: requirements1 */
CommandAPICommand("repair")
    .withRequirement { (it as Player).level >= 30 }
    .executesPlayer(PlayerCommandExecutor { player, _ ->

        // Repair the item back to full durability
        val item = player.inventory.itemInMainHand
        val itemMeta = item.itemMeta
        if (itemMeta is Damageable) {
            itemMeta.setDamage(0)
            item.setItemMeta(itemMeta)
        }

        // Subtract 30 levels
        player.setLevel(player.level - 30)
    })
    .register()
/* ANCHOR_END: requirements1 */

/* ANCHOR: requirements2 */
val partyMembers = mutableMapOf<UUID, String>()
/* ANCHOR_END: requirements2 */

/* ANCHOR: requirements3 */
var arguments = mutableListOf<Argument<*>>()

// The "create" literal, with a requirement that a player must have a party
arguments.add(LiteralArgument("create")
    .withRequirement { !partyMembers.containsKey((it as Player).uniqueId) }
)

arguments.add(StringArgument("partyName"))
/* ANCHOR_END: requirements3 */

/* ANCHOR: requirements4 */
CommandAPICommand("party")
    .withArguments(*arguments.toTypedArray())
    .executesPlayer(PlayerCommandExecutor { player, args ->

        // Get the name of the party to create
        val partyName = args["partyName"] as String

        partyMembers[player.uniqueId] = partyName
    })
    .register()
/* ANCHOR_END: requirements4 */

/* ANCHOR: requirements5 */
arguments = mutableListOf<Argument<*>>()
arguments.add(LiteralArgument("tp")
    .withRequirement { partyMembers.containsKey((it as Player).uniqueId) })
/* ANCHOR_END: predicateTips2 */

arguments.add(PlayerArgument("player")
    .replaceSafeSuggestions(SafeSuggestions.suggest { info ->

        // Store the list of party members to teleport to
        val playersToTeleportTo = mutableListOf<Player>()

        val partyName = partyMembers[(info.sender() as Player).uniqueId]

        // Find the party members
        for ((uuid, party) in partyMembers) {

            // Ignore yourself
            if (uuid == (info.sender() as Player).uniqueId) {
                continue
            } else {
                // If the party member is in the same party as you
                if (party == partyName) {
                    val target = Bukkit.getPlayer(uuid)!!
                    if (target.isOnline) {
                        // Add them if they are online
                        playersToTeleportTo.add(target)
                    }
                }
            }
        }

        playersToTeleportTo.toTypedArray()
    }))
/* ANCHOR_END: requirements5 */

/* ANCHOR: requirements6 */
CommandAPICommand("party")
    .withArguments(arguments)
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val target = args["player"] as Player
        player.teleport(target)
    })
    .register()
/* ANCHOR_END: requirements6 */

/* ANCHOR: requirements7 */
CommandAPICommand("party")
    .withArguments(arguments)
    .executesPlayer(PlayerCommandExecutor { player, args ->

        // Get the name of the party to create
        val partyName = args["partyName"] as String

        partyMembers[player.uniqueId] = partyName

        CommandAPI.updateRequirements(player)
    })
    .register()
/* ANCHOR_END: requirements7 */

/* ANCHOR: requirements8 */
CommandAPICommand("someCommand")
    .withRequirement { (it as Player).level >= 30 }
    .withRequirement { (it as Player).inventory.contains(Material.DIAMOND_PICKAXE) }
    .withRequirement { (it as Player).isInvulnerable() }
    .executesPlayer(PlayerCommandExecutor { player, args ->
        // Code goes here
    })
    .register()
/* ANCHOR_END: requirements8 */
}

fun resultingCommandExecutors() {
/* ANCHOR: resultingCommandExecutors1 */
CommandAPICommand("randnum")
    .executes(ResultingCommandExecutor { _, _ ->
        Random.nextInt()
    })
    .register()
/* ANCHOR_END: resultingCommandExecutors1 */

/* ANCHOR: resultingCommandExecutors2 */
// Register random number generator command from 1 to 99 (inclusive)
CommandAPICommand("randomnumber")
    .executes(ResultingCommandExecutor { _, _ ->
        (1..100).random() // Returns random number from 1 <= x < 100
    })
    .register()
/* ANCHOR_END: resultingCommandExecutors2 */

/* ANCHOR: resultingCommandExecutors3 */
// Register reward giving system for a target player
CommandAPICommand("givereward")
    .withArguments(EntitySelectorArgument.OnePlayer("target"))
    .executes(CommandExecutor { _, args ->
        val player = args["target"] as Player
        player.inventory.addItem(ItemStack(Material.DIAMOND, 64))
        Bukkit.broadcastMessage("${player.name} won a rare 64 diamonds from a loot box!")
    })
    .register()
/* ANCHOR_END: resultingCommandExecutors3 */
}

fun safeArgumentSuggestions() {
/* ANCHOR: safeArgumentSuggestions1 */
// Create our itemstack
val emeraldSword = ItemStack(Material.DIAMOND_SWORD)
val meta = emeraldSword.itemMeta
meta?.setDisplayName("Emerald Sword")
meta?.isUnbreakable = true
emeraldSword.itemMeta = meta

// Create and register our recipe
val emeraldSwordRecipe = ShapedRecipe(NamespacedKey(this, "emerald_sword"), emeraldSword)
emeraldSwordRecipe.shape(
    "AEA",
    "AEA",
    "ABA"
)
emeraldSwordRecipe.setIngredient('A', Material.AIR)
emeraldSwordRecipe.setIngredient('E', Material.EMERALD)
emeraldSwordRecipe.setIngredient('B', Material.BLAZE_ROD)
server.addRecipe(emeraldSwordRecipe)

// Omitted, more itemstacks and recipes
/* ANCHOR_END: safeArgumentSuggestions1 */

/* ANCHOR: safeArgumentSuggestions2 */
// Safely override with the recipe we've defined
val arguments = listOf<Argument<*>>(
    RecipeArgument("recipe").replaceSafeSuggestions(SafeSuggestions.suggest {
        arrayOf(emeraldSwordRecipe, /* Other recipes here */)
    })
)

// Register our command
CommandAPICommand("giverecipe")
    .withArguments(arguments)
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val recipe = args["recipe"] as Recipe
        player.inventory.addItem(recipe.result)
    })
    .register()
/* ANCHOR_END: safeArgumentSuggestions2 */

/* ANCHOR: safeArgumentSuggestions3 */
val forbiddenMobs = listOf<EntityType>(EntityType.ENDER_DRAGON, EntityType.WITHER)
val allowedMobs = EntityType.values().toMutableList()
allowedMobs.removeAll(forbiddenMobs) // Now contains everything except enderdragon and wither
/* ANCHOR_END: safeArgumentSuggestions3 */

/* ANCHOR: safeArgumentSuggestions4 */
val safeArguments = listOf<Argument<*>>(
    EntityTypeArgument("mob").replaceSafeSuggestions(SafeSuggestions.suggest {
        info ->
            if (info.sender().isOp) {
                // All entity types
                EntityType.values()
            } else {
                // Only allowedMobs
                allowedMobs.toTypedArray()
            }
        }
    )
)
/* ANCHOR_END: safeArgumentSuggestions4 */

/* ANCHOR: safeArgumentSuggestions5 */
CommandAPICommand("spawnmob")
    .withArguments(safeArguments)
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val entityType = args["mob"] as EntityType
        player.world.spawnEntity(player.location, entityType)
    })
    .register()
/* ANCHOR_END: safeArgumentSuggestions5 */

/* ANCHOR: safeArgumentSuggestions6 */
val safeArgs = mutableListOf<Argument<*>>()
safeArgs.add(EntitySelectorArgument.OnePlayer("target"))
safeArgs.add(PotionEffectArgument("potioneffect").replaceSafeSuggestions(SafeSuggestions.suggest {
    info ->
        val target = info.previousArgs()["target"] as Player

        // Convert PotionEffect[] into PotionEffectType[]
        target.activePotionEffects.map{ it.type }.toTypedArray()
    })
)
/* ANCHOR_END: safeArgumentSuggestions6 */

/* ANCHOR: safeArgumentSuggestions7 */
CommandAPICommand("removeeffect")
    .withArguments(safeArgs)
    .executesPlayer(PlayerCommandExecutor { _, args ->
        val target = args["target"] as Player
        val potionEffect = args["potioneffect"] as PotionEffectType
        target.removePotionEffect(potionEffect)
    })
    .register()
/* ANCHOR_END: safeArgumentSuggestions7 */
}

class setupShading {
val plugin: JavaPlugin = object: JavaPlugin() {}

fun setupShading1() {
/* ANCHOR: setupShading1 */
CommandAPI.onLoad(CommandAPIBukkitConfig(plugin).silentLogs(true))
/* ANCHOR_END: setupShading1 */
}

/* ANCHOR: setupShading2 */
class MyPlugin : JavaPlugin() {

    override fun onLoad() {
        CommandAPI.onLoad(CommandAPIBukkitConfig(this).verboseOutput(true)) // Load with verbose output

        CommandAPICommand("ping")
            .executes(CommandExecutor { sender, _ ->
                sender.sendMessage("pong!")
            })
            .register()
    }

    override fun onEnable() {
        CommandAPI.onEnable()

        // Register commands, listeners etc.
    }

    override fun onDisable() {
        CommandAPI.onDisable()
    }

}
/* ANCHOR_END: setupShading2 */
}

class stringArgumentSuggestions {
fun stringArgumentSuggestions1() {
val warps = mutableMapOf<String, Location>()
/* ANCHOR: stringArgumentSuggestions1 */
val arguments = listOf<Argument<*>>(
    StringArgument("world").replaceSuggestions(ArgumentSuggestions.strings(
        "northland", "eastland", "southland", "westland"
    ))
)

CommandAPICommand("warp")
    .withArguments(arguments)
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val warp = args["world"] as String
        player.teleport(warps[warp]!!) // Look up the warp in a map, for example
    })
    .register()
/* ANCHOR_END: stringArgumentSuggestions1 */
}

/* ANCHOR: stringArgumentSuggestions2 */
class Friends {

    companion object {

        val friends = mutableMapOf<UUID, Array<String>>()

        fun getFriends(sender: CommandSender): Array<String> {
            if (sender is Player) {
                // Look up friends in a database or file
                return friends[sender.uniqueId]!!
            } else {
                return arrayOf<String>()
            }
        }

    }

}
/* ANCHOR_END: stringArgumentSuggestions2 */

fun stringArgumentSuggestions34() {
/* ANCHOR: stringArgumentSuggestions3 */
val arguments = listOf<Argument<*>>(
    PlayerArgument("friend").replaceSuggestions(ArgumentSuggestions.strings { info ->
        Friends.getFriends(info.sender())
    } )
)

CommandAPICommand("friendtp")
    .withArguments(arguments)
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val target = args["friend"] as Player
        player.teleport(target)
    })
    .register()
/* ANCHOR_END: stringArgumentSuggestions3 */

/* ANCHOR: stringArgumentSuggestions4 */
// Declare our arguments as normal
val commandArgs = mutableListOf<Argument<*>>()
commandArgs.add(IntegerArgument("radius"))

// Replace the suggestions for the PlayerArgument.
// info.sender() refers to the command sender that is running this command
// info.previousArgs() refers to the Object[] of previously declared arguments (in this case, the IntegerArgument radius)
commandArgs.add(PlayerArgument("target").replaceSuggestions(ArgumentSuggestions.strings { info: SuggestionInfo<CommandSender> ->

    // Cast the first argument (radius, which is an IntegerArgument) to get its value
    val radius = (info.previousArgs()["radius"] as Int).toDouble()

    // Get nearby entities within the provided radius
    val player = info.sender() as Player
    val entities = player.world.getNearbyEntities(player.location, radius, radius, radius)

    // Get player names within that radius
    entities
        .filter { it.type == EntityType.PLAYER }
        .map { it.name }
        .toTypedArray()
}))
commandArgs.add(GreedyStringArgument("message"))

// Declare our command as normal
CommandAPICommand("localmsg")
    .withArguments(*commandArgs.toTypedArray())
    .executesPlayer(PlayerCommandExecutor { _, args ->
        val target = args["target"] as Player
        val message = args["message"] as String
        target.sendMessage(message)
    })
    .register()
/* ANCHOR_END: stringArgumentSuggestions4 */
}
}

@Suppress("UNUSED_PARAMETER")
fun subcommands() {
/* ANCHOR: subcommands1 */
val groupAdd = CommandAPICommand("add")
    .withArguments(StringArgument("permission"))
    .withArguments(StringArgument("groupName"))
    .executes(CommandExecutor { sender, args ->
        // perm group add code
    })
/* ANCHOR_END: subcommands1 */

/* ANCHOR: subcommands2 */
val groupRemove = CommandAPICommand("remove")
    .withArguments(StringArgument("permission"))
    .withArguments(StringArgument("groupName"))
    .executes(CommandExecutor { sender, args ->
        // perm group remove code
    })

val group = CommandAPICommand("group")
    .withSubcommand(groupAdd)
    .withSubcommand(groupRemove)
/* ANCHOR_END: subcommands2 */

/* ANCHOR: subcommands3 */
CommandAPICommand("perm")
    .withSubcommand(group)
    .register()
/* ANCHOR_END: subcommands3 */

/* ANCHOR: subcommands4 */
CommandAPICommand("perm")
    .withSubcommand(CommandAPICommand("group")
        .withSubcommand(CommandAPICommand("add")
            .withArguments(StringArgument("permission"))
            .withArguments(StringArgument("groupName"))
            .executes(CommandExecutor { sender, args ->
                // perm group add code
            })
        )
        .withSubcommand(CommandAPICommand("remove")
            .withArguments(StringArgument("permission"))
            .withArguments(StringArgument("groupName"))
            .executes(CommandExecutor { sender, args ->
                // perm group remove code
            })
        )
    )
    .withSubcommand(CommandAPICommand("user")
        .withSubcommand(CommandAPICommand("add")
            .withArguments(StringArgument("permission"))
            .withArguments(StringArgument("userName"))
            .executes(CommandExecutor { sender, args ->
                // perm user add code
            })
        )
        .withSubcommand(CommandAPICommand("remove")
            .withArguments(StringArgument("permission"))
            .withArguments(StringArgument("userName"))
            .executes(CommandExecutor { sender, args ->
                // perm user remove code
            })
        )
    )
    .register()
/* ANCHOR_END: subcommands4 */
}

class tooltips {
fun tooltips12() {
/* ANCHOR: tooltips1 */
val arguments = mutableListOf<Argument<*>>()
arguments.add(StringArgument("emote")
    .replaceSuggestions(ArgumentSuggestions.stringsWithTooltips { info ->
        arrayOf<IStringTooltip>(
            StringTooltip.ofString("wave", "Waves at a player"),
            StringTooltip.ofString("hug", "Gives a player a hug"),
            StringTooltip.ofString("glare", "Gives a player the death glare")
        )
    })
)
arguments.add(PlayerArgument("target"))
/* ANCHOR_END: tooltips1 */

/* ANCHOR: tooltips2 */
CommandAPICommand("emote")
    .withArguments(*arguments.toTypedArray())
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val emote = args["emote"] as String
        val target = args["target"] as Player

        when (emote) {
            "wave" -> target.sendMessage("${player.name} waves at you!")
            "hug" -> target.sendMessage("${player.name} hugs you!")
            "glare" -> target.sendMessage("${player.name} gives you the death glare...")
        }
    })
    .register()
/* ANCHOR_END: tooltips2 */
}

/* ANCHOR: tooltips3 */
class CustomItem(val item: ItemStack, val name: String, lore: String): IStringTooltip {

    init {
        val meta = item.itemMeta
        meta.setDisplayName(name)
        meta.lore = listOf(lore)
        item.itemMeta = meta
    }

    override fun getSuggestion(): String = this.item.itemMeta.displayName

    override fun getTooltip(): Message = BukkitTooltip.messageFromString(this.item.itemMeta.lore?.get(0) ?: "")

}
/* ANCHOR_END: tooltips3 */

fun tooltips456() {
/* ANCHOR: tooltips4 */
val customItems = arrayOf<CustomItem>(
    CustomItem(ItemStack(Material.DIAMOND_SWORD), "God sword", "A sword from the heavens"),
    CustomItem(ItemStack(Material.PUMPKIN_PIE), "Sweet pie", "Just like grandma used to make")
)

CommandAPICommand("giveitem")
    .withArguments(StringArgument("item").replaceSuggestions(ArgumentSuggestions.stringsWithTooltips(*customItems))) // We use customItems[] as the input for our suggestions with tooltips
    .executesPlayer(PlayerCommandExecutor { player, args ->
        val itemName = args["item"] as String

        // Give them the item
        for (item in customItems) {
            if (item.name == itemName) {
                player.inventory.addItem(item.item)
                break
            }
        }
    })
    .register()
/* ANCHOR_END: tooltips4 */

/* ANCHOR: tooltips5 */
val arguments = listOf<Argument<*>>(
    LocationArgument("location")
        .replaceSafeSuggestions(SafeSuggestions.tooltips { info ->
            // We know the sender is a player if we use .executesPlayer()
            val player = info.sender() as Player
            BukkitTooltip.arrayOf(
                BukkitTooltip.ofString(player.world.spawnLocation, "World spawn"),
                BukkitTooltip.ofString(player.bedSpawnLocation, "Your bed"),
                BukkitTooltip.ofString(player.getTargetBlockExact(256)?.location, "Target block")
            )
        } )
)
/* ANCHOR_END: tooltips5 */

/* ANCHOR: tooltips6 */
CommandAPICommand("warp")
    .withArguments(arguments)
    .executesPlayer(PlayerCommandExecutor { player, args ->
        player.teleport(args["location"] as Location)
    })
    .register()
/* ANCHOR_END: tooltips6 */
}
}

// TODO: This example isn't used in the documentation! Can we delete this?
fun treeexample() {
CommandTree("treeexample")
    // Set the aliases as you normally would
    .withAliases("treealias")
    // Set an executor on the command itself
    .executes(CommandExecutor { sender, _ ->
        sender.sendMessage("Root with no arguments")
    })
    // Create a new branch starting with a the literal 'integer'
    .then(LiteralArgument("integer")
        // Execute on the literal itself
        .executes(CommandExecutor { sender, _ ->
            sender.sendMessage("Integer Branch with no arguments")
        })
        // Create a further branch starting with an integer argument, which executes a command
        .then(IntegerArgument("integer").executes(CommandExecutor { sender, args ->
            sender.sendMessage("Integer Branch with integer argument: ${args[0]}")
        })))
    .then(LiteralArgument("biome")
        .executes(CommandExecutor { sender, _ ->
            sender.sendMessage("Biome Branch with no arguments")
        })
        .then(BiomeArgument("biome").executes(CommandExecutor { sender, args ->
            sender.sendMessage("Biome Branch with biome argument: ${args[0]}")
        })))
    .then(LiteralArgument("string")
        .executes(CommandExecutor { sender, _ ->
            sender.sendMessage("String Branch with no arguments")
        })
        .then(StringArgument("string").executes(CommandExecutor { sender, args ->
            sender.sendMessage("String Branch with string argument: ${args[0]}")
        })))
    // Call register to finish as you normally would
    .register()


}

}