package dev.jorel.commandapi.nms;

import dev.jorel.commandapi.CommandRegistrationStrategy;

public interface PaperNMS<CommandListenerWrapper> {

	<Source> NMS<Source> bukkitNMS();

	CommandRegistrationStrategy<CommandListenerWrapper> createCommandRegistrationStrategy();

}
