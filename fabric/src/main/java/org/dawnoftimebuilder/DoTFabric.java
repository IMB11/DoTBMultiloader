package org.dawnoftimebuilder;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

public class DoTFabric implements ModInitializer, ClientModInitializer {
    
    @Override
    public void onInitialize() {
        RegistryImpls.init();
        CommonClass.init();
    }

    @Override
    public void onInitializeClient() {
        RegistryImpls.initClient();
        RenderLayers.init();
    }
}
