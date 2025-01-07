package moze_intel.projecte.utils;

import com.google.common.collect.ImmutableBiMap;
import com.mojang.blaze3d.platform.InputConstants;
import moze_intel.projecte.PECore;
import moze_intel.projecte.network.packets.to_server.KeyPressPKT;
import moze_intel.projecte.utils.text.PELang;
import moze_intel.projecte.utils.text.TextComponentUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = PECore.MODID, value = Dist.CLIENT)
public class ClientKeyHelper {

	private static ImmutableBiMap<KeyMapping, PEKeybind> mcToPe = ImmutableBiMap.of();
	private static ImmutableBiMap<PEKeybind, KeyMapping> peToMc = ImmutableBiMap.of();

	@SubscribeEvent
	public static void keyPress(ClientTickEvent.Pre event) {//TODO - 1.21: Should this be pre or post?
		for (KeyMapping k : mcToPe.keySet()) {
			while (k.consumeClick()) {
				PacketDistributor.sendToServer(new KeyPressPKT(mcToPe.get(k)));
			}
		}
	}

	public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
		ImmutableBiMap.Builder<KeyMapping, PEKeybind> builder = ImmutableBiMap.builder();
		addKeyBinding(builder, PEKeybind.HELMET_TOGGLE, KeyModifier.SHIFT, GLFW.GLFW_KEY_X);
		addKeyBinding(builder, PEKeybind.BOOTS_TOGGLE, KeyModifier.NONE, GLFW.GLFW_KEY_X);
		addKeyBinding(builder, PEKeybind.CHARGE, KeyModifier.NONE, GLFW.GLFW_KEY_V);
		addKeyBinding(builder, PEKeybind.EXTRA_FUNCTION, KeyModifier.NONE, GLFW.GLFW_KEY_C);
		addKeyBinding(builder, PEKeybind.FIRE_PROJECTILE, KeyModifier.NONE, GLFW.GLFW_KEY_R);
		addKeyBinding(builder, PEKeybind.MODE, KeyModifier.NONE, GLFW.GLFW_KEY_G);
		mcToPe = builder.build();
		peToMc = mcToPe.inverse();
		mcToPe.keySet().forEach(event::register);
	}

	private static void addKeyBinding(ImmutableBiMap.Builder<KeyMapping, PEKeybind> builder, PEKeybind keyBind, KeyModifier modifier, int keyCode) {
		builder.put(new KeyMapping(keyBind.getTranslationKey(), KeyConflictContext.IN_GAME, modifier, InputConstants.Type.KEYSYM, keyCode,
						PELang.PROJECTE.getTranslationKey()), keyBind);
	}

	public static Component getKeyName(PEKeybind k) {
		if (peToMc.containsKey(k)) {
			return peToMc.get(k).getTranslatedKeyMessage();
		}
		//Fallback to the translation key of the key's function
		return TextComponentUtil.build(k);
	}
}