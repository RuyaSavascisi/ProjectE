package moze_intel.projecte.api.mapper.recipe;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Set;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;

/**
 * Interface to make for a cleaner API than using a {@link java.util.function.Function} when creating groupings of {@link NormalizedSimpleStack}s.
 */
public interface INSSFakeGroupManager {

	/**
	 * Gets or creates a singular {@link NormalizedSimpleStack} representing the grouping or "ingredient" of the given stacks. Additionally, a boolean is returned
	 * specifying if it was created or already existed. {@code true} for if it was created.
	 *
	 * @param stacks Individual stacks to represent as a single "combined" stack. May be modified after this method is called.
	 *
	 * @apiNote If the combined representation had to be created the {@link FakeGroupData} will represent this, and conversions from the individual elements to the
	 * returned stack <strong>MUST</strong> be added.
	 */
	FakeGroupData getOrCreateFakeGroup(Set<NormalizedSimpleStack> stacks);

	/**
	 * Gets or creates a singular {@link NormalizedSimpleStack} representing the grouping or "ingredient" of the given stacks. Additionally, a boolean is returned
	 * specifying if it was created or already existed. {@code true} for if it was created.
	 *
	 * @param stacks Individual stacks to represent as a single "combined" stack. Must not be modified after this method is called.
	 *
	 * @apiNote If the combined representation had to be created the {@link FakeGroupData} will represent this, and conversions from the individual elements to the
	 * returned stack <strong>MUST</strong> be added.
	 */
	FakeGroupData getOrCreateFakeGroupDirect(Set<NormalizedSimpleStack> stacks);

	/**
	 * Gets or creates a singular {@link NormalizedSimpleStack} representing the grouping or "ingredient" of the given stacks. Additionally, a boolean is returned
	 * specifying if it was created or already existed. {@code true} for if it was created.
	 *
	 * @param stacks Individual stacks to represent as a single "combined" stack. May be modified after this method is called.
	 *
	 * @apiNote If the combined representation had to be created the {@link FakeGroupData} will represent this, and conversions from the individual elements to the
	 * returned stack <strong>MUST</strong> be added.
	 */
	FakeGroupData getOrCreateFakeGroup(Object2IntMap<NormalizedSimpleStack> stacks);

	/**
	 * Gets or creates a singular {@link NormalizedSimpleStack} representing the grouping or "ingredient" of the given stacks. Additionally, a boolean is returned
	 * specifying if it was created or already existed. {@code true} for if it was created.
	 *
	 * @param stacks Individual stacks to represent as a single "combined" stack. Must not be modified after this method is called.
	 *
	 * @apiNote If the combined representation had to be created the {@link FakeGroupData} will represent this, and conversions from the individual elements to the
	 * returned stack <strong>MUST</strong> be added.
	 */
	FakeGroupData getOrCreateFakeGroupDirect(Object2IntMap<NormalizedSimpleStack> stacks);

	/**
	 * Represents data for a fake group.
	 *
	 * @param dummy   Fake stack that represents the group.
	 * @param created Whether the fake group had to be created for this call.
	 */
	record FakeGroupData(NormalizedSimpleStack dummy, boolean created) {
	}
}