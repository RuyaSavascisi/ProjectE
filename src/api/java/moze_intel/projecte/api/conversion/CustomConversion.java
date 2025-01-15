package moze_intel.projecte.api.conversion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import moze_intel.projecte.api.codec.IPECodecHelper;
import moze_intel.projecte.api.nss.NSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;

/**
 * Represents a conversion
 *
 * @param count         Amount of output this conversion produces (may not be equal to zero)
 * @param output        Output stack
 * @param ingredients   Map of the ingredients and how many of each are necessary to perform the conversion (may not be equal to zero)
 * @param propagateTags if {@code true} and the output is an {@link NSSTag}, this conversion will be propagated to all elements in the tag
 */
public record CustomConversion(int count, NormalizedSimpleStack output, Map<NormalizedSimpleStack, Integer> ingredients, boolean propagateTags) {

	private static final CustomConversion INVALID = new CustomConversion(0, null, Collections.emptyMap(), false);

	private static final Codec<Map<NormalizedSimpleStack, Integer>> INGREDIENT_CODEC = IPECodecHelper.INSTANCE.modifiableMap(IPECodecHelper.INSTANCE.unboundedMap(
			IPECodecHelper.INSTANCE.nssMapCodec(),
			Codec.INT.validate(
					value -> value == 0 ? DataResult.error(() -> "Value must not be zero: " + value) : DataResult.success(value)
			).optionalFieldOf("amount", 1),
			(map, nss, amount) -> {
				map.merge(nss, amount, Integer::sum);
				//Note: Return null as we allow duplicates
				return null;
			}
	).validate(map -> map.isEmpty() ? DataResult.error(() -> "Map must have contents") : DataResult.success(map)));

	private static final MapCodec<CustomConversion> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(CustomConversion::count),
			IPECodecHelper.INSTANCE.nssCodec().fieldOf("output").forGetter(CustomConversion::output),
			INGREDIENT_CODEC.fieldOf("ingredients").forGetter(CustomConversion::ingredients),
			//Note: If propagateTags is set to true and the output isn't an NSSTag this gracefully gets set to false
			Codec.BOOL.optionalFieldOf("propagateTags", false).forGetter(CustomConversion::propagateTags)
	).apply(instance, CustomConversion::new));

	public static final Codec<CustomConversion> CODEC = MAP_CODEC.codec();
	private static final Codec<CustomConversion> OR_INVALID_CODEC = IPECodecHelper.INSTANCE.orElseWithLog(MAP_CODEC, INVALID, () -> "Failed to read conversions: {}").codec();

	public static final Codec<List<CustomConversion>> MODIFIABLE_LIST_CODEC = Util.make(() -> {
		Codec<List<CustomConversion>> listCodec = OR_INVALID_CODEC.listOf();
		//We only need to modify the decoder as we don't care about whether the list is modifiable during encoding
		return Codec.of(listCodec, listCodec.map(list -> list.stream()
				//Filter out any invalid entries we are skipping over from decoding
				.filter(conversion -> conversion != INVALID)
				//Collect to a mutable list
				.collect(Collectors.toList())
		));
	});

	public CustomConversion(int count, NormalizedSimpleStack output, Map<NormalizedSimpleStack, Integer> ingredients) {
		this(count, output, ingredients, false);
	}

	public CustomConversion {
		//Only allow propagateTags to be true if the output is an NSSTag that represents a tag
		propagateTags = propagateTags && output instanceof NSSTag nssTag && nssTag.representsTag();
	}

	/**
	 * Creates a new conversion copying the passed in ingredients
	 *
	 * @param count       Amount of output this conversion produces (may not be equal to zero)
	 * @param output      Output stack
	 * @param ingredients Map of the ingredients and how many of each are necessary to perform the conversion (may not be equal to zero)
	 */
	public static CustomConversion getFor(int count, NormalizedSimpleStack output, Map<NormalizedSimpleStack, Integer> ingredients) {
		//TODO: Figure out if this copying is even necessary
		CustomConversion conversion = new CustomConversion(count, output, new HashMap<>());
		conversion.ingredients.putAll(ingredients);
		return conversion;
	}

	@Override
	public String toString() {
		return "{" + count + " * " + output + " = " + ingredients + "}";
	}
}