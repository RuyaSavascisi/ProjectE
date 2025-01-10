package moze_intel.projecte.config;

import com.google.gson.JsonParseException;
import java.util.Map;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.config.CustomEMCParser.CustomEMCFile;
import moze_intel.projecte.impl.codec.CodecTestHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
@DisplayName("Test parsing Custom EMC files")
class CustomEMCParserTest {

	private static CustomEMCFile parseJson(HolderLookup.Provider registryAccess, String json) {
		return CodecTestHelper.parseJson(registryAccess, CustomEMCParser.CustomEMCFile.CODEC, "custom emc test", json);
	}

	@BeforeAll
	@DisplayName("Manually load the default supported codecs")
	static void setupBuiltinCodecs() {
		//Registry init does not happen for tests, so we need to manually add our codecs
		CodecTestHelper.initBuiltinNSS();
	}

	@Test
	@DisplayName("Test custom emc file that is empty")
	void testEmpty(MinecraftServer server) {
		//New format that just uses it as an array of NSS -> emc
		CustomEMCFile customEMCFile = parseJson(server.registryAccess(), """
				{
					"entries": {
					}
				}""");
		Assertions.assertNull(customEMCFile.comment());
		Assertions.assertEquals(0, customEMCFile.entries().size());
		//Legacy format using an array that lists the item and the emc value as separate values in the object
		CustomEMCFile customEMCFileLegacy = parseJson(server.registryAccess(), """
				{
					"entries": [
					]
				}""");
		Assertions.assertNull(customEMCFileLegacy.comment());
		Assertions.assertEquals(0, customEMCFileLegacy.entries().size());
	}

	@Test
	@DisplayName("Test custom emc file that only contains a comment")
	void testCommentOnly(MinecraftServer server) {
		//New format that just uses it as an array of NSS -> emc
		CustomEMCFile customEMCFile = parseJson(server.registryAccess(), """
				{
					"comment": "A very simple Example",
					"entries": {
					}
				}""");
		Assertions.assertEquals("A very simple Example", customEMCFile.comment());
		Assertions.assertEquals(0, customEMCFile.entries().size());
		//Legacy format using an array that lists the item and the emc value as separate values in the object
		CustomEMCFile customEMCFileLegacy = parseJson(server.registryAccess(),  """
				{
					"comment": "A very simple Example",
					"entries": [
					]
				}""");
		Assertions.assertEquals("A very simple Example", customEMCFileLegacy.comment());
		Assertions.assertEquals(0, customEMCFileLegacy.entries().size());
	}

	@Test
	@DisplayName("Test custom emc file with a few entries")
	void testSimple(MinecraftServer server) {
		//New format that just uses it as an array of NSS -> emc
		CustomEMCFile customEMCFile = parseJson(server.registryAccess(), """
				{
					"entries": {
						"minecraft:dirt": 1,
						"minecraft:stone": 2,
						"#c:ingots/iron": 3
					}
				}""");
		Map<NSSItem, Long> entries = customEMCFile.entries();
		Assertions.assertEquals(3, entries.size());
		Assertions.assertEquals(1, entries.get(NSSItem.createItem(Items.DIRT)));
		Assertions.assertEquals(2, entries.get(NSSItem.createItem(Items.STONE)));
		Assertions.assertEquals(3, entries.get(NSSItem.createTag(Tags.Items.INGOTS_IRON)));
		//Legacy format using an array that lists the item and the emc value as separate values in the object
		CustomEMCFile customEMCFileLegacy = parseJson(server.registryAccess(), """
				{
					"entries": [
						{
							"item": "minecraft:dirt",
							"emc": 1
						},
						{
							"item": "minecraft:stone",
							"emc": 2
						},
						{
							"item": "#c:ingots/iron",
							"emc": 3
						}
					]
				}""");
		Map<NSSItem, Long> legacyEntries = customEMCFileLegacy.entries();
		Assertions.assertEquals(3, legacyEntries.size());
		Assertions.assertEquals(1, legacyEntries.get(NSSItem.createItem(Items.DIRT)));
		Assertions.assertEquals(2, legacyEntries.get(NSSItem.createItem(Items.STONE)));
		Assertions.assertEquals(3, legacyEntries.get(NSSItem.createTag(Tags.Items.INGOTS_IRON)));
	}

