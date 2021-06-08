package dev.gegy.colored_lights;

import dev.gegy.colored_lights.render.ColorConsumer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

public final class ColoredLightCorner {
    private static final ColoredLightCorner[] BY_PACKED = new ColoredLightCorner[ColoredLightPacking.COLOR_COUNT];

    static {
        for (int packed = 0; packed < ColoredLightPacking.COLOR_COUNT; packed++) {
            Vec3f color = ColoredLightPacking.unpack(packed);
            BY_PACKED[packed] = new ColoredLightCorner(color.getX(), color.getY(), color.getZ(), packed);
        }
    }

    public final float red;
    public final float green;
    public final float blue;
    public final int packed;

    private ColoredLightCorner(float red, float green, float blue, int packed) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.packed = packed;
    }

    public static ColoredLightCorner byPacked(int packed) {
        return BY_PACKED[packed];
    }

    public static void mix(ColoredLightCorner[] corners, float x, float y, float z, ColorConsumer consumer) {
        consumer.accept(
                mix(corners, x, y, z, c -> c.red),
                mix(corners, x, y, z, c -> c.green),
                mix(corners, x, y, z, c -> c.blue)
        );
    }

    private static float mix(ColoredLightCorner[] corners, float x, float y, float z, GetComponent component) {
        float x0y0z0 = component.apply(corners[0]);
        float x0y0z1 = component.apply(corners[1]);
        float x0y1z0 = component.apply(corners[2]);
        float x0y1z1 = component.apply(corners[3]);
        float x1y0z0 = component.apply(corners[4]);
        float x1y0z1 = component.apply(corners[5]);
        float x1y1z0 = component.apply(corners[6]);
        float x1y1z1 = component.apply(corners[7]);

        return MathHelper.lerp(
                x,
                MathHelper.lerp(
                        y,
                        MathHelper.lerp(z, x0y0z0, x0y0z1),
                        MathHelper.lerp(z, x0y1z0, x0y1z1)
                ),
                MathHelper.lerp(
                        y,
                        MathHelper.lerp(z, x1y0z0, x1y0z1),
                        MathHelper.lerp(z, x1y1z0, x1y1z1)
                )
        );
    }

    public boolean isDefault() {
        return this.packed == ColoredLightPacking.DEFAULT;
    }

    private interface GetComponent {
        float apply(ColoredLightCorner point);
    }
}
