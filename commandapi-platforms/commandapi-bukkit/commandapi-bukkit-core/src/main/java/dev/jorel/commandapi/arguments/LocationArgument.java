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
package dev.jorel.commandapi.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.Location;

/**
 * An argument that represents the Bukkit {@link Location} object
 * 
 * @since 1.1
 * @apiNote Returns a {@link Location} object
 */
public class LocationArgument extends SafeOverrideableArgument<Location, Location> {
	
	/**
	 * A Location argument. Represents Minecraft locations. Defaults to {@link LocationType#PRECISE_POSITION}
	 * @param nodeName the name of the node for this argument
	 */
	public LocationArgument(String nodeName) {
		this(nodeName, LocationType.PRECISE_POSITION);
	}
	
	/**
	 * A Location argument. Represents Minecraft locations
	 * @param nodeName the name of the node for this argument
	 * @param type the location type of this location, either {@link LocationType#BLOCK_POSITION} or {@link LocationType#PRECISE_POSITION}
	 */
	public LocationArgument(String nodeName, LocationType type) {
		this(nodeName, type, true);
	}
	
	/**
	 * A Location argument. Represents Minecraft locations
	 * @param nodeName the name of the node for this argument
	 * @param type the location type of this location, either {@link LocationType#BLOCK_POSITION} or {@link LocationType#PRECISE_POSITION}
	 * @param centerPosition whether LocationType.PRECISE_POSITION should center the position of the location within a block
	 */
	public LocationArgument(String nodeName, LocationType type, boolean centerPosition) {
		super(nodeName, type == LocationType.BLOCK_POSITION ? CommandAPIBukkit.get().getNMS()._ArgumentPosition()
				: CommandAPIBukkit.get().getNMS()._ArgumentVec3(centerPosition),
				type == LocationType.BLOCK_POSITION
						? (Location l) -> l.getBlockX() + " " + l.getBlockY() + " " + l.getBlockZ()
						: (Location l) -> l.getX() + " " + l.getY() + " " + l.getZ());
		isPrecise = type == LocationType.PRECISE_POSITION;
	}
	
	private final boolean isPrecise;

	/**
	 * Returns whether this argument is {@link LocationType#BLOCK_POSITION} or {@link LocationType#PRECISE_POSITION}
	 * @return the location type of this argument
	 */
	public LocationType getLocationType() {
		return isPrecise ? LocationType.PRECISE_POSITION : LocationType.BLOCK_POSITION;
	}
	
	@Override
	public Class<Location> getPrimitiveType() {
		return Location.class;
	}
	
	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.LOCATION;
	}
	
	@Override
	public <CommandSourceStack> Location parseArgument(CommandContext<CommandSourceStack> cmdCtx, String key, CommandArguments previousArgs) throws CommandSyntaxException {
		return isPrecise ?
			CommandAPIBukkit.<CommandSourceStack>get().getNMS().getLocationPrecise(cmdCtx, key) :
			CommandAPIBukkit.<CommandSourceStack>get().getNMS().getLocationBlock(cmdCtx, key);
	}
}
