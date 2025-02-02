package moze_intel.projecte.emc.components.processor;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.components.DataComponentProcessor;
import moze_intel.projecte.config.PEConfigTranslations;
import moze_intel.projecte.emc.EMCMappingHandler;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BannerPatternTags;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.NotNull;

@DataComponentProcessor
public class BannerProcessor extends PersistentComponentProcessor<BannerPatternLayers> {

	@Override
	public String getName() {
		return PEConfigTranslations.DCP_BANNERS.title();
	}

	@Override
	public String getTranslationKey() {
		return PEConfigTranslations.DCP_BANNERS.getTranslationKey();
	}

	@Override
	public String getDescription() {
		return PEConfigTranslations.DCP_BANNERS.tooltip();
	}

	@Override
	public long recalculateEMC(@NotNull ItemInfo info, long currentEMC, @NotNull BannerPatternLayers patternLayers) throws ArithmeticException {
		for (BannerPatternLayers.Layer layer : patternLayers.layers()) {
			DyeItem dye = DyeItem.byColor(layer.color());
			long dyeEmc = EMCHelper.getEmcValue(dye);
			if (dyeEmc == 0) {//The dye doesn't have an EMC value so we can't get the emc value of the total thing
				return 0;
			}
			currentEMC = Math.addExact(currentEMC, dyeEmc);
			Holder<BannerPattern> pattern = layer.pattern();
			if (!pattern.is(BannerPatternTags.NO_ITEM_REQUIRED)) {
				//If an item was required for the pattern, calculate what the emc of it was
				long patternEmc = EMCMappingHandler.getBannerPatternEmc(pattern);
				if (patternEmc == 0) {//The pattern doesn't have an EMC value so we can't get the emc value of the total thing
					return 0;
				}
				currentEMC = Math.addExact(currentEMC, patternEmc);
			}
		}
		return currentEMC;
	}

	@Override
	protected boolean validItem(@NotNull ItemInfo info) {
		return info.getItem().value() instanceof BannerItem;
	}

	@Override
	protected boolean shouldPersist(@NotNull ItemInfo info, @NotNull BannerPatternLayers component) {
		return !component.equals(BannerPatternLayers.EMPTY);
	}

	@Override
	protected DataComponentType<BannerPatternLayers> getComponentType(@NotNull ItemInfo info) {
		return DataComponents.BANNER_PATTERNS;
	}
}
