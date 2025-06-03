package dev.jorel.commandapi.examples.java;

import de.tr7zw.changeme.nbtapi.NBTContainer;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandAPISpigot;
import dev.jorel.commandapi.CommandAPISpigotConfig;
import dev.jorel.commandapi.arguments.ChatArgument;
import dev.jorel.commandapi.arguments.ChatColorArgument;
import dev.jorel.commandapi.arguments.ChatComponentArgument;
import dev.jorel.commandapi.arguments.PlayerProfileArgument;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Examples {

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
    .withArguments(new PlayerProfileArgument("player"))
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

class argument_nbt extends JavaPlugin {
/* ANCHOR: argumentNBT1 */
@Override
public void onLoad() {
    CommandAPI.onLoad(new CommandAPISpigotConfig(this)
        .initializeNBTAPI(NBTContainer.class, NBTContainer::new)
    );
}
/* ANCHOR_END: argumentNBT1 */
}

class setupShading {
JavaPlugin plugin = new JavaPlugin() {
};

{
/* ANCHOR: setupShading1 */
CommandAPI.onLoad(new CommandAPISpigotConfig(plugin).silentLogs(true));
/* ANCHOR_END: setupShading1 */
}

/* ANCHOR: setupShading2 */
class MyPlugin extends JavaPlugin {

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPISpigotConfig(this).verboseOutput(true)); // Load with verbose output

        new CommandAPICommand("ping")
            .executes((sender, args) -> {
                sender.sendMessage("pong!");
            })
            .register();
    }

    @Override
    public void onEnable() {
        CommandAPISpigot.onEnable();

        // Register commands, listeners etc.
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
    }

}
/* ANCHOR_END: setupShading2 */
}

}
