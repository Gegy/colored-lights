package dev.gegy.colored_lights;

import net.minecraft.util.math.MathHelper;

public final class ColoredLightPacking {
    public static final int DEFAULT = 0;

    private static final float HIGH_SATURATION = 0.9F;
    private static final float MEDIUM_SATURATION = HIGH_SATURATION / 2.0F;

    private static final float THRESHOLD_MEDIUM = MEDIUM_SATURATION / 2.0F;
    private static final float THRESHOLD_HIGH = (HIGH_SATURATION + MEDIUM_SATURATION) / 2.0F;

    public static int pack(float hue, float saturation) {
        if (saturation <= THRESHOLD_MEDIUM) {
            return DEFAULT;
        }

        // odd = medium saturation; even = high saturation
        if (saturation > THRESHOLD_HIGH) {
            return MathHelper.floor(hue * 15.0F) * 2 + 1;
        } else {
            return MathHelper.floor(hue * 14.0F) * 2 + 2;
        }
    }

    // adapted from: <http://lolengine.net/blog/2013/07/27/rgb-to-hsv-in-glsl>
    public static int pack(float red, float green, float blue) {
        float px = Math.max(green, blue);
        float py = Math.min(blue, green);
        float pz = green < blue ? -1.0F : 0.0F;
        float pw = green < blue ? 2.0F / 3.0F : -1.0F / 3.0F;

        float qx = Math.max(red, px);
        float qz = red < px ? pw : pz;
        float qw = Math.min(red, px);

        float d = qx - Math.min(qw, py);
        float e = 1e-10F;

        float hue = Math.abs(qz + (qw - py) / (6.0F * d + e));
        float saturation = d / (qx + e);
        return pack(hue, saturation);
    }

    public static int packHigh(int x0y0z0, int x0y0z1, int x0y1z0, int x0y1z1) {
        return (x0y0z0 << 24) | (x0y0z1 << 16) | (x0y1z0 << 8) | x0y1z1;
    }

    public static int packLow(int x1y0z0, int x1y0z1, int x1y1z0, int x1y1z1) {
        return (x1y0z0 << 24) | (x1y0z1 << 16) | (x1y1z0 << 8) | x1y1z1;
    }
}
