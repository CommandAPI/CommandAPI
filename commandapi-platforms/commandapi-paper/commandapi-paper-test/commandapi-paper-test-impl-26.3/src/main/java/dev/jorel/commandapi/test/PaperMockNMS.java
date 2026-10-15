package dev.jorel.commandapi.test;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.serialization.DynamicOps;
import dev.jorel.commandapi.CommandRegistrationStrategy;
import dev.jorel.commandapi.MockPaperCommandRegistration;
import dev.jorel.commandapi.commandsenders.AbstractCommandSender;
import dev.jorel.commandapi.nms.NMS;
import dev.jorel.commandapi.nms.NMS_26_3;
import dev.jorel.commandapi.nms.PaperNMS;
import dev.jorel.commandapi.nms.PaperNMS_26_3;
import io.papermc.paper.command.brigadier.PaperCommands;
import io.papermc.paper.command.brigadier.argument.VanillaArgumentProviderImpl.NativeWrapperArgumentType;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.Removed;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TaskChainer;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.entity.Entity;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Paper version of {@link MockNMS}. Like {@link PaperNMS_26_3} wraps the Bukkit {@link NMS_26_3}
 * implementation, this wraps the Bukkit {@link MockNMS} and adds the Paper-specific parts.
 */
public class PaperMockNMS extends MockNMS implements PaperNMS<CommandSourceStack> {
	public PaperMockNMS() {
		// Same as PaperNMS_26_3#bukkitNMS, except PaperNMS_26_3 gets its CommandBuildContext from the CraftServer
		super(setupRegistries(new NMS_26_3(MockRegistries::commandBuildContext, PaperMockNMS::serializeComponents, CraftItemStack::asBukkitCopy, CraftBlockData::createData)));

		// Paper's argument types API gets its CommandBuildContext from PaperCommands, which is normally set up by the server
		PaperCommands.INSTANCE.setDispatcher(this.<MinecraftServer>getMinecraftServer().getCommands(), MockRegistries.commandBuildContext());

		// Some of Paper's argument resolvers (e.g. for worlds) use the static MinecraftServer#getServer
		setField(MinecraftServer.class, "SERVER", null, this.<MinecraftServer>getMinecraftServer());
	}

	private static <T> T setupRegistries(T baseNMS) {
		setupRegistries();
		return baseNMS;
	}

	/**
	 * Sets up the registries behind Paper's Bukkit registries (potion effects, enchantments, biomes, etc.), which are
	 * backed by the real Minecraft registries. MockBukkit's {@code ServerMock} uses Bukkit's registries when it's
	 * created, so this must be called before mocking the server. This is safe to call multiple times.
	 */
	public static void setupRegistries() {
		// Paper's RegistryDataLoader registers the Bukkit registries for the data pack registries when MockRegistries
		//  loads them, like when the server starts
		RegistryAccess.Frozen registryAccess = MockRegistries.registryAccess();
		if (CraftRegistry.getMinecraftRegistry() == null) {
			CraftRegistry.setMinecraftRegistry(registryAccess);
		}
	}

	// Same as PaperNMS_26_3#serializeComponents, which is private
	private static String serializeComponents(ItemInput itemInput, HolderLookup.Provider provider) {
		DynamicOps<Tag> serializationContext = provider.createSerializationContext(NbtOps.INSTANCE);
		return itemInput.components().map.entrySet().stream().flatMap((entry) -> {
			DataComponentType<?> type = entry.getKey();
			Identifier identifier = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
			if (identifier == null) {
				return Stream.empty();
			} else {
				Object value = entry.getValue();
				if (!Removed.isRemoved(value)) {
					TypedDataComponent<?> typedDataComponent = TypedDataComponent.createUnchecked(type, value);
					return typedDataComponent.encodeValue(serializationContext).result().stream().map((tag) -> {
						String componentString = identifier.toString();
						return componentString + "=" + tag;
					});
				} else {
					return Stream.of("!" + identifier);
				}
			}
		}).collect(Collectors.joining(String.valueOf(',')));
	}

	@SuppressWarnings({ "removal", "rawtypes", "unchecked" })
	@Override
	protected Registry<?> createCraftRegistry(Class<?> className) {
		// Same as Paper's CraftServer#getRegistry
		return io.papermc.paper.registry.RegistryAccess.registryAccess().getRegistry((Class) className);
	}

	@Override
	public NMS<CommandSourceStack> bukkitNMS() {
		return this;
	}

	@Override
	public CommandRegistrationStrategy<CommandSourceStack> createCommandRegistrationStrategy() {
		// Same dispatcher as PaperNMS_26_3 uses
		return new MockPaperCommandRegistration<>(
			() -> this.<MinecraftServer>getMinecraftServer().getCommands().getDispatcher()
		);
	}

