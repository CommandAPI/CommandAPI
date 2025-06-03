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
import dev.jorel.commandapi.CommandAPISpigot;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.profile.PlayerProfile;

import java.util.List;

/**
 * An argument that represents the Paper PlayerProfile object.
 * <p>
 * Note that this Argument internally references Mojang's authentication servers to resolve
 * usernames, which gives it a slight performance overhead compared to {@link dev.jorel.commandapi.arguments.EntitySelectorArgument.OnePlayer}.
 * 
 * @since 1.1
 */
public class PlayerProfileArgument extends SafeOverrideableArgument<List, PlayerProfile> {

	/**
	 * A Player argument. Produces a single player, regardless of whether
	 * <code>@a</code>, <code>@p</code>, <code>@r</code> or <code>@e</code> is used.
	 * 
	 * @param nodeName the name of the node for this argument
	 */
	public PlayerProfileArgument(String nodeName) {
		super(nodeName, CommandAPIBukkit.get().getNMS()._ArgumentProfile(), PlayerProfile::getName);
	}

	@Override
	public Class<List> getPrimitiveType() {
		return List.class;
	}
	
	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.PLAYER;
	}
	
	@Override
	public <CommandSourceStack> List<PlayerProfile> parseArgument(CommandContext<CommandSourceStack> cmdCtx, String key, CommandArguments previousArgs) throws CommandSyntaxException {
		return CommandAPISpigot.<CommandSourceStack>getSpigot().getProfile(cmdCtx, key);
	}
}
