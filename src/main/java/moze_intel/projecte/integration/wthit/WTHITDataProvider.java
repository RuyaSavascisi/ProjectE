package moze_intel.projecte.integration.wthit;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.utils.EMCHelper;

public class WTHITDataProvider implements IBlockComponentProvider {

	static final WTHITDataProvider INSTANCE = new WTHITDataProvider();

	@Override
	public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
		if (ProjectEConfig.server.misc.lookingAtDisplay.get()) {
			long value = IEMCProxy.INSTANCE.getValue(accessor.getBlock());
			if (value > 0) {
				tooltip.addLine(EMCHelper.getEmcTextComponent(value, 1));
			}
		}
	}
}