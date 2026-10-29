package xkzuto.hitcolor.client.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class ColorSlider extends AbstractSliderButton {
    private final String prefix;
    private final int maxValue;
    private final boolean isPercentage;
    private final Consumer<Integer> onValueChange;

    public ColorSlider(int x, int y, int width, int height, String prefix, int initialValue, int maxValue, boolean isPercentage, Consumer<Integer> onValueChange) {
        super(x, y, width, height, Component.empty(), (double) initialValue / maxValue);
        this.prefix = prefix;
        this.maxValue = maxValue;
        this.isPercentage = isPercentage;
        this.onValueChange = onValueChange;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        int cur = getIntValue();
        if (isPercentage) {
            int pct = (int) Math.round(((double) cur / maxValue) * 100.0);
            setMessage(Component.literal(prefix + ": " + pct + "% (" + cur + ")"));
        } else {
            setMessage(Component.literal(prefix + ": " + cur));
        }
    }

    @Override
    protected void applyValue() {
        onValueChange.accept(getIntValue());
    }

    public int getIntValue() {
        return (int) Math.round(this.value * maxValue);
    }

    public void setIntValue(int val) {
        this.value = Math.clamp((double) val / maxValue, 0.0, 1.0);
        updateMessage();
    }
}
