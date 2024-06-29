package wiiu.mavity.lock_on.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class LockOnConfig extends MidnightConfig {

    @Entry(category = "settings") public static double triangleHeight = 0.7;

    @Entry(category = "settings") public static double triangleWidth = 0.9;
}