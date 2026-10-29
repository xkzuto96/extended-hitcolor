package xkzuto.hitcolor.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import xkzuto.hitcolor.client.config.HitColorConfig;
import xkzuto.hitcolor.client.util.ColorHelper;
import xkzuto.hitcolor.client.util.ScreenHelper;

public class ExtendedHitColorClient implements ClientModInitializer {
    public static KeyMapping openConfigKey;

    @Override
    public void onInitializeClient() {
        HitColorConfig.get();

        openConfigKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.extended-hitcolor.open_config",
                InputConstants.KEY_H,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openConfigKey.consumeClick()) {
                ScreenHelper.openConfigScreen(mc);
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var open = ClientCommands.literal("hitcolor")
                    .executes(ctx -> {
                        Minecraft mc = ctx.getSource().getClient();
                        mc.schedule(() -> ScreenHelper.openConfigScreen(mc));
                        return 1;
                    });
            dispatcher.register(open);
            dispatcher.register(ClientCommands.literal("extendedhitcolor").executes(open.getCommand()));
        });
    }

    public static void syncOverlayTexture() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer != null && mc.gameRenderer.overlayTexture() instanceof ExtendedOverlayTexture ext) {
            HitColorConfig cfg = HitColorConfig.get();
            int argb = cfg.customHitColorEnabled ? cfg.getOverlayTextureArgb() : ColorHelper.VANILLA_HIT_COLOR;
            ext.extendedhitcolor$updateColor(argb);
        }
    }
}
