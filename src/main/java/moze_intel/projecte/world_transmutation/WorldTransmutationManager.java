package moze_intel.projecte.world_transmutation;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.world_transmutation.IWorldTransmutation;
import moze_intel.projecte.api.world_transmutation.WorldTransmutationFile;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.conditions.WithConditions;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldTransmutationManager extends SimpleJsonResourceReloadListener {

	//Copy of gson settings from RecipeManager's gson instance
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	public static final WorldTransmutationManager INSTANCE = new WorldTransmutationManager();
	private Set<IWorldTransmutation> entries = Collections.emptySet();
	@Nullable
	private Set<IWorldTransmutation> modifiedEntries = null;

	private WorldTransmutationManager() {
		super(GSON, "pe_world_transmutations");
	}

	@Override
	protected void apply(@NotNull Map<ResourceLocation, JsonElement> object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
		//Ensure we are interacting with the condition context
		RegistryOps<JsonElement> registryOps = makeConditionalOps();

		ImmutableSet.Builder<IWorldTransmutation> builder = ImmutableSet.builder();

		// Find all data/<domain>/pe_world_transmutations/foo/bar.json
		for (Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
			ResourceLocation file = entry.getKey();//<domain>:foo/bar
			DataResult<Optional<WithConditions<WorldTransmutationFile>>> result = WorldTransmutationFile.CONDITIONAL_CODEC.parse(registryOps, entry.getValue());
			if (result.isSuccess()) {
				Optional<WithConditions<WorldTransmutationFile>> decoded = result.getOrThrow();
				if (decoded.isPresent()) {
					//TODO - 1.21: How do we want to resolve conflicts?
					//TODO - 1.21: Re-evaluate how we want to handle the modid or domain etc
					String senderModId = file.getNamespace();
					for (IWorldTransmutation transmutation : decoded.get().carrier().transmutations()) {
						PECore.debugLog("Mod: '{}' registered {}", senderModId, transmutation);
						builder.add(transmutation);
					}
				} else {
					PECore.debugLog("Skipping loading world transmutation file {} as its conditions were not met", file);
				}
			} else {
				result.ifError(error -> PECore.LOGGER.error("Parsing error loading world transmutation file {}: {}", file, error.message()));
			}
		}
		entries = builder.build();
	}

	public Collection<IWorldTransmutation> getWorldTransmutations() {
		return modifiedEntries == null ? entries : modifiedEntries;
	}

	@Nullable
	public IWorldTransmutation getWorldTransmutation(BlockState current) {
		//TODO - 1.21: Do we want the current state as a key in a map rather than iterating all entries?
		// Prioritize explicit matches, and then do the simple ones?
		for (IWorldTransmutation entry : getWorldTransmutations()) {
			if (entry.canTransmute(current)) {
				return entry;
			}
		}
		return null;
	}

	/// Methods that exist for CrT integration

	@ApiStatus.Internal
	public void clearTransmutations() {
		//Explicitly mark that the modified version is empty
		this.modifiedEntries = Collections.emptySet();
	}

	@ApiStatus.Internal
	public void resetWorldTransmutations() {
		modifiedEntries = null;
	}

	@ApiStatus.Internal
	public void register(IWorldTransmutation transmutation) {
		if (modifiedEntries == null) {
			modifiedEntries = new HashSet<>(entries);
		} else if (modifiedEntries == Collections.<IWorldTransmutation>emptySet()) {
			//If we have no entries and the set we used was immutable, we need to switch to using a mutable one
			modifiedEntries = new HashSet<>();
		}
		//Try to add the transmutation
		modifiedEntries.add(transmutation);
	}

	@ApiStatus.Internal
	public void removeWorldTransmutation(IWorldTransmutation transmutation) {
		if (modifiedEntries == null) {
			if (entries.contains(transmutation)) {
				//Only bother instantiating the modified one if we actually have the transmutation
				modifiedEntries = new HashSet<>(entries);
				modifiedEntries.remove(transmutation);
			}
		} else {
			modifiedEntries.remove(transmutation);
		}
	}
}