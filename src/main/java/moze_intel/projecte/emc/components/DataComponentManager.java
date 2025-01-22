package moze_intel.projecte.emc.components;

import java.util.ArrayList;
import java.util.List;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.components.IDataComponentProcessor;
import moze_intel.projecte.config.DataComponentProcessorConfig;
import moze_intel.projecte.emc.EMCMappingHandler;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.utils.AnnotationHelper;
import net.minecraft.core.component.DataComponentPatch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class DataComponentManager {

	private static final List<IDataComponentProcessor> processors = new ArrayList<>();

	public static void loadProcessors() {
		if (processors.isEmpty()) {
			processors.addAll(AnnotationHelper.getDataComponentProcessors());
			//Set up the config for the Data Component Processors
			DataComponentProcessorConfig.setup(processors);
		}
	}

	@NotNull
	public static ItemInfo getPersistentInfo(@NotNull ItemInfo info) {
		if (!info.hasModifiedComponents() || info.getItem().is(PETags.Items.DATA_COMPONENT_WHITELIST) || EMCMappingHandler.hasEmcValue(info)) {
			//If we have no custom Data Components, we want to allow data components to be kept, or we have an exact match to a stored value just go with it
			return info;
		}
		//Cleans up the tag in item to reduce it as much as possible
		DataComponentPatch.Builder builder = DataComponentPatch.builder();
		for (IDataComponentProcessor processor : processors) {
			if (DataComponentProcessorConfig.isEnabled(processor) && processor.hasPersistentComponents() && DataComponentProcessorConfig.hasPersistent(processor)) {
				processor.collectPersistentComponents(info, builder);
			}
		}
		return ItemInfo.fromItem(info.getItem(), builder.build());
	}

	@Range(from = 0, to = Long.MAX_VALUE)
	public static long getEmcValue(@NotNull ItemInfo info) {
		//TODO: Fix this, as it does not catch the edge case that we have an exact match and then there are random added Data Components on top of it
		// but that can be thought about more once we have the first pass complete. For example if someone put an enchantment on a potion
		long emcValue = EMCMappingHandler.getStoredEmcValue(info);
		if (!info.hasModifiedComponents()) {
			//If our item has no custom Data Components anyway, just return based on the value we got for it
			return emcValue;
		} else if (emcValue == 0) {
			//Try getting a base emc value from the Data Component less variant if we don't have one matching our Data Components
			emcValue = EMCMappingHandler.getStoredEmcValue(info.itemOnly());
			if (emcValue == 0) {
				//The base item doesn't have an EMC value either so just exit
				return 0;
			}
		}

		//Note: We continue to use our initial ItemInfo so that we are calculating based on the Data Components
		for (IDataComponentProcessor processor : processors) {
			if (DataComponentProcessorConfig.isEnabled(processor)) {
				try {
					emcValue = processor.recalculateEMC(info, emcValue);
				} catch (ArithmeticException e) {
					//Return the last successfully calculated EMC value
					return emcValue;
				}
				if (emcValue <= 0) {
					//Exit if it gets to zero (also safety check for less than zero in case a mod didn't bother sanctifying their data)
					return 0;
				}
			}
		}
		return emcValue;
	}
}