package moze_intel.projecte.impl.codec;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.Reader;
import java.io.StringReader;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;

public class CodecTestHelper {

	public static final DataComponentPatch MY_TAG_PATCH = Util.make(() -> {
		CompoundTag nbt = new CompoundTag();
		nbt.putString("my", "tag");
		return DataComponentPatch.builder()
				.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt))
				.build();
	});

	public static void initBuiltinNSS() {
		PECodecHelper.initBuiltinNSS();
	}

	public static <OBJ> OBJ parseJson(Codec<OBJ> codec, String description, String json) throws JsonParseException {
		return readJson(new StringReader(json), codec, description);
	}

	public static <OBJ> OBJ readJson(Reader reader, Codec<OBJ> codec, String description) throws JsonParseException {
		//Similar to PECodecHelper#read except without any extra logging
		JsonElement json = JsonParser.parseReader(reader);
		return codec.parse(JsonOps.INSTANCE, json)
				.mapOrElse(Function.identity(), error -> {
					throw new JsonParseException("Failed to deserialize json (" + description + "): " + error.message());
				});
	}
}