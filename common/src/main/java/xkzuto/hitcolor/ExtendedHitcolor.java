package xkzuto.hitcolor;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtendedHitcolor implements ModInitializer {
    public static final String MOD_ID = "extended-hitcolor";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Extended HitColor ready.");
    }
}
