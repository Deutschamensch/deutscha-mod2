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
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CosmeticsLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
	private static final String MOD_ID = "deutschamod";
	private static final int FULL_BRIGHT = 0xF000F0;

	private static final float CROUCH_TILT_DEGREES = 28.0F;

	private static final float CLOAK_TOP_Y = 0.02F;
	private static final float CLOAK_Z = 0.21F;
	private static final float CLOAK_WIDTH_SCALE = 0.85F;
	private static final float CLOAK_HEIGHT_SCALE = 1.0F;
	private static final float CLOAK_SEGMENT_UNITS = 6.0F;
	private static final float[] SWING_WEIGHT = {0.45F, 0.72F, 0.98F};
	private static final float[] SWAY_WEIGHT = {0.35F, 0.65F, 1.0F};
	private static final float[] SPRING_K = {75.0F, 52.0F, 38.0F};
	private static final float[] SPRING_C = {9.5F, 7.8F, 6.5F};

	private static final float WING_JOINT_X = 0.12F;
	private static final float WING_JOINT_Y = 0.28F;
	private static final float WING_JOINT_Z = 0.27F;
	private static final float WING_PIVOT_UNITS = 12.96F;
	private static final float WING_SCALE = 0.9F;
	private static final float WING_JOINT_V = 0.46F;
	private static final float WING_HEIGHT = 1.5F;

	private static final float CHAIN_SCALE = 0.55F;
	private static final float CHAIN_SEGMENT_UNITS = 5.0F;
	private static final float[][] CHAINS = {{0.31F, 0.61F, 6.0F}, {0.50F, 0.54F, 4.0F}};

	private final ItemModelResolver itemModelResolver;
	private ItemStack[] cloakStacks;
	private ItemStack chainStack;
	private ItemStack wingLeftStack;
	private ItemStack wingRightStack;

	private boolean hasLast;
	private long lastNanos;
	private double lastX;
	private double lastY;
	private double lastZ;
	private float lastYaw;
	private float smoothForward;
	private float smoothSide;
	private float smoothVertical;
	private float smoothYawRate;
	private float movement;
	private float fall;
	private float phase;
	private float flapRate;
	private final float[] swingAngle = new float[3];
	private final float[] swingVelocity = new float[3];
	private final float[] swayAngle = new float[3];
	private final float[] swayVelocity = new float[3];

	public CosmeticsLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent, ItemModelResolver itemModelResolver) {
		super(parent);
		this.itemModelResolver = itemModelResolver;
	}

	private void ensureInitialized() {
		if (this.cloakStacks != null) {
			return;
		}
		this.cloakStacks = new ItemStack[] {createStack("cloak_0"), createStack("cloak_1"), createStack("cloak_2")};
		this.chainStack = createStack("chain");
		this.wingLeftStack = createStack("wing_left");
		this.wingRightStack = createStack("wing_right");
	}

	private static ItemStack createStack(String modelName) {
		ItemStack stack = new ItemStack(Items.STICK);
		stack.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath(MOD_ID, modelName));
		return stack;
	}

	private static Matrix4f rotX(float degrees) {
		return new Matrix4f().rotationX((float) Math.toRadians(degrees));
	}

	private static Matrix4f rotY(float degrees) {
		return new Matrix4f().rotationY((float) Math.toRadians(degrees));
	}

	private static Matrix4f rotZ(float degrees) {
		return new Matrix4f().rotationZ((float) Math.toRadians(degrees));
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
		LocalPlayer self = Minecraft.getInstance().player;
		if (self == null || state.id != self.getId() || state.isInvisible) {
			return;
		}
		ensureInitialized();
		updateMotion(state);

		poseStack.pushPose();
		if (state.isCrouching) {
			poseStack.mulPose(rotX(CROUCH_TILT_DEGREES));
		}
		if (Cosmetics.cloakEnabled()) {
			renderCloak(poseStack, collector, light, state);
		}
		if (Cosmetics.wingsEnabled()) {
			renderWings(poseStack, collector, state);
		}
		poseStack.popPose();
	}

	private void updateMotion(AvatarRenderState state) {
		long now = System.nanoTime();
		if (!this.hasLast || now - this.lastNanos > 500_000_000L) {
			this.hasLast = true;
			this.lastNanos = now;
			this.lastX = state.x;
			this.lastY = state.y;
			this.lastZ = state.z;
			this.lastYaw = state.bodyRot;
			return;
		}
		float realDt = (now - this.lastNanos) / 1.0E9F;
		if (realDt < 0.0005F) {
			return;
		}
		float dt = Math.min(realDt, 0.05F);

		float vx = (float) ((state.x - this.lastX) / realDt);
		float vy = (float) ((state.y - this.lastY) / realDt);
		float vz = (float) ((state.z - this.lastZ) / realDt);
		float yawRate = Mth.wrapDegrees(state.bodyRot - this.lastYaw) / realDt;
		this.lastNanos = now;
		this.lastX = state.x;
		this.lastY = state.y;
		this.lastZ = state.z;
		this.lastYaw = state.bodyRot;

		vx = Mth.clamp(vx, -15.0F, 15.0F);
		vz = Mth.clamp(vz, -15.0F, 15.0F);
		vy = Mth.clamp(vy, -40.0F, 40.0F);
		yawRate = Mth.clamp(yawRate, -720.0F, 720.0F);

		double yaw = Math.toRadians(state.bodyRot);
		float forward = (float) (-vx * Math.sin(yaw) + vz * Math.cos(yaw));
		float right = (float) (-(vx * Math.cos(yaw) + vz * Math.sin(yaw)));

		float blend = 1.0F - (float) Math.exp(-dt * 12.0F);
		this.smoothForward += (forward - this.smoothForward) * blend;
		this.smoothSide += (right - this.smoothSide) * blend;
		this.smoothVertical += (vy - this.smoothVertical) * blend;
		this.smoothYawRate += (yawRate - this.smoothYawRate) * blend;

		float speed = (float) Math.sqrt(forward * forward + right * right);
		float slow = 1.0F - (float) Math.exp(-dt * 4.0F);
		this.movement += (Mth.clamp(speed / 5.0F, 0.0F, 1.0F) - this.movement) * slow;
		this.fall += (Mth.clamp(-this.smoothVertical / 12.0F, 0.0F, 1.0F) - this.fall) * slow;

		this.flapRate = 4.5F + 3.5F * this.movement + 4.0F * this.fall;
		this.phase += this.flapRate * dt;
		if (this.phase > 62831.0F) {
			this.phase -= 62831.0F;
		}

		float t = now / 1.0E9F;
		float swingTarget = Mth.clamp(this.smoothForward * 8.5F, -6.0F, 60.0F)
			+ Mth.clamp(-this.smoothVertical * 2.5F, -6.0F, 25.0F)
			+ (state.isCrouching ? 16.0F : 0.0F)
			+ 2.0F * (float) Math.sin(t * 1.7F);
		float swayTarget = Mth.clamp(-24.0F * (this.smoothSide / 4.3F), -32.0F, 32.0F)
			- Mth.clamp(this.smoothYawRate * 0.03F, -14.0F, 14.0F);

		int steps = Math.max(1, (int) Math.ceil(dt / 0.008F));
		float h = dt / steps;
		for (int s = 0; s < steps; s++) {
			for (int i = 0; i < 3; i++) {
				float swingGoal = 3.0F + swingTarget * SWING_WEIGHT[i];
				this.swingVelocity[i] += (SPRING_K[i] * (swingGoal - this.swingAngle[i]) - SPRING_C[i] * this.swingVelocity[i]) * h;
				this.swingAngle[i] += this.swingVelocity[i] * h;

				float swayGoal = swayTarget * SWAY_WEIGHT[i];
				this.swayVelocity[i] += (SPRING_K[i] * (swayGoal - this.swayAngle[i]) - SPRING_C[i] * this.swayVelocity[i]) * h;
				this.swayAngle[i] += this.swayVelocity[i] * h;
			}
		}
		for (int i = 0; i < 3; i++) {
			this.swingAngle[i] = Mth.clamp(this.swingAngle[i], 1.5F, 85.0F);
			this.swayAngle[i] = Mth.clamp(this.swayAngle[i], -45.0F, 45.0F);
		}
	}

	private void renderCloak(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state) {
		float segmentHeight = CLOAK_SEGMENT_UNITS / 16.0F * CLOAK_HEIGHT_SCALE;
		poseStack.pushPose();
		poseStack.translate(0.0F, CLOAK_TOP_Y, CLOAK_Z);
		float previousSwing = 0.0F;
		float previousSway = 0.0F;
		for (int i = 0; i < 3; i++) {
			poseStack.mulPose(rotX(this.swingAngle[i] - previousSwing));
			poseStack.mulPose(rotZ(this.swayAngle[i] - previousSway));
			previousSwing = this.swingAngle[i];
			previousSway = this.swayAngle[i];
			renderModel(poseStack, collector, light, state, this.cloakStacks[i], CLOAK_SEGMENT_UNITS, CLOAK_WIDTH_SCALE, CLOAK_HEIGHT_SCALE);
			poseStack.translate(0.0F, segmentHeight * 0.97F, 0.0F);
		}
		poseStack.popPose();
	}

	private void renderWings(PoseStack poseStack, SubmitNodeCollector collector, AvatarRenderState state) {
		float up = 30.0F + 18.0F * (float) Math.sin(this.phase) + 14.0F * this.fall;
		float sweep = 26.0F + 8.0F * (float) Math.sin(this.phase - 0.5F);
		if (state.isCrouching) {
			up -= 12.0F;
		}
		float upRate = 18.0F * (float) Math.cos(this.phase) * this.flapRate;
		float chainLagZ = Mth.clamp(upRate * 0.1F, -20.0F, 20.0F);
		float chainSwing = this.swingAngle[0] * 0.5F;

		for (int side = 1; side >= -1; side -= 2) {
			poseStack.pushPose();
			poseStack.translate(side * WING_JOINT_X, WING_JOINT_Y, WING_JOINT_Z);
			poseStack.mulPose(rotY(-side * sweep));
			poseStack.mulPose(rotZ(-side * up));
			ItemStack wing = side > 0 ? this.wingRightStack : this.wingLeftStack;
			renderModel(poseStack, collector, FULL_BRIGHT, state, wing, WING_PIVOT_UNITS, WING_SCALE, WING_SCALE);

			for (float[] chain : CHAINS) {
				poseStack.pushPose();
				poseStack.translate(side * chain[0] * WING_SCALE, (chain[1] - WING_JOINT_V) * WING_HEIGHT * WING_SCALE, 0.03F);
				poseStack.mulPose(rotZ(side * up * 0.9F));
				renderChain(poseStack, collector, state, side, (int) chain[2], chainLagZ, chainSwing);
				poseStack.popPose();
			}
			poseStack.popPose();
		}
	}

	private void renderChain(PoseStack poseStack, SubmitNodeCollector collector, AvatarRenderState state, int side, int segments, float lagZ, float swing) {
		float segmentLength = CHAIN_SEGMENT_UNITS / 16.0F * CHAIN_SCALE;
		for (int j = 0; j < segments; j++) {
			float weight = 0.10F + 0.02F * j;
			poseStack.mulPose(rotZ(side * lagZ * weight));
			poseStack.mulPose(rotX(swing * weight));
			for (int k = -1; k <= 1; k += 2) {
				poseStack.pushPose();
				poseStack.mulPose(rotY(45.0F * k));
				renderModel(poseStack, collector, FULL_BRIGHT, state, this.chainStack, CHAIN_SEGMENT_UNITS, CHAIN_SCALE, CHAIN_SCALE);
				poseStack.popPose();
			}
			poseStack.translate(0.0F, segmentLength * 0.95F, 0.0F);
		}
	}

	private void renderModel(
		PoseStack poseStack,
		SubmitNodeCollector collector,
		int light,
		AvatarRenderState state,
		ItemStack stack,
		float pivotUnits,
		float scaleX,
		float scaleY
	) {
		ItemStackRenderState renderState = new ItemStackRenderState();
		this.itemModelResolver.updateForTopItem(renderState, stack, ItemDisplayContext.NONE, null, null, 0);
		poseStack.pushPose();
		poseStack.scale(scaleX, -scaleY, -scaleY);
		poseStack.translate(0.0F, 0.5F - pivotUnits / 16.0F, 0.5F);
		renderState.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
		poseStack.popPose();
	}
}
