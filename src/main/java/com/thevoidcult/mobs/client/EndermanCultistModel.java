// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports
package com.thevoidcult.mobs.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thevoidcult.main.TheVoidCult;
import com.thevoidcult.mobs.custom.EndermanCultistEntity;
import net.minecraft.client.animation.definitions.CamelAnimation;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class EndermanCultistModel<T extends EndermanCultistEntity> extends HierarchicalModel<T> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheVoidCult.MOD_ID, "enderman_cultist"), "main");

    private final ModelPart root;
    private final ModelPart leg_left;
    private final ModelPart leg_right;
    private final ModelPart arm_left;
    private final ModelPart arm_right;
    private final ModelPart head;
    private final ModelPart upper_head;
    private final ModelPart eyes;

    public EndermanCultistModel(ModelPart root) {
        this.root = root.getChild("root");
        this.leg_left = this.root.getChild("leg_left");
        this.leg_right = this.root.getChild("leg_right");
        this.arm_left = this.root.getChild("arm_left");
        this.arm_right = this.root.getChild("arm_right");
        this.head = this.root.getChild("head");
        this.upper_head = this.head.getChild("upper_head");
        this.eyes = this.upper_head.getChild("eyes");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create().texOffs(32, 16).addBox(-4.0F, -12.0F, -1.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -5.0F, -1.0F));

        PartDefinition leg_left = root.addOrReplaceChild("leg_left", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -1.0F, 1.0F));

        PartDefinition leg_right = root.addOrReplaceChild("leg_right", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -1.0F, 1.0F));

        PartDefinition arm_left = root.addOrReplaceChild("arm_left", CubeListBuilder.create().texOffs(56, 0).addBox(0.0F, -1.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -10.0F, 1.0F));

        PartDefinition arm_right = root.addOrReplaceChild("arm_right", CubeListBuilder.create().texOffs(56, 0).addBox(-2.0F, -1.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -10.0F, 1.0F));

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -11.0F, 1.0F));

        PartDefinition upper_head = head.addOrReplaceChild("upper_head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -6.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 0.0F));

        PartDefinition eyes = upper_head.addOrReplaceChild("eyes", CubeListBuilder.create().texOffs(24, 0).addBox(-4.0F, -1.0F, 0.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.001F)), PartPose.offset(0.0F, -1.0F, -4.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(EndermanCultistEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        applyHeadRotation(netHeadYaw, headPitch);

        this.animate(entity.attackAnimationState, EndermanCultistAnimations.anim_ritual, ageInTicks, 1.0f);
        this.animate(entity.ritualAnimationState, EndermanCultistAnimations.anim_ritual, ageInTicks, 1.0f);
        this.animate(entity.angryAnimationState, EndermanCultistAnimations.anim_angry, ageInTicks, 1.0F);
        this.animateWalk(EndermanCultistAnimations.anim_walk, limbSwing, limbSwingAmount, 2f, 2.5f);
        this.animate(entity.idleAnimationState, EndermanCultistAnimations.anim_idle, ageInTicks, 1f);
    }

    private void applyHeadRotation(float headYaw, float headPitch){
        headYaw = Mth.clamp(headYaw, -60.0f, 60.0f);
        headPitch = Mth.clamp(headPitch, -60.0f, 60.0f);

        this.head.yRot = headYaw * ((float)Mth.PI/180.0f);
        this.head.xRot = headPitch * ((float)Mth.PI/180.0f);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root(){
        return root;
    }
}