	@Override
	protected void setupCommandDispatchers(MinecraftServer minecraftServerMock) {
		// Paper has neither MinecraftServer#vanillaCommandDispatcher nor the no-argument Commands constructor
		Commands resourcesCommands = Mockito.mock(Commands.class);
		Mockito.when(resourcesCommands.getDispatcher()).thenReturn(getMockResourcesDispatcher());
		Mockito.when(minecraftServerMock.getCommands()).thenReturn(resourcesCommands);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void createDispatcherFile(File file, CommandDispatcher dispatcher) throws IOException {
		// On Paper, commands go through Paper's command registrar before the dispatcher file is written, which
		//  replaces Paper's argument types with the vanilla ones they wrap. MockPaperCommandRegistration doesn't
		//  do that, so we do it here, otherwise vanilla can't serialize the Paper argument types.
		RootCommandNode root = (RootCommandNode) unwrapPaperArguments(dispatcher.getRoot(), new IdentityHashMap<>());
		super.createDispatcherFile(file, new CommandDispatcher<>(root));
	}

	/**
	 * Copies a command tree, replacing Paper's {@link NativeWrapperArgumentType}s with the vanilla argument types
	 * they wrap
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static CommandNode unwrapPaperArguments(CommandNode node, Map<CommandNode, CommandNode> copies) {
		CommandNode existing = copies.get(node);
		if (existing != null) {
			return existing;
		}

		CommandNode copy;
		if (node instanceof RootCommandNode) {
			copy = new RootCommandNode<>();
		} else {
			ArgumentBuilder builder;
			if (node instanceof ArgumentCommandNode argument && argument.getType() instanceof NativeWrapperArgumentType wrapper) {
				builder = RequiredArgumentBuilder.argument(argument.getName(), wrapper.nativeNmsArgumentType())
					.suggests(argument.getCustomSuggestions())
					.requires(argument.getRequirement())
					.executes(argument.getCommand());
			} else {
				builder = node.createBuilder();
			}
			if (node.getRedirect() != null) {
				builder.forward(unwrapPaperArguments(node.getRedirect(), copies), node.getRedirectModifier(), node.isFork());
			}
			copy = builder.build();
		}

		copies.put(node, copy);
		for (Object child : node.getChildren()) {
			copy.addChild(unwrapPaperArguments((CommandNode) child, copies));
		}
		return copy;
	}

	@Override
	public CommandSourceStack getBrigadierSourceFromCommandSender(AbstractCommandSender<? extends CommandSender> senderWrapper) {
		CommandSourceStack css = super.getBrigadierSourceFromCommandSender(senderWrapper);

		// On Paper, the NMS CommandSourceStack also implements Paper's CommandSourceStack API, which Paper's
		//  argument resolvers use. These need to be stubbed because the source is a mock.
		CommandSender sender = senderWrapper.getSource();
		Mockito.when(css.getSender()).thenReturn(sender);
		Mockito.when(css.getExecutor()).thenAnswer(i -> sender instanceof Entity entity ? entity : null);
		Mockito.when(css.getLocation()).thenAnswer(i -> sender instanceof Entity entity ? entity.getLocation() : null);

		// Paper checks some permissions (e.g. for entity selectors) with this instead of CommandSourceStack#permissions,
		//  which also accepts the Bukkit permission
		Mockito.when(css.hasPermission(Mockito.any(), Mockito.anyString())).thenAnswer(i -> sender.isOp() || sender.hasPermission(i.<String>getArgument(1)));

		// Paper's signed message resolver (used by ChatArgument) looks for signed arguments. These tests don't sign them,
		//  so the message is resolved as an unsigned one, which goes through the server's chat decorator.
		Mockito.when(css.getSigningContext()).thenReturn(CommandSigningContext.ANONYMOUS);
		Mockito.when(css.getChatMessageChainer()).thenReturn(TaskChainer.immediate(Runnable::run));
		Mockito.when(this.<MinecraftServer>getMinecraftServer().getChatDecorator()).thenReturn(new ChatDecorator() {
			// Paper's ChatDecorator#PLAIN doesn't implement the version used for commands
			@Override
			public CompletableFuture<Component> decorate(ServerPlayer player, Component message) {
				return CompletableFuture.completedFuture(message);
			}

			@Override
			public CompletableFuture<Component> decorate(ServerPlayer player, CommandSourceStack source, Component message) {
				return CompletableFuture.completedFuture(message);
			}
		});

		return css;
	}
}
