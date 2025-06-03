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
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.AStringArgument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;

/* ANCHOR: warps */
/* ANCHOR: warps_command */
@Command("warp")    
public class WarpCommand {
/* ANCHOR_END: warps_command */
    
    // List of warp names and their locations
    static Map<String, Location> warps = new HashMap<>();
    
    @Default
    public static void warp(CommandSender sender) {
        sender.sendMessage("--- Warp help ---");
        sender.sendMessage("/warp - Show this help");
        sender.sendMessage("/warp <warp> - Teleport to <warp>");
        sender.sendMessage("/warp create <warpname> - Creates a warp at your current location");
    }
    
    @Default
    public static void warp(Player player, @AStringArgument String warpName) {
        player.teleport(warps.get(warpName));
    }
    
    @Subcommand("create")
    @Permission("warps.create")
    public static void createWarp(Player player, @AStringArgument String warpName) {
        warps.put(warpName, player.getLocation());
    }
    
}
/* ANCHOR_END: warps */

class  A {
    { 
/* ANCHOR: warps_register */
CommandAPI.registerCommand(WarpCommand.class);
/* ANCHOR_END: warps_register */
    }
    
    static Map<String, Location> warps = new HashMap<>();
    
/* ANCHOR: warps_help */
@Default
public static void warp(CommandSender sender) {
    sender.sendMessage("--- Warp help ---");
    sender.sendMessage("/warp - Show this help");
    sender.sendMessage("/warp <warp> - Teleport to <warp>");
    sender.sendMessage("/warp create <warpname> - Creates a warp at your current location");
}
/* ANCHOR_END: warps_help */
    
/* ANCHOR: warps_warp */
@Default
public static void warp(Player player, @AStringArgument String warpName) {
    player.teleport(warps.get(warpName));
}
/* ANCHOR_END: warps_warp */
    
/* ANCHOR: warps_create */
@Subcommand("create")
@Permission("warps.create")
public static void createWarp(Player player, @AStringArgument String warpName) {
    warps.put(warpName, player.getLocation());
}
/* ANCHOR_END: warps_create */

}

class Examples {
{
/* ANCHOR: old_warps */
Map<String, Location> warps = new HashMap<>();

// /warp
new CommandAPICommand("warp")
    .executes((sender, args) -> {
        sender.sendMessage("--- Warp help ---");
        sender.sendMessage("/warp - Show this help");
        sender.sendMessage("/warp <warp> - Teleport to <warp>");
        sender.sendMessage("/warp create <warpname> - Creates a warp at your current location");
    })
    .register();

// /warp <warp>
new CommandAPICommand("warp")
    .withArguments(new StringArgument("warp").replaceSuggestions(ArgumentSuggestions.strings(info -> 
        warps.keySet().toArray(new String[0])
    )))
    .executesPlayer((player, args) -> {
        player.teleport(warps.get((String) args.get(0)));
    })
    .register();

// /warp create <warpname>
new CommandAPICommand("warp")
    .withSubcommand(
        new CommandAPICommand("create")
            .withPermission("warps.create")
            .withArguments(new StringArgument("warpname"))
            .executesPlayer((player, args) -> {
                warps.put((String) args.get(0), player.getLocation());
            })
    )
    .register();
/* ANCHOR_END: old_warps */
}

/* ANCHOR: warp_register2 */
class MyPlugin extends JavaPlugin {
    
    @Override
    public void onLoad() {
        CommandAPI.registerCommand(WarpCommand.class);
    }
    
}
/* ANCHOR_END: warp_register2 */
    
}