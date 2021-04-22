package dev.gegy.colored_lights.render;

import org.jetbrains.annotations.Nullable;

public final class ColoredLightEntityRenderContext {
    private static final ColoredLightEntityRenderContext INSTANCE = new ColoredLightEntityRenderContext();

    private boolean active;

    public float red;
    public float green;
    public float blue;
    public float skyBrightness = 1.0F;

    @Nullable
    public static ColoredLightEntityRenderContext get() {
        ColoredLightEntityRenderContext context = INSTANCE;
        return context.active ? context : null;
    }

    public float getLightColorFactor(int light) {
        float blockLight = (light & 0xFF) / 256.0F;
        float skyLight = (light >> 16 & 0xFF) / 256.0F;
        skyLight *= this.skyBrightness;

        return blockLight * (1.0F - skyLight);
    }

    public static void setGlobal(float skyBrightness) {
        INSTANCE.skyBrightness = skyBrightness;
    }

    public static void set(float red, float green, float blue) {
        ColoredLightEntityRenderContext context = INSTANCE;
        context.red = red;
        context.green = green;
        context.blue = blue;
        context.active = true;
    }

    public static void end() {
        INSTANCE.active = false;
    }
}
