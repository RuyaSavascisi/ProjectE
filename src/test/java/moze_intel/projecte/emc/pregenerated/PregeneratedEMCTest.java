package moze_intel.projecte.emc.pregenerated;

import com.google.gson.JsonParseException;
import java.util.Map;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.impl.codec.CodecTestHelper;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test Pregenerated EMC Serialization")
class PregeneratedEMCTest {

	private static Map<ItemInfo, Long> parseJson(String json) {
		return CodecTestHelper.parseJson(PregeneratedEMC.CODEC, "pregnerated emc test", json);
	}

	@BeforeAll
	@DisplayName("Manually load the default supported codecs")
	static void setupBuiltinCodecs() {
		//Registry init does not happen for tests, so we need to manually add our codecs
		CodecTestHelper.initBuiltinNSS();
	}

	@Test
	@DisplayName("Test empty pregen file")
	void testEmptyPregenFile() {
		Map<ItemInfo, Long> pregenerated = parseJson("{}");
		Assertions.assertEquals(0, pregenerated.size());
	}

	@Test
	@DisplayName("Test a simple pregen file")
	void testSimplePregenFile() {
		Map<ItemInfo, Long> pregenerated = parseJson("""
				{
					"minecraft:dirt": 1
				}""");
		Assertions.assertEquals(1, pregenerated.size());
		Assertions.assertEquals(1, pregenerated.get(ItemInfo.fromItem(Items.DIRT)));
	}

	@Test
	@DisplayName("Test pregen file with long values")
	void testPregenFileLongValues() {
		Map<ItemInfo, Long> pregenerated = parseJson("""
				{
					"minecraft:dirt": 2147483648
				}""");
		Assertions.assertEquals(1, pregenerated.size());
		//Max int + 1
		Assertions.assertEquals(2_147_483_648L, pregenerated.get(ItemInfo.fromItem(Items.DIRT)));
	}

	@Test
	@DisplayName("Test pregen file with keys that contain nbt")
	void testPregenFileWithNbt() {
		Map<ItemInfo, Long> pregenerated = parseJson("""
				{
					"minecraft:dirt[custom_data={my: \\"tag\\"}]": 1
				}""");
		Assertions.assertEquals(1, pregenerated.size());
		Assertions.assertEquals(1, pregenerated.get(ItemInfo.fromItem(Items.DIRT, CodecTestHelper.MY_TAG_PATCH)));
	}

	@Test
	@DisplayName("Test pregen file with keys that contain empty nbt")
	void testPregenFileWithEmptyNbt() {
		//Empty nbt is ignored and is treated as if it isn't there
		Map<ItemInfo, Long> pregenerated = parseJson("""
				{
					"minecraft:dirt[]": 1
				}""");
		Assertions.assertEquals(1, pregenerated.size());
		Assertions.assertEquals(1, pregenerated.get(ItemInfo.fromItem(Items.DIRT)));
	}

	@Test
	@DisplayName("Test pregen file with invalid value")
	void testPregenFileInvalidValues() {
		Assertions.assertThrows(JsonParseException.class, () -> parseJson("""
				{
					"minecraft:dirt": 0
				}"""));
		Assertions.assertThrows(JsonParseException.class, () -> parseJson("""
				{
					"minecraft:dirt": -1
				}"""));
	}

	@Test
	@DisplayName("Test pregen file with invalid keys")
	void testPregenFileInvalidKeys() {
		//Test to ensure we skip over any invalid keys rather than throwing an exception and failing to deserialize anything
		Map<ItemInfo, Long> pregenerated = parseJson("""
				{
					"minecraft:dirt": 1,
					"projecte:invalid": 2,
					"minecraft:stone": 3,
					"INVALID": 4
				}""");
		Assertions.assertEquals(2, pregenerated.size());
		Assertions.assertEquals(1, pregenerated.get(ItemInfo.fromItem(Items.DIRT)));
		Assertions.assertEquals(3, pregenerated.get(ItemInfo.fromItem(Items.STONE)));
	}
}