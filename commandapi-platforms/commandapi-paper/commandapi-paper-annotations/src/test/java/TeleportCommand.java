/*******************************************************************************
 * Copyright 2018, 2020 Jorel Ali (Skepter) - MIT License
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
import com.destroystokyo.paper.profile.PlayerProfile;
import dev.jorel.commandapi.annotations.arguments.APlayerProfileArgument;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Subcommand;

import java.util.List;

/* ANCHOR: teleport_command */
@Command("teleport")    
@Alias({"tp", "tele"})
public class TeleportCommand {
/* ANCHOR_END: teleport_command */
    
/* ANCHOR: teleport_subcommand */
@Subcommand({"teleport", "tp"})
public static void teleport(Player player, @APlayerProfileArgument List<PlayerProfile> target) {
	for (PlayerProfile profile : target) {
		if (profile.getId() == null) {
			continue;
		}
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(profile.getId());
		if (offlinePlayer.isOnline() && offlinePlayer instanceof Player onlineTarget) {
			player.teleport(onlineTarget);
		}
	}
}
/* ANCHOR_END: teleport_subcommand */

}