package com.thevoidcult.events;

import com.thevoidcult.main.TheVoidCult;
import com.thevoidcult.main.TheVoidCultConfig;
import com.thevoidcult.mobs.client.EndermanCultistModel;
import com.thevoidcult.mobs.custom.EndermanCultistEntity;
import com.thevoidcult.registers.RegisterContent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.EnderManAngerEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import java.util.List;


@EventBusSubscriber(modid = TheVoidCult.MOD_ID)
public class ModEventBusEvents {

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(EndermanCultistModel.BODY_LAYER_LOCATION, EndermanCultistModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(RegisterContent.ENDERMAN_CULTIST.get(), EndermanCultistEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onEndermanAnger(EnderManAngerEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.is(RegisterContent.ENDER_CULTIST_HELMET.get())) {

            event.setCanceled(true);
        }
    }


    private static void spawnPortalMatter(Level level, Vec3 pos) {
        if (level instanceof ServerLevel serverLevel) {

            ItemStack stack = new ItemStack(RegisterContent.PORTAL_MATTER.get());
            ItemEntity itemEntity = new ItemEntity(level, pos.x, pos.y, pos.z, stack);

            itemEntity.setDeltaMovement(
                    (level.random.nextDouble() - 0.5) * 0.1,
                    0.2,
                    (level.random.nextDouble() - 0.5) * 0.1
            );

            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    pos.x, pos.y, pos.z,
                    10, 0.2, 0.2, 0.2, 0.05);

            level.addFreshEntity(itemEntity);
        }
    }

    @SubscribeEvent
    public static void onEndermanBlink(EntityTeleportEvent.EnderEntity event) {
        Entity entity = event.getEntity();
        if (entity instanceof EnderMan || entity instanceof EndermanCultistEntity || entity instanceof Shulker) {
            Level level = event.getEntity().level();
            if (level.random.nextFloat() < TheVoidCultConfig.ENDERMAN_DROP_CHANCE.get()) {
                spawnPortalMatter(level, new Vec3(event.getTargetX(), event.getTargetY(), event.getTargetZ()));
            }
        }
    }

    @SubscribeEvent
    public static void onChorusEat(EntityTeleportEvent.ChorusFruit event) {
        Level level = event.getEntity().level();

        if (level.random.nextFloat() < TheVoidCultConfig.CHORUS_DROP_CHANCE.get()) {
            spawnPortalMatter(level, new Vec3(event.getTargetX(), event.getTargetY(), event.getTargetZ()));
        }
    }

    @SubscribeEvent
    public static void onPearlUse(EntityTeleportEvent.EnderPearl event) {
        Level level = event.getEntity().level();

        if (level.random.nextFloat() < TheVoidCultConfig.PEARL_USE_DROP_CHANCE.get()) {
            spawnPortalMatter(level, new Vec3(event.getTargetX(), event.getTargetY(), event.getTargetZ()));
        }
    }

    private static void handlePortalMatterDrop(LivingDropsEvent event, double baseChance) {
        DamageSource source = event.getSource();
        Level level = event.getEntity().level();
        int lootingLevel = 0;

        if (source.getEntity() instanceof LivingEntity attacker) {
            lootingLevel = EnchantmentHelper.getEnchantmentLevel(
                    level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING),
                    attacker
            );
        }

        double lootingBonus = baseChance * (lootingLevel);
        double finalRoll = baseChance + level.getRandom().nextDouble() * lootingBonus;

        int count = (int) finalRoll;
        float extraChance = (float) (finalRoll - count);

        if (level.getRandom().nextFloat() < extraChance) {
            count++;
        }

        if (count > 0) {
            event.getDrops().add(new ItemEntity(level,
                    event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(),
                    new ItemStack(RegisterContent.PORTAL_MATTER.get(), count)));
        }
    }

    @SubscribeEvent
    public static void onEntityDrop(LivingDropsEvent event) {
        Level level = event.getEntity().level();
        if (level.isClientSide) return;

        String killedEntityId = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).toString();

        List<? extends String> entities = TheVoidCultConfig.PORTAL_MATTER_ENTITIES.get();
        List<? extends Double> chances = TheVoidCultConfig.PORTAL_MATTER_DROP_CHANCES.get();

        int index = entities.indexOf(killedEntityId);

        if (index != -1) {
            double baseChance = 0.1;

            if (index < chances.size()) {
                Double configValue = chances.get(index);
                if (configValue != null) {
                    baseChance = configValue;
                }
            } else {
                TheVoidCult.LOGGER.warn("Entity '{}' is in portalMatterEntities but lacks a corresponding chance in portalMatterChances! Defaulting to 0.1", killedEntityId);
            }

            handlePortalMatterDrop(event, baseChance);
        }
    }
}
