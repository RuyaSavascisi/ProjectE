package moze_intel.projecte.api.conversion;

import java.util.List;
import java.util.Map;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.nss.NSSFake;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.impl.codec.CodecTestHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Items;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
@DisplayName("Test Custom Conversion Mappers")
class CustomConversionMapperTest {

	private static CustomConversionFile parseJson(HolderLookup.Provider registryAccess, String json) {
		return CodecTestHelper.parseJson(registryAccess, CustomConversionFile.CODEC, "custom conversion test", json);
	}

	@BeforeAll
	@DisplayName("Manually load the default supported codecs")
	static void setupBuiltinCodecs() {
		//Registry init does not happen for tests, so we need to manually add our codecs
		CodecTestHelper.initBuiltinNSS();
	}

	@Test
	@DisplayName("Test conversion file that only contains a comment")
	void testCommentOnlyCustomFile(MinecraftServer server) {
		CustomConversionFile conversionFile = parseJson(server.registryAccess(), """
				{
					"comment": "A very simple Example"
				}""");
		Assertions.assertEquals("A very simple Example", conversionFile.comment());
	}

	@Test
	@DisplayName("Test conversion file with empty group")
	void testSingleEmptyGroupFile(MinecraftServer server) {
		CustomConversionFile conversionFile = parseJson(server.registryAccess(), """
				{
					"groups": {
						"groupa": {
							"comment": "A conversion group for something",
							"conversions": [
							]
						}
					}
				}""");
		Assertions.assertEquals(1, conversionFile.groups().size());
		Assertions.assertTrue(conversionFile.groups().containsKey("groupa"), "Map contains key for group");
		ConversionGroup group = conversionFile.groups().get("groupa");
		Assertions.assertEquals(group.comment(), "A conversion group for something", "Group contains specific comment");
		Assertions.assertEquals(0, group.size());
	}

	@Test
	@DisplayName("Test simple conversion file")
	void testSimpleFile(MinecraftServer server) {
		CustomConversionFile conversionFile = parseJson(server.registryAccess(), """
				{
					"groups": {
						"groupa": {
							"conversions": [
								{
									"output": "iron_ingot",
									"ingredients": {
										"stone": 1,
										"granite": 2,
										"diorite": 3
									}
								},
								{
									"output": "gold_ingot",
									"ingredients": [
										"stone",
										"granite",
										"diorite"
									]
								},
								{
									"output": "copper_ingot",
									"count": 3,
									"ingredients": [
										"stone",
										"stone",
										"stone"
									]
								}
							]
						}
					}
				}""");
		Assertions.assertEquals(1, conversionFile.groups().size());
		Assertions.assertTrue(conversionFile.groups().containsKey("groupa"), "Map contains key for group");
		ConversionGroup group = conversionFile.groups().get("groupa");
		Assertions.assertEquals(3, group.size());
		List<CustomConversion> conversions = group.conversions();
		{
			CustomConversion conversion = conversions.get(0);
			Assertions.assertEquals(NSSItem.createItem(Items.IRON_INGOT), conversion.output());
			Assertions.assertEquals(1, conversion.count());
			Map<NormalizedSimpleStack, Integer> ingredients = conversion.ingredients();
			Assertions.assertEquals(3, ingredients.size());
			Assertions.assertEquals(1, ingredients.get(NSSItem.createItem(Items.STONE)));
			Assertions.assertEquals(2, ingredients.get(NSSItem.createItem(Items.GRANITE)));
			Assertions.assertEquals(3, ingredients.get(NSSItem.createItem(Items.DIORITE)));
		}
		{
			CustomConversion conversion = conversions.get(1);
			Assertions.assertEquals(NSSItem.createItem(Items.GOLD_INGOT), conversion.output());
			Assertions.assertEquals(1, conversion.count());
			Map<NormalizedSimpleStack, Integer> ingredients = conversion.ingredients();
			Assertions.assertEquals(3, ingredients.size());
			Assertions.assertEquals(1, ingredients.get(NSSItem.createItem(Items.STONE)));
			Assertions.assertEquals(1, ingredients.get(NSSItem.createItem(Items.GRANITE)));
			Assertions.assertEquals(1, ingredients.get(NSSItem.createItem(Items.DIORITE)));
		}
		{
			CustomConversion conversion = conversions.get(2);
			Assertions.assertEquals(NSSItem.createItem(Items.COPPER_INGOT), conversion.output());
			Assertions.assertEquals(3, conversion.count());
			Map<NormalizedSimpleStack, Integer> ingredients = conversion.ingredients();
			Assertions.assertEquals(1, ingredients.size());
			Assertions.assertEquals(3, ingredients.get(NSSItem.createItem(Items.STONE)));
		}
	}

