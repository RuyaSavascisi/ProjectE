package moze_intel.projecte.gameObjs.customRecipes;

import com.mojang.serialization.MapCodec;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.registries.PERecipeConditions;
import net.neoforged.neoforge.common.conditions.ICondition;

public class FullKleinStarsCondition implements ICondition {

	public static final FullKleinStarsCondition INSTANCE = new FullKleinStarsCondition();

	private FullKleinStarsCondition() {
	}

	@Override
	public boolean test(IContext context) {
		return ProjectEConfig.common.fullKleinStars.get();
	}

	@Override
	public MapCodec<? extends ICondition> codec() {
		return PERecipeConditions.FULL_KLEIN_STARS.value();
	}
}