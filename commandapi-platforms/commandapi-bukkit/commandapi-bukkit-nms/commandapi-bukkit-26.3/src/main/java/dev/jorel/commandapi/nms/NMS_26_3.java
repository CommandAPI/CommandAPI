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
package dev.jorel.commandapi.nms;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.DynamicOps;
import dev.jorel.commandapi.preprocessor.Differs;
import dev.jorel.commandapi.preprocessor.NMSMeta;
import dev.jorel.commandapi.wrappers.DoubleRange;
import dev.jorel.commandapi.wrappers.IntegerRange;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.RangeArgument;
import net.minecraft.commands.arguments.TeamColorArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * NMS implementation for Minecraft 26.3
 */
@NMSMeta(compatibleWith = {"26.3"})
public class NMS_26_3 extends NMS_26_Common {

	private final BiFunction<ItemInput, HolderLookup.Provider, String> serializeComponents;

	public NMS_26_3(Supplier<CommandBuildContext> commandBuildContext, BiFunction<ItemInput, HolderLookup.Provider, String> serializeComponents) {
		super(commandBuildContext);
		this.serializeComponents = serializeComponents;
	}

	@Override
	public String[] compatibleVersions() {
		return new String[]{"26.3"};
	}

	String serializeComponents(ItemInput itemInput, HolderLookup.Provider provider) {
		return this.serializeComponents.apply(itemInput, provider);
	}

	// It looks like there is now also a "HexColorArgument"
	@Differs(from = "26.1", by = "ColorArgument -> TeamColorArgument")
	@Override
	public final ArgumentType<?> _ArgumentChatFormat() {
		return TeamColorArgument.teamColor();
	}

	@Differs(from = "26.1", by = "net.minecraft.advancements.criterion.MinMaxBounds -> net.minecraft.advancements.predicates.MinMaxBounds")
	@Override
	public DoubleRange getDoubleRange(CommandContext<CommandSourceStack> cmdCtx, String key) {
		MinMaxBounds.Doubles range = RangeArgument.Floats.getRange(cmdCtx, key);
		final Double lowBoxed = range.min().orElse(null);
		final Double highBoxed = range.max().orElse(null);
		final double low = lowBoxed == null ? -Double.MAX_VALUE : lowBoxed;
		final double high = highBoxed == null ? Double.MAX_VALUE : highBoxed;
		return new DoubleRange(low, high);
	}

	@Differs(from = "26.1", by = "net.minecraft.advancements.criterion.MinMaxBounds -> net.minecraft.advancements.predicates.MinMaxBounds")
	@Override
	public IntegerRange getIntRange(CommandContext<CommandSourceStack> cmdCtx, String key) {
		MinMaxBounds.Ints range = RangeArgument.Ints.getRange(cmdCtx, key);
		final Integer lowBoxed = range.min().orElse(null);
		final Integer highBoxed = range.max().orElse(null);
		final int low = lowBoxed == null ? Integer.MIN_VALUE : lowBoxed;
		final int high = highBoxed == null ? Integer.MAX_VALUE : highBoxed;
		return new IntegerRange(low, high);
	}
}
