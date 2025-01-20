package moze_intel.projecte.emc;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class IngredientMap<T> {

	private final Object2IntMap<T> ingredients = new Object2IntOpenHashMap<>();

	public void addIngredient(T thing, int amount) {
		ingredients.mergeInt(thing, amount, Integer::sum);
	}

	public Object2IntMap<T> getMap() {
		return new Object2IntOpenHashMap<>(ingredients);
	}

	@Override
	public String toString() {
		return ingredients.toString();
	}
}