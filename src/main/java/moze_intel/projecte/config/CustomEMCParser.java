package moze_intel.projecte.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import moze_intel.projecte.api.codec.IPECodecHelper;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.impl.codec.PECodecHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.Nullable;

public final class CustomEMCParser {

	private static final Path CONFIG = ProjectEConfig.CONFIG_DIR.resolve("custom_emc.json");

	public record CustomEMCFile(Map<NSSItem, Long> entries, @Nullable String comment) {

		public static final Codec<CustomEMCFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ExtraCodecs.NON_EMPTY_STRING.lenientOptionalFieldOf("comment").forGetter(file -> Optional.ofNullable(file.comment)),
				//Skip invalid keys
				IPECodecHelper.INSTANCE.modifiableMap(IPECodecHelper.INSTANCE.lenientKeyUnboundedMap(
						NSSItem.CODEC,
						IPECodecHelper.INSTANCE.nonNegativeLong().fieldOf("emc")
				), LinkedHashMap::new).fieldOf("entries").forGetter(CustomEMCFile::entries)
		).apply(instance, (comment, entries) -> new CustomEMCFile(entries, comment.orElse(null))));
	}

	public static CustomEMCFile currentEntries;
	private static boolean dirty = false;

	private static CustomEMCFile createDefault() {
		return new CustomEMCFile(new LinkedHashMap<>(), "Use the in-game commands to edit this file");
	}

	public static void init(HolderLookup.Provider registries) {
		flush(registries);

		if (Files.exists(CONFIG)) {
			currentEntries = PECodecHelper.readFromFile(registries, CONFIG, CustomEMCFile.CODEC, "custom emc")
					.orElseGet(CustomEMCParser::createDefault);
		} else {
			currentEntries = createDefault();
			PECodecHelper.writeToFile(registries, CONFIG, CustomEMCFile.CODEC, currentEntries, "default custom EMC");
		}
	}

	public static void addToFile(NSSItem toAdd, long emc) {
		if (emc < 0) {
			throw new IllegalArgumentException("EMC must be non-negative: " + emc);
		}
		Long old = currentEntries.entries().put(toAdd, emc);
		if (old == null || old != emc) {
			dirty = true;
		}
	}

	public static boolean removeFromFile(NSSItem toRemove) {
		boolean removed = currentEntries.entries().remove(toRemove) != null;
		if (removed) {
			dirty = true;
		}
		return removed;
	}

	public static void flush(HolderLookup.Provider registries) {
		if (dirty) {
			PECodecHelper.writeToFile(registries, CONFIG, CustomEMCFile.CODEC, currentEntries, "custom EMC");
			dirty = false;
		}
	}
}