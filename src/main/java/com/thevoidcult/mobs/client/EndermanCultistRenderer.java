package com.thevoidcult.mobs.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thevoidcult.main.TheVoidCult;
import com.thevoidcult.mobs.custom.EndermanCultistEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;

import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class EndermanCultistRenderer extends MobRenderer<EndermanCultistEntity, EndermanCultistModel<EndermanCultistEntity>> {

    public EndermanCultistRenderer(EntityRendererProvider.Context context) {
        super(context, new EndermanCultistModel<>(context.bakeLayer(EndermanCultistModel.BODY_LAYER_LOCATION)), 0.5f);

        this.addLayer(new RenderLayer<EndermanCultistEntity, EndermanCultistModel<EndermanCultistEntity>>(this) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, EndermanCultistEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

                ResourceLocation eyeTexture = ResourceLocation.fromNamespaceAndPath(TheVoidCult.MOD_ID, "textures/entity/enderman_cultist/eyes_purple.png");
                VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.eyes(eyeTexture));

                poseStack.pushPose();
                this.getParentModel().root.translateAndRotate(poseStack);
                this.getParentModel().head.translateAndRotate(poseStack);
                this.getParentModel().upper_head.translateAndRotate(poseStack);
                poseStack.translate(0.0D, 0.0D, -0.001D);

                // 3. Render the specific part
                this.getParentModel().eyes.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
                poseStack.popPose();
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(EndermanCultistEntity endermanCultistEntity) {
        return ResourceLocation.fromNamespaceAndPath(TheVoidCult.MOD_ID, "textures/entity/enderman_cultist/base.png");
    }

    @Override
    public void render(EndermanCultistEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
