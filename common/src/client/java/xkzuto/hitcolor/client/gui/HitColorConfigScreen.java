package xkzuto.hitcolor.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import xkzuto.hitcolor.client.ExtendedHitColorClient;
import xkzuto.hitcolor.client.config.HitColorConfig;
import xkzuto.hitcolor.client.util.ColorHelper;

public class HitColorConfigScreen extends Screen {
    private static final int SPECTRUM_WIDTH = 136;
    private static final int SPECTRUM_HEIGHT = 80;
    private static final int BAR_HEIGHT = 12;
    private static final int PREVIEW_WIDTH = 64;
    private static final int PREVIEW_HEIGHT = 24;
    private static final int PRESET_SIZE = 16;
    private static final int PRESET_SPACING = 3;

    private static final Identifier SPECTRUM_TEX = Identifier.fromNamespaceAndPath("extended-hitcolor", "gui/spectrum");
    private static final Identifier HUE_TEX = Identifier.fromNamespaceAndPath("extended-hitcolor", "gui/hue_bar");
    private static final Identifier ALPHA_TEX = Identifier.fromNamespaceAndPath("extended-hitcolor", "gui/alpha_bar");
    private static final Identifier CHECKER_TEX = Identifier.fromNamespaceAndPath("extended-hitcolor", "gui/checkerboard");

    private static final int[] PRESETS = {
            0xFFFF0000, 0xFFDC143C, 0xFFFF4500, 0xFFFFD700, 0xFF00FF00,
            0xFF00FFFF, 0xFF00BFFF, 0xFF9370DB, 0xFFFF1493, 0xFFFFFFFF
    };

    private final HitColorConfig config;

    private float hue = 0.0f;
    private float saturation = 1.0f;
    private float value = 1.0f;
    private int alpha = 178;

    private boolean draggingSpectrum;
    private boolean draggingHue;
    private boolean draggingAlpha;

    private int panelX, panelY, panelW, panelH;
    private int spectrumX, spectrumY;
    private int hueBarX, hueBarY;
    private int alphaBarX, alphaBarY;
    private int previewX, previewY;
    private int presetsStartX, presetsStartY;

    private DynamicTexture spectrumTexture;
    private DynamicTexture hueTexture;
    private DynamicTexture alphaTexture;
    private DynamicTexture checkerTexture;

    private ColorSlider redSlider;
    private ColorSlider greenSlider;
    private ColorSlider blueSlider;
    private ColorSlider alphaSlider;
    private EditBox hexBox;

    private boolean isUpdating;

    public HitColorConfigScreen(Screen parent) {
        super(Component.literal("Extended HitColor"));
        this.config = HitColorConfig.get();
    }

    @Override
    protected void init() {
        this.alpha = config.alpha;
        float[] hsv = ColorHelper.rgbToHsv(config.red, config.green, config.blue);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];

        this.panelW = 380;
        this.panelH = 230;
        this.panelX = (this.width - panelW) / 2;
        this.panelY = (this.height - panelH) / 2;

        this.spectrumX = panelX + 16;
        this.spectrumY = panelY + 40;

        this.hueBarX = this.spectrumX;
        this.hueBarY = this.spectrumY + SPECTRUM_HEIGHT + 6;

        this.alphaBarX = this.spectrumX;
        this.alphaBarY = this.hueBarY + BAR_HEIGHT + 6;

        this.previewX = this.spectrumX;
        this.previewY = this.alphaBarY + BAR_HEIGHT + 8;

        int rightColX = panelX + 170;
        int rightColW = 194;
        int y = panelY + 36;

        addRenderableWidget(Button.builder(
                getArmorToggleMessage(),
                btn -> {
                    config.armorHitColorEnabled = !config.armorHitColorEnabled;
                    btn.setMessage(getArmorToggleMessage());
                }
        ).bounds(rightColX, y, rightColW, 18).build());

