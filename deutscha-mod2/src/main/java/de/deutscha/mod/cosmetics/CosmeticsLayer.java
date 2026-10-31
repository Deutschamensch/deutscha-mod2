package de.deutscha.mod.cosmetics;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CosmeticsLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
	private static final String MOD_ID = "deutschamod";
	private static final int FULL_BRIGHT = 0xF000F0;
	private static final float CLOAK_Z = 0.23F;
	private static final float WINGS_Z = 0.27F;
	private static final float SHOULDER_Y = 1.25F;
	private static final float CROUCH_TILT_DEGREES = 28.0F;
	private static final float FLAP_BASE_DEGREES = 10.0F;
	private static final float FLAP_AMPLITUDE_DEGREES = 10.0F;
	private static final double FLAP_SPEED = 0.004D;

	private final ItemModelResolver itemModelResolver;
	private final ItemStack cloakStack = createStack("cloak");
	private final ItemStack wingLeftStack = createStack("wing_left");
	private final ItemStack wingRightStack = createStack("wing_right");
	private final ItemStackRenderState cloakState = new ItemStackRenderState();
	private final ItemStackRenderState wingLeftState = new ItemStackRenderState();
	private final ItemStackRenderState wingRightState = new ItemStackRenderState();

	public CosmeticsLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent, ItemModelResolver itemModelResolver) {
		super(parent);
		this.itemModelResolver = itemModelResolver;
	}

	private static ItemStack createStack(String modelName) {
		ItemStack stack = new ItemStack(Items.STICK);
		stack.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath(MOD_ID, modelName));
		return stack;
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
		LocalPlayer self = Minecraft.getInstance().player;
		if (self == null || state.id != self.getId() || state.isInvisible) {
			return;
		}

		poseStack.pushPose();
		if (state.isCrouching) {
			poseStack.mulPose(new Matrix4f().rotationX((float) Math.toRadians(CROUCH_TILT_DEGREES)));
		}

		if (Cosmetics.cloakEnabled()) {
			renderPiece(poseStack, collector, light, state, this.cloakStack, this.cloakState, CLOAK_Z, 0.0F);
		}

		if (Cosmetics.wingsEnabled()) {
			float flap = FLAP_BASE_DEGREES + FLAP_AMPLITUDE_DEGREES * (float) Math.sin(System.currentTimeMillis() * FLAP_SPEED);
			renderPiece(poseStack, collector, FULL_BRIGHT, state, this.wingRightStack, this.wingRightState, WINGS_Z, -flap);
			renderPiece(poseStack, collector, FULL_BRIGHT, state, this.wingLeftStack, this.wingLeftState, WINGS_Z, flap);
		}
		poseStack.popPose();
	}

	private void renderPiece(
		PoseStack poseStack,
		SubmitNodeCollector collector,
		int light,
		AvatarRenderState state,
		ItemStack stack,
		ItemStackRenderState renderState,
		float translateZ,
		float yawDegrees
	) {
		this.itemModelResolver.updateForTopItem(renderState, stack, ItemDisplayContext.NONE, null, null, 0);
		poseStack.pushPose();
		poseStack.translate(0.0F, SHOULDER_Y, translateZ);
		poseStack.mulPose(new Matrix4f().rotationY((float) Math.toRadians(yawDegrees)));
		poseStack.scale(1.0F, -1.0F, -1.0F);
		poseStack.translate(-0.5F, 0.0F, 0.0F);
		renderState.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
		poseStack.popPose();
	}
}
