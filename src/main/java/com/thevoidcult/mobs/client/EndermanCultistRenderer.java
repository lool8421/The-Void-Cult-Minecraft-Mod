package com.thevoidcult.mobs.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thevoidcult.items.SinsList;
import com.thevoidcult.main.TheVoidCult;
import com.thevoidcult.mobs.custom.EndermanCultistEntity;

import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;

import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.EnumMap;
import java.util.Map;

public class EndermanCultistRenderer extends MobRenderer<EndermanCultistEntity, EndermanCultistModel<EndermanCultistEntity>> {

    private static final Map<SinsList, ResourceLocation> BODY_TEXTURES = Util.make(new EnumMap<>(SinsList.class), map -> {
        map.put(SinsList.NONE, ResourceLocation.fromNamespaceAndPath("thevoidcult", "textures/entity/enderman_cultist/base.png"));
        map.put(SinsList.WRATH, ResourceLocation.fromNamespaceAndPath("thevoidcult", "textures/entity/enderman_cultist/cultist_wrath.png"));
        map.put(SinsList.GREED, ResourceLocation.fromNamespaceAndPath("thevoidcult", "textures/entity/enderman_cultist/cultist_greed.png"));
        map.put(SinsList.PRIDE, ResourceLocation.fromNamespaceAndPath("thevoidcult", "textures/entity/enderman_cultist/cultist_pride.png"));
        map.put(SinsList.GLUTTONY, ResourceLocation.fromNamespaceAndPath("thevoidcult", "textures/entity/enderman_cultist/cultist_gluttony.png"));
        map.put(SinsList.ENVY, ResourceLocation.fromNamespaceAndPath("thevoidcult", "textures/entity/enderman_cultist/cultist_envy.png"));
        map.put(SinsList.SLOTH, ResourceLocation.fromNamespaceAndPath("thevoidcult", "textures/entity/enderman_cultist/cultist_sloth.png"));
    });
    private static final Map<SinsList, ResourceLocation> EYE_TEXTURES = Util.make(new EnumMap<>(SinsList.class), map -> {
        map.put(SinsList.NONE, ResourceLocation.fromNamespaceAndPath("thevoidcult", "textures/entity/enderman_cultist/eyes_purple.png"));
        map.put(SinsList.WRATH, ResourceLocation.fromNamespaceAndPath("thevoidcult", "textures/entity/enderman_cultist/eyes_wrath.png"));
        map.put(SinsList.GREED, ResourceLocation.fromNamespaceAndPath("thevoidcult", "textures/entity/enderman_cultist/eyes_greed.png"));
        map.put(SinsList.PRIDE, ResourceLocation.fromNamespaceAndPath("thevoidcult", "textures/entity/enderman_cultist/eyes_pride.png"));
        map.put(SinsList.GLUTTONY, ResourceLocation.fromNamespaceAndPath("thevoidcult", "textures/entity/enderman_cultist/eyes_gluttony.png"));
        map.put(SinsList.ENVY, ResourceLocation.fromNamespaceAndPath("thevoidcult", "textures/entity/enderman_cultist/eyes_envy.png"));
        map.put(SinsList.SLOTH, ResourceLocation.fromNamespaceAndPath("thevoidcult", "textures/entity/enderman_cultist/eyes_sloth.png"));
    });


    public EndermanCultistRenderer(EntityRendererProvider.Context context) {
        super(context, new EndermanCultistModel<>(context.bakeLayer(EndermanCultistModel.BODY_LAYER_LOCATION)), 0.5f);

        this.addLayer(new RenderLayer<EndermanCultistEntity, EndermanCultistModel<EndermanCultistEntity>>(this) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, EndermanCultistEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

                ResourceLocation eyeTexture = EYE_TEXTURES.getOrDefault(entity.getSyncedType(), EYE_TEXTURES.get(SinsList.NONE));
                VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.eyes(eyeTexture));
                poseStack.pushPose();
                this.getParentModel().root.translateAndRotate(poseStack);
                this.getParentModel().head.translateAndRotate(poseStack);
                this.getParentModel().upper_head.translateAndRotate(poseStack);
                poseStack.translate(0.0D, 0.0D, -0.001D);
                this.getParentModel().eyes.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
                poseStack.popPose();
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(EndermanCultistEntity endermanCultistEntity) {
        return BODY_TEXTURES.getOrDefault(endermanCultistEntity.getSyncedType(), BODY_TEXTURES.get(SinsList.NONE));
    }

    @Override
    public void render(EndermanCultistEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
