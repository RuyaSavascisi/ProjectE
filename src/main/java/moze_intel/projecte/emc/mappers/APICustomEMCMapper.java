package moze_intel.projecte.emc.mappers;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.imc.CustomEMCRegistration;
import moze_intel.projecte.api.imc.IMCMethods;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.emc.EMCMappingHandler;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;

@EMCMapper
public class APICustomEMCMapper implements IEMCMapper<NormalizedSimpleStack, Long> {

	@EMCMapper.Instance
	public static final APICustomEMCMapper INSTANCE = new APICustomEMCMapper();
	private static final int PRIORITY_MIN_VALUE = 0;
	private static final int PRIORITY_MAX_VALUE = 512;
	private static final int PRIORITY_DEFAULT_VALUE = 1;

	private APICustomEMCMapper() {
	}

	private final Map<String, Object2LongMap<NormalizedSimpleStack>> customEMCforMod = new HashMap<>();

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
		return "APICustomEMCMapper";
	}

	@Override
	public String getDescription() {
		return "Allows other mods to easily set EMC values using the ProjectEAPI";
	}

	@Override
	public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper, CommentedFileConfig config, ReloadableServerResources serverResources,
			RegistryAccess registryAccess, ResourceManager resourceManager) {
		Object2IntMap<String> priorityMap = new Object2IntOpenHashMap<>();

		for (String modId : customEMCforMod.keySet()) {
			String configKey = getName() + ".priority." + modId;
			int priority = EMCMappingHandler.getOrSetDefault(config, configKey, "Priority for this mod", PRIORITY_DEFAULT_VALUE);
			priorityMap.put(modId, priority);
		}

		List<String> modIds = new ArrayList<>(customEMCforMod.keySet());
		modIds.sort(Comparator.comparingInt((ToIntFunction<String>) priorityMap::getInt).reversed());

		for (String modId : modIds) {
			for (Object2LongMap.Entry<NormalizedSimpleStack> entry : customEMCforMod.getOrDefault(modId, Object2LongMaps.emptyMap()).object2LongEntrySet()) {
				NormalizedSimpleStack normStack = entry.getKey();
				long emc = entry.getLongValue();
				if (isAllowedToSet(modId, normStack, emc, config)) {
					//Note: We set it for each of the values in the tag to make sure it is properly taken into account when calculating the individual EMC values
					normStack.forSelfAndEachElement(mapper, emc, IMappingCollector::setValueBefore);
					PECore.debugLog("{} setting value for {} to {}", modId, normStack, emc);
				} else {
					PECore.debugLog("Disallowed {} to set the value for {} to {}", modId, normStack, emc);
				}
			}
		}
	}

	private boolean isAllowedToSet(String modId, NormalizedSimpleStack stack, Long value, CommentedFileConfig config) {
		String resourceLocation;
		if (stack instanceof NSSItem nssItem) {
			//Allow both item names and tag locations
			resourceLocation = nssItem.getResourceLocation().toString();
		} else {
			resourceLocation = "IntermediateFakeItemsUsedInRecipes:";
		}
		String modForItem = resourceLocation.substring(0, resourceLocation.indexOf(':'));
		String configPath = String.format("permissions.%s.%s", modId, modForItem);
		String comment = String.format("Allow mod '%s' to set and or remove values for mod '%s'. Options: [both, set, remove, none]", modId, modForItem);
		String permission = EMCMappingHandler.getOrSetDefault(config, configPath, comment, "both");
		if (permission.equals("both")) {
			return true;
		}
		if (value == 0) {
			return permission.equals("remove");
		}
		return permission.equals("set");
	}
}