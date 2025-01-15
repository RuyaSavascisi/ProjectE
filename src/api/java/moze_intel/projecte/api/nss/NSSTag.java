package moze_intel.projecte.api.nss;

import java.util.function.Consumer;
import org.apache.logging.log4j.util.TriConsumer;

/**
 * An extension of {@link NormalizedSimpleStack} that allows for representing stacks that are both "simple" and can have a tag representation.
 */
public interface NSSTag extends NormalizedSimpleStack {

	/**
	 * Checks if our {@link NormalizedSimpleStack} is representing a tag
	 *
	 * @return True if this {@link NSSTag} object is representing a tag, or false if we are really just a {@link NormalizedSimpleStack}.
	 */
	boolean representsTag();

	/**
	 * For every element in our tag run the given {@link Consumer<NormalizedSimpleStack>} on the {@link NormalizedSimpleStack} that represents them.
	 *
	 * @param consumer The {@link Consumer<NormalizedSimpleStack>} to run on our {@link NormalizedSimpleStack}s.
	 *
	 * @apiNote This does not do anything if this {@link NSSTag} is not currently representing a tag.
	 */
	void forEachElement(Consumer<NormalizedSimpleStack> consumer);

	/**
	 * For every element in our tag run the given {@link Consumer<NormalizedSimpleStack>} on the {@link NormalizedSimpleStack} that represents them.
	 *
	 * @param consumer The {@link Consumer<NormalizedSimpleStack>} to run on our {@link NormalizedSimpleStack}s.
	 *
	 * @apiNote This does not do anything if this {@link NSSTag} is not currently representing a tag.
	 */
	<CONTEXT, DATA> void forEachElement(CONTEXT context, DATA data, TriConsumer<CONTEXT, NormalizedSimpleStack, DATA> consumer);

	@Override
	default void forSelfAndEachElement(Consumer<NormalizedSimpleStack> consumer) {
		NormalizedSimpleStack.super.forSelfAndEachElement(consumer);
		forEachElement(consumer);
	}

	@Override
	default <CONTEXT, DATA> void forSelfAndEachElement(CONTEXT context, DATA data, TriConsumer<CONTEXT, NormalizedSimpleStack, DATA> consumer) {
		NormalizedSimpleStack.super.forSelfAndEachElement(context, data, consumer);
		forEachElement(context, data, consumer);
	}
}