	@Test
	@DisplayName("Test conversion file setting value")
	void testSetValueFile(MinecraftServer server) {
		CustomConversionFile conversionFile = parseJson(server.registryAccess(), """
				{
					"values": {
						"before": {
							"stone": 1,
							"granite": 2,
							"diorite": "free"
						},
						"after": {
							"andesite": 3
						}
					}
				}""");
		FixedValues values = conversionFile.values();
		Assertions.assertEquals(1, values.setValueBefore().get(NSSItem.createItem(Items.STONE)));
		Assertions.assertEquals(2, values.setValueBefore().get(NSSItem.createItem(Items.GRANITE)));
		Assertions.assertEquals(ProjectEAPI.FREE_ARITHMETIC_VALUE, values.setValueBefore().get(NSSItem.createItem(Items.DIORITE)));
		Assertions.assertEquals(3, values.setValueAfter().get(NSSItem.createItem(Items.ANDESITE)));
	}

	@Test
	@DisplayName("Test conversion file skipping invalid keys for setting value")
	void testInvalidKeySetValueFile(MinecraftServer server) {
		CustomConversionFile conversionFile = parseJson(server.registryAccess(), """
				{
					"values": {
						"before": {
							"INVALID|stone": 1,
							"granite": 2
						},
						"after": {
							"INVALID|andesite": 3
						}
					}
				}""");
		FixedValues values = conversionFile.values();
		Assertions.assertEquals(2, values.setValueBefore().get(NSSItem.createItem(Items.GRANITE)));
		Assertions.assertTrue(values.setValueAfter().isEmpty());
	}

	@Test
	@DisplayName("Test set value from conversion")
	void testSetValueFromConversion(MinecraftServer server) {
		CustomConversionFile conversionFile = parseJson(server.registryAccess(), """
				{
					"values": {
						"conversion": [
							{
								"output": "iron_ingot",
								"ingredients": {
									"stone": 1,
									"granite": 2,
									"diorite": 3
								}
							}
						]
					}
				}""");
		Assertions.assertEquals(1, conversionFile.values().conversions().size());
		CustomConversion conversion = conversionFile.values().conversions().get(0);
		Assertions.assertEquals(NSSItem.createItem(Items.IRON_INGOT), conversion.output());
		Assertions.assertEquals(1, conversion.count());
		Map<NormalizedSimpleStack, Integer> ingredients = conversion.ingredients();
		Assertions.assertEquals(3, ingredients.size());
		Assertions.assertEquals(1, ingredients.get(NSSItem.createItem(Items.STONE)));
		Assertions.assertEquals(2, ingredients.get(NSSItem.createItem(Items.GRANITE)));
		Assertions.assertEquals(3, ingredients.get(NSSItem.createItem(Items.DIORITE)));
	}

	@Test
	@DisplayName("Test explicit format in conversions")
	void testConversionExplicitFormat(MinecraftServer server) {
		CustomConversionFile conversionFile = parseJson(server.registryAccess(), """
				{
					"values": {
						"conversion": [
							{
								"output": {
									"type": "projecte:item",
									"id": "iron_ingot"
								},
								"ingredients": {
									"stone": 1,
									"granite": 2,
									"diorite": 3
								}
							},
							{
								"output": {
									"type": "projecte:item",
									"id": "gold_ingot",
									"data": {
										"custom_data": {
											"my": "tag"
										}
									}
								},
								"ingredients": [
									{
										"type": "projecte:item",
										"id": "stone"
									},
									"granite",
									{
										"type": "projecte:item",
										"id": "diorite",
										"data": {
											"custom_data": "{my: \\"tag\\"}"
										}
									}
								]
							}
						]
					}
				}""");
		List<CustomConversion> conversions = conversionFile.values().conversions();
		Assertions.assertEquals(2, conversions.size());
		{
			CustomConversion conversion = conversions.get(0);
			Assertions.assertEquals(NSSItem.createItem(Items.IRON_INGOT), conversion.output());
			Assertions.assertEquals(1, conversion.count());
			Map<NormalizedSimpleStack, Integer> ingredients = conversion.ingredients();
			Assertions.assertEquals(3, ingredients.size());
			Assertions.assertEquals(1, ingredients.get(NSSItem.createItem(Items.STONE)));
			Assertions.assertEquals(2, ingredients.get(NSSItem.createItem(Items.GRANITE)));
			Assertions.assertEquals(3, ingredients.get(NSSItem.createItem(Items.DIORITE)));
		}
		{
			CustomConversion conversion = conversions.get(1);
			Assertions.assertEquals(NSSItem.createItem(Items.GOLD_INGOT, CodecTestHelper.MY_TAG_PATCH), conversion.output());
			Assertions.assertEquals(1, conversion.count());
			Map<NormalizedSimpleStack, Integer> ingredients = conversion.ingredients();
			Assertions.assertEquals(3, ingredients.size());
			Assertions.assertEquals(1, ingredients.get(NSSItem.createItem(Items.STONE)));
			Assertions.assertEquals(1, ingredients.get(NSSItem.createItem(Items.GRANITE)));
			Assertions.assertEquals(1, ingredients.get(NSSItem.createItem(Items.DIORITE, CodecTestHelper.MY_TAG_PATCH)));
		}
	}

