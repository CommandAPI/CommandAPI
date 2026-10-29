package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ItemStackArgument;
import dev.jorel.commandapi.test.MockPlatform;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;

/**
 * Tests for the {@link ItemStackArgument}
 */
class ArgumentItemStackTests extends TestBase {

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

	@Test
	void executionTestWithItemStackArgument() {
		Mut<ItemStack> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new ItemStackArgument("item"))
			.executesPlayer((player, args) -> {
				results.set((ItemStack) args.get(0));
			})
			.register();

		PlayerMock player = server.addPlayer();

		// /test dirt
		server.dispatchCommand(player, "test dirt");
		assertEquals(new ItemStack(Material.DIRT), results.get());
		
		// Dev note: We're no longer using assertEquals on the ItemStack
		// object here because these itemstacks will now have ItemMeta
		// which has a reference to the item's components which we can't
		// perform equals against.
		
		// Dev note: To make these tests work, we have to overwrite MockBukkit's
		// ItemFactory with our own, see CommandAPIServerMock#getItemFactory()
		
		// Since 1.20.5, items are given components instead of NBT, see https://minecraft.wiki/w/Data_component_format
		
		// /test minecraft:diamond_sword[enchantments={"minecraft:sharpness":1}]
		{
			server.dispatchCommand(player, "test minecraft:diamond_sword[enchantments={\"minecraft:sharpness\":1}]");
			ItemStack actual = results.get();
			assertEquals(Material.DIAMOND_SWORD, actual.getType());
			assertEquals(1, actual.getAmount());
			assertEquals(1, actual.getEnchantmentLevel(Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft("sharpness"))));
		}
		
		// /test minecraft:leather_chestplate[custom_name={text:"Tunic of Destiny",color:"blue"},lore=[{text:"A magical blue tunic"},{text:"worn by the gods..."}],dyed_color=3949738]
		{
			server.dispatchCommand(player, "test minecraft:leather_chestplate[custom_name={text:\"Tunic of Destiny\",color:\"blue\"},lore=[{text:\"A magical blue tunic\"},{text:\"worn by the gods...\"}],dyed_color=3949738]");
			ItemStack actual = results.get();
			ItemMeta actualMeta = actual.getItemMeta();
			assertEquals(Material.LEATHER_CHESTPLATE, actual.getType());
			assertEquals(1, actual.getAmount());
			assertEquals(ChatColor.BLUE + "Tunic of Destiny", actualMeta.getDisplayName());
			assertEquals(List.of("A magical blue tunic", "worn by the gods..."), actualMeta.getLore());
			assertEquals(Color.fromRGB(3949738), ((LeatherArmorMeta) actualMeta).getColor());
		}
		
		// /test minecraft:diamond_pickaxe[tooltip_display={hidden_components:["enchantments","attribute_modifiers","unbreakable","can_break"]}]
		{
			server.dispatchCommand(player, "test minecraft:diamond_pickaxe[tooltip_display={hidden_components:[\"enchantments\",\"attribute_modifiers\",\"unbreakable\",\"can_break\"]}]");
			ItemStack actual = results.get();
			assertEquals(Material.DIAMOND_PICKAXE, actual.getType());
			assertEquals(1, actual.getAmount());
			assertTrue(actual.getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS));
			assertTrue(actual.getItemMeta().hasItemFlag(ItemFlag.HIDE_ATTRIBUTES));
			assertTrue(actual.getItemMeta().hasItemFlag(ItemFlag.HIDE_UNBREAKABLE));
			assertTrue(actual.getItemMeta().hasItemFlag(ItemFlag.HIDE_DESTROYS));
		}

		// /test firework_rocket[fireworks={explosions:[{shape:"creeper",has_twinkle:true,colors:[I;4312372],fade_colors:[I;11743532]}],flight_duration:1}]
		{
			server.dispatchCommand(player, "test firework_rocket[fireworks={explosions:[{shape:\"creeper\",has_twinkle:true,colors:[I;4312372],fade_colors:[I;11743532]}],flight_duration:1}]");
			ItemStack actual = results.get();
			ItemMeta actualMeta = actual.getItemMeta();
			assertEquals(Material.FIREWORK_ROCKET, actual.getType());
			assertEquals(1, actual.getAmount());
			assertInstanceOf(FireworkMeta.class, actualMeta);

			FireworkMeta fireworkMeta = (FireworkMeta) actualMeta;
			assertEquals(1, fireworkMeta.getEffectsSize());
			assertEquals(1, fireworkMeta.getPower());
			assertTrue(fireworkMeta.getEffects().get(0).hasFlicker());
			assertEquals(FireworkEffect.Type.CREEPER, fireworkMeta.getEffects().get(0).getType());
			assertEquals(Color.fromRGB(4312372), fireworkMeta.getEffects().get(0).getColors().get(0));      // LIME
			assertEquals(Color.fromRGB(11743532), fireworkMeta.getEffects().get(0).getFadeColors().get(0)); // RED
		}

		// /test item_that_doesnt_exist
		// Fails because "item_that_doesnt_exist" isn't a valid item
		assertCommandFailsWith(player, "test item_that_doesnt_exist", "Unknown item 'minecraft:item_that_doesnt_exist' at position 5: test <--[HERE]");
		
		// /test dirt[invalid_component]
		// Fails because "invalid_component" isn't a component
		assertCommandFailsWith(player, "test dirt[invalid_component]", "Unknown item component 'minecraft:invalid_component' at position 10: test dirt[<--[HERE]");

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithItemStackArgument() {
		new CommandAPICommand("test")
			.withArguments(new ItemStackArgument("item"))
			.executesPlayer(P_EXEC)
			.register();

		PlayerMock player = server.addPlayer();

		// /test
		// All items should be suggested
		assertEquals(MockPlatform.getInstance().getAllItemNames(), server.getSuggestions(player, "test "));

		// /test x
		// All items starting with 'a' should be suggested, as well as items which
		// are underscore-separated and start with 'a', such as 'wooden_axe'
		assertEquals(MockPlatform.getInstance().getAllItemNames().stream().filter(s -> s.contains(":a") || s.contains("_a")).toList(), server.getSuggestions(player, "test a"));
		
		// test dirt
		// Completed item names should suggest opening the component list
		assertEquals(List.of("["), server.getSuggestions(player, "test dirt"));
		
		// test dirt[
		// Components are suggested, as well as ! to remove one of the item's default components
		List<String> suggestions = server.getSuggestions(player, "test dirt[");
		assertEquals("!", suggestions.get(0));
		assertTrue(suggestions.contains("minecraft:damage="));
		
		// test dirt[]
		// Nothing is suggested after the component list
		assertEquals(List.of(), server.getSuggestions(player, "test dirt[]"));
	}

}
