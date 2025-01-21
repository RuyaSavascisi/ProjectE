package moze_intel.projecte.handlers;

import java.util.function.IntSupplier;
import moze_intel.projecte.config.ProjectEConfig;

public class InternalTimers {

	public final Timer repair = new Timer(ProjectEConfig.server.cooldown.player.repair);
	public final Timer heal = new Timer(ProjectEConfig.server.cooldown.player.heal);
	public final Timer feed = new Timer(ProjectEConfig.server.cooldown.player.feed);

	public void tick() {
		repair.tick();
		heal.tick();
		feed.tick();
	}

	public static class Timer {

		private final IntSupplier defaultTickCount;
		private int tickCount = 0;
		private boolean shouldUpdate = false;

		private Timer(IntSupplier defaultTickCount) {
			this.defaultTickCount = defaultTickCount;
		}

		/*public void activate() {
			shouldUpdate = defaultTickCount.getAsInt() != -1;
		}

		public boolean canFunction() {
			if (tickCount == 0) {
				tickCount = defaultTickCount.getAsInt();
				shouldUpdate = false;
				return true;
			}
			return false;
		}*/

		//TODO - 1.21: Test this
		public boolean activateAndCanFunction(boolean tryFunction) {
			int defaultTickCount = this.defaultTickCount.getAsInt();
			if (tryFunction && tickCount == 0) {
				tickCount = defaultTickCount;
				shouldUpdate = false;
				return true;
			}
			shouldUpdate = defaultTickCount != -1;
			return false;
		}

		private void tick() {
			if (shouldUpdate) {
				if (tickCount > 0) {
					//Ensure we don't go negative if we are set to go off every tick
					tickCount--;
				}
				shouldUpdate = false;
			}
		}
	}
}