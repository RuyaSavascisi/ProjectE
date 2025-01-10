package moze_intel.projecte.emc;

import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import moze_intel.projecte.api.codec.IPECodecHelper;
import moze_intel.projecte.api.nss.NSSFake;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.impl.codec.CodecTestHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

//TODO - 1.21: Add some tests that actually test serialization as all of these (and the ones for the other tests) only test deserialization
@DisplayName("Test Serialization of Normalized Simple Stacks")
class SerializationTest {

	private static NormalizedSimpleStack deserializeLegacyNSS(String jsonString) {
		//TODO - 1.21: Do we need to create a serialization context?
		return IPECodecHelper.INSTANCE.legacyNSSCodec().parse(JsonOps.INSTANCE, new JsonPrimitive(jsonString)).getOrThrow(JsonParseException::new);
	}

	private static NormalizedSimpleStack parseJson(String json) {
		return CodecTestHelper.parseJson(IPECodecHelper.INSTANCE.nssCodec(), "serialization test", json);
	}

	@BeforeAll
	@DisplayName("Manually load the default supported codecs")
	static void setupBuiltinCodecs() {
		//Registry init does not happen for tests, so we need to manually add our codecs
		CodecTestHelper.initBuiltinNSS();
	}

	@Test
	@DisplayName("Test Serialization of a valid Item")
	void testValidItemSerialization() {
		NSSItem expected = NSSItem.createItem(Items.DIRT);
		Assertions.assertEquals(expected, deserializeLegacyNSS("minecraft:dirt"));
		//Test explicit syntax
		Assertions.assertEquals(expected, parseJson("""
				{
					"type": "projecte:item",
					"id": "minecraft:dirt"
				}"""));
	}

	@Test
	@DisplayName("Test Serialization of a valid Item with prefix included")
	void testValidItemSerializationAlt() {
		Assertions.assertEquals(NSSItem.createItem(Items.DIRT), deserializeLegacyNSS("ITEM|minecraft:dirt"));
	}

	@Test
	@DisplayName("Test Serialization of an invalid Item")
	void testInvalidItemSerialization() {
		Assertions.assertThrows(JsonParseException.class, () -> deserializeLegacyNSS("minecraft:Dirt"));
		//Test explicit syntax
		Assertions.assertThrows(JsonParseException.class, () -> parseJson("""
				{
					"type": "projecte:item",
					"id": "minecraft:Dirt"
				}"""));
	}

	@Test
	@DisplayName("Test Serialization of an Item with Data Components")
	void testItemDCSerialization() {
		NSSItem expected = NSSItem.createItem(Items.DIRT, CodecTestHelper.MY_TAG_PATCH);
		Assertions.assertEquals(expected, deserializeLegacyNSS("minecraft:dirt[custom_data={my: \"tag\"}]"));
		//Test explicit syntax
		Assertions.assertEquals(expected, parseJson("""
				{
					"type": "projecte:item",
					"id": "minecraft:dirt",
					"data": {
						"custom_data": "{my: \\"tag\\"}"
					}
				}"""));
		//Alternate data component format
		Assertions.assertEquals(expected, parseJson("""
				{
					"type": "projecte:item",
					"id": "minecraft:dirt",
					"data": {
						"custom_data": {
							"my": "tag"
						}
					}
				}"""));
	}

	@Test
	@DisplayName("Test Serialization of a valid Item Tag")
	void testValidItemTagSerialization() {
		NSSItem expected = NSSItem.createTag(Tags.Items.COBBLESTONES);
		Assertions.assertEquals(expected, deserializeLegacyNSS("#c:cobblestones"));
		//Test explicit syntax
		Assertions.assertEquals(expected, parseJson("""
				{
					"type": "projecte:item",
					"tag": "c:cobblestones"
				}"""));
	}

	@Test
	@DisplayName("Test Serialization of an invalid Item Tag")
	void testInvalidItemTagSerialization() {
		Assertions.assertThrows(JsonParseException.class, () -> deserializeLegacyNSS("#minecraft:TAG"));
		Assertions.assertThrows(JsonParseException.class, () -> deserializeLegacyNSS("#TAG"));
		//Test explicit syntax
		Assertions.assertThrows(JsonParseException.class, () -> parseJson("""
				{
					"type": "projecte:item",
					"tag": "minecraft:TAG"
				}"""));
		//Explicit with # (which makes it invalid)
		Assertions.assertThrows(JsonParseException.class, () -> parseJson("""
				{
					"type": "projecte:item",
					"tag": "#c:cobblestones"
				}"""));
	}

	@Test
	@DisplayName("Test Serialization of an Item Tag with Data Components")
	void testItemTagDCSerialization() {
		Assertions.assertThrows(JsonParseException.class, () -> deserializeLegacyNSS("#c:cobblestones{my: \"tag\"}"));
	}

	@Test
	@DisplayName("Test Serialization of an Explicit Item Tag with Data Components")
	void testExplicitItemTagDCSerialization() {
		//The tag is ignored
		NSSItem expected = NSSItem.createTag(Tags.Items.COBBLESTONES);
		Assertions.assertEquals(expected, parseJson("""
				{
					"type": "projecte:item",
					"tag": "c:cobblestones",
					"data": {
						"custom_data": {
							"my": "tag"
						}
					}
				}"""));
	}

	@Test
	@DisplayName("Test Serialization of a valid Fluid")
	void testValidFluidSerialization() {
		NSSFluid expected = NSSFluid.createFluid(Fluids.WATER);
		Assertions.assertEquals(expected, deserializeLegacyNSS("FLUID|minecraft:water"));
		//Test explicit syntax
		Assertions.assertEquals(expected, parseJson("""
				{
					"type": "projecte:fluid",
					"id": "minecraft:water"
				}"""));
	}

