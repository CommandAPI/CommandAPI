package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.EndPortalFrame;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandAPIVersionHandler;
import dev.jorel.commandapi.arguments.BlockStateArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;

/**
 * Tests for the {@link BlockStateArgument}
 */
class ArgumentBlockStateTests extends TestBase {

	/*********
	 * Setup *
	 *********/

	@BeforeEach
	public void setUp() {
		super.setUp();
	}

	@AfterEach
	public void tearDown() {
		super.tearDown();
	}

	/*********
	 * Tests *
	 *********/
	
	// Block states can be looked up here: https://minecraft.wiki/w/Block_states

	@Test
	void executionTestWithBlockStateArgument() {
		Mut<BlockState> results = Mut.of();

		new CommandAPICommand("test")
		.withArguments(new BlockStateArgument("blockstate"))
		.executesPlayer((player, args) -> {
			results.set(args.getUnchecked(0));
		})
		.register();

		PlayerMock player = server.addPlayer();

		// /test dirt
		server.dispatchCommand(player, "test dirt");
		assertEquals(Material.DIRT, results.get().getType());

		// /test end_portal_frame[eye=true,facing=north]
		// This tests block data metadata which is accessible via BlockData and
		// its instances
		server.dispatchCommand(player, "test end_portal_frame[eye=true,facing=north]");
		BlockData endPortalFrameBlockData = results.get().getBlockData();
		assertEquals(Material.END_PORTAL_FRAME, endPortalFrameBlockData.getMaterial());
		assertInstanceOf(EndPortalFrame.class, endPortalFrameBlockData);
		EndPortalFrame frame = (EndPortalFrame) endPortalFrameBlockData;
		assertTrue(frame.hasEye());
		assertEquals(BlockFace.NORTH, frame.getFacing());

		// /test chest{CustomName:"\"Custom Name\""}
		// TODO: Not overly important, but we don't actually set any NBT, so it's
		// impossible to look up any NBT info
		// On Spigot, creating the chest's block entity initializes CraftBukkit's SpigotTimings, which needs a
		//  field that only Spigot's JavaPluginLoader has. MockBukkit puts paper-api's version on the classpath.
		if (CommandAPIVersionHandler.IS_PAPER) {
			server.dispatchCommand(player, "test chest{CustomName:\"\\\"Custom Name\\\"\"}");
			assertEquals("minecraft:chest[facing=north,type=single,waterlogged=false]", results.get().getBlockData().getAsString());
		}

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	// TODO

}
