package com.thevoidcult.events;

import com.thevoidcult.main.TheVoidCult;
import com.thevoidcult.mobs.client.EndermanCultistModel;
import com.thevoidcult.mobs.custom.EndermanCultistEntity;
import com.thevoidcult.registers.RegisterContent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.EnderManAngerEvent;

@EventBusSubscriber(modid = TheVoidCult.MOD_ID)
public class ModEventBusEvents {

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event){
        event.registerLayerDefinition(EndermanCultistModel.BODY_LAYER_LOCATION, EndermanCultistModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event){
        event.put(RegisterContent.ENDERMAN_CULTIST.get(), EndermanCultistEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onEndermanAnger(EnderManAngerEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        // Check if the player is wearing your specific helmet
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.is(RegisterContent.ENDER_CULTIST_HELMET.get())) {
            // This tells the Enderman: "Move along, nothing to see here."
            event.setCanceled(true);
        }
    }
}
