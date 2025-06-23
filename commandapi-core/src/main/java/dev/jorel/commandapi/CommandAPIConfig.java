/*******************************************************************************
 * Copyright 2018, 2021 Jorel Ali (Skepter) - MIT License
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
package dev.jorel.commandapi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A class to contain information about how to configure the CommandAPI during its loading step.
 * You shouldn't use this class directly. Instead, use an appropriate subclass that corresponds to
 * the platform you are developing for.
 */
public abstract class CommandAPIConfig<Impl
/// @cond DOX
extends CommandAPIConfig<Impl>
/// @endcond
> implements ChainableBuilder<Impl> {
	// The default configuration. This should mirror the commandapi-plugin config.yml file.
	boolean verboseOutput = false;
	boolean silentLogs = false;
	boolean useLatestNMSVersion = false;
	boolean beLenientForMinorVersions = false;
	String missingExecutorImplementationMessage = "This command has no implementations for %s";

	File dispatcherFile = null;

	List<String> skipSenderProxy = new ArrayList<>();

	// NBT API
	Class<?> nbtContainerClass = null;
	Function<Object, ?> nbtContainerConstructor = null;

	boolean usePluginNamespace = false;
	String namespace = null;

	boolean errorOnFailedPacketSends;

	/**
	 * Sets verbose output logging for the CommandAPI if true.
	 *
	 * @param value whether verbose output should be enabled
	 * @return this CommandAPIConfig
	 */
	public Impl verboseOutput(boolean value) {
		this.verboseOutput = value;
		return instance();
	}

	/**
	 * Silences all logs (including warnings, but not errors) for the CommandAPI if
	 * true.
	 *
	 * @param value whether logging suppression should be enabled
	 * @return this CommandAPIConfig
	 */
	public Impl silentLogs(boolean value) {
		this.silentLogs = value;
		return instance();
	}

	/**
	 * Sets whether the CommandAPI should run the latest available version of NMS
	 * support, regardless of Minecraft version. This may produce unexpected results
	 * if the latest NMS version is not supported by the CommandAPI. This can be
	 * used to potentially provide compatibility with future Minecraft versions
	 * before the CommandAPI pushes a release to support it.
	 *
	 * @param value whether the latest version of NMS should be used
	 * @return this CommandAPIConfig
	 */
	public Impl useLatestNMSVersion(boolean value) {
		this.useLatestNMSVersion = value;
		return instance();
	}

	/**
	 * Sets whether the CommandAPI should load a (potentially unsupported) NMS version
	 * when updating to a minor release of Minecraft. As an example, this setting can allow
	 * updating to 1.21.2 from 1.21.1 but doesn't allow updating to 1.22 from 1.21.2.
	 * Unlike {@link #useLatestNMSVersion(boolean)}, this setting does
	 * not blindly load the latest NMS version, but will prefer loading the correct NMS implementation when available.
	 *
	 * @param value whether the CommandAPI should assume that minor Minecraft releases do not cause incompatibilities
	 * @return this CommandAPIConfig
	 */
	public Impl beLenientForMinorVersions(boolean value) {
		this.beLenientForMinorVersions = value;
		return instance();
	}

	/**
	 * Sets the message to display to users when a command has no executor.
	 * Available formatting parameters are:
	 *
	 * <ul>
	 * <li>%s - the executor class (lowercase)</li>
	 * <li>%S - the executor class (normal case)</li>
	 * </ul>
	 *
	 * @param value the message to display when a command has no executor
	 * @return this CommandAPIConfig
	 */
	public Impl missingExecutorImplementationMessage(String value) {
		this.missingExecutorImplementationMessage = value;
		return instance();
	}

	/**
	 * Specifies the location for the CommandAPI to store the internal
	 * representation of Brigadier's command tree.
	 *
	 * @param file a file pointing to where to store Brigadier's JSON command
	 *             dispatcher, for example
	 *             {@code new File(getDataFolder(), "command_registration.json")}.
	 *             If this argument is {@code null}, this file will not be created.
	 * @return this CommandAPIConfig
	 */
	public Impl dispatcherFile(File file) {
		this.dispatcherFile = file;
		return instance();
	}

	public Impl addSkipSenderProxy(String... names) {
		this.skipSenderProxy.addAll(List.of(names));
		return instance();
	}

	public Impl addSkipSenderProxy(List<String> names) {
		this.skipSenderProxy.addAll(names);
		return instance();
	}

	/**
	 * Initializes the CommandAPI's implementation of an NBT API.
	 *
	 * @param <T>                     the type that the NBT compound container class
	 *                                is
	 * @param nbtContainerClass       the NBT compound container class. For example,
	 *                                {@code NBTContainer.class}
	 * @param nbtContainerConstructor a function that takes an Object (NMS
	 *                                {@code NBTTagCompound}) and returns an
	 *                                instance of the provided NBT compound
	 *                                container. For example,
	 *                                {@code NBTContainer::new}.
	 * @return this CommandAPIConfig
	 */
	public <T> Impl initializeNBTAPI(Class<T> nbtContainerClass,
												 Function<Object, T> nbtContainerConstructor) {
		this.nbtContainerClass = nbtContainerClass;
		this.nbtContainerConstructor = nbtContainerConstructor;
		return instance();
	}

	/**
	 * Sets the default namespace to use when register commands
	 *
	 * @param namespace the namespace to use when register commands
	 * @return this CommandAPIConfig
	 */
	public Impl setNamespace(String namespace) {
		if (!usePluginNamespace) {
			this.namespace = namespace;
		}
		return instance();
	}

	/**
	 * Sets whether the CommandAPI should use the plugin's name as the default namespace
	 * <p>
	 * If called, any call to {@link CommandAPIConfig#setNamespace(String)} will be ignored
	 *
	 * @return this CommandAPIConfig
	 */
	public abstract Impl usePluginNamespace();

	/**
	 * Sets whether the CommandAPI should throw an exception if it cannot send a packet.
	 * If false, the problem will be logged as a warning instead.
	 *
	 * @param value Whether an exception should be thrown when a packet cannot be sent
	 * @return this CommandAPIConfig
	 */
	public Impl errorOnFailedPacketSends(boolean value) {
		this.errorOnFailedPacketSends = value;
		return instance();
	}
}