	@Test
	@DisplayName("Test custom emc file using a mix of legacy and extended legacy")
	void testMixedLegacy(MinecraftServer server) {
		CustomEMCFile customEMCFile = parseJson(server.registryAccess(), """
				{
					"entries": [
						{
							"item": "minecraft:dirt",
							"emc": 1
						},
						{
							"id": "minecraft:stone",
							"emc": 2
						},
						{
							"tag": "c:ingots/iron",
							"emc": 3
						}
					]
				}""");
		Map<NSSItem, Long> entries = customEMCFile.entries();
		Assertions.assertEquals(3, entries.size());
		Assertions.assertEquals(1, entries.get(NSSItem.createItem(Items.DIRT)));
		Assertions.assertEquals(2, entries.get(NSSItem.createItem(Items.STONE)));
		Assertions.assertEquals(3, entries.get(NSSItem.createTag(Tags.Items.INGOTS_IRON)));
	}

	@Test
	@DisplayName("Test custom emc file with an entry that is a long")
	void testCustomEmcFileWithLongValue(MinecraftServer server) {
		//New format that just uses it as an array of NSS -> emc
		CustomEMCFile customEMCFile = parseJson(server.registryAccess(), """
				{
					"entries": {
						"minecraft:dirt": 2147483648
					}
				}""");
		Map<NSSItem, Long> entries = customEMCFile.entries();
		Assertions.assertEquals(1, entries.size());
		//Max int + 1
		Assertions.assertEquals(2_147_483_648L, entries.get(NSSItem.createItem(Items.DIRT)));
		//Legacy format using an array that lists the item and the emc value as separate values in the object
		CustomEMCFile customEMCFileLegacy = parseJson(server.registryAccess(), """
				{
					"entries": [
						{
							"item": "minecraft:dirt",
							"emc": 2147483648
						}
					]
				}""");
		Map<NSSItem, Long> legacyEntries = customEMCFileLegacy.entries();
		Assertions.assertEquals(1, legacyEntries.size());
		//Max int + 1
		Assertions.assertEquals(2_147_483_648L, legacyEntries.get(NSSItem.createItem(Items.DIRT)));
	}

	@Test
	@DisplayName("Test custom emc file with an invalid value")
	void testCustomEmcFileWithInvalidValue(MinecraftServer server) {
		//New format that just uses it as an array of NSS -> emc
		Assertions.assertThrows(JsonParseException.class, () -> parseJson(server.registryAccess(), """
				{
					"entries": {
						"minecraft:dirt": -1
					}
				}"""));
		//Legacy format using an array that lists the item and the emc value as separate values in the object
		Assertions.assertThrows(JsonParseException.class, () -> parseJson(server.registryAccess(), """
				{
					"entries": [
						{
							"item": "minecraft:dirt",
							"emc": -1
						}
					]
				}"""));
	}

	@Test
	@DisplayName("Test custom emc file with an invalid value")
	void testInvalidKeyAndValue(MinecraftServer server) {
		//New format that just uses it as an array of NSS -> emc
		//Note: We validate this doesn't throw as invalid keys in the new format are just entirely ignored and their values are not checked
		Assertions.assertDoesNotThrow(() -> parseJson(server.registryAccess(), """
				{
					"entries": {
						"INVALID|minecraft:dirt": -1
					}
				}"""));
		//Legacy format using an array that lists the item and the emc value as separate values in the object
		Assertions.assertThrows(JsonParseException.class, () -> parseJson(server.registryAccess(), """
				{
					"entries": [
						{
							"invalid": "minecraft:dirt",
							"emc": -1
						}
					]
				}"""));
	}

	@Test
	@DisplayName("Test ignoring invalid keys in a custom emc file")
	void testIgnoreInvalidKeys(MinecraftServer server) {
		//New format that just uses it as an array of NSS -> emc
		CustomEMCFile customEMCFile = parseJson(server.registryAccess(), """
				{
					"entries": {
						"INVALID|minecraft:dirt": 1,
						"minecraft:stone": 2
					}
				}""");
		Map<NSSItem, Long> entries = customEMCFile.entries();
		Assertions.assertEquals(1, entries.size());
		Assertions.assertEquals(2, entries.get(NSSItem.createItem(Items.STONE)));
		//Legacy format using an array that lists the item and the emc value as separate values in the object
		CustomEMCFile customEMCFileLegacy = parseJson(server.registryAccess(), """
				{
					"entries": [
						{
							"item": "INVALID|minecraft:dirt",
							"emc": 1
						},
						{
							"id": "INVALID|minecraft:dirt",
							"emc": 1
						},
						{
							"item": "minecraft:stone",
							"emc": 2
						},
						{
							"fluid": "minecraft:stone",
							"emc": 2
						}
					]
				}""");
		Map<NSSItem, Long> legacyEntries = customEMCFileLegacy.entries();
		Assertions.assertEquals(1, legacyEntries.size());
		Assertions.assertEquals(2, legacyEntries.get(NSSItem.createItem(Items.STONE)));
	}

