package wiiu.mavity.lock_on;

import eu.midnightdust.lib.config.MidnightConfig;

import net.fabricmc.api.ModInitializer;

import wiiu.mavity.lock_on.config.LockOnConfig;

public class LockOnRefabricated implements ModInitializer {

    @Override
    public void onInitialize() {
        MidnightConfig.init(LockOnRefabricatedClient.MOD_ID, LockOnConfig.class);
    }
}