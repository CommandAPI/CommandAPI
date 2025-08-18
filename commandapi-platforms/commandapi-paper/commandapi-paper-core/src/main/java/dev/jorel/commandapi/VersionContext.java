package dev.jorel.commandapi;

import dev.jorel.commandapi.nms.BundledNMS;

import java.util.function.Consumer;

public record VersionContext(BundledNMS<?> nms, Consumer<CommandAPILogger> context) {

	public VersionContext(BundledNMS<?> nms) {
		this(nms, (logger) -> {});
	}

}
