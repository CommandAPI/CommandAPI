// TODO: I removed the dependency for the NBT API
//  And I'm not sure about the version stuff yet
//package dev.jorel.commandapi.test.arguments;
//
//import static org.junit.jupiter.api.Assertions.assertArrayEquals;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assumptions.assumeTrue;
//
//import java.util.List;
//
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
//
//import be.seeseemelk.mockbukkit.entity.PlayerMock;
//import de.tr7zw.changeme.nbtapi.NBTContainer;
//import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
//import dev.jorel.commandapi.CommandAPICommand;
//import dev.jorel.commandapi.CommandAPIVersionHandler;
//import dev.jorel.commandapi.MCVersion;
//import dev.jorel.commandapi.arguments.NBTCompoundArgument;
//import dev.jorel.commandapi.test.MockPlatform;
//import dev.jorel.commandapi.test.Mut;
//import dev.jorel.commandapi.test.TestBase;
//
///**
// * Tests for the {@link NBTCompoundArgument}
// */
//@Disabled
//class ArgumentNBTCompoundNBTAPITests extends TestBase {
//
//	/*********
//	 * Setup *
//	 *********/
//
//	@BeforeEach
//	public void setUp() {
//		// Ignoring 1.20.5 for NBT tests until they update
//		assumeTrue(version.lessThan(MCVersion.V1_20_5));
//
//		// NBT API can't run via Mojang Mappings
//		assumeTrue(!CommandAPIVersionHandler.IS_MOJANG_MAPPED);
//
//		super.setUp(NBTContainer.class, NBTContainer::new);
//		MinecraftVersion.disableBStats();
//		MinecraftVersion.disablePackageWarning();
//		MinecraftVersion.disableUpdateCheck();
//
//		MinecraftVersion currentNBTAPIMinecraftVersion = switch (version) {
//			case V1_16_5 -> MinecraftVersion.MC1_16_R3;
//			case V1_17 -> MinecraftVersion.MC1_17_R1;
//			case V1_18 -> MinecraftVersion.MC1_18_R1;
//			case V1_19_2 -> MinecraftVersion.MC1_19_R1;
//			case V1_19_4 -> MinecraftVersion.MC1_19_R3;
//			case V1_20 -> MinecraftVersion.MC1_20_R1;
//			case V1_20_2 -> MinecraftVersion.MC1_20_R2;
//			case V1_20_3 -> MinecraftVersion.MC1_20_R3;
//			default -> throw new IllegalArgumentException("Unexpected value: " + version);
//		};
//		MockPlatform.setField(MinecraftVersion.class, "version", null, currentNBTAPIMinecraftVersion);
//	}
//
//	@AfterEach
//	public void tearDown() {
//		super.tearDown();
//	}
//
//	/*********
//	 * Tests *
//	 *********/
//
//	@Test
//	void executionTestWithNBTCompoundArgumentEmpty() {
//		Mut<NBTContainer> results = Mut.of();
//
//		new CommandAPICommand("test")
//			.withArguments(new NBTCompoundArgument<NBTContainer>("nbt"))
//			.executesPlayer((player, args) -> {
//				results.set((NBTContainer) args.get("nbt"));
//			})
//			.register();
//
//		Player player = addPlayer();
//
//		// /test {}
//		server.dispatchCommand(player, "test {}");
//		assertEquals(new NBTContainer().getCompound(), results.get().getCompound());
//
//		assertNoMoreResults(results);
//	}
//
//	@Test
//	void executionTestWithNBTCompoundArgumentBoolean() {
//		Mut<NBTContainer> results = Mut.of();
//
//		new CommandAPICommand("test")
//			.withArguments(new NBTCompoundArgument<NBTContainer>("nbt"))
//			.executesPlayer((player, args) -> {
//				results.set((NBTContainer) args.get("nbt"));
//			})
//			.register();
//
//		Player player = addPlayer();
//
//		// /test {val:true}
//		server.dispatchCommand(player, "test {val:true}");
//		assertEquals(true, results.get().getBoolean("val"));
//
//		// /test {val:false}
//		server.dispatchCommand(player, "test {val:false}");
//		assertEquals(false, results.get().getBoolean("val"));
//
//		assertNoMoreResults(results);
//	}
//
//	@Test
//	void executionTestWithNBTCompoundArgumentNumbers() {
//		Mut<NBTContainer> results = Mut.of();
//
//		new CommandAPICommand("test")
//			.withArguments(new NBTCompoundArgument<NBTContainer>("nbt"))
//			.executesPlayer((player, args) -> {
//				results.set((NBTContainer) args.get("nbt"));
//			})
//			.register();
//
//		Player player = addPlayer();
//
//		// /test {val:2b}
//		server.dispatchCommand(player, "test {val:2b}");
//		assertEquals(Byte.valueOf((byte) 2), results.get().getByte("val"));
//
//		// /test {val:200s}
//		server.dispatchCommand(player, "test {val:200s}");
//		assertEquals(Short.valueOf((short) 200), results.get().getShort("val"));
//
//		// /test {val:2000000}
//		server.dispatchCommand(player, "test {val:2000000}");
//		assertEquals(Integer.valueOf(2000000), results.get().getInteger("val"));
//
//		// /test {val:20000000000l}
//		server.dispatchCommand(player, "test {val:20000000000l}");
//		assertEquals(Long.valueOf(20000000000L), results.get().getLong("val"));
//
//		// /test {val:2.3f}
//		server.dispatchCommand(player, "test {val:2.3f}");
//		assertEquals(Float.valueOf(2.3f), results.get().getFloat("val"));
//
//		// /test {val:2.3d}
//		server.dispatchCommand(player, "test {val:2.3d}");
//		assertEquals(Double.valueOf(2.3d), results.get().getDouble("val"));
//
//		assertNoMoreResults(results);
//	}
//
//	@Test
//	void executionTestWithNBTCompoundArgumentNumberArrays() {
//		Mut<NBTContainer> results = Mut.of();
//
//		new CommandAPICommand("test")
//			.withArguments(new NBTCompoundArgument<NBTContainer>("nbt"))
//			.executesPlayer((player, args) -> {
//				results.set((NBTContainer) args.get("nbt"));
//			})
//			.register();
//
//		Player player = addPlayer();
//
//		// /test {val:[B;2b,3b,4b]}
//		server.dispatchCommand(player, "test {val:[B;2b,3b,4b]}");
//		assertArrayEquals(new byte[] { 2, 3, 4 }, results.get().getByteArray("val"));
//
//		// /test {val:[I;200,300,400]}
//		server.dispatchCommand(player, "test {val:[I;200,300,400]}");
//		assertArrayEquals(new int[] { 200, 300, 400 }, results.get().getIntArray("val"));
//
//		assertNoMoreResults(results);
//	}
//
//	@Test
//	void executionTestWithNBTCompoundArgumentNumberLists() {
//		Mut<NBTContainer> results = Mut.of();
//
//		new CommandAPICommand("test")
//			.withArguments(new NBTCompoundArgument<NBTContainer>("nbt"))
//			.executesPlayer((player, args) -> {
//				results.set((NBTContainer) args.get("nbt"));
//			})
//			.register();
//
//		Player player = addPlayer();
//
//		// /test {val:[200,300,400]}
//		server.dispatchCommand(player, "test {val:[200,300,400]}");
//		assertEquals(List.of(200, 300, 400), results.get().getIntegerList("val"));
//
//		// /test {val:[3000000000l,4000000000l,5000000000l]}
//		server.dispatchCommand(player, "test {val:[3000000000l,4000000000l,5000000000l]}");
//		assertEquals(List.of(3000000000L, 4000000000L, 5000000000L), results.get().getLongList("val"));
//
//		assertNoMoreResults(results);
//	}
//
//	@Test
//	void executionTestWithNBTCompoundArgumentStrings() {
//		Mut<NBTContainer> results = Mut.of();
//
//		new CommandAPICommand("test")
//			.withArguments(new NBTCompoundArgument<NBTContainer>("nbt"))
//			.executesPlayer((player, args) -> {
//				results.set((NBTContainer) args.get("nbt"));
//			})
//			.register();
//
//		Player player = addPlayer();
//
//		// /test {val:"Hello, world!"}
//		server.dispatchCommand(player, "test {val:\"Hello, world!\"}");
//		assertEquals("Hello, world!", results.get().getString("val"));
//
//		assertNoMoreResults(results);
//	}
//
//	@Test
//	void executionTestWithNBTCompoundArgumentCompound() {
//		Mut<NBTContainer> results = Mut.of();
//
//		new CommandAPICommand("test")
//			.withArguments(new NBTCompoundArgument<NBTContainer>("nbt"))
//			.executesPlayer((player, args) -> {
//				results.set((NBTContainer) args.get("nbt"));
//			})
//			.register();
//
//		Player player = addPlayer();
//
//		// /test {val1:{val2:2}}
//		server.dispatchCommand(player, "test {val1:{val2:2}}");
//		assertEquals(2, results.get().getCompound("val1").getInteger("val2"));
//
//		assertNoMoreResults(results);
//	}
//
//	@Test
//	void executionTestWithNBTCompoundArgumentMultiple() {
//		Mut<NBTContainer> results = Mut.of();
//
//		new CommandAPICommand("test")
//			.withArguments(new NBTCompoundArgument<NBTContainer>("nbt"))
//			.executesPlayer((player, args) -> {
//				results.set((NBTContainer) args.get("nbt"));
//			})
//			.register();
//
//		Player player = addPlayer();
//
//		// /test {val1:10,val2:20}
//		server.dispatchCommand(player, "test {val1:10,val2:20}");
//		NBTContainer result = results.get();
//		assertEquals(10, result.getInteger("val1"));
//		assertEquals(20, result.getInteger("val2"));
//
//		assertNoMoreResults(results);
//	}
//
//	/********************
//	 * Suggestion tests *
//	 ********************/
//
//	@Test
//	void suggestionTestWithNBTCompoundArgument() {
//		new CommandAPICommand("test")
//			.withArguments(new NBTCompoundArgument<NBTContainer>("nbt"))
//			.executesPlayer(P_EXEC)
//			.register();
//
//		Player player = addPlayer();
//
//		// /test
//		// Should suggest nothing
//		assertEquals(List.of(), server.getSuggestions(player, "test "));
//
//		// /test {
//		// Should suggest nothing
//		assertEquals(List.of(), server.getSuggestions(player, "test {"));
//	}
//
//}
