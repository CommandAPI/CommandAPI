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
import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Help;
import dev.jorel.commandapi.annotations.NeedsOp;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.arguments.ADoubleArgument;
import dev.jorel.commandapi.annotations.arguments.AEntitySelectorArgument;
import dev.jorel.commandapi.annotations.arguments.AFloatArgument;
import dev.jorel.commandapi.annotations.arguments.AIntegerArgument;
import dev.jorel.commandapi.annotations.arguments.ALiteralArgument;
import dev.jorel.commandapi.annotations.arguments.ALocation2DArgument;
import dev.jorel.commandapi.annotations.arguments.ALocationArgument;
import dev.jorel.commandapi.annotations.arguments.ALongArgument;
import dev.jorel.commandapi.annotations.arguments.AMultiLiteralArgument;
import dev.jorel.commandapi.annotations.arguments.AScoreHolderArgument;
import dev.jorel.commandapi.arguments.LocationType;

/* ANCHOR: teleport_command */
@Command("teleport")    
@Alias({"tp", "tele"})
public class Test2Command {
/* ANCHOR_END: teleport_command */
    
    {
        {
/* ANCHOR: teleport_command_needsop */
@Command("teleport")    
@NeedsOp
class TeleportCommand {
/* ANCHOR_END: teleport_command_needsop */
}
        }
        {
/* ANCHOR: teleport_command_perms */
@Command("teleport")    
@Permission("myplugin.tp")
class TeleportCommand {
/* ANCHOR_END: teleport_command_perms */
}
        }
    }
    
    {
/* ANCHOR: teleport_help */
@Command("teleport")
@Help("Teleports yourself to another location")
class TeleportCommand {
/* ANCHOR_END: teleport_help */
}
    }
    {
/* ANCHOR: teleport_full_help */
@Command("teleport")    
@Help(value = "Teleports yourself to another location", shortDescription = "TP to a location")
class TeleportCommand {
/* ANCHOR_END: teleport_full_help */
}
    }
}

@Command("aa")
class AA {
/* ANCHOR: number_arguments */
@Default
public static void command(CommandSender sender, 
    @ADoubleArgument(min = 0.0, max = 10.0) double someDouble,
    @AFloatArgument(min = 5.0f, max = 10.0f) float someFloat,
    @AIntegerArgument(max = 100) int someInt,
    @ALongArgument(min = -10) long someLong
) {
    // Command implementation here
}
/* ANCHOR_END: number_arguments */

/* ANCHOR: literal_arguments */
@Default
public static void command(CommandSender sender, 
    @ALiteralArgument("myliteral") String literal,
    @AMultiLiteralArgument({"literal", "anotherliteral"}) String multipleLiterals
) {
    // Command implementation here
}
/* ANCHOR_END: literal_arguments */


// TODO: EntitySelectorArgument and ScoreHolder argument have changed - these need updating in the documentation
/* ANCHOR: other_arguments */
@Default
public static void command(CommandSender sender, 
    @ALocationArgument(LocationType.BLOCK_POSITION) Location location,
    @ALocation2DArgument(LocationType.PRECISE_POSITION) Location location2d,
    @AEntitySelectorArgument.ManyEntities Collection<Entity> entities,
    @AScoreHolderArgument.Multiple Collection<String> scoreHolders
) {
    // Command implementation here
}
/* ANCHOR_END: other_arguments */

}