package moze_intel.projecte.emc.mappers;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.config.IConfigBuilder;
import moze_intel.projecte.api.imc.CustomEMCRegistration;
import moze_intel.projecte.api.imc.IMCMethods;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.EMCMapper.Instance;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.config.PEConfigTranslations;
import moze_intel.projecte.utils.text.IHasTranslationKey;
import moze_intel.projecte.utils.text.IHasTranslationKey.IHasEnumNameTranslationKey;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

@EMCMapper
public class APICustomEMCMapper implements IEMCMapper<NormalizedSimpleStack, Long> {

	@Instance
	public static final APICustomEMCMapper INSTANCE = new APICustomEMCMapper();
	private static final int PRIORITY_MIN_VALUE = 0;
	private static final int PRIORITY_MAX_VALUE = 512;
	private static final int PRIORITY_DEFAULT_VALUE = 1;

	private APICustomEMCMapper() {
	}

	private final Map<String, Object2LongMap<NormalizedSimpleStack>> customEMCforMod = new HashMap<>();
	private final Map<String, DataForMod> configOptionsForMod = new HashMap<>();

	public static void handleIMC(InterModProcessEvent event) {
		event.getIMCStream(IMCMethods.REGISTER_CUSTOM_EMC::equals).forEach(msg -> {
			if (msg.messageSupplier().get() instanceof CustomEMCRegistration(NormalizedSimpleStack stack, long value) && stack != null) {
				if (value < 0) {
					value = 0;
				}
				String modid = msg.senderModId();
				PECore.debugLog("Mod: '{}' registered a custom EMC value of: '{}' for the NormalizedSimpleStack: '{}'", modid, value, stack);
				INSTANCE.customEMCforMod.computeIfAbsent(modid, k -> {
					Object2LongMap<NormalizedSimpleStack> map = new Object2LongOpenHashMap<>();
					map.defaultReturnValue(-1);
					return map;
				}).put(stack, value);
			}
		});
	}

	@Override
	public String getName() {
		return PEConfigTranslations.MAPPING_API_CUSTOM_MAPPER.title();
	}

	@Override
	public String getTranslationKey() {
		return PEConfigTranslations.MAPPING_API_CUSTOM_MAPPER.getTranslationKey();
	}

	@Override
	public String getDescription() {
		return PEConfigTranslations.MAPPING_API_CUSTOM_MAPPER.tooltip();
	}

	private static void addDisplayInfo(ModConfigSpec.Builder builder, String modid) {
		Optional<? extends ModContainer> modContainer = ModList.get().getModContainerById(modid);
		//noinspection OptionalIsPresent - Capturing lambda
		if (modContainer.isPresent()) {
			//TODO - 1.21: Can we somehow specify a translation key?
			builder.comment(modContainer.get().getModInfo().getDisplayName());
		}
	}

	@Override
	public void addConfigOptions(IConfigBuilder<IEMCMapper<NormalizedSimpleStack, Long>> configBuilder) {
		ModConfigSpec.Builder builder = configBuilder.builder();
		for (String modId : customEMCforMod.keySet()) {
			addDisplayInfo(builder, modId);
			builder.push("mod-" + modId);

			PEConfigTranslations.MAPPING_API_CUSTOM_MAPPER_PRIORITY.applyToBuilder(builder);
			DataForMod data = new DataForMod(configBuilder.create("priority", PRIORITY_DEFAULT_VALUE, PRIORITY_MIN_VALUE, PRIORITY_MAX_VALUE));

			Object2LongMap<NormalizedSimpleStack> stackMap = customEMCforMod.getOrDefault(modId, Object2LongMaps.emptyMap());
			if (!stackMap.isEmpty()) {
				PEConfigTranslations.MAPPING_API_CUSTOM_MAPPER_PERMISSIONS.applyToBuilder(builder).push("permissions");
				for (NormalizedSimpleStack normStack : stackMap.keySet()) {
					String targetMod = getMod(normStack);
					if (targetMod != null && !data.permissions.containsKey(targetMod)) {
						//TODO - 1.21: Comment?? String.format("Allow mod '%s' to set and or remove values for mod '%s'. Options: [both, set, remove, none]", modId, modForStack);
						addDisplayInfo(builder, targetMod);
						data.permissions.put(targetMod, configBuilder.create(targetMod, Permission.BOTH));
					}
				}
				builder.pop();//permissions
			}

			configOptionsForMod.put(modId, data);
			builder.pop();//mod
		}
	}

	@Nullable
	private String getMod(NormalizedSimpleStack stack) {
		if (stack instanceof AbstractNSSTag<?> nssTag) {
			//Allow both item names and tag locations
			return nssTag.getResourceLocation().getNamespace();
		}
		return null;
	}

	@Override
	public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper, ReloadableServerResources serverResources, RegistryAccess registryAccess,
			ResourceManager resourceManager) {
		List<String> modIds = new ArrayList<>(customEMCforMod.keySet());
		modIds.sort(Comparator.comparingInt((String modId) -> {
			DataForMod data = configOptionsForMod.get(modId);
			return data == null ? PRIORITY_DEFAULT_VALUE : data.getPriority();
		}).reversed());

		for (String modId : modIds) {
			Object2LongMap<NormalizedSimpleStack> emcForMod = customEMCforMod.get(modId);
			if (emcForMod != null) {
				@Nullable
				DataForMod dataForMod = configOptionsForMod.get(modId);
				for (Iterator<Object2LongMap.Entry<NormalizedSimpleStack>> iterator = Object2LongMaps.fastIterator(emcForMod); iterator.hasNext(); ) {
					Object2LongMap.Entry<NormalizedSimpleStack> entry = iterator.next();
					NormalizedSimpleStack normStack = entry.getKey();
					long emc = entry.getLongValue();
					if (dataForMod == null || dataForMod.hasPermission(getMod(normStack), emc)) {
						//Note: We set it for each of the values in the tag to make sure it is properly taken into account when calculating the individual EMC values
						normStack.forSelfAndEachElement(mapper, emc, IMappingCollector::setValueBefore);
						PECore.debugLog("{} setting value for {} to {}", modId, normStack, emc);
					} else {
						PECore.debugLog("Disallowed {} to set the value for {} to {}", modId, normStack, emc);
					}
				}
			}
		}
	}

	private static class DataForMod {

		private final Map<String, Supplier<Permission>> permissions = new HashMap<>();
		private final IntSupplier priority;

		public DataForMod(IntSupplier priority) {
			this.priority = priority;
		}

		public int getPriority() {
			return priority.getAsInt();
		}

		public boolean hasPermission(@Nullable String otherMod, long value) {
			if (otherMod == null) {
				return true;
			}
			Supplier<Permission> supplier = permissions.get(otherMod);
			return supplier == null || supplier.get().hasPermission(value);
		}
	}

	private enum Permission implements IHasEnumNameTranslationKey {
		BOTH(PELang.CUSTOM_EMC_PERMISSION_BOTH),
		REMOVE(PELang.CUSTOM_EMC_PERMISSION_REMOVE),
		SET(PELang.CUSTOM_EMC_PERMISSION_SET),
		NONE(PELang.CUSTOM_EMC_PERMISSION_NONE);

		private final IHasTranslationKey langEntry;

		Permission(IHasTranslationKey langEntry) {
			this.langEntry = langEntry;
		}

		@Override
		public String getTranslationKey() {
			return langEntry.getTranslationKey();
		}

		public boolean hasPermission(long value) {
			return switch (this) {
				case BOTH -> true;
				case REMOVE -> value == 0;
				case SET -> value > 0;
				case NONE -> false;
			};
		}
	}
}