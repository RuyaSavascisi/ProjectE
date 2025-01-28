package moze_intel.projecte.emc;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.event.EMCRemapEvent;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.arithmetic.IValueArithmetic;
import moze_intel.projecte.api.mapper.collector.IExtendedMappingCollector;
import moze_intel.projecte.api.mapper.generator.IValueGenerator;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.emc.arithmetic.HiddenBigFractionArithmetic;
import moze_intel.projecte.emc.collector.DumpToFileCollector;
import moze_intel.projecte.emc.collector.LongToBigFractionCollector;
import moze_intel.projecte.emc.generator.BigFractionToLongGenerator;
import moze_intel.projecte.emc.mappers.TagMapper;
import moze_intel.projecte.emc.pregenerated.PregeneratedEMC;
import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.impl.capability.KnowledgeImpl;
import moze_intel.projecte.network.packets.to_client.SyncEmcPKT;
import moze_intel.projecte.utils.AnnotationHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.commons.math3.fraction.BigFraction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public final class EMCMappingHandler {

	private static final List<IEMCMapper<NormalizedSimpleStack, Long>> mappers = new ArrayList<>();
	private static final Object2LongMap<ItemInfo> emc = new Object2LongOpenHashMap<>();
	private static int loadIndex = -1;

	public static void loadMappers() {
		//If we don't have any mappers loaded try to load them
		if (mappers.isEmpty()) {
			//Add all the EMC mappers we have encountered
			mappers.addAll(AnnotationHelper.getEMCMappers());
			//Manually register the Tag Mapper to ensure that it is registered last so that it can "fix" all the tags used in any of the other mappers
			// This also has the side effect to make sure that we can use EMC_MAPPERS.isEmpty to check if we have attempted to initialize our cache yet
			mappers.add(new TagMapper());
		}
	}

	public static <T> T getOrSetDefault(CommentedFileConfig config, String key, String comment, T defaultValue) {
		T val = config.get(key);
		if (val == null) {
			val = defaultValue;
			config.set(key, val);
			config.setComment(key, comment);
		}
		return val;
	}

	public static void map(ReloadableServerResources serverResources, RegistryAccess registryAccess, ResourceManager resourceManager) {
		//Start by clearing the cached map so if values are removed say by setting EMC to zero then we respect the change
		clearEmcMap();
		SimpleGraphMapper<NormalizedSimpleStack, BigFraction, IValueArithmetic<BigFraction>> mapper = new SimpleGraphMapper<>(new HiddenBigFractionArithmetic());
		IValueGenerator<NormalizedSimpleStack, Long> valueGenerator = new BigFractionToLongGenerator<>(mapper);
		IExtendedMappingCollector<NormalizedSimpleStack, Long, IValueArithmetic<BigFraction>> mappingCollector = new LongToBigFractionCollector<>(mapper);

		//TODO - 1.21: Make this a slightly more normal config?? Similar to Data component processors
		// That way we can also make it so it displays in the config gui
		Path path = ProjectEConfig.CONFIG_DIR.resolve("mapping.toml");
		try {
			if (path.toFile().createNewFile()) {
				PECore.debugLog("Created mapping.toml");
			}
		} catch (IOException ex) {
			PECore.LOGGER.error("Couldn't create mapping.toml", ex);
		}

		CommentedFileConfig config = CommentedFileConfig.builder(path).build();
		config.load();

		boolean dumpToFile = getOrSetDefault(config, "general.dumpEverythingToFile", "Want to take a look at the internals of EMC Calculation? Enable this to write all the conversions and setValue-Commands to config/ProjectE/mappingdump.json", false);
		boolean shouldUsePregenerated = getOrSetDefault(config, "general.pregenerate", "When the next EMC mapping occurs write the results to config/ProjectE/pregenerated_emc.json and only ever run the mapping again" +
																					   " when that file does not exist, this setting is set to false, or an error occurred parsing that file.", false);
		boolean logFoundExploits = getOrSetDefault(config, "general.logEMCExploits", "Log known EMC Exploits. This can not and will not find all possible exploits. " +
																					 "This will only find exploits that result in fixed/custom emc values that the algorithm did not overwrite. " +
																					 "Exploits that derive from conversions that are unknown to ProjectE will not be found.", true);

		if (dumpToFile) {
			mappingCollector = new DumpToFileCollector<>(ProjectEConfig.CONFIG_DIR.resolve("mappingdump.json"), mappingCollector);
		}

		Path pregeneratedEmcFile = ProjectEConfig.CONFIG_DIR.resolve("pregenerated_emc.json");
		Map<ItemInfo, Long> graphMapperItemValues;
		Optional<Map<ItemInfo, Long>> readPregeneratedValues = PregeneratedEMC.read(registryAccess, pregeneratedEmcFile, shouldUsePregenerated);
		if (readPregeneratedValues.isPresent()) {
			graphMapperItemValues = readPregeneratedValues.get();
			PECore.LOGGER.info("Loaded {} values from pregenerated EMC File", graphMapperItemValues.size());
		} else {
			SimpleGraphMapper.setLogFoundExploits(logFoundExploits);

			PECore.debugLog("Starting to collect Mappings...");
			for (IEMCMapper<NormalizedSimpleStack, Long> emcMapper : mappers) {
				try {
					if (getOrSetDefault(config, "enabledMappers." + emcMapper.getName(), emcMapper.getDescription(), emcMapper.isAvailable())) {
						DumpToFileCollector.currentGroupName = emcMapper.getName();
						emcMapper.addMappings(mappingCollector, config, serverResources, registryAccess, resourceManager);
						PECore.debugLog("Collected Mappings from " + emcMapper.getClass().getName());
					}
				} catch (Exception e) {
					PECore.LOGGER.error(LogUtils.FATAL_MARKER, "Exception during Mapping Collection from Mapper {}. PLEASE REPORT THIS! EMC VALUES MIGHT BE INCONSISTENT!",
							emcMapper.getClass().getName(), e);
				}
			}
			DumpToFileCollector.currentGroupName = "NSSHelper";

			PECore.debugLog("Mapping Collection finished");
			mappingCollector.finishCollection(registryAccess);

			PECore.debugLog("Starting to generate Values:");

			config.save();
			config.close();

			Map<NormalizedSimpleStack, Long> graphMapperValues = valueGenerator.generateValues();
			PECore.debugLog("Generated Values...");

			graphMapperItemValues = filterEMCMap(graphMapperValues);

			if (shouldUsePregenerated) {
				//Should have used pregenerated, but the file was not read => regenerate.
				PregeneratedEMC.write(registryAccess, pregeneratedEmcFile, graphMapperItemValues);
				PECore.debugLog("Wrote Pregen-file!");
			}
		}

		emc.putAll(graphMapperItemValues);

		fireEmcRemapEvent();
	}

	private static void fireEmcRemapEvent() {
		//Start by doing our implementations
		FuelMapper.loadMap();
		loadIndex++;
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server != null) {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				IKnowledgeProvider knowledge = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
				if (knowledge != null) {
					if (knowledge instanceof KnowledgeImpl impl && impl.pruneStaleKnowledge()) {
						knowledge.sync(player);
					} else if (player.containerMenu instanceof TransmutationContainer) {
						//If knowledge didn't get trimmed due to pruning, tell clients that have the transmutation gui open
						// that they should update targets anyway, as it is possible EMC values changed and the order things
						// are drawn needs to be changed
						PECore.packetHandler().updateTransmutationTargets(player);
					}
				}
			}
		}
		NeoForge.EVENT_BUS.post(new EMCRemapEvent());
	}

	public static int getLoadIndex() {
		return loadIndex;
	}

	private static Object2LongMap<ItemInfo> filterEMCMap(Map<NormalizedSimpleStack, Long> map) {
		Object2LongMap<ItemInfo> resultMap = new Object2LongOpenHashMap<>(map.size());
		for (Map.Entry<NormalizedSimpleStack, Long> entry : map.entrySet()) {
			if (entry.getKey() instanceof NSSItem nssItem) {
				long value = entry.getValue();
				if (value > 0) {
					ItemInfo info = ItemInfo.fromNSS(nssItem);
					if (info != null) {//Ensure the item actually exists and is not a tag
						resultMap.put(info, value);
					}
				}
			}
		}
		return resultMap;
	}

	public static int getEmcMapSize() {
		return emc.size();
	}

	public static boolean hasEmcValue(@NotNull ItemInfo info) {
		return emc.containsKey(info);
	}

	/**
	 * Gets the stored emc value or zero if there is no entry in the map for the given value.
	 */
	@Range(from = 0, to = Long.MAX_VALUE)
	public static long getStoredEmcValue(@NotNull ItemInfo info) {
		return emc.getLong(info);
	}

	public static void clearEmcMap() {
		emc.clear();
	}

	/**
	 * Returns a modifiable set of all the mapped {@link ItemInfo}
	 */
	public static Set<ItemInfo> getMappedItems() {
		return new HashSet<>(emc.keySet());
	}

	public static void fromPacket(Object2LongMap<ItemInfo> data) {
		emc.clear();
		emc.putAll(data);
	}

	public static SyncEmcPKT createPacketData() {
		return new SyncEmcPKT(Object2LongMaps.unmodifiable(emc));
	}
}