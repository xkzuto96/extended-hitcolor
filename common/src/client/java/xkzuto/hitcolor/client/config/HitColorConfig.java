package xkzuto.hitcolor.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.ARGB;
import xkzuto.hitcolor.ExtendedHitcolor;
import xkzuto.hitcolor.client.util.ColorHelper;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class HitColorConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("extended_hitcolor.json");

    private static HitColorConfig instance;

    public boolean armorHitColorEnabled = true;
    public boolean customHitColorEnabled = true;
    public int red = 255;
    public int green = 0;
    public int blue = 0;
    public int alpha = ColorHelper.VANILLA_ALPHA;

    public static HitColorConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public static HitColorConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                HitColorConfig cfg = GSON.fromJson(reader, HitColorConfig.class);
                if (cfg != null) {
                    cfg.clamp();
                    return cfg;
                }
            } catch (Exception e) {
                ExtendedHitcolor.LOGGER.error("Failed to read config, falling back to defaults", e);
            }
        }
        HitColorConfig def = new HitColorConfig();
        def.save();
        return def;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            ExtendedHitcolor.LOGGER.error("Failed to save config", e);
        }
    }

    public void clamp() {
        red = Math.clamp(red, 0, 255);
        green = Math.clamp(green, 0, 255);
        blue = Math.clamp(blue, 0, 255);
        alpha = Math.clamp(alpha, 0, 255);
    }

    public void resetToVanilla() {
        this.armorHitColorEnabled = true;
        this.customHitColorEnabled = true;
        this.red = 255;
        this.green = 0;
        this.blue = 0;
        this.alpha = ColorHelper.VANILLA_ALPHA;
        save();
    }

    public int getColorAsArgb() {
        return ARGB.color(alpha, red, green, blue);
    }

    public int getOverlayTextureArgb() {
        return ARGB.color(255 - alpha, red, green, blue);
    }

    public int getOpaqueColor() {
        return ARGB.color(255, red, green, blue);
    }

    public String getHex() {
        return ColorHelper.toHex(red, green, blue);
    }

    public String getHexWithAlpha() {
        return ColorHelper.toHexWithAlpha(alpha, red, green, blue);
    }
}
