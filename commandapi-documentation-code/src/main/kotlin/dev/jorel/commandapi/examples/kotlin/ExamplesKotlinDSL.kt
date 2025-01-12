package dev.jorel.commandapi.examples.kotlin

import de.tr7zw.changeme.nbtapi.NBTContainer
import dev.jorel.commandapi.arguments.*
import dev.jorel.commandapi.kotlindsl.*
import dev.jorel.commandapi.wrappers.*
import dev.jorel.commandapi.wrappers.Rotation
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.*
import org.bukkit.advancement.Advancement
import org.bukkit.block.*
import org.bukkit.block.data.BlockData
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.inventory.ComplexRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.loot.LootTable
import org.bukkit.loot.Lootable
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Team
import org.bukkit.util.EulerAngle
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Predicate
import kotlin.random.Random

class ExamplesKotlinDSL : JavaPlugin() {

fun advancementArgument() {
/* ANCHOR: advancementArgument1 */
commandAPICommand("award") {
    playerArgument("player")
    advancementArgument("advancement")
    anyExecutor { _, args ->
        val target = args["player"] as Player
        val advancement = args["advancement"] as Advancement

        // Award all criteria for the advancement
        val progress = target.getAdvancementProgress(advancement)
        for (criteria in advancement.criteria) {
            progress.awardCriteria(criteria)
        }
    }
}
/* ANCHOR_END: advancementArgument1 */
}

fun aliases() {
/* ANCHOR: aliases1 */
commandAPICommand("getpos") {
    // Declare your aliases
    withAliases("getposition", "getloc", "getlocation", "whereami")

    // Declare your implementation
    entityExecutor { entity, _ ->
        val loc = entity.location
        entity.sendMessage("You are at ${loc.blockX}, ${loc.blockY}, ${loc.blockZ}")
    }
    commandBlockExecutor { block, _ ->
        val loc = block.block.location
        block.sendMessage("You are at ${loc.blockX}, ${loc.blockY}, ${loc.blockZ}")
    }
}
/* ANCHOR_END: aliases1 */
}

fun argument_angle() {
// TODO: This example isn't used!
/* ANCHOR: argumentAngle1 */
commandAPICommand("yaw") {
    angleArgument("amount")
    playerExecutor { player, args ->
        val newLocation = player.location
        newLocation.yaw = args["amount"] as Float
        player.teleport(newLocation)
    }
}
/* ANCHOR_END: argumentAngle1 */
}

fun argument_biome() {
/* ANCHOR: argumentBiome1 */
commandAPICommand("setbiome") {
    biomeArgument("biome")
    playerExecutor { player, args ->
        val biome = args["biome"] as Biome

        val chunk = player.location.chunk
        player.world.setBiome(chunk.x, player.location.blockY, chunk.z, biome)
    }
}
/* ANCHOR_END: argumentBiome1 */
}

fun argument_blockPredicate() {
val arguments = arrayOf<Argument<*>>(
    IntegerArgument("radius"),
    BlockPredicateArgument("fromBlock"),
    BlockStateArgument("toBlock"),
)

/* ANCHOR: argumentBlockPredicate1 */
commandAPICommand("replace") {
    arguments(*arguments)
    playerExecutor { player, args ->
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
    }
}
/* ANCHOR_END: argumentBlockPredicate1 */
}

fun argument_blockState() {
/* ANCHOR: argumentBlockState1 */
commandAPICommand("set") {
    blockStateArgument("block")
    playerExecutor { player, args ->
        val blockdata = args["block"] as BlockData
        val targetBlock = player.getTargetBlockExact(256)

        // Set the block, along with its data
        targetBlock?.type = blockdata.material
        targetBlock?.state?.blockData = blockdata
    }
}
/* ANCHOR_END: argumentBlockState1 */
}

fun argument_chatAdventure() {
/* ANCHOR: argumentChatAdventure1 */
commandAPICommand("namecolor") {
    chatColorArgument("chatcolor")
    playerExecutor { player, args ->
        val color = args["chatcolor"] as NamedTextColor
        player.displayName(Component.text().color(color).append(Component.text(player.name)).build())
    }
}
/* ANCHOR_END: argumentChatAdventure1 */

/* ANCHOR: argumentChatAdventure2 */
commandAPICommand("showbook") {
    playerArgument("target")
    textArgument("title")
    stringArgument("author")
    adventureChatComponentArgument("contents")
    anyExecutor { _, args ->
        val target = args["target"] as Player
        val title = args["title"] as String
        val author = args["author"] as String
        val content = args["contents"] as Component

        // Create a book and show it to the user (Requires Paper)
        val mybook = Book.book(Component.text(title), Component.text(author), content)
        target.openBook(mybook)
    }
}
/* ANCHOR_END: argumentChatAdventure2 */

/* ANCHOR: argumentChatAdventure3 */
commandAPICommand("pbroadcast") {
    adventureChatArgument("message")
    anyExecutor { _, args ->
        val message = args["message"] as Component

        // Broadcast the message to everyone with broadcast permissions.
        Bukkit.getServer().broadcast(message, Server.BROADCAST_CHANNEL_USERS)
        Bukkit.getServer().broadcast(message)
    }
}
/* ANCHOR_END: argumentChatAdventure3 */
}

fun argument_chatSpigot() {
/* ANCHOR: argumentChatSpigot1 */
commandAPICommand("namecolor") {
    chatColorArgument("chatcolor")
    playerExecutor { player, args ->
        val color = args["chatcolor"] as ChatColor
        player.setDisplayName("$color${player.name}")
    }
}
/* ANCHOR_END: argumentChatSpigot1 */

/* ANCHOR: argumentChatSpigot2 */
commandAPICommand("makebook") {
    playerArgument("player")
    chatComponentArgument("contents")
    anyExecutor { _, args ->
        val player = args["player"] as Player
        val array = args["contents"] as Array<BaseComponent>

        // Create book
        val item = ItemStack(Material.WRITTEN_BOOK)
        val meta = item.itemMeta as BookMeta
        meta.title = "Custom Book"
        meta.author = player.name
        meta.spigot().setPages(array)
        item.itemMeta = meta

        // Give player the book
        player.inventory.addItem(item)
    }
}
/* ANCHOR_END: argumentChatSpigot2 */

/* ANCHOR: argumentChatSpigot3 */
commandAPICommand("pbroadcast") {
    chatArgument("message")
    anyExecutor { _, args ->
        val message = args["message"] as Array<BaseComponent>

        // Broadcast the message to everyone on the server
        Bukkit.getServer().spigot().broadcast(*message)
    }
}
/* ANCHOR_END: argumentChatSpigot3 */
}

fun argument_command() {
/* ANCHOR: argumentCommand1 */
commandAPICommand("sudo") {
    playerArgument("target")
    commandArgument("command")
    anyExecutor { _, args ->
        val target = args["target"] as Player
        val command = args["command"] as CommandResult

        command.execute(target)
    }
}
/* ANCHOR_END: argumentCommand1 */
}

fun argument_custom() {
/* ANCHOR: argumentCustom2 */
commandAPICommand("tpworld") {
    worldArgument("world") // This method is actually also built into the Kotlin DSL
    playerExecutor { player, args ->
        player.teleport((args["world"] as World).spawnLocation)
    }
}
/* ANCHOR_END: argumentCustom2 */
}

fun argument_enchantment() {
/* ANCHOR: argumentEnchantment1 */
commandAPICommand("enchantitem") {
    enchantmentArgument("enchantment")
    integerArgument("level", 1, 5)
    playerExecutor { player, args ->
        val enchantment = args["enchantment"] as Enchantment
        val level = args["level"] as Int

        // Add the enchantment
        player.inventory.itemInMainHand.addEnchantment(enchantment, level)
    }
}
/* ANCHOR_END: argumentEnchantment1 */
}

fun argument_entities() {
/* ANCHOR: argumentEntities1 */
commandAPICommand("remove") {
    // Using a collective entity selector to select multiple entities
    entitySelectorArgumentManyEntities("entities")
    anyExecutor { sender, args ->
        // Parse the argument as a collection of entities (as stated above in the documentation)
        val entities = args["entities"] as Collection<Entity>

        sender.sendMessage("Removed ${entities.size} entities")
        for (e in entities) {
            e.remove()
        }
    }
}
/* ANCHOR_END: argumentEntities1 */

/* ANCHOR: argumentEntities2 */
commandAPICommand("spawnmob") {
    entityTypeArgument("entity")
    integerArgument("amount", 1, 100) // Prevent spawning too many entities
    playerExecutor { player, args ->
        for (i in 0 until args["amount"] as Int) {
            player.world.spawnEntity(player.location, args["entity"] as EntityType)
        }
    }
}
/* ANCHOR_END: argumentEntities2 */
}

fun argument_function() {
/* ANCHOR: argumentFunction1 */
commandAPICommand("runfunction") {
    functionArgument("function")
    anyExecutor { _, args ->
        val functions = args["function"] as Array<FunctionWrapper>

        // Run all functions in our FunctionWrapper[]
        for (function in functions) {
            function.run()
        }
    }
}
/* ANCHOR_END: argumentFunction1 */
}

fun argument_itemStack() {
/* ANCHOR: argumentItemStack1 */
commandAPICommand("item") {
    itemStackArgument("itemstack")
    playerExecutor { player, args ->
        player.inventory.addItem(args["itemstack"] as ItemStack)
    }
}
/* ANCHOR_END: argumentItemStack1 */
}

fun argument_itemStackPredicate() {
/* ANCHOR: argumentItemStackPredicate1 */
// Register our command
commandAPICommand("rem") {
    itemStackPredicateArgument("items")
    playerExecutor { player, args ->
        // Get our predicate
        val predicate = args["items"] as Predicate<ItemStack>

        for (item in player.inventory) {
            if (predicate.test(item)) {
                player.inventory.remove(item)
            }
        }
    }
}
/* ANCHOR_END: argumentItemStackPredicate1 */
}

fun argument_list() {
/* ANCHOR: argumentList1 */
commandAPICommand("multigive") {
    integerArgument("amount", 1, 64)
    argument(ListArgumentBuilder<Material>("materials")
        .withList(Material.values().toList())
        .withMapper { material -> material.name.lowercase() }
        .buildGreedy()
    )
    playerExecutor { player, args ->
        val amount = args["amount"] as Int
        val theList = args["materials"] as List<Material>

        for (item in theList) {
            player.inventory.addItem(ItemStack(item, amount))
        }
    }
}
/* ANCHOR_END: argumentList1 */
}

fun argument_literal() {
/* ANCHOR: argumentLiteral1 */
commandAPICommand("mycommand") {
    literalArgument("hello")
    textArgument("text")
    anyExecutor { _, args ->
        // This gives the variable "text" the contents of the TextArgument, and not the literal "hello"
        val text = args[0] as String
    }
}
/* ANCHOR_END: argumentLiteral1 */

/* ANCHOR: argumentLiteral2 */
commandAPICommand("mycommand") {
    argument(LiteralArgument.of("hello"))
    textArgument("text")
    anyExecutor { _, args ->
        val text = args[0] as String
    }
}

commandAPICommand("mycommand") {
    argument(LiteralArgument.literal("hello"))
    textArgument("text")
    anyExecutor { _, args ->
        val text = args[0] as String
    }
}
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
    commandAPICommand("changegamemode") {
        literalArgument(key)
        playerExecutor { player, args ->
            // Retrieve the object from the map via the key and NOT the args[]
            player.gameMode = gamemodes[key]!!
        }
    }

}
/* ANCHOR_END: argumentLiteral3 */
}