	@Test
	@DisplayName("Test to make sure FAKE values in conversions don't break things")
	void testNonInterferingFakes(MinecraftServer server) {
		String file1 = """
				{
					"values": {
						"conversion": [
							{
								"output": "FAKE|FOO",
								"ingredients": [
									"FAKE|BAR"
								]
							}
						]
					}
				}""";

		NSSFake.setCurrentNamespace("file1");
		CustomConversionFile conversionFile1 = parseJson(server.registryAccess(), file1);
		CustomConversionFile conversionFile2 = parseJson(server.registryAccess(), file1);
		NSSFake.setCurrentNamespace("file2");
		CustomConversionFile conversionFile3 = parseJson(server.registryAccess(), file1);

		CustomConversion conversion1 = conversionFile1.values().conversions().get(0);
		CustomConversion conversion2 = conversionFile2.values().conversions().get(0);
		CustomConversion conversion3 = conversionFile3.values().conversions().get(0);

		Assertions.assertEquals(conversion1.output(), conversion2.output());
		Assertions.assertNotEquals(conversion1.output(), conversion3.output());
		Assertions.assertNotEquals(conversion2.output(), conversion3.output());
	}

	@Test
	@DisplayName("Test ignore invalid conversions")
	void testIgnoreInvalidConversions(MinecraftServer server) {
		CustomConversionFile conversionFile = parseJson(server.registryAccess(), """
				{
					"groups": {
						"groupa": {
							"conversions": [
								{
									"output": "iron_ingot",
									"ingredients": {
										"stone": 1,
										"granite": 2,
										"diorite": 3
									}
								},
								{
									"output": "gold_ingot"
								},
								{
									"output": "copper_ingot",
									"count": 3,
									"ingredients": [
										"stone",
										"stone",
										"stone"
									]
								}
							]
						}
					}
				}""");
		Assertions.assertEquals(1, conversionFile.groups().size());
		Assertions.assertTrue(conversionFile.groups().containsKey("groupa"), "Map contains key for group");
		ConversionGroup group = conversionFile.groups().get("groupa");
		Assertions.assertEquals(2, group.size());
		List<CustomConversion> conversions = group.conversions();
		{
			CustomConversion conversion = conversions.get(0);
			Assertions.assertEquals(NSSItem.createItem(Items.IRON_INGOT), conversion.output());
			Assertions.assertEquals(1, conversion.count());
			Map<NormalizedSimpleStack, Integer> ingredients = conversion.ingredients();
			Assertions.assertEquals(3, ingredients.size());
			Assertions.assertEquals(1, ingredients.get(NSSItem.createItem(Items.STONE)));
			Assertions.assertEquals(2, ingredients.get(NSSItem.createItem(Items.GRANITE)));
			Assertions.assertEquals(3, ingredients.get(NSSItem.createItem(Items.DIORITE)));
		}
		{
			CustomConversion conversion = conversions.get(1);
			Assertions.assertEquals(NSSItem.createItem(Items.COPPER_INGOT), conversion.output());
			Assertions.assertEquals(3, conversion.count());
			Map<NormalizedSimpleStack, Integer> ingredients = conversion.ingredients();
			Assertions.assertEquals(1, ingredients.size());
			Assertions.assertEquals(3, ingredients.get(NSSItem.createItem(Items.STONE)));
		}
	}
}