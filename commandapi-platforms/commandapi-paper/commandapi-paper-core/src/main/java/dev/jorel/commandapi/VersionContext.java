package dev.jorel.commandapi;

import dev.jorel.commandapi.nms.BundledNMS;

public record VersionContext(BundledNMS<?> nms, Runnable context) {

	public VersionContext(BundledNMS<?> nms) {
		this(nms, () -> {});
	}

}
