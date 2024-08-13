package org.dawnoftimebuilder;

import net.fabricmc.api.ModInitializer;

public class DoTFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        RegistryImpls.init();
        CommonClass.init();
    }
}
