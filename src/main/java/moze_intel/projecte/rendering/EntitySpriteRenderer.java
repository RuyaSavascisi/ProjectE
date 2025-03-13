package moze_intel.projecte.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class EntitySpriteRenderer<ENTITY extends Entity> extends EntityRenderer<ENTITY> {

	private final ResourceLocation texture;

	public EntitySpriteRenderer(EntityRendererProvider.Context context, ResourceLocation texture) {
		super(context);
		this.texture = texture;
	}

	@NotNull
	@Override
	public ResourceLocation getTextureLocation(@NotNull ENTITY entity) {
		return texture;
	}

	@Override
	public void render(@NotNull ENTITY entity, float entityYaw, float partialTick, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light) {
		matrix.pushPose();
		matrix.scale(0.5F, 0.5F, 0.5F);
		matrix.mulPose(entityRenderDispatcher.cameraOrientation());
		VertexConsumer builder = renderer.getBuffer(PERenderType.SPRITE_RENDERER.apply(getTextureLocation(entity)));
		Matrix4f matrix4f = matrix.last().pose();
		builder.addVertex(matrix4f, 1, -1, 0).setUv(0, 1);
		builder.addVertex(matrix4f, 1, 1, 0).setUv(0, 0);
		builder.addVertex(matrix4f, -1, 1, 0).setUv(1, 0);
		builder.addVertex(matrix4f, -1, -1, 0).setUv(1, 1);
		matrix.popPose();
	}
}