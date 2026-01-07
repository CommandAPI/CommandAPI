package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BlockStateArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.EndPortalFrame;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
		Mut<BlockData> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new BlockStateArgument("blockstate"))
			.executesPlayer((player, args) -> {
				results.set(args.getUnchecked(0));
			})
			.register();

		Player player = addPlayer();

		// /test dirt
		getServer().dispatchCommand(player, "test dirt");
		assertEquals(Material.DIRT, results.get().getMaterial());

		// /test end_portal_frame[eye=true,facing=north]
		// This tests block data metadata which is accessible via BlockData and
		// its instances
		getServer().dispatchCommand(player, "test end_portal_frame[eye=true,facing=north]");
		BlockData endPortalFrameBlockData = results.get();
		assertEquals(Material.END_PORTAL_FRAME, endPortalFrameBlockData.getMaterial());
		assertInstanceOf(EndPortalFrame.class, endPortalFrameBlockData);
		EndPortalFrame frame = (EndPortalFrame) endPortalFrameBlockData;
		assertTrue(frame.hasEye());
		assertEquals(BlockFace.NORTH, frame.getFacing());

		// /test chest{CustomName:"\"Custom Name\""}
		// TODO: Not overly important, but we don't actually set any NBT, so it's
		// impossible to look up any NBT info
		getServer().dispatchCommand(player, "test chest{CustomName:\"\\\"Custom Name\\\"\"}");
		assertEquals("minecraft:chest[facing=north,type=single,waterlogged=false]", results.get().getAsString());

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	// TODO

}
