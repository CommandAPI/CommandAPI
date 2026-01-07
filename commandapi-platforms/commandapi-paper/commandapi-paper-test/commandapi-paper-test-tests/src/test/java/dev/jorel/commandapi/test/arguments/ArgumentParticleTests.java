// TODO: Not sure about version stuff yet
//package dev.jorel.commandapi.test.arguments;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertInstanceOf;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.junit.jupiter.api.Assumptions.assumeFalse;
//import static org.junit.jupiter.api.Assumptions.assumeTrue;
//
//import java.util.Set;
//import java.util.concurrent.ThreadLocalRandom;
//
//import org.bukkit.Bukkit;
//import org.bukkit.Color;
//import org.bukkit.Material;
//import org.bukkit.Particle;
//import org.bukkit.Particle.DustOptions;
//import org.bukkit.Particle.DustTransition;
//import org.bukkit.Vibration;
//import org.bukkit.Vibration.Destination.BlockDestination;
//import org.bukkit.block.data.BlockData;
//import org.bukkit.block.data.Snowable;
//import org.bukkit.inventory.ItemStack;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.RepeatedTest;
//import org.junit.jupiter.api.Test;
//
//import be.seeseemelk.mockbukkit.entity.PlayerMock;
//import dev.jorel.commandapi.CommandAPICommand;
//import dev.jorel.commandapi.MCVersion;
//import dev.jorel.commandapi.arguments.ParticleArgument;
//import dev.jorel.commandapi.test.MockPlatform;
//import dev.jorel.commandapi.test.Mut;
//import dev.jorel.commandapi.test.TestBase;
//import dev.jorel.commandapi.wrappers.ParticleData;
//
///**
// * Tests for the {@link ParticleArgument}
// */
//class ArgumentParticleTests extends TestBase {
//
//	/*********
//	 * Setup *
//	 *********/
//
//	@BeforeEach
//	public void setUp() {
//		super.setUp();
//
//		// Disabled for 1.20.3+ due to "reasons"
//		// assumeTrue(version.lessThan(MCVersion.V1_20_3));
//
//		/**
//		 * From https://misode.github.io/versions/?id=1.20.5-pre1&tab=changelog&tags=command
//		 *
//		 * Changed the /particle command syntax for particles with extra options:
//
//block redstone_lamp[lit=true] → block{block_state:{Name:"redstone_lamp",Properties:{lit:"true"}}} (also for block_marker, falling_dust, and dust_pillar)
//dust 0.1 0.2 0.3 0.4 → dust{color:[0.1,0.2,0.3],scale:0.4}
//dust_color_transition 0.1 0.2 0.3 0.4 0.5 0.6 0.7 → dust_color_transition{from_color:[0.1,0.2,0.3],scale:0.4,to_color:[0.5,0.6,0.7]}
//entity_effect → entity_effect{color:[0.1,0.2,0.3,0.4]}
//item diamond → item{item:"diamond"}
//sculk_charge 0.1 → sculk_charge{roll:0.1}
//shriek 1 → shriek{delay:1}
//vibration 0.1 0.2 0.3 4 → vibration{destination:{type:"block",pos:[0.1,0.2,0.3]},arrival_in_ticks:4}
//		 */
//	}
//
//	@AfterEach
//	public void tearDown() {
//		super.tearDown();
//	}
//
//	private Set<Particle> dodgyParticles = Set.of(
//		Particle.SNOW_SHOVEL, // "item_snowball" -> SNOWBALL
//		Particle.SUSPENDED_DEPTH // "underwater" -> SUSPENDED
//	);
//
//	private float round(float value, int n) {
//		return (float) (Math.round(value * Math.pow(10, n)) / Math.pow(10, n));
//	}
//
//	/*********
//	 * Tests *
//	 *********/
//
//	@Test
//	void executionTestWithParticleArgumentVoid() {
//		Mut<ParticleData<?>> results = Mut.of();
//
//		new CommandAPICommand("test")
//			.withArguments(new ParticleArgument("particle"))
//			.executesPlayer((player, args) -> {
//				results.set((ParticleData<?>) args.get("particle"));
//			})
//			.register();
//
//		Player player = addPlayer();
//
//		for (Particle particle : Particle.values()) {
//			if (particle.getDataType().equals(Void.class) && !dodgyParticles.contains(particle)) {
//				if (version.greaterThanOrEqualTo(MCVersion.V1_20_5) && particle.name().equals("SPELL_MOB")) {
//					// SPELL_MOB (entity_effect) has an additional parameter in 1.20.5 onwards
//					continue;
//				} else {
//					String particleName = MockPlatform.getInstance().getNMSParticleNameFromBukkit(particle);
//					if (particleName != null) {
//						server.dispatchCommand(player, "test " + particleName);
//					} else {
//						continue;
//					}
//				}
//				assertEquals(particle, results.get().particle());
//			}
//		}
//
//		assertNoMoreResults(results);
//	}
//
//	@Test
//	void executionTestWithParticleArgumentDust() {
//		Mut<ParticleData<?>> results = Mut.of();
//
//		new CommandAPICommand("test")
//			.withArguments(new ParticleArgument("particle"))
//			.executesPlayer((player, args) -> {
//				results.set((ParticleData<?>) args.get("particle"));
//			})
//			.register();
//
//		Player player = addPlayer();
//
//		// dust red green blue size, where red, green and blue are between 0 and 1
//		if (version.greaterThanOrEqualTo(MCVersion.V1_20_5)) {
//			server.dispatchCommand(player, "test dust{color:[1.0f,0.5f,0.0f],scale:4.0f}");
//		} else {
//			server.dispatchCommand(player, "test dust 1 0.5 0 4");
//		}
//		@SuppressWarnings("unchecked")
//		ParticleData<DustOptions> result = (ParticleData<DustOptions>) results.get();
//
//		// Check the particle type is correct
//		assertEquals(Particle.REDSTONE, result.particle());
//
//		// Check the particle properties
//		assertEquals(4, result.data().getSize());
//		assertEquals(Color.fromRGB(255, 127, 0), result.data().getColor());
//
//		assertNoMoreResults(results);
//	}
//
//	@RepeatedTest(10)
//	void executionTestWithParticleArgumentDustRandom() {
//		Mut<ParticleData<?>> results = Mut.of();
//
//		new CommandAPICommand("test")
//			.withArguments(new ParticleArgument("particle"))
//			.executesPlayer((player, args) -> {
//				results.set((ParticleData<?>) args.get("particle"));
//			})
//			.register();
//
//		Player player = addPlayer();
//
//		// dust red green blue size, where red, green and blue are between 0 and 1
//		float red = round(ThreadLocalRandom.current().nextFloat(), 2);
//		float green = round(ThreadLocalRandom.current().nextFloat(), 2);
//		float blue = round(ThreadLocalRandom.current().nextFloat(), 2);
//
//		// I don't know if there's a hard limit, but if this value is greater than 4, it
//		// caps it off at 4.
//		int size = ThreadLocalRandom.current().nextInt(1, 5);
//
//		if (version.greaterThanOrEqualTo(MCVersion.V1_20_5)) {
//			server.dispatchCommand(player, "test dust{color:[%sf,%sf,%sf],scale:%d.0f}".formatted(red, green, blue, size));
//		} else {
//			server.dispatchCommand(player, "test dust %s %s %s %d".formatted(red, green, blue, size));
//		}
//		@SuppressWarnings("unchecked")
//		ParticleData<DustOptions> result = (ParticleData<DustOptions>) results.get();
//
//		// Check the particle type is correct
//		assertEquals(Particle.REDSTONE, result.particle());
//
//		// Check the particle properties
//		assertEquals(size, result.data().getSize());
//		assertEquals(Color.fromRGB((int) (red * 255), (int) (green * 255), (int) (blue * 255)), result.data().getColor());
//
//		assertNoMoreResults(results);
//	}
//
//	@Test
//	void executionTestWithParticleArgumentDustColorTransition() {
//		Mut<ParticleData<?>> results = Mut.of();
//
//		new CommandAPICommand("test")
//			.withArguments(new ParticleArgument("particle"))
//			.executesPlayer((player, args) -> {
//				results.set((ParticleData<?>) args.get("particle"));
//			})
//			.register();
//
//		Player player = addPlayer();
//
//		// dust_color_transition from_red from_green from_blue size to_red to_green to_blue, where red, green and blue are between 0 and 1
//		//
//		if (version.greaterThanOrEqualTo(MCVersion.V1_20_5)) {
//			server.dispatchCommand(player, "test dust_color_transition{from_color:[0.1,0.2,0.3],scale:0.4,to_color:[0.5,0.6,0.7]}");
//		} else {
//			server.dispatchCommand(player, "test dust_color_transition 0.1 0.2 0.3 0.4 0.5 0.6 0.7");
//		}
//		@SuppressWarnings("unchecked")
//		ParticleData<DustTransition> result = (ParticleData<DustTransition>) results.get();
//
//		// Check the particle type is correct
//		assertEquals(Particle.DUST_COLOR_TRANSITION, result.particle());
//
//		// Check the particle properties
//		assertEquals(Color.fromRGB((int) (0.1f * 255), (int) (0.2f * 255), (int) (0.3f * 255)), result.data().getColor());
//		assertEquals(0.4f, result.data().getSize());
//		assertEquals(Color.fromRGB((int) (0.5f * 255), (int) (0.6f * 255), (int) (0.7f * 255)), result.data().getToColor());
//
//		assertNoMoreResults(results);
//	}
//
//	@Test
//	void executionTestWithParticleArgumentBlock() {
//		Mut<ParticleData<?>> results = Mut.of();
//
//		new CommandAPICommand("test")
//			.withArguments(new ParticleArgument("particle"))
//			.executesPlayer((player, args) -> {
//				results.set((ParticleData<?>) args.get("particle"));
//			})
//			.register();
//
//		Player player = addPlayer();
//
//		if (version.greaterThanOrEqualTo(MCVersion.V1_20_5)) {
//			// block{block_state{state}}
//			server.dispatchCommand(player, "test block{block_state:{Name:\"minecraft:grass_block\",Properties:{snowy:\"true\"}}}");
//		} else {
//			// block block_type[meta]
//			server.dispatchCommand(player, "test block minecraft:grass_block[snowy=true]");
//		}
//
//		@SuppressWarnings("unchecked")
//		ParticleData<BlockData> result = (ParticleData<BlockData>) results.get();
//
//		// Check the particle type is correct
//		assertEquals(Particle.BLOCK_CRACK, result.particle());
//
//		// Check the particle properties
//		assertEquals(Material.GRASS_BLOCK, result.data().getMaterial());
//		assertTrue(((Snowable) result.data()).isSnowy());
//
//		assertNoMoreResults(results);
//	}
//
//	@Test
//	void executionTestWithParticleArgumentItem() {
//		Mut<ParticleData<?>> results = Mut.of();
//
//		new CommandAPICommand("test")
//			.withArguments(new ParticleArgument("particle"))
//			.executesPlayer((player, args) -> {
//				results.set((ParticleData<?>) args.get("particle"));
//			})
//			.register();
//
//		Player player = addPlayer();
//
//		if (version.greaterThanOrEqualTo(MCVersion.V1_20_5)) {
//			// item{item:"item_id"}
//			server.dispatchCommand(player, "test item{item:\"apple\"}");
//		} else {
//			// item item_id
//			server.dispatchCommand(player, "test item apple");
//		}
//		@SuppressWarnings("unchecked")
//		ParticleData<ItemStack> result = (ParticleData<ItemStack>) results.get();
//
//		// Check the particle type is correct
//		assertEquals(Particle.ITEM_CRACK, result.particle());
//
//		// Check the particle properties
//		assertEquals(new ItemStack(Material.APPLE), (ItemStack) result.data());
//
//		assertNoMoreResults(results);
//	}
//
//	@Test
//	void executionTestWithParticleArgumentEntityEffect() {
//		// Only effective from 1.20.5+
//		assumeTrue(version.greaterThanOrEqualTo(MCVersion.V1_20_5));
//
//		Mut<ParticleData<?>> results = Mut.of();
//
//		new CommandAPICommand("test")
//			.withArguments(new ParticleArgument("particle"))
//			.executesPlayer((player, args) -> {
//				results.set(args.getUnchecked("particle"));
//			})
//			.register();
//
//		Player player = addPlayer();
//
//		// entity_effect{color:[r,g,b,a]}
//		// (red entity effect, fully visible)
//		server.dispatchCommand(player, "test entity_effect{color:[1.0,0.0,0.0,1.0]}");
//
//		@SuppressWarnings("unchecked")
//		ParticleData<Color> result = (ParticleData<Color>) results.get();
//
//		assumeFalse(Bukkit.getBukkitVersion().equals("1.20.1-R0.1-SNAPSHOT"));
//
//		// Check the particle type is correct
//		assertEquals(Particle.valueOf("ENTITY_EFFECT"), result.particle());
//
//		// Check the particle properties
//		assertEquals(255, result.data().getRed());
//		assertEquals(0, result.data().getGreen());
//		assertEquals(0, result.data().getBlue());
//		assertEquals(255, result.data().getAlpha());
//
//		assertNoMoreResults(results);
//	}
//
//	@Test
//	void executionTestWithParticleArgumentVibration() {
//		Mut<ParticleData<?>> results = Mut.of();
//
//		new CommandAPICommand("test")
//			.withArguments(new ParticleArgument("particle"))
//			.executesPlayer((player, args) -> {
//				results.set((ParticleData<?>) args.get("particle"));
//			})
//			.register();
//
//		Player player = addPlayer();
//
//		// vibration 1.0 2.0 3.0 4
//		// vibration{destination:{type:\"block\",pos:[1.0,2.0,3.0]},arrival_in_ticks:4}
//		if (version.greaterThanOrEqualTo(MCVersion.V1_20_5)) {
//			server.dispatchCommand(player, "test vibration{destination:{type:\"block\",pos:[1.0,2.0,3.0]},arrival_in_ticks:4}");
//		} else {
//			server.dispatchCommand(player, "test vibration 1.0 2.0 3.0 4");
//		}
//
//		@SuppressWarnings("unchecked")
//		ParticleData<Vibration> result = (ParticleData<Vibration>) results.get();
//
//		// Check the particle type is correct
//		assertEquals(Particle.VIBRATION, result.particle());
//
//		// Check the particle properties
//		assertEquals(4, result.data().getArrivalTime());
//		assertInstanceOf(BlockDestination.class, result.data().getDestination());
//
//		BlockDestination blockDestination = (BlockDestination) result.data().getDestination();
//		assertEquals(1, blockDestination.getLocation().getBlockX());
//		assertEquals(2, blockDestination.getLocation().getBlockY());
//		assertEquals(3, blockDestination.getLocation().getBlockZ());
//
//		assertNoMoreResults(results);
//	}
//
//}
