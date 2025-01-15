package moze_intel.projecte.emc.pregenerated;

import com.mojang.serialization.Codec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.codec.IPECodecHelper;
import moze_intel.projecte.impl.codec.PECodecHelper;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.VisibleForTesting;

public class PregeneratedEMC {

	//Allow skipping when there are invalid entries in the map as a common case of this might be if a mod is removed after
	// emc values were pregenerated, and then it will be referencing an item that doesn't exist anymore
	@VisibleForTesting
	static final Codec<Map<ItemInfo, Long>> CODEC = IPECodecHelper.INSTANCE.lenientKeyUnboundedMap(
			ItemInfo.MAP_CODEC,
			IPECodecHelper.INSTANCE.positiveLong().fieldOf("emc")
	);

	public static Optional<Map<ItemInfo, Long>> read(HolderLookup.Provider registries, Path path, boolean shouldUsePregenerated) {
		if (shouldUsePregenerated && Files.isReadable(path)) {
			return PECodecHelper.readFromFile(registries, path, CODEC, "pregenerated emc");
		}
		return Optional.empty();
	}

	public static void write(HolderLookup.Provider registries, Path path, Map<ItemInfo, Long> map) {
		PECodecHelper.writeToFile(registries, path, CODEC, map, "pregenerated emc");
	}
}