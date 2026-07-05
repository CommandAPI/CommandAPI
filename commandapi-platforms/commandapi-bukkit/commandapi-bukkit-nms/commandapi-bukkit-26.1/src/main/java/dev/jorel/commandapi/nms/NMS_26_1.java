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
import dev.jorel.commandapi.preprocessor.NMSMeta;
import dev.jorel.commandapi.wrappers.DoubleRange;
import dev.jorel.commandapi.wrappers.IntegerRange;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.RangeArgument;

import java.util.function.Supplier;

/**
 * NMS implementation for Minecraft 26.1
 */
@NMSMeta(compatibleWith = {"26.1"})
public class NMS_26_1 extends NMS_26_Common {
	public NMS_26_1(Supplier<CommandBuildContext> commandBuildContext) {
		super(commandBuildContext);
	}

	@Override
	public String[] compatibleVersions() {
		return new String[]{"26.1"};
	}

	@Override
	public final ArgumentType<?> _ArgumentChatFormat() {
		return ColorArgument.color();
	}

	@Override
	public DoubleRange getDoubleRange(CommandContext<CommandSourceStack> cmdCtx, String key) {
		MinMaxBounds.Doubles range = RangeArgument.Floats.getRange(cmdCtx, key);
		final Double lowBoxed = range.min().orElse(null);
		final Double highBoxed = range.max().orElse(null);
		final double low = lowBoxed == null ? -Double.MAX_VALUE : lowBoxed;
		final double high = highBoxed == null ? Double.MAX_VALUE : highBoxed;
		return new DoubleRange(low, high);
	}

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
