package xkzuto.hitcolor.client.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import xkzuto.hitcolor.client.ExtendedOverlayTexture;
import xkzuto.hitcolor.client.config.HitColorConfig;

@Mixin(OverlayTexture.class)
public abstract class OverlayTextureMixin implements ExtendedOverlayTexture {
    @Shadow
    @Final
    private DynamicTexture texture;

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 0xB2FF0000))
    private int initHurtColor(int original) {
        HitColorConfig cfg = HitColorConfig.get();
        return cfg.customHitColorEnabled ? cfg.getOverlayTextureArgb() : original;
    }

    @Override
    public void extendedhitcolor$updateColor(int argb) {
        if (this.texture == null) return;
        NativeImage pixels = this.texture.getPixels();
        if (pixels == null) return;

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 16; x++) {
                pixels.setPixel(x, y, argb);
            }
        }
        this.texture.upload();
    }
}
