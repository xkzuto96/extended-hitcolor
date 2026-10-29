package xkzuto.hitcolor.client.util;

import net.minecraft.client.Minecraft;
import xkzuto.hitcolor.client.gui.HitColorConfigScreen;

public final class ScreenHelper {
    private ScreenHelper() {}

    public static void openConfigScreen(Minecraft mc) {
        mc.setScreen(new HitColorConfigScreen(mc.screen));
    }
}
