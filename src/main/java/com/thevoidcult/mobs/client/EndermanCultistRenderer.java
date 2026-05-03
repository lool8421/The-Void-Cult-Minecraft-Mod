package com.thevoidcult.mobs.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thevoidcult.main.TheVoidCult;
import com.thevoidcult.mobs.custom.EndermanCultistEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class EndermanCultistRenderer extends MobRenderer<EndermanCultistEntity, EndermanCultistModel<EndermanCultistEntity>> {

    public EndermanCultistRenderer(EntityRendererProvider.Context context) {
        super(context, new EndermanCultistModel<>(context.bakeLayer(EndermanCultistModel.LAYER_LOCATION)), 0.3f);
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
