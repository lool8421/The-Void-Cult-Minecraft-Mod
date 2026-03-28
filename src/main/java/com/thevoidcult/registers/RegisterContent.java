package com.thevoidcult.registers;

import com.thevoidcult.main.TheVoidCult;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RegisterContent {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheVoidCult.MOD_ID);

    public static final DeferredItem<Item> PORTAL_MATTER = ITEMS.register("portal_matter",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ENDER_CULTIST_HELMET = ITEMS.register("ender_cultist_helmet",
            () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }


}
