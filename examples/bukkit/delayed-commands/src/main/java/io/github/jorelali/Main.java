package io.github.jorelali;

import io.github.jorelali.delayedapicommand.DelayedAPICommands;
import io.github.jorelali.delayhandler.DelayHandlerCommands;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	@Override
	public void onEnable() {
		DelayHandlerCommands.registerCommands();
		DelayedAPICommands.registerCommands();
	}
}
