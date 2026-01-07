// TODO: I removed the dependency for the NBT API
//  And I'm not sure about the version stuff yet
//package dev.jorel.commandapi.test.arguments;
//
//import static org.junit.jupiter.api.Assertions.assertArrayEquals;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.fail;
//
//import java.util.List;
//import java.util.Map;
//
//import org.bukkit.Bukkit;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//
//import com.saicone.rtag.Rtag;
//import com.saicone.rtag.tag.TagCompound;
//
//import be.seeseemelk.mockbukkit.entity.PlayerMock;
//import dev.jorel.commandapi.CommandAPICommand;
//import dev.jorel.commandapi.arguments.NBTCompoundArgument;
//import dev.jorel.commandapi.test.MockPlatform;
//import dev.jorel.commandapi.test.Mut;
//import dev.jorel.commandapi.test.TestBase;
//
///**
// * Tests for the {@link NBTCompoundArgument}
// */
//class ArgumentNBTCompoundRtagTests extends TestBase {
//
//	/*********
//	 * Setup *
//	 *********/
//
//	@BeforeEach
//	public void setUp() {
//		// There are lots of ways to use Rtag!
//
//		// The simplest case just uses plain ol' object to object mapping. This
//		// gives you the most control over parsing NBT arguments from the
//		// CommandAPI because this passes the raw NMS NBTTagCompound object,
//		// allowing you to use Rtag#get:
//		//
//		//   super.setUp(Object.class, o -> o);
//
//		// A partially parsed case which returns a Map<String, Object>, where
//		// the values in the map are standard NMS NBTBase objects:
//		//
//		//   super.setUp(Map.class, TagCompound::getValue);
//
//		// A fully parsed case which returns a Map<String, Object>, where the
//		// values in the map are "converted values" or "normal Java objects".
//		// This specifically means "String, Short, Integer, Double, Float,
//		// Long, Byte, Map and List", as well as "Byte, Integer and Long arrays"
//		// Note that this doesn't support Boolean, so boolean values will be a
//		// byte with value either 1 (true) or 0 (false)
//		super.setUp(Map.class, o -> TagCompound.getValue(Rtag.INSTANCE, o));
//
//		// Rtag needs to hook into the CraftServer class to figure out what
//		// version it's running. This requires getting the Bukkit server class's
//		// package name and parsing the version from that (See ServerInstance).
//		// We need to instantiate Rtag to set its static values, but we don't
//		// know when we'll do that, so we'll initialize those values here by
//		// temporarily setting Bukkit.getServer() to "CraftServer"
//		try {
//			// Store current mocked server
//			Object oldServer = MockPlatform.getField(Bukkit.class, "server", null);
//
//			String craftServerVersion = switch (version) {
//				case V1_16_5 -> "v1_16_R3";
//				case V1_17 -> "v1_17_R1";
//				case V1_18 -> "v1_18_R1";
//				case V1_19_2 -> "v1_19_R1";
//				case V1_19_4 -> "v1_19_R3";
//				case V1_20 -> "v1_20_R1";
//				case V1_20_2 -> "v1_20_R2";
//				case V1_20_3 -> "v1_20_R3";
//				case V1_20_5 -> "v1_20_R4";
//				default -> throw new IllegalArgumentException("Unexpected value: " + version);
//			};
//
//			Object craftServer = Mockito.mock(Class.forName("org.bukkit.craftbukkit." + craftServerVersion + ".CraftServer"));
//			MockPlatform.setField(Bukkit.class, "server", null, craftServer);
//
//			// Instantiate Rtag, which instantiates ServerInstance
//			Rtag.INSTANCE.getClass();
//
//			// Reset current mocked server
//			MockPlatform.setField(Bukkit.class, "server", null, oldServer);
//
//		} catch(ReflectiveOperationException e) {
//			fail(e.getMessage());
//		}
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
//	@SuppressWarnings("unchecked")
//	@Test
//	void executionTestWithNBTCompoundArgumentEmpty() {
//		Mut<Map<String, Object>> results = Mut.of();
//
//		new CommandAPICommand("test")
//			.withArguments(new NBTCompoundArgument<Map<String, Object>>("nbt"))
//			.executesPlayer((player, args) -> {
//				results.set((Map<String, Object>) args.get("nbt"));
//			})
//			.register();
//
//		Player player = addPlayer();
//
//		// /test {}
//		server.dispatchCommand(player, "test {}");
//		assertEquals(Map.of(), results.get());
//
//		assertNoMoreResults(results);
//	}
//
//	@SuppressWarnings("unchecked")
//	@Test
//	void executionTestWithNBTCompoundArgumentBoolean() {
//		Mut<Map<String, Object>> results = Mut.of();
//
//		new CommandAPICommand("test")
//			.withArguments(new NBTCompoundArgument<Map<String, Object>>("nbt"))
//			.executesPlayer((player, args) -> {
//				results.set((Map<String, Object>) args.get("nbt"));
//			})
//			.register();
//
//		Player player = addPlayer();
//
//		// /test {val:true}
//		server.dispatchCommand(player, "test {val:true}");
//		assertEquals(Byte.valueOf((byte) 1), results.get().get("val"));
//
//		// /test {val:false}
//		server.dispatchCommand(player, "test {val:false}");
//		assertEquals(Byte.valueOf((byte) 0), results.get().get("val"));
//
//		assertNoMoreResults(results);
//	}
//
//	@SuppressWarnings("unchecked")
//	@Test
//	void executionTestWithNBTCompoundArgumentNumbers() {
//		Mut<Map<String, Object>> results = Mut.of();
//
//		new CommandAPICommand("test")
//			.withArguments(new NBTCompoundArgument<Map<String, Object>>("nbt"))
//			.executesPlayer((player, args) -> {
//				results.set((Map<String, Object>) args.get("nbt"));
//			})
//			.register();
//
//		Player player = addPlayer();
//
//		// /test {val:2b}
//		server.dispatchCommand(player, "test {val:2b}");
//		assertEquals(Byte.valueOf((byte) 2), results.get().get("val"));
//
//		// /test {val:200s}
//		server.dispatchCommand(player, "test {val:200s}");
//		assertEquals(Short.valueOf((short) 200), results.get().get("val"));
//
//		// /test {val:2000000}
//		server.dispatchCommand(player, "test {val:2000000}");
//		assertEquals(Integer.valueOf(2000000), results.get().get("val"));
//
//		// /test {val:20000000000l}
//		server.dispatchCommand(player, "test {val:20000000000l}");
//		assertEquals(Long.valueOf(20000000000L), results.get().get("val"));
//
//		// /test {val:2.3f}
//		server.dispatchCommand(player, "test {val:2.3f}");
//		assertEquals(Float.valueOf(2.3f), results.get().get("val"));
//
//		// /test {val:2.3d}
//		server.dispatchCommand(player, "test {val:2.3d}");
//		assertEquals(Double.valueOf(2.3d), results.get().get("val"));
//
//		assertNoMoreResults(results);
//	}
//
//	@SuppressWarnings("unchecked")
//	@Test
//	void executionTestWithNBTCompoundArgumentNumberArrays() {
//		Mut<Map<String, Object>> results = Mut.of();
//
//		new CommandAPICommand("test")
//			.withArguments(new NBTCompoundArgument<Map<String, Object>>("nbt"))
//			.executesPlayer((player, args) -> {
//				results.set((Map<String, Object>) args.get("nbt"));
//			})
//			.register();
//
//		Player player = addPlayer();
//
//		// /test {val:[B;2b,3b,4b]}
//		server.dispatchCommand(player, "test {val:[B;2b,3b,4b]}");
//		assertArrayEquals(new byte[] { 2, 3, 4 }, (byte[]) results.get().get("val"));
//
//		// /test {val:[I;200,300,400]}
//		server.dispatchCommand(player, "test {val:[I;200,300,400]}");
//		assertArrayEquals(new int[] { 200, 300, 400 }, (int[]) results.get().get("val"));
//
//		assertNoMoreResults(results);
//	}
//
//	@SuppressWarnings("unchecked")
//	@Test
//	void executionTestWithNBTCompoundArgumentNumberLists() {
//		Mut<Map<String, Object>> results = Mut.of();
//
//		new CommandAPICommand("test")
//			.withArguments(new NBTCompoundArgument<Map<String, Object>>("nbt"))
//			.executesPlayer((player, args) -> {
//				results.set((Map<String, Object>) args.get("nbt"));
//			})
//			.register();
//
//		Player player = addPlayer();
//
//		// /test {val:[200,300,400]}
//		server.dispatchCommand(player, "test {val:[200,300,400]}");
//		assertEquals(List.of(200, 300, 400), results.get().get("val"));
//
//		// /test {val:[3000000000l,4000000000l,5000000000l]}
//		server.dispatchCommand(player, "test {val:[3000000000l,4000000000l,5000000000l]}");
//		assertEquals(List.of(3000000000L, 4000000000L, 5000000000L), results.get().get("val"));
//
//		assertNoMoreResults(results);
//	}
//
//	@SuppressWarnings("unchecked")
//	@Test
//	void executionTestWithNBTCompoundArgumentStrings() {
//		Mut<Map<String, Object>> results = Mut.of();
//
//		new CommandAPICommand("test")
//			.withArguments(new NBTCompoundArgument<Map<String, Object>>("nbt"))
//			.executesPlayer((player, args) -> {
//				results.set((Map<String, Object>) args.get("nbt"));
//			})
//			.register();
//
//		Player player = addPlayer();
//
//		// /test {val:"Hello, world!"}
//		server.dispatchCommand(player, "test {val:\"Hello, world!\"}");
//		assertEquals("Hello, world!", results.get().get("val"));
//
//		assertNoMoreResults(results);
//	}
//
//	@SuppressWarnings("unchecked")
//	@Test
//	void executionTestWithNBTCompoundArgumentCompound() {
//		Mut<Map<String, Object>> results = Mut.of();
//
//		new CommandAPICommand("test")
//			.withArguments(new NBTCompoundArgument<Map<String, Object>>("nbt"))
//			.executesPlayer((player, args) -> {
//				results.set((Map<String, Object>) args.get("nbt"));
//			})
//			.register();
//
//		Player player = addPlayer();
//
//		// /test {val1:{val2:2}}
//		server.dispatchCommand(player, "test {val1:{val2:2}}");
//		assertEquals(Map.of("val2", 2), results.get().get("val1"));
//
//		assertNoMoreResults(results);
//	}
//
//	@SuppressWarnings("unchecked")
//	@Test
//	void executionTestWithNBTCompoundArgumentMultiple() {
//		Mut<Map<String, Object>> results = Mut.of();
//
//		new CommandAPICommand("test")
//			.withArguments(new NBTCompoundArgument<Map<String, Object>>("nbt"))
//			.executesPlayer((player, args) -> {
//				results.set((Map<String, Object>) args.get("nbt"));
//			})
//			.register();
//
//		Player player = addPlayer();
//
//		// /test {val1:10,val2:20}
//		server.dispatchCommand(player, "test {val1:10,val2:20}");
//		Map<String, Object> result = results.get();
//		assertEquals(10, result.get("val1"));
//		assertEquals(20, result.get("val2"));
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
//			.withArguments(new NBTCompoundArgument<Map<String, Object>>("nbt"))
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