	@Test
	@DisplayName("Test Serialization of an invalid Fluid")
	void testInvalidFluidSerialization() {
		Assertions.assertThrows(JsonParseException.class, () -> deserializeLegacyNSS("FLUID|minecraft:Milk"));
		//Test explicit syntax
		Assertions.assertThrows(JsonParseException.class, () -> parseJson("""
				{
					"type": "projecte:fluid",
					"id": "minecraft:Milk"
				}"""));
	}

	@Test
	@DisplayName("Test Serialization of a Fluid with Data Components")
	void testFluidDCSerialization() {
		NSSFluid expected = NSSFluid.createFluid(Fluids.WATER, CodecTestHelper.MY_TAG_PATCH);
		Assertions.assertEquals(expected, deserializeLegacyNSS("FLUID|minecraft:water[custom_data={my: \"tag\"}]"));
		//Test explicit syntax
		Assertions.assertEquals(expected, parseJson("""
				{
					"type": "projecte:fluid",
					"id": "minecraft:water",
					"data": {
						"custom_data": "{my: \\"tag\\"}"
					}
				}"""));
		//Alternate data component format
		Assertions.assertEquals(expected, parseJson("""
				{
					"type": "projecte:fluid",
					"id": "minecraft:water",
					"data": {
						"custom_data": {
							"my": "tag"
						}
					}
				}"""));
	}

	@Test
	@DisplayName("Test Serialization of a valid Fluid Tag")
	void testValidFluidTagSerialization() {
		NSSFluid expected = NSSFluid.createTag(Tags.Fluids.MILK);
		Assertions.assertEquals(expected, deserializeLegacyNSS("FLUID|#c:milk"));
		//Test explicit syntax
		Assertions.assertEquals(expected, parseJson("""
				{
					"type": "projecte:fluid",
					"tag": "c:milk"
				}"""));
	}

	@Test
	@DisplayName("Test Serialization of an invalid Fluid Tag")
	void testInvalidFluidTagSerialization() {
		Assertions.assertThrows(JsonParseException.class, () -> deserializeLegacyNSS("FLUID|#c:Milk"));
		Assertions.assertThrows(JsonParseException.class, () -> deserializeLegacyNSS("FLUID|#TAG"));
		//Test explicit syntax
		Assertions.assertThrows(JsonParseException.class, () -> parseJson("""
				{
					"type": "projecte:fluid",
					"tag": "minecraft:Milk"
				}"""));
		//Explicit with # (which makes it invalid)
		Assertions.assertThrows(JsonParseException.class, () -> parseJson("""
				{
					"type": "projecte:fluid",
					"tag": "#c:milk"
				}"""));
	}

	@Test
	@DisplayName("Test Serialization of a Fluid Tag with Data Components")
	void testFluidTagDCSerialization() {
		Assertions.assertThrows(JsonParseException.class, () -> deserializeLegacyNSS("FLUID|#c:milk{my: \"tag\"}"));
		//The tag is ignored
		NSSFluid expected = NSSFluid.createTag(Tags.Fluids.MILK);
		Assertions.assertEquals(expected, parseJson("""
				{
					"type": "projecte:fluid",
					"tag": "c:milk",
					"data": {
						"custom_data": {
							"my": "tag"
						}
					}
				}"""));
	}

	@Test
	@DisplayName("Test Serialization of a FAKE entry")
	void testFake() {
		NSSFake expected = NSSFake.create("MyFakeEntry");
		Assertions.assertEquals(expected, deserializeLegacyNSS("FAKE|MyFakeEntry"));
		//Test explicit syntax
		Assertions.assertEquals(expected, parseJson("""
				{
					"type": "projecte:fake",
					"description": "MyFakeEntry"
				}"""));
		//Optional namespace
		NormalizedSimpleStack withNameSpace = parseJson("""
				{
					"type": "projecte:fake",
					"namespace": "test",
					"description": "MyFakeEntry"
				}""");
		Assertions.assertNotEquals(expected, withNameSpace);
		NSSFake.setCurrentNamespace("test");
		//Recreate the expected to make sure it is set with the correct namespace
		NSSFake expectedWithNamespace = NSSFake.create("MyFakeEntry");
		Assertions.assertEquals(expectedWithNamespace, withNameSpace);
		//Test it without the namespace being present but having set the namespace for NSSFake's instead
		Assertions.assertEquals(expectedWithNamespace, parseJson("""
				{
					"type": "projecte:fake",
					"description": "MyFakeEntry"
				}"""));
	}

	@Test
	@DisplayName("Test Serialization of a FAKE entry with an explicitly empty namespace")
	void testFakeEmptyNamespace() {
		Assertions.assertThrows(JsonParseException.class, () -> parseJson("""
				{
					"type": "projecte:fake",
					"namespace": "",
					"description": "MyFakeEntry"
				}"""));
	}

	@Test
	@DisplayName("Test Serialization of an invalid type")
	void testInvalid() {
		Assertions.assertThrows(JsonParseException.class, () -> deserializeLegacyNSS("INVALID|minecraft:test"));
		//Test explicit syntax
		Assertions.assertThrows(JsonParseException.class, () -> parseJson("""
				{
					"type": "projecte:invalid",
					"id": "minecraft:dirt"
				}"""));
		//Valid type but missing keys for said type
		Assertions.assertThrows(JsonParseException.class, () -> parseJson("""
				{
					"type": "projecte:item",
					"wrong_id": "minecraft:water"
				}"""));
	}
}