        y += 21;
        addRenderableWidget(Button.builder(
                getCustomToggleMessage(),
                btn -> {
                    config.customHitColorEnabled = !config.customHitColorEnabled;
                    ExtendedHitColorClient.syncOverlayTexture();
                    btn.setMessage(getCustomToggleMessage());
                }
        ).bounds(rightColX, y, rightColW, 18).build());

        y += 22;
        this.redSlider = addRenderableWidget(new ColorSlider(
                rightColX, y, rightColW, 16, "Red", config.red, 255, false,
                val -> onRgbSliderChanged(val, config.green, config.blue)
        ));

        y += 19;
        this.greenSlider = addRenderableWidget(new ColorSlider(
                rightColX, y, rightColW, 16, "Green", config.green, 255, false,
                val -> onRgbSliderChanged(config.red, val, config.blue)
        ));

        y += 19;
        this.blueSlider = addRenderableWidget(new ColorSlider(
                rightColX, y, rightColW, 16, "Blue", config.blue, 255, false,
                val -> onRgbSliderChanged(config.red, config.green, val)
        ));

        y += 19;
        this.alphaSlider = addRenderableWidget(new ColorSlider(
                rightColX, y, rightColW, 16, "Opacity", config.alpha, 255, true,
                this::onAlphaSliderChanged
        ));

        y += 21;
        this.hexBox = addRenderableWidget(new EditBox(this.font, rightColX + 54, y, rightColW - 54, 16, Component.literal("Hex")));
        this.hexBox.setMaxLength(9);
        this.hexBox.setValue(config.getHex());
        this.hexBox.setResponder(this::onHexTyped);

        this.presetsStartX = rightColX;
        this.presetsStartY = y + 26;

        int bottomBtnW = 100;
        int bottomY = panelY + panelH - 24;

        addRenderableWidget(Button.builder(
                Component.literal("Reset to Vanilla"),
                btn -> {
                    config.resetToVanilla();
                    ExtendedHitColorClient.syncOverlayTexture();
                    this.init();
                }
        ).bounds(panelX + 20, bottomY, bottomBtnW + 20, 18).build());

        addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                btn -> this.onClose()
        ).bounds(panelX + panelW - bottomBtnW - 20, bottomY, bottomBtnW, 18).build());

        initTextures();
    }

    private void initTextures() {
        releaseTextures();
        TextureManager tm = this.minecraft.getTextureManager();

        this.spectrumTexture = new DynamicTexture("hitcolor_spectrum", SPECTRUM_WIDTH, SPECTRUM_HEIGHT, false);
        tm.register(SPECTRUM_TEX, this.spectrumTexture);

        this.hueTexture = new DynamicTexture("hitcolor_hue", SPECTRUM_WIDTH, BAR_HEIGHT, false);
        tm.register(HUE_TEX, this.hueTexture);
        renderHueTexture();

        this.alphaTexture = new DynamicTexture("hitcolor_alpha", SPECTRUM_WIDTH, BAR_HEIGHT, false);
        tm.register(ALPHA_TEX, this.alphaTexture);

        this.checkerTexture = new DynamicTexture("hitcolor_checker", SPECTRUM_WIDTH, PREVIEW_HEIGHT, false);
        tm.register(CHECKER_TEX, this.checkerTexture);
        renderCheckerTexture();

        renderSpectrumTexture();
        renderAlphaTexture();
    }

    private void renderHueTexture() {
        NativeImage pixels = hueTexture.getPixels();
        if (pixels == null) return;
        for (int x = 0; x < SPECTRUM_WIDTH; x++) {
            float h = (float) x / (float) (SPECTRUM_WIDTH - 1);
            int rgb = Mth.hsvToArgb(h, 1.0f, 1.0f, 255);
            for (int y = 0; y < BAR_HEIGHT; y++) {
                pixels.setPixel(x, y, rgb);
            }
        }
        hueTexture.upload();
    }

    private void renderSpectrumTexture() {
        NativeImage pixels = spectrumTexture.getPixels();
        if (pixels == null) return;
        for (int y = 0; y < SPECTRUM_HEIGHT; y++) {
            float v = 1.0f - (float) y / (float) (SPECTRUM_HEIGHT - 1);
            for (int x = 0; x < SPECTRUM_WIDTH; x++) {
                float s = (float) x / (float) (SPECTRUM_WIDTH - 1);
                pixels.setPixel(x, y, Mth.hsvToArgb(this.hue, s, v, 255));
            }
        }
        spectrumTexture.upload();
    }

    private void renderAlphaTexture() {
        NativeImage pixels = alphaTexture.getPixels();
        if (pixels == null) return;
        int r = config.red, g = config.green, b = config.blue;
        for (int x = 0; x < SPECTRUM_WIDTH; x++) {
            int a = Math.round(((float) x / (float) (SPECTRUM_WIDTH - 1)) * 255.0f);
            int col = ARGB.color(a, r, g, b);
            for (int y = 0; y < BAR_HEIGHT; y++) {
                pixels.setPixel(x, y, col);
            }
        }
        alphaTexture.upload();
    }

    private void renderCheckerTexture() {
        NativeImage pixels = checkerTexture.getPixels();
        if (pixels == null) return;
        int light = 0xFFFFFFFF;
        int dark = 0xFFCCCCCC;
        int checkSize = 4;
        for (int y = 0; y < PREVIEW_HEIGHT; y++) {
            for (int x = 0; x < SPECTRUM_WIDTH; x++) {
                boolean isLight = ((x / checkSize) + (y / checkSize)) % 2 == 0;
                pixels.setPixel(x, y, isLight ? light : dark);
            }
        }
        checkerTexture.upload();
    }

    private void releaseTextures() {
        if (this.minecraft == null) return;
        TextureManager tm = this.minecraft.getTextureManager();
        if (spectrumTexture != null) {
            tm.release(SPECTRUM_TEX);
            spectrumTexture.close();
            spectrumTexture = null;
        }
        if (hueTexture != null) {
            tm.release(HUE_TEX);
            hueTexture.close();
            hueTexture = null;
        }
        if (alphaTexture != null) {
            tm.release(ALPHA_TEX);
            alphaTexture.close();
            alphaTexture = null;
        }
        if (checkerTexture != null) {
            tm.release(CHECKER_TEX);
            checkerTexture.close();
            checkerTexture = null;
        }
    }

    private Component getArmorToggleMessage() {
        return Component.literal("Armor Flash: ")
                .append(config.armorHitColorEnabled
                        ? Component.literal("ON").withStyle(ChatFormatting.GREEN)
                        : Component.literal("OFF").withStyle(ChatFormatting.RED));
    }

    private Component getCustomToggleMessage() {
        return Component.literal("Custom Color: ")
                .append(config.customHitColorEnabled
                        ? Component.literal("ON").withStyle(ChatFormatting.GREEN)
                        : Component.literal("OFF").withStyle(ChatFormatting.RED));
    }

    private void onRgbSliderChanged(int r, int g, int b) {
        if (isUpdating) return;
        config.red = r;
        config.green = g;
        config.blue = b;

        float[] hsv = ColorHelper.rgbToHsv(r, g, b);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];

        renderSpectrumTexture();
        renderAlphaTexture();
        updateHexBox();
        ExtendedHitColorClient.syncOverlayTexture();
    }

    private void onAlphaSliderChanged(int val) {
        if (isUpdating) return;
        this.alpha = val;
        config.alpha = val;
        updateHexBox();
        ExtendedHitColorClient.syncOverlayTexture();
    }

    private void syncFromHsv() {
        int rgb = Mth.hsvToArgb(this.hue, this.saturation, this.value, 255);
        int r = ARGB.red(rgb);
        int g = ARGB.green(rgb);
        int b = ARGB.blue(rgb);

        config.red = r;
        config.green = g;
        config.blue = b;
        config.alpha = this.alpha;

        isUpdating = true;
        try {
            if (redSlider != null) redSlider.setIntValue(r);
            if (greenSlider != null) greenSlider.setIntValue(g);
            if (blueSlider != null) blueSlider.setIntValue(b);
            if (alphaSlider != null) alphaSlider.setIntValue(this.alpha);
            updateHexBox();
        } finally {
            isUpdating = false;
        }

        renderAlphaTexture();
        ExtendedHitColorClient.syncOverlayTexture();
    }

    private void updateHexBox() {
        if (hexBox != null && !hexBox.isFocused()) {
            hexBox.setValue(config.getHex());
        }
    }

    private void onHexTyped(String text) {
        if (isUpdating) return;
        ColorHelper.ParsedColor parsed = ColorHelper.parseHex(text);
        if (parsed == null) return;

        isUpdating = true;
        try {
            if (parsed.hasAlpha()) {
                this.alpha = parsed.a();
                config.alpha = parsed.a();
                if (alphaSlider != null) alphaSlider.setIntValue(parsed.a());
            }
            config.red = parsed.r();
            config.green = parsed.g();
            config.blue = parsed.b();

            float[] hsv = ColorHelper.rgbToHsv(parsed.r(), parsed.g(), parsed.b());
            this.hue = hsv[0];
            this.saturation = hsv[1];
            this.value = hsv[2];

            if (redSlider != null) redSlider.setIntValue(parsed.r());
            if (greenSlider != null) greenSlider.setIntValue(parsed.g());
            if (blueSlider != null) blueSlider.setIntValue(parsed.b());
        } finally {
            isUpdating = false;
        }

        renderSpectrumTexture();
        renderAlphaTexture();
        ExtendedHitColorClient.syncOverlayTexture();
    }

    private static boolean inBounds(double x, double y, int bx, int by, int bw, int bh) {
        return x >= bx && x <= bx + bw && y >= by && y <= by + bh;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDouble) {
        double mx = event.x();
        double my = event.y();

        if (inBounds(mx, my, spectrumX, spectrumY, SPECTRUM_WIDTH, SPECTRUM_HEIGHT)) {
            this.draggingSpectrum = true;
            updateSpectrumFromMouse(mx, my);
            return true;
        }

        if (inBounds(mx, my, hueBarX, hueBarY, SPECTRUM_WIDTH, BAR_HEIGHT)) {
            this.draggingHue = true;
            updateHueFromMouse(mx);
            return true;
        }

        if (inBounds(mx, my, alphaBarX, alphaBarY, SPECTRUM_WIDTH, BAR_HEIGHT)) {
            this.draggingAlpha = true;
            updateAlphaFromMouse(mx);
            return true;
        }

        int defX = previewX + PREVIEW_WIDTH + 8;
        if (inBounds(mx, my, defX, previewY, PREVIEW_WIDTH, PREVIEW_HEIGHT)) {
            applyColor(255, 0, 0, ColorHelper.VANILLA_ALPHA);
            return true;
        }

        for (int i = 0; i < PRESETS.length; i++) {
            int px = presetsStartX + i * (PRESET_SIZE + PRESET_SPACING);
            if (inBounds(mx, my, px, presetsStartY, PRESET_SIZE, PRESET_SIZE)) {
                int c = PRESETS[i];
                applyColor(ARGB.red(c), ARGB.green(c), ARGB.blue(c), this.alpha);
                return true;
            }
        }

        return super.mouseClicked(event, isDouble);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        double mx = event.x();
        double my = event.y();

        if (this.draggingSpectrum) {
            updateSpectrumFromMouse(mx, my);
            return true;
        }
        if (this.draggingHue) {
            updateHueFromMouse(mx);
            return true;
        }
        if (this.draggingAlpha) {
            updateAlphaFromMouse(mx);
            return true;
        }

        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.draggingSpectrum = false;
        this.draggingHue = false;
        this.draggingAlpha = false;
        return super.mouseReleased(event);
    }

    private void updateSpectrumFromMouse(double mx, double my) {
        this.saturation = Math.clamp((float) (mx - spectrumX) / (float) (SPECTRUM_WIDTH - 1), 0.0f, 1.0f);
        this.value = 1.0f - Math.clamp((float) (my - spectrumY) / (float) (SPECTRUM_HEIGHT - 1), 0.0f, 1.0f);
        syncFromHsv();
    }

    private void updateHueFromMouse(double mx) {
        this.hue = Math.clamp((float) (mx - hueBarX) / (float) (SPECTRUM_WIDTH - 1), 0.0f, 1.0f);
        renderSpectrumTexture();
        syncFromHsv();
    }

    private void updateAlphaFromMouse(double mx) {
        float relX = Math.clamp((float) (mx - alphaBarX) / (float) (SPECTRUM_WIDTH - 1), 0.0f, 1.0f);
        this.alpha = (int) Math.round(relX * 255.0);
        config.alpha = this.alpha;

        isUpdating = true;
        try {
            if (alphaSlider != null) alphaSlider.setIntValue(this.alpha);
            updateHexBox();
        } finally {
            isUpdating = false;
        }
        ExtendedHitColorClient.syncOverlayTexture();
    }

    private void applyColor(int r, int g, int b, int a) {
        this.alpha = a;
        config.red = r;
        config.green = g;
        config.blue = b;
        config.alpha = a;

        float[] hsv = ColorHelper.rgbToHsv(r, g, b);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];

        isUpdating = true;
        try {
            if (redSlider != null) redSlider.setIntValue(r);
            if (greenSlider != null) greenSlider.setIntValue(g);
            if (blueSlider != null) blueSlider.setIntValue(b);
            if (alphaSlider != null) alphaSlider.setIntValue(a);
            if (hexBox != null) hexBox.setValue(config.getHex());
        } finally {
            isUpdating = false;
        }

        renderSpectrumTexture();
        renderAlphaTexture();
        ExtendedHitColorClient.syncOverlayTexture();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0x90000000);

        graphics.fill(panelX - 1, panelY - 1, panelX + panelW + 1, panelY + panelH + 1, 0xFF3C3C3C);
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xEE161616);

        graphics.centeredText(this.font, "Extended HitColor", this.width / 2, panelY + 12, 0xFFFFFFFF);

        graphics.fill(spectrumX - 1, spectrumY - 1, spectrumX + SPECTRUM_WIDTH + 1, spectrumY + SPECTRUM_HEIGHT + 1, 0xFF2A2A2A);
        graphics.blit(SPECTRUM_TEX, spectrumX, spectrumY, spectrumX + SPECTRUM_WIDTH, spectrumY + SPECTRUM_HEIGHT, 0f, 1f, 0f, 1f);

        int markerX = spectrumX + Math.round(this.saturation * (float) (SPECTRUM_WIDTH - 1));
        int markerY = spectrumY + Math.round((1.0f - this.value) * (float) (SPECTRUM_HEIGHT - 1));
        graphics.outline(markerX - 3, markerY - 3, 7, 7, 0xFF000000);
        graphics.outline(markerX - 2, markerY - 2, 5, 5, 0xFFFFFFFF);

        graphics.fill(hueBarX - 1, hueBarY - 1, hueBarX + SPECTRUM_WIDTH + 1, hueBarY + BAR_HEIGHT + 1, 0xFF2A2A2A);
        graphics.blit(HUE_TEX, hueBarX, hueBarY, hueBarX + SPECTRUM_WIDTH, hueBarY + BAR_HEIGHT, 0f, 1f, 0f, 1f);

        int hueCursorX = hueBarX + Math.round(this.hue * (float) (SPECTRUM_WIDTH - 1));
        graphics.fill(hueCursorX - 1, hueBarY - 1, hueCursorX + 2, hueBarY + BAR_HEIGHT + 1, 0xFF000000);
        graphics.fill(hueCursorX, hueBarY, hueCursorX + 1, hueBarY + BAR_HEIGHT, 0xFFFFFFFF);

        graphics.fill(alphaBarX - 1, alphaBarY - 1, alphaBarX + SPECTRUM_WIDTH + 1, alphaBarY + BAR_HEIGHT + 1, 0xFF2A2A2A);
        graphics.blit(CHECKER_TEX, alphaBarX, alphaBarY, alphaBarX + SPECTRUM_WIDTH, alphaBarY + BAR_HEIGHT, 0f, 1f, 0f, (float) BAR_HEIGHT / (float) PREVIEW_HEIGHT);
        graphics.blit(ALPHA_TEX, alphaBarX, alphaBarY, alphaBarX + SPECTRUM_WIDTH, alphaBarY + BAR_HEIGHT, 0f, 1f, 0f, 1f);

        int alphaCursorX = alphaBarX + Math.round(((float) this.alpha / 255.0f) * (float) (SPECTRUM_WIDTH - 1));
        graphics.fill(alphaCursorX - 1, alphaBarY - 1, alphaCursorX + 2, alphaBarY + BAR_HEIGHT + 1, 0xFF000000);
        graphics.fill(alphaCursorX, alphaBarY, alphaCursorX + 1, alphaBarY + BAR_HEIGHT, 0xFFFFFFFF);

        renderPreviewSwatches(graphics, mouseX, mouseY);

        graphics.text(this.font, "Presets", presetsStartX, presetsStartY - 9, 0xFFAAAAAA);
        renderPresets(graphics, mouseX, mouseY);
        graphics.text(this.font, "Hex", hexBox.getX() - 28, hexBox.getY() + 4, 0xFFAAAAAA);

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private void renderPreviewSwatches(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        float swatchU = (float) PREVIEW_WIDTH / (float) SPECTRUM_WIDTH;

        graphics.fill(previewX - 1, previewY - 1, previewX + PREVIEW_WIDTH + 1, previewY + PREVIEW_HEIGHT + 1, 0xFF3C3C3C);
        graphics.blit(CHECKER_TEX, previewX, previewY, previewX + PREVIEW_WIDTH, previewY + PREVIEW_HEIGHT, 0f, swatchU, 0f, 1f);
        graphics.fill(previewX, previewY, previewX + PREVIEW_WIDTH, previewY + PREVIEW_HEIGHT, config.getColorAsArgb());
        graphics.centeredText(this.font, "Custom", previewX + PREVIEW_WIDTH / 2, previewY + 8, 0xFFFFFFFF);

        int defX = previewX + PREVIEW_WIDTH + 8;
        graphics.fill(defX - 1, previewY - 1, defX + PREVIEW_WIDTH + 1, previewY + PREVIEW_HEIGHT + 1, 0xFF3C3C3C);
        graphics.blit(CHECKER_TEX, defX, previewY, defX + PREVIEW_WIDTH, previewY + PREVIEW_HEIGHT, 0f, swatchU, 0f, 1f);
        graphics.fill(defX, previewY, defX + PREVIEW_WIDTH, previewY + PREVIEW_HEIGHT, ARGB.color(ColorHelper.VANILLA_ALPHA, 255, 0, 0));
        graphics.centeredText(this.font, "Default", defX + PREVIEW_WIDTH / 2, previewY + 8, 0xFFFFFFFF);

        if (inBounds(mouseX, mouseY, defX, previewY, PREVIEW_WIDTH, PREVIEW_HEIGHT)) {
            graphics.setTooltipForNextFrame(Component.literal("Click to restore Default Red"), mouseX, mouseY);
        }
    }

    private void renderPresets(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        for (int i = 0; i < PRESETS.length; i++) {
            int px = presetsStartX + i * (PRESET_SIZE + PRESET_SPACING);
            int py = presetsStartY;
            boolean hovered = inBounds(mouseX, mouseY, px, py, PRESET_SIZE, PRESET_SIZE);

            graphics.fill(px - 1, py - 1, px + PRESET_SIZE + 1, py + PRESET_SIZE + 1, hovered ? 0xFFFFFFFF : 0xFF2A2A2A);
            graphics.fill(px, py, px + PRESET_SIZE, py + PRESET_SIZE, PRESETS[i]);
        }
    }

    @Override
    public void removed() {
        config.save();
        releaseTextures();
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
