package dev.jorel.commandapi.wrappers;

import dev.jorel.commandapi.arguments.PreviewInfo;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;

@FunctionalInterface
public interface PreviewableFunction<T, Player> {

	public T generatePreview(PreviewInfo<T, Player> info) throws WrapperCommandSyntaxException;
}
