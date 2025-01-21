package moze_intel.projecte.integration.jade;

import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class JadeDataProvider implements IBlockComponentProvider {

	static final JadeDataProvider INSTANCE = new JadeDataProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (ProjectEConfig.server.misc.lookingAtDisplay.get()) {
			long value = EMCHelper.getEmcValue(accessor.getBlock());
			if (value > 0) {
				tooltip.add(EMCHelper.getEmcTextComponent(value, 1));
			}
		}
	}

	@Override
	public ResourceLocation getUid() {
		return PEJadeConstants.EMC_PROVIDER;
	}
}