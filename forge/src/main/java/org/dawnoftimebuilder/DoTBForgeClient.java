package org.dawnoftimebuilder;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(modid = DoTBCommon.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DoTBForgeClient {
    public DoTBForgeClient() {}

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        final IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        RegistryImpls.initClient(eventBus);

        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraftClient, parent) -> DoTBForge.HANDLER.generateGui().generateScreen(parent)
                )
        );
    }
}
