package moze_intel.projecte.emc;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import moze_intel.projecte.utils.Constants;

public class IngredientMap<T> {

	private final Object2IntMap<T> ingredients = new Object2IntOpenHashMap<>();

	public void addIngredient(T thing, int amount) {
		ingredients.mergeInt(thing, amount, Constants.INT_SUM);
	}

	public Object2IntMap<T> getMap() {
		//Note: We use an array map under the assumption that no conversion will have a massive number of ingredients
		// especially as copying into an array map via constructor is cheaper, and then we only iterate it in the mapper
		return new Object2IntArrayMap<>(ingredients);
	}

	@Override
	public String toString() {
		return ingredients.toString();
	}
}