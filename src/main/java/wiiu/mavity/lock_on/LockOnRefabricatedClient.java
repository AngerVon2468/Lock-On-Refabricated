package wiiu.mavity.lock_on;

import net.fabricmc.api.ClientModInitializer;

import org.slf4j.*;

public class LockOnRefabricatedClient implements ClientModInitializer {

	public static final String MOD_ID = "lock_on";

	public static final String NAME = "Lock On Refabricated";

    public static final Logger LOGGER = LoggerFactory.getLogger("lock_on");

	@Override
	public void onInitializeClient() {
		LockOnHandler.registerLockOnKeybinds();
	}
}