	@Test
	@DisplayName("Test custom emc file with values of zero")
	void testCustomEmcFileWithZero(MinecraftServer server) {
		//New format that just uses it as an array of NSS -> emc
		CustomEMCFile customEMCFile = parseJson(server.registryAccess(), """
				{
					"entries": {
						"minecraft:dirt": 0
					}
				}""");
		Map<NSSItem, Long> entries = customEMCFile.entries();
		Assertions.assertEquals(1, entries.size());
		Assertions.assertEquals(0, entries.get(NSSItem.createItem(Items.DIRT)));
		//Legacy format using an array that lists the item and the emc value as separate values in the object
		CustomEMCFile customEMCFileLegacy = parseJson(server.registryAccess(), """
				{
					"entries": [
						{
							"item": "minecraft:dirt",
							"emc": 0
						}
					]
				}""");
		Map<NSSItem, Long> legacyEntries = customEMCFileLegacy.entries();
		Assertions.assertEquals(1, legacyEntries.size());
		Assertions.assertEquals(0, legacyEntries.get(NSSItem.createItem(Items.DIRT)));
	}

	@Test
	@DisplayName("Test custom emc file with items dependent on data components")
	void testCustomEmcFileWithDC(MinecraftServer server) {
		//New format that just uses it as an array of NSS -> emc
		CustomEMCFile customEMCFile = parseJson(server.registryAccess(), """
				{
					"entries": {
						"minecraft:dirt[custom_data={my: \\"tag\\"}]": 1
					}
				}""");
		Map<NSSItem, Long> entries = customEMCFile.entries();
		Assertions.assertEquals(1, entries.size());
		Assertions.assertEquals(1, entries.get(NSSItem.createItem(Items.DIRT, CodecTestHelper.MY_TAG_PATCH)));
		//Legacy format using an array that lists the item and the emc value as separate values in the object
		CustomEMCFile customEMCFileLegacy = parseJson(server.registryAccess(), """
				{
					"entries": [
						{
							"item": "minecraft:dirt[custom_data={my: \\"tag\\"}]",
							"emc": 1
						}
					]
				}""");
		Map<NSSItem, Long> legacyEntries = customEMCFileLegacy.entries();
		Assertions.assertEquals(1, legacyEntries.size());
		Assertions.assertEquals(1, legacyEntries.get(NSSItem.createItem(Items.DIRT, CodecTestHelper.MY_TAG_PATCH)));
		//Expanded legacy format using an array that lists the item and the emc value as separate values in the object but supporting using the explicit format for representing the item
		CustomEMCFile customEMCFileExtendedLegacy = parseJson(server.registryAccess(), """
				{
					"entries": [
						{
							"id": "minecraft:dirt",
							"data": {
								"custom_data": {
									"my": "tag"
								}
							},
							"emc": 1
						},
						{
							"id": "minecraft:stone",
							"data": {
								"custom_data": "{my: \\"tag\\"}"
							},
							"emc": 2
						}
					]
				}""");
		Map<NSSItem, Long> extendedLegacyEntries = customEMCFileExtendedLegacy.entries();
		Assertions.assertEquals(2, extendedLegacyEntries.size());
		Assertions.assertEquals(1, extendedLegacyEntries.get(NSSItem.createItem(Items.DIRT, CodecTestHelper.MY_TAG_PATCH)));
		Assertions.assertEquals(2, extendedLegacyEntries.get(NSSItem.createItem(Items.STONE, CodecTestHelper.MY_TAG_PATCH)));
	}

	@Test
	@DisplayName("Test custom emc file using the extended legacy format")
	void testCustomEmcFileWithExtendedLegacy(MinecraftServer server) {
		//Expanded legacy format using an array that lists the item and the emc value as separate values in the object but supporting using the explicit format for representing the item
		CustomEMCFile customEMCFile = parseJson(server.registryAccess(), """
				{
					"entries": [
						{
							"id": "minecraft:dirt",
							"emc": 1
						},
						{
							"tag": "c:cobblestones",
							"emc": 2
						}
					]
				}""");
		Map<NSSItem, Long> entries = customEMCFile.entries();
		Assertions.assertEquals(2, entries.size());
		Assertions.assertEquals(1, entries.get(NSSItem.createItem(Items.DIRT)));
		Assertions.assertEquals(2, entries.get(NSSItem.createTag(Tags.Items.COBBLESTONES)));
	}
}