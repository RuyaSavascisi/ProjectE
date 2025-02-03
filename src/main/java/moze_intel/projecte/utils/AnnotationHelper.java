package moze_intel.projecte.utils;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.components.DataComponentProcessor;
import moze_intel.projecte.api.components.IDataComponentProcessor;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.ModFileScanData;
import net.neoforged.neoforgespi.language.ModFileScanData.AnnotationData;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

public class AnnotationHelper {

	private static final Type MAPPER_TYPE = Type.getType(EMCMapper.class);
	private static final Type RECIPE_TYPE_MAPPER_TYPE = Type.getType(RecipeTypeMapper.class);
	private static final Type DATA_COMPONENT_PROCESSOR_TYPE = Type.getType(DataComponentProcessor.class);

	public static List<IDataComponentProcessor> getDataComponentProcessors() {
		ModList modList = ModList.get();
		List<IDataComponentProcessor> dataComponentProcessors = new ArrayList<>();
		Object2IntMap<IDataComponentProcessor> priorities = new Object2IntOpenHashMap<>();
		for (ModFileScanData scanData : modList.getAllScanData()) {
			for (AnnotationData data : scanData.getAnnotations()) {
				if (DATA_COMPONENT_PROCESSOR_TYPE.equals(data.annotationType()) && checkRequiredMods(data)) {
					//If all the mods were loaded then attempt to get the processor
					IDataComponentProcessor processor = getDataComponentProcessor(data.memberName());
					if (processor != null) {
						int priority = getPriority(data);
						dataComponentProcessors.add(processor);
						priorities.put(processor, priority);
						PECore.LOGGER.info("Found and loaded Data Component Processor: {}, with priority {}", processor.getName(), priority);
					}
				}
			}
		}
		dataComponentProcessors.sort(Comparator.comparingInt(priorities::getInt).reversed());
		return dataComponentProcessors;
	}

	public static List<IRecipeTypeMapper> getRecipeTypeMappers() {
		ModList modList = ModList.get();
		List<IRecipeTypeMapper> recipeTypeMappers = new ArrayList<>();
		Object2IntMap<IRecipeTypeMapper> priorities = new Object2IntOpenHashMap<>();
		for (ModFileScanData scanData : modList.getAllScanData()) {
			for (AnnotationData data : scanData.getAnnotations()) {
				if (RECIPE_TYPE_MAPPER_TYPE.equals(data.annotationType()) && checkRequiredMods(data)) {
					//If all the mods were loaded then attempt to get the processor
					IRecipeTypeMapper mapper = getRecipeTypeMapper(data.memberName());
					if (mapper != null) {
						int priority = getPriority(data);
						recipeTypeMappers.add(mapper);
						priorities.put(mapper, priority);
						PECore.LOGGER.info("Found and loaded RecipeType Mapper: {}, with priority {}", mapper.getName(), priority);
					}
				}
			}
		}
		recipeTypeMappers.sort(Comparator.comparingInt(priorities::getInt).reversed());
		return recipeTypeMappers;
	}

	//Note: We don't bother caching this value because EMCMappingHandler#loadMappers caches our processed result
	public static List<IEMCMapper<NormalizedSimpleStack, Long>> getEMCMappers() {
		ModList modList = ModList.get();
		List<IEMCMapper<NormalizedSimpleStack, Long>> emcMappers = new ArrayList<>();
		Object2IntMap<IEMCMapper<NormalizedSimpleStack, Long>> priorities = new Object2IntOpenHashMap<>();
		for (ModFileScanData scanData : modList.getAllScanData()) {
			for (AnnotationData data : scanData.getAnnotations()) {
				if (MAPPER_TYPE.equals(data.annotationType()) && checkRequiredMods(data)) {
					//If all the mods were loaded then attempt to get the mapper
					IEMCMapper<?, ?> mapper = getEMCMapper(data.memberName());
					if (mapper != null) {
						try {
							IEMCMapper<NormalizedSimpleStack, Long> emcMapper = (IEMCMapper<NormalizedSimpleStack, Long>) mapper;
							int priority = getPriority(data);
							emcMappers.add(emcMapper);
							priorities.put(emcMapper, priority);
							PECore.LOGGER.info("Found and loaded EMC mapper: {}, with priority {}", mapper.getName(), priority);
						} catch (ClassCastException e) {
							PECore.LOGGER.error("{}: Is not a mapper for {}, to {}", mapper.getClass(), NormalizedSimpleStack.class, Long.class, e);
						}
					}
				}
			}
		}
		emcMappers.sort(Comparator.comparingInt(priorities::getInt).reversed());
		return emcMappers;
	}

