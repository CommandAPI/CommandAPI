package dev.jorel.commandapi.nms;

import dev.jorel.commandapi.CommandRegistrationStrategy;

public interface PaperNMS<CommandListenerWrapper> {

	NMS<CommandListenerWrapper> bukkitNMS();

	CommandRegistrationStrategy<CommandListenerWrapper> createCommandRegistrationStrategy();

}
