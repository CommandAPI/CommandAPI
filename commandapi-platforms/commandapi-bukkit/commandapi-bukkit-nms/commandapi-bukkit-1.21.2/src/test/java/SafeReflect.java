/*******************************************************************************
 * Copyright 2024 Jorel Ali (Skepter) - MIT License
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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Either;
import dev.jorel.commandapi.preprocessor.RequireField;
import net.minecraft.commands.arguments.blocks.ArgumentTileLocation;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.CustomFunctionManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import org.bukkit.craftbukkit.v1_21_R2.help.SimpleHelpMap;

import java.util.Map;

// Spigot-Mapped reflection
@RequireField(in = CustomFunctionManager.class, name = "h", ofType = CommandDispatcher.class)
@RequireField(in = EntitySelector.class, name = "p", ofType = boolean.class)
@RequireField(in = SimpleHelpMap.class, name = "helpTopics", ofType = Map.class)
@RequireField(in = EntityPositionSource.class, name = "e", ofType = Either.class)
@RequireField(in = MinecraftServer.class, name = "aE", ofType = FuelValues.class)
@RequireField(in = ArgumentTileLocation.class, name = "c", ofType = NBTTagCompound.class)
public class SafeReflect {}