	@Nullable
	private static IEMCMapper<?, ?> getEMCMapper(String className) {
		return createOrGetInstance(className, IEMCMapper.class, EMCMapper.Instance.class, IEMCMapper::getName);
	}

	@Nullable
	private static IRecipeTypeMapper getRecipeTypeMapper(String className) {
		return createOrGetInstance(className, IRecipeTypeMapper.class, RecipeTypeMapper.Instance.class, IRecipeTypeMapper::getName);
	}

	@Nullable
	private static IDataComponentProcessor getDataComponentProcessor(String className) {
		return createOrGetInstance(className, IDataComponentProcessor.class, DataComponentProcessor.Instance.class, IDataComponentProcessor::getName);
	}

	@Nullable
	private static <T> T createOrGetInstance(String className, Class<T> baseClass, Class<? extends Annotation> instanceAnnotation, Function<T, String> nameFunction) {
		//Try to create an instance of the class
		try {
			Class<? extends T> subClass = Class.forName(className).asSubclass(baseClass);
			//First try looking at the fields of the class to see if one of them is specified as the instance
			Field[] fields = subClass.getDeclaredFields();
			for (Field field : fields) {
				if (field.isAnnotationPresent(instanceAnnotation)) {
					if (Modifier.isStatic(field.getModifiers())) {
						try {
							Object fieldValue = field.get(null);
							if (baseClass.isInstance(fieldValue)) {
								T instance = (T) fieldValue;
								PECore.debugLog("Found specified {} instance for: {}. Using it rather than creating a new instance.", baseClass.getSimpleName(),
										nameFunction.apply(instance));
								return instance;
							} else {
								PECore.LOGGER.error("{} annotation found on non {} field: {}", instanceAnnotation.getSimpleName(), baseClass.getSimpleName(), field);
								return null;
							}
						} catch (IllegalAccessException e) {
							PECore.LOGGER.error("{} annotation found on inaccessible field: {}", instanceAnnotation.getSimpleName(), field);
							return null;
						}
					} else {
						PECore.LOGGER.error("{} annotation found on non static field: {}", instanceAnnotation.getSimpleName(), field);
						return null;
					}
				}
			}
			//If we don't have any fields that have the Instance annotation, then try to create a new instance of the class
			return subClass.getDeclaredConstructor().newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | LinkageError | InvocationTargetException | NoSuchMethodException e) {
			PECore.LOGGER.error("Failed to load: {}", className, e);
		}
		return null;
	}

	private static boolean checkRequiredMods(AnnotationData data) {
		Map<String, Object> annotationData = data.annotationData();
		if (annotationData.containsKey("requiredMods")) {
			//Check if all the mods the EMCMapper wants to be loaded are loaded
			List<String> requiredMods = (List<String>) annotationData.get("requiredMods");
			if (requiredMods.stream().anyMatch(modid -> !ModList.get().isLoaded(modid))) {
				PECore.debugLog("Skipped checking class {}, as its required mods ({}) are not loaded.", data.memberName(), Arrays.toString(requiredMods.toArray()));
				return false;
			}
		}
		return true;
	}

	private static int getPriority(AnnotationData data) {
		Map<String, Object> annotationData = data.annotationData();
		if (annotationData.containsKey("priority")) {
			return (int) annotationData.get("priority");
		}
		return 0;
	}
}