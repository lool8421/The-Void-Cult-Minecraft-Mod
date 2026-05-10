package com.thevoidcult.main;

import com.thevoidcult.items.MobPearlItem;
import com.thevoidcult.mobs.client.EndermanCultistRenderer;
import com.thevoidcult.registers.RegisterContent;
import net.minecraft.client.renderer.entity.EntityRenderers;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(TheVoidCult.MOD_ID)
public class TheVoidCult {

    public static final String MOD_ID = "thevoidcult";
    public static final Logger LOGGER = LogUtils.getLogger();


    public TheVoidCult(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);
        RegisterContent.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, TheVoidCultConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        MobPearlItem.registerDispenserBehavior();
    }



    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    @EventBusSubscriber(modid = TheVoidCult.MOD_ID, value = Dist.CLIENT)
    static class ClientModEvents {
        @SubscribeEvent
        static void onClientSetup(FMLClientSetupEvent event) {
            MobPearlItem.SetupDisplayType(event);
            EntityRenderers.register(RegisterContent.ENDERMAN_CULTIST.get(), EndermanCultistRenderer::new);
        }
    }
}
