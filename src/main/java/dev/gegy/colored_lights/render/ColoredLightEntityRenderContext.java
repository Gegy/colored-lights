package dev.gegy.colored_lights.render;

import org.jetbrains.annotations.Nullable;

public final class ColoredLightEntityRenderContext {
    private static final ColoredLightEntityRenderContext INSTANCE = new ColoredLightEntityRenderContext();

    private boolean active;

    public float red;
    public float green;
    public float blue;

    @Nullable
    public static ColoredLightEntityRenderContext get() {
        ColoredLightEntityRenderContext context = INSTANCE;
        return context.active ? context : null;
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
