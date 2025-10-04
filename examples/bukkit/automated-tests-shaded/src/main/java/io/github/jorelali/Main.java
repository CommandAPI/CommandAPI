package io.github.jorelali;

import dev.jorel.commandapi.*;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.IntegerRangeArgument;
import dev.jorel.commandapi.arguments.SafeSuggestions;
import dev.jorel.commandapi.wrappers.IntegerRange;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	@Override
	public void onLoad() {
		// Load CommandAPI as usual
		CommandAPI.onLoad(new CommandAPIPaperConfig(this));
	}

	@Override
	public void onEnable() {
		// Enable CommandAPI as usual
		CommandAPI.onEnable();

		// Register command as usual - no changes should be needed to test them
		new CommandAPICommand("ping").executes((sender, args) -> {
			sender.sendMessage("pong!");
		}).register();

		new CommandAPICommand("rangecheck")
			.withArguments(
				new IntegerRangeArgument("range"),
				new IntegerArgument("number")
					.replaceSafeSuggestions(SafeSuggestions.tooltips(info -> {
						IntegerRange range = info.previousArgs().getUnchecked("range");
						assert range != null;

						int low = range.getLowerBound();
						int high = range.getUpperBound();

						int middle = (high + low) / 2;
						int step;
						if (low == high) {
							step = low/2;
						} else {
							step = (high - middle) * 2;
						}

						return Tooltip.arrayOf(
							Tooltip.ofString(middle, "Middle"),
							Tooltip.ofString(middle + step, "Too high"),
							Tooltip.ofString(middle - step, "Too low")
						);
					}))
			)
			.executes((sender, args) -> {
				IntegerRange range = args.getUnchecked("range");
				Integer number = args.getUnchecked("number");

				assert range != null && number != null;

				if (!range.isInRange(number)) {
					throw CommandAPI.failWithString("Number " + number + " is outside of range " + range);
				}

				sender.sendMessage("Number " + number + " is within range " + range);
			})
			.register();
	}

	@Override
	public void onDisable() {
		// Disabling the CommandAPI is required to reset properly after each test
		CommandAPI.onDisable();
	}
}
