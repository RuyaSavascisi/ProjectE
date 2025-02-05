package moze_intel.projecte.integration.crafttweaker.actions;

import com.blamejared.crafttweaker.api.action.base.IUndoableAction;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.integration.crafttweaker.mappers.CrTCustomEMCMapper;
import moze_intel.projecte.utils.EMCHelper;
import org.jetbrains.annotations.NotNull;

public class CustomEMCAction implements IUndoableAction {

	@NotNull
	private final NormalizedSimpleStack stack;
	private final long emc;

	public CustomEMCAction(@NotNull NormalizedSimpleStack stack, long emc) {
		this.stack = stack;
		this.emc = emc;
	}

	@Override
	public void apply() {
		CrTCustomEMCMapper.registerCustomEMC(stack, emc);
	}

	@Override
	public String describe() {
		return "Registered emc value of '" + EMCHelper.formatEmc(emc) + "' for: " + stack;
	}

	@Override
	public void undo() {
		CrTCustomEMCMapper.unregisterNSS(stack);
	}

	@Override
	public String describeUndo() {
		return "Undoing emc registration for: " + stack;
	}

	@Override
	public String systemName() {
		return PECore.MODNAME;
	}
}