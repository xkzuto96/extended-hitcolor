package xkzuto.hitcolor.client.util;

public final class ColorHelper {
    public static final int VANILLA_HIT_COLOR = 0xB2FF0000;
    public static final int VANILLA_ALPHA = 77;

    private ColorHelper() {}

    public record ParsedColor(int r, int g, int b, int a, boolean hasAlpha) {}

    public static float[] rgbToHsv(int r, int g, int b) {
        float rf = r / 255.0f, gf = g / 255.0f, bf = b / 255.0f;
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;

        float h = 0f;
        if (delta > 0.0001f) {
            if (max == rf) h = ((gf - bf) / delta) % 6f;
            else if (max == gf) h = ((bf - rf) / delta) + 2f;
            else h = ((rf - gf) / delta) + 4f;
            h /= 6f;
            if (h < 0f) h += 1f;
        }

        float s = max == 0f ? 0f : delta / max;
        return new float[]{ h, s, max };
    }

    public static String toHex(int r, int g, int b) {
        return String.format("#%02X%02X%02X", r, g, b);
    }

    public static String toHexWithAlpha(int a, int r, int g, int b) {
        return String.format("#%02X%02X%02X%02X", a, r, g, b);
    }

    public static ParsedColor parseHex(String hex) {
        if (hex == null) return null;
        String s = hex.trim();
        if (s.startsWith("#")) s = s.substring(1);

        try {
            if (s.length() == 6) {
                int val = (int) Long.parseLong(s, 16);
                return new ParsedColor((val >> 16) & 0xFF, (val >> 8) & 0xFF, val & 0xFF, -1, false);
            }
            if (s.length() == 8) {
                long val = Long.parseLong(s, 16);
                return new ParsedColor((int) ((val >> 16) & 0xFF), (int) ((val >> 8) & 0xFF), (int) (val & 0xFF), (int) ((val >> 24) & 0xFF), true);
            }
        } catch (NumberFormatException ignored) {}
        return null;
    }
}