fun argument_locations() {
/* ANCHOR: argumentLocations1 */
commandAPICommand("break") {
    // We want to target blocks in particular, so use BLOCK_POSITION
    locationArgument("block", LocationType.BLOCK_POSITION)
    playerExecutor { _, args ->
        (args["block"] as Location).block.type = Material.AIR
    }
}
/* ANCHOR_END: argumentLocations1 */
}

fun argument_lootTable() {
/* ANCHOR: argumentLootTable1 */
commandAPICommand("giveloottable") {
    lootTableArgument("loottable")
    locationArgument("location", LocationType.BLOCK_POSITION)
    anyExecutor { _, args ->
        val lootTable = args["loottable"] as LootTable
        val location = args["location"] as Location

        val state = location.block.state

        // Check if the input block is a container (e.g. chest)
        if (state is Container && state is Lootable) {
            // Apply the loot table to the chest
            state.lootTable = lootTable
            state.update()
        }
    }
}
/* ANCHOR_END: argumentLootTable1 */
}

fun argument_map() {
/* ANCHOR: argumentMap1 */
commandAPICommand("sendmessage") {
    // Parameter 'delimiter' is missing, delimiter will be a colon
    // Parameter 'separator' is missing, separator will be a space
    argument(MapArgumentBuilder<Player, String>("message")

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
    playerExecutor { _, args ->
        // The MapArgument returns a LinkedHashMap
        val map: LinkedHashMap<Player, String> = args["message"] as LinkedHashMap<Player, String>

        // Sending the messages to the players
        for (messageRecipient in map.keys) {
            messageRecipient.sendMessage(map[messageRecipient]!!)
        }
    }
}
/* ANCHOR_END: argumentMap1 */
}

fun argument_mathOperation() {
/* ANCHOR: argumentMathOperation1 */
commandAPICommand("changelevel") {
    playerArgument("player")
    mathOperationArgument("operation")
    integerArgument("value")
    anyExecutor { _, args ->
        val target = args["player"] as Player
        val op = args["operation"] as MathOperation
        val value = args["value"] as Int

        target.level = op.apply(target.level, value)
    }
}
/* ANCHOR_END: argumentMathOperation1 */
}

fun argument_multiLiteral() {
/* ANCHOR: argumentMultiLiteral1 */
commandAPICommand("gamemode") {
    multiLiteralArgument(nodeName = "gamemodes", "adventure", "creative", "spectator", "survival") // Adding this for now, needed because ambiguous methods exist
    playerExecutor { player, args ->
        // The literal string that the player enters IS available in the args[]
        when (args["gamemodes"] as String) {
            "adventure" -> player.gameMode = GameMode.ADVENTURE
            "creative" -> player.gameMode = GameMode.CREATIVE
            "spectator" -> player.gameMode = GameMode.SPECTATOR
            "survival" -> player.gameMode = GameMode.SURVIVAL
        }
    }
}
/* ANCHOR_END: argumentMultiLiteral1 */
}

fun argument_nbt() {
/* ANCHOR: argumentNBT1 */
commandAPICommand("award") {
    nbtCompoundArgument<NBTContainer>("nbt")
    anyExecutor { _, args ->
        val nbt = args["nbt"] as NBTContainer

        // Do something with "nbt" here...
    }
}
/* ANCHOR_END: argumentNBT1 */
}

fun argument_objectives() {
/* ANCHOR: argumentObjectives1 */
commandAPICommand("sidebar") {
    objectiveArgument("objective")
    anyExecutor { _, args ->
        val objective = args["objective"] as Objective

        // Set display slot
        objective.displaySlot = DisplaySlot.SIDEBAR
    }
}
/* ANCHOR_END: argumentObjectives1 */

/* ANCHOR: argumentObjectives2 */
commandAPICommand("unregisterall") {
    objectiveCriteriaArgument("objective criteria")
    anyExecutor { _, args ->
        val objectiveCriteria = args["objective criteria"] as String
        val objectives = Bukkit.getScoreboardManager().mainScoreboard.getObjectivesByCriteria(objectiveCriteria)

        // Unregister the objectives
        for (objective in objectives) {
            objective.unregister()
        }
    }
}
/* ANCHOR_END: argumentObjectives2 */
}

fun argument_particle() {
/* ANCHOR: argumentParticle1 */
commandAPICommand("showparticle") {
    particleArgument("particle")
    playerExecutor { player, args ->
        val particleData = args["particle"] as ParticleData<Any>
        player.world.spawnParticle(particleData.particle(), player.location, 1)
    }
}
/* ANCHOR_END: argumentParticle1 */

/* ANCHOR: argumentParticle2 */
commandAPICommand("showparticle") {
    particleArgument("particle")
    playerExecutor { player, args ->
        val particleData = args["particle"] as ParticleData<Any>
        player.world.spawnParticle(particleData.particle(), player.location, 1, particleData.data())
    }
}
/* ANCHOR_END: argumentParticle2 */
}

fun argument_potion() {
/* ANCHOR: argumentPotion1 */
commandAPICommand("potion") {
    playerArgument("target")
    potionEffectArgument("potion")
    timeArgument("duration")
    integerArgument("strength")
    anyExecutor { _, args ->
        val target = args["target"] as Player
        val potion = args["potion"] as PotionEffectType
        val duration = args["duration"] as Int
        val strength = args["strength"] as Int

        // Add the potion effect to the target player
        target.addPotionEffect(PotionEffect(potion, duration, strength))
    }
}
/* ANCHOR_END: argumentPotion1 */
/* ANCHOR: argumentPotion2 */
commandAPICommand("potion") {
    playerArgument("target")
    potionEffectArgument("potion", true)
    timeArgument("duration")
    integerArgument("strength")
    anyExecutor { _, args ->
        val target = args["target"] as Player
        val potionKey = args["potion"] as NamespacedKey
        val duration = args["duration"] as Int
        val strength = args["strength"] as Int

        val potion = PotionEffectType.getByKey(potionKey)!!

        // Add the potion effect to the target player
        target.addPotionEffect(PotionEffect(potion, duration, strength))
    }
}
/* ANCHOR_END: argumentPotion2 */
}

fun argument_primitives() {
/* ANCHOR: argumentPrimitives1 */
// Load keys from config file
val configKeys: Array<String> = getConfig().getKeys(true).toTypedArray()

// Register our command
commandAPICommand("editconfig") {
    argument(TextArgument("config-key").replaceSuggestions(ArgumentSuggestions.strings { configKeys }))
    booleanArgument("value")
    anyExecutor { _, args ->
        // Update the config with the boolean argument
        getConfig().set(args["config-key"] as String, args["value"] as Boolean)
    }
}
/* ANCHOR_END: argumentPrimitives1 */
}

fun argument_range() {
/* ANCHOR: argumentRange1 */
commandAPICommand("searchrange") {
    integerRangeArgument("range") // Range argument
    itemStackArgument("item") // The item to search for
    playerExecutor { player, args ->
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
    }
}
/* ANCHOR_END: argumentRange1 */
}

fun argument_recipe() {
/* ANCHOR: argumentRecipe1 */
commandAPICommand("giverecipe") {
    recipeArgument("recipe")
    playerExecutor { player, args ->
        val recipe = args["recipe"] as ComplexRecipe
        player.inventory.addItem(recipe.result)
    }
}
/* ANCHOR_END: argumentRecipe1 */

/* ANCHOR: argumentRecipe2 */
commandAPICommand("unlockrecipe") {
    playerArgument("player")
    recipeArgument("recipe")
    anyExecutor { _, args ->
        val target = args["player"] as Player
        val recipe = args["recipe"] as ComplexRecipe

        target.discoverRecipe(recipe.key)
    }
}
/* ANCHOR_END: argumentRecipe2 */
}

fun argument_rotation() {
/* ANCHOR: argumentRotation1 */
commandAPICommand("rotate") {
    rotationArgument("rotation")
    entitySelectorArgumentOneEntity("target")
    anyExecutor { _, args ->
        val rotation = args["rotation"] as Rotation
        val target = args["target"] as Entity

        if (target is ArmorStand) {
            target.headPose = EulerAngle(Math.toRadians(rotation.pitch.toDouble()), Math.toRadians(rotation.yaw.toDouble() - 90), 0.0)
        }
    }
}
/* ANCHOR_END: argumentRotation1 */
}

fun argument_scoreboards() {
/* ANCHOR: argumentScoreboards1 */
commandAPICommand("reward") {
    // We want multiple players, so we use the scoreHolderArgumentMultiple method
    scoreHolderArgumentMultiple("player")
    anyExecutor { _, args ->
        // Get player names by casting to Collection<String>
        val players = args["player"] as Collection<String>

        for (playerName in players) {
            Bukkit.getPlayer(playerName)?.inventory!!.addItem(ItemStack(Material.DIAMOND, 3))
        }
    }
}
/* ANCHOR_END: argumentScoreboards1 */

/* ANCHOR: argumentScoreboards2 */
commandAPICommand("clearobjectives") {
    scoreboardSlotArgument("slot")
    anyExecutor { _, args ->
        val scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        val slot = (args["slot"] as ScoreboardSlot).displaySlot
        scoreboard.clearSlot(slot)
    }
}
/* ANCHOR_END: argumentScoreboards2 */
}

fun argument_sound() {
/* ANCHOR: argumentSound1 */
commandAPICommand("sound") {
    soundArgument("sound")
    playerExecutor { player, args ->
        player.world.playSound(player.location, args["sound"] as Sound, 100.0f, 1.0f)
    }
}
/* ANCHOR_END: argumentSound1 */

/* ANCHOR: argumentSound2 */
commandAPICommand("sound") {
    soundArgument("sound", true)
    playerExecutor { player, args ->
        player.world.playSound(player.location, (args["sound"] as NamespacedKey).asString(), 100.0f, 1.0f)
    }
}
/* ANCHOR_END: argumentSound2 */
}

fun argument_strings() {
/* ANCHOR: argumentStrings1 */
commandAPICommand("message") {
    playerArgument("target")
    greedyStringArgument("message")
    anyExecutor { _, args ->
        (args["target"] as Player).sendMessage(args["message"] as String)
    }
}
/* ANCHOR_END: argumentStrings1 */
}

fun argument_team() {
/* ANCHOR: argumentTeam1 */
commandAPICommand("togglepvp") {
    teamArgument("team")
    anyExecutor { _, args ->
        val team = args["team"] as Team

        // Toggle pvp
        team.setAllowFriendlyFire(team.allowFriendlyFire())
    }
}
/* ANCHOR_END: argumentTeam1 */
}

fun argument_time() {
/* ANCHOR: argumentTime1 */
commandAPICommand("bigmsg") {
    timeArgument("duration")
    greedyStringArgument("message")
    anyExecutor { _, args ->
        // Duration in ticks
        val duration = args["duration"] as Int
        val message = args["message"] as String

        for (player in Bukkit.getOnlinePlayers()) {
            // Display the message to all players, with the default fade in/out times (10 and 20).
            player.sendTitle(message, "", 10, duration, 20)
        }
    }
}
/* ANCHOR_END: argumentTime1 */
}

fun argument_world() {
/* ANCHOR: argumentWorld1 */
commandAPICommand("unloadworld") {
    worldArgument("world")
    anyExecutor { sender, args ->
        val world = args["world"] as World

        // Unload the world (and save the world's chunks)
        Bukkit.getServer().unloadWorld(world, true)
    }
}
/* ANCHOR_END: argumentWorld1 */
}

fun arguments() {
/* ANCHOR: arguments1 */
commandAPICommand("mycommand") {
    stringArgument("arg0")
    stringArgument("arg1")
    stringArgument("arg2")
    // And so on
}
/* ANCHOR_END: arguments1 */


/* ANCHOR: arguments2 */
commandAPICommand("mycommand") {
    arguments(StringArgument("arg0"), StringArgument("arg1"), StringArgument("arg2"))
    // And so on
}
/* ANCHOR_END: arguments2 */


/* ANCHOR: arguments3 */
val arguments = listOf(
    StringArgument("arg0"),
    StringArgument("arg1"),
    StringArgument("arg2")
)

commandAPICommand("mycommand") {
    arguments(*arguments.toTypedArray())
    // And so on
}
/* ANCHOR_END: arguments3 */

/* ANCHOR: arguments4 */
val args = listOf(
    StringArgument("arg0"),
    PotionEffectArgument("arg1"),
    LocationArgument("arg2")
)

commandAPICommand("cmd") {
    arguments(*args.toTypedArray())
    anyExecutor { _, args ->
        val stringArg = args["arg0"] as String
        val potionArg = args["arg1"] as PotionEffectType
        val locationArg = args["arg2"] as Location
    }
}
/* ANCHOR_END: arguments4 */
}

fun delegatedProperties() {
/* ANCHOR: delegatedProperties1 */
commandAPICommand("mycommand") {
    stringArgument("string")
    playerArgument("target")
    playerExecutor { player, args ->
        val string: String by args
        val target: Player by args
        // Implementation...
    }
}
/* ANCHOR_END: delegatedProperties1 */
}

fun help() {
/* ANCHOR: help1 */
commandAPICommand("mycmd") {
    withShortDescription("Says hi")
    withFullDescription("Broadcasts hi to everyone on the server")
    anyExecutor { _, _ ->
        Bukkit.broadcastMessage("Hi!")
    }
}
/* ANCHOR_END: help1 */

/* ANCHOR: help2 */
commandAPICommand("mycmd") {
    withHelp("Says hi", "Broadcasts hi to everyone on the server")
    anyExecutor { _, _ ->
        Bukkit.broadcastMessage("Hi!")
    }
}

/* ANCHOR_END: help2 */
}

fun kotlindsl() {
/* ANCHOR: kotlindsl1 */
commandTree("sendmessageto") {
    playerArgument("player") { // Defines a new PlayerArgument("player")
        greedyStringArgument("msg") { // Defines a new GreedyStringArgument("msg")
            anyExecutor { _, args -> // Command can be executed by anyone and anything (such as entities, the console, etc.)
                val player: Player = args["player"] as Player
                val message: String = args["msg"] as String
                player.sendMessage(message)
            }
        }
    }
}
/* ANCHOR_END: kotlindsl1 */

/* ANCHOR: kotlindsl2 */
commandAPICommand("sendmessageto") {
    playerArgument("player") // Defines a new PlayerArgument("player")
    greedyStringArgument("msg") // Defines a new GreedyStringArgument("msg")
    anyExecutor { _, args -> // Command can be executed by anyone and anything (such as entities, the console, etc.)
        val player: Player = args["player"] as Player
        val message: String = args["msg"] as String
        player.sendMessage(message)
    }
}
/* ANCHOR_END: kotlindsl2 */

/* ANCHOR: kotlindsl3 */
commandTree("sendMessageTo") {
    playerArgument("player") {
        greedyStringArgument("msg") {
            playerExecutor { _, args ->
                val player: Player = args["player"] as Player
                val message: String = args["msg"] as String
                player.sendMessage(message)
            }
        }
    }
    literalArgument("broadcast") {
        withRequirement { sender: CommandSender -> sender.isOp } // Applies the requirement to the broadcast literal argument
        /* add more methods here that modify argument behaviour */
        greedyStringArgument("msg") {
            playerExecutor { _, args ->
                val message: String = args["msg"] as String
                Bukkit.broadcastMessage(message)
            }
        }
    }
}
/* ANCHOR_END: kotlindsl3 */

/* ANCHOR: kotlindsl4 */
commandAPICommand("sendMessageTo") {
    playerArgument("player")
    greedyStringArgument("msg")
    playerExecutor { _, args ->
        val player: Player = args["player"] as Player
        val message: String = args["msg"] as String
        player.sendMessage(message)
    }
}

commandAPICommand("sendMessageTo") {
    literalArgument("broadcast") {
        withRequirement { sender: CommandSender -> sender.isOp } // Applies the requirement to the broadcast literal argument
        /* add more methods here that modify argument behaviour */
    }
    greedyStringArgument("msg")
    playerExecutor { _, args ->
        val message: String = args["msg"] as String
        Bukkit.broadcastMessage(message)
    }
}
/* ANCHOR_END: kotlindsl4 */

/* ANCHOR: kotlindsl5 */
commandTree("commandRequirement") {
    withRequirement { sender: CommandSender -> sender.isOp}
    playerExecutor { player, _ ->
        player.sendMessage("This command can only be executed by players who are server operators.")
    }
}
/* ANCHOR_END: kotlindsl5 */

/* ANCHOR: kotlindsl6 */
commandAPICommand("commandRequirement") {
    withRequirement { sender: CommandSender -> sender.isOp}
    playerExecutor { player, _ ->
        player.sendMessage("This command can only be executed by players who are server operators.")
    }
}
/* ANCHOR_END: kotlindsl6 */

/* ANCHOR: kotlindsl7 */
commandTree("optionalArgument") {
    literalArgument("give") {
        itemStackArgument("item") {
            integerArgument("amount", optional = true) {
                playerExecutor { player, args ->
                    // This command will let you execute:
                    // "/optionalArgument give minecraft:stick"
                    // "/optionalArgument give minecraft:stick 5"
                    val itemStack: ItemStack = args["item"] as ItemStack
                    val amount: Int = args.getOptional("amount").orElse(1) as Int
                    itemStack.amount = amount
                    player.inventory.addItem(itemStack)
                }
            }
        }
    }
}
/* ANCHOR_END: kotlindsl7 */

/* ANCHOR: kotlindsl8 */
commandAPICommand("optionalArgument") {
    literalArgument("give")
    itemStackArgument("item")
    integerArgument("amount", optional = true) // This sets the argument as optional
    playerExecutor { player, args ->
        // This command will let you execute:
        // "/optionalArgument give minecraft:stick"
        // "/optionalArgument give minecraft:stick 5"
        val itemStack: ItemStack = args["item"] as ItemStack
        val amount: Int = args.getOptional("amount").orElse(1) as Int
        itemStack.amount = amount
        player.inventory.addItem(itemStack)
    }
}
/* ANCHOR_END: kotlindsl8 */

/* ANCHOR: kotlindsl9 */
commandTree("replaceSuggestions") {
    stringArgument("strings") {
        replaceSuggestions(ArgumentSuggestions.strings("one", "two", "three")) // Replaces the suggestions for the "strings" StringArgument
        playerExecutor { player, args ->
            player.sendMessage("You chose option ${args["strings"] as String}!")
        }
    }
}
/* ANCHOR_END: kotlindsl9 */

/* ANCHOR: kotlindsl10 */
commandAPICommand("replaceSuggestions") {
    stringArgument("strings") {
        replaceSuggestions(ArgumentSuggestions.strings("one", "two", "three")) // Replaces the suggestions for the "strings" StringArgument
    }
    playerExecutor { player, args ->
        player.sendMessage("You chose option ${args["strings"] as String}!")
    }
}
/* ANCHOR_END: kotlindsl10 */
}

fun native() {
/* ANCHOR: native1 */
commandAPICommand("break") {
    nativeExecutor { sender, _ ->
        val location = sender.location
        location.block.breakNaturally()
    }
}
/* ANCHOR_END: native1 */
}

fun optional_arguments() {
/* ANCHOR: optionalArguments1 */
commandAPICommand("sayhi") {
    playerArgument("target", optional = true)
    playerExecutor { player, args ->
        val target: Player? = args["target"] as Player?
        if (target != null) {
            target.sendMessage("Hi!")
        } else {
            player.sendMessage("Hi!")
        }
    }
}
/* ANCHOR_END: optionalArguments1 */

/* ANCHOR: optionalArguments2 */
commandAPICommand("sayhi") {
    playerArgument("target", optional = true)
    playerExecutor { player, args ->
        val target: Player = args.getOptional("target").orElse(player) as Player
        target.sendMessage("Hi!")
    }
}
/* ANCHOR_END: optionalArguments2 */

/* ANCHOR: optionalArguments3 */
commandAPICommand("rate") {
    argument(StringArgument("topic").setOptional(true).combineWith(IntegerArgument("rating", 0, 10)))
    playerArgument("target", optional = true)
    anyExecutor { sender, args ->
        val topic: String? = args["topic"] as String?
        if (topic == null) {
            sender.sendMessage(
                "Usage: /rate <topic> <rating> <player>(optional)",
                "Select a topic to rate, then give a rating between 0 and 10",
                "You can optionally add a player at the end to give the rating to"
            )
            return@anyExecutor
        }

        // We know this is not null because rating is required if topic is given
        val rating = args["rating"] as Int

        // The target player is optional, so give it a default here
        val target: CommandSender = args.getOptional("target").orElse(sender) as CommandSender

        target.sendMessage("Your $topic was rated: $rating/10")
    }
}
/* ANCHOR_END: optionalArguments3 */
}

fun proxysender() {
/* ANCHOR: proxySender1 */
commandAPICommand("killme") {
    playerExecutor { player, _ ->
        player.health = 0.0
    }
}
/* ANCHOR_END: proxySender1 */

/* ANCHOR: proxySender2 */
commandAPICommand("killme") {
    playerExecutor { player, _ ->
        player.health = 0.0
    }
    proxyExecutor { proxy, _ ->
        // Check if the callee (target) is an Entity and kill it
        if (proxy.callee is LivingEntity) {
            (proxy.callee as LivingEntity).health = 0.0
        }
    }
}
/* ANCHOR_END: proxySender2 */
}

fun resultingcommandexecutor() {
/* ANCHOR: resultingCommandExecutors1 */
commandAPICommand("randnum") {
    anyResultingExecutor { _, _ ->
        Random.nextInt()
    }
}
/* ANCHOR_END: resultingCommandExecutors1 */

/* ANCHOR: resultingCommandExecutors2 */
// Register random number generator command from 1 to 99 (inclusive)
commandAPICommand("randomnumber") {
    anyResultingExecutor { _, _ ->
        (1..100).random()
    }
}
/* ANCHOR_END: resultingCommandExecutors2 */

/* ANCHOR: resultingCommandExecutors3 */
// Register reward giving system for a target player
commandAPICommand("givereward") {
    entitySelectorArgumentOnePlayer("target")
    anyExecutor { _, args ->
        val player = args["target"] as Player
        player.inventory.addItem(ItemStack(Material.DIAMOND, 64))
        Bukkit.broadcastMessage("${player.name} won a rare 64 diamonds from a loot box!")
    }
}
/* ANCHOR_END: resultingCommandExecutors3 */
}

@Suppress("UNUSED_PARAMETER")
fun subcommands() {

/* ANCHOR: subcommands1 */
val groupAdd = subcommand("add") {
    stringArgument("permission")
    stringArgument("groupName")
    anyExecutor { sender, args ->
        // perm group add code
    }
}
/* ANCHOR_END: subcommands1 */

/* ANCHOR: subcommands2 */
val groupRemove = subcommand("remove") {
    stringArgument("permission")
    stringArgument("groupName")
    anyExecutor { sender, args ->
        // perm group remove code
    }
}

val group = subcommand("group") {
    subcommand(groupAdd)
    subcommand(groupRemove)
}
/* ANCHOR_END: subcommands2 */

/* ANCHOR: subcommands3 */
commandAPICommand("perm") {
    subcommand(group)
}
/* ANCHOR_END: subcommands3 */

/* ANCHOR: subcommands4 */
commandAPICommand("perm") {
    subcommand("group") {
        subcommand("add") {
            stringArgument("permission")
            stringArgument("groupName")
            anyExecutor { sender, args ->
                // perm group add code
            }
        }
        subcommand("remove") {
            stringArgument("permission")
            stringArgument("groupName")
            anyExecutor { sender, args ->
                // perm group remove code
            }
        }
    }
    subcommand("user") {
        subcommand("add") {
            stringArgument("permission")
            stringArgument("userName")
            anyExecutor { sender, args ->
                // perm user add code
            }
        }
        subcommand("remove") {
            stringArgument("permission")
            stringArgument("userName")
            anyExecutor { sender, args ->
                // perm user add code
            }
        }
    }
}
/* ANCHOR_END: subcommands4 */
}

// TODO: This example isn't used in the documentation!
fun treeexample() {
commandTree("teeexample") {
    // Set the aliases as you normally would
    withAliases("treealias")
    // Set an executor on the command itself
    anyExecutor { sender, _ ->
        sender.sendMessage("Root with no arguments")
    }
    // Create a new branch starting with a the literal 'integer'
    literalArgument("integer") {
        // Execute on the literal itself
        anyExecutor { sender, _ ->
            sender.sendMessage("Integer Branch with no arguments")
        }
        // Create a further branch starting with an integer argument, which executes a command
        integerArgument("integer") {
            anyExecutor { sender, args ->
                sender.sendMessage("Integer Branch with integer argument: ${args[0]}")
            }
        }
    }
    literalArgument("biome") {
        anyExecutor { sender, _ ->
            sender.sendMessage("Biome Branch with no arguments")
        }
        biomeArgument("biome") {
            anyExecutor { sender, args ->
                sender.sendMessage("Biome Branch with biome argument: ${args[0]}")
            }
        }
    }
    literalArgument("string") {
        anyExecutor { sender, _ ->
            sender.sendMessage("String Branch with no arguments")
        }
        stringArgument("string") {
            anyExecutor { sender, args ->
                sender.sendMessage("String Branch with string argument: ${args[0]}")
            }
        }
    }
}

}
}