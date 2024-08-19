package org.dawnoftimebuilder;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.dawnoftimebuilder.registry.DoTBColorsRegistry;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = DoTBCommon.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DoTBForgeClient {
    public DoTBForgeClient() {}
    @SubscribeEvent
    public static void setupBlockColors(final RegisterColorHandlersEvent.Block event) {
        DoTBColorsRegistry.getBlocksColorRegistry().forEach((blockColor, blocks) -> event.register(blockColor, blocks.stream().map(Supplier::get).toArray(Block[]::new)));
    }

    @SubscribeEvent
    public static void setupItemColors(final RegisterColorHandlersEvent.Item event) {
        DoTBColorsRegistry.getItemsColorRegistry().forEach((itemColor, items) -> event.register(itemColor, items.stream().map(Supplier::get).toArray(Item[]::new)));
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        final IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(DoTBForgeClient::setupBlockColors);
        eventBus.addListener(DoTBForgeClient::setupItemColors);
        RegistryImpls.initClient(eventBus);
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraftClient, parent) -> DoTBForge.HANDLER.generateGui().generateScreen(parent)
                )
        );
    }
}
