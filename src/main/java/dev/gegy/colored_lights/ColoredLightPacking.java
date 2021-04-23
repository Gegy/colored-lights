package dev.gegy.colored_lights;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

public final class ColoredLightPacking {
    public static final int DEFAULT = 0;

    public static final int BITS = 8;
    public static final int VALUE_COUNT = 1 << BITS;

    private static final float HIGH_SATURATION = 0.8F;
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

    public static Vec3f unpack(int packed) {
        // we use a value of 0 to represent saturation=0, given hue is irrelevant here.
        if (packed == 0) {
            return BlockLightColors.WHITE;
        }

        int color = packed - 1;

        // we interleave high and medium saturation colors, giving us 15 values for high saturation and 14 for medium.
        // this is acceptable because the difference between colors of lower saturation is harder to distinguish.

        float hue;
        float saturation;
        if ((color & 1) == 0) {
            hue = (float) (color / 2) / 15.0F;
            saturation = 0.8F;
        } else {
            hue = (float) ((color - 1) / 2) / 14.0F;
            saturation = 0.4F;
        }

        float px = Math.abs(MathHelper.fractionalPart(hue + 1.0F) * 6.0F - 3.0F);
        float py = Math.abs(MathHelper.fractionalPart(hue + 2.0F / 3.0F) * 6.0F - 3.0F);
        float pz = Math.abs(MathHelper.fractionalPart(hue + 1.0F / 3.0F) * 6.0F - 3.0F);

        return new Vec3f(
                MathHelper.lerp(saturation, 1.0F, MathHelper.clamp(px - 1.0F, 0.0F, 1.0F)),
                MathHelper.lerp(saturation, 1.0F, MathHelper.clamp(py - 1.0F, 0.0F, 1.0F)),
                MathHelper.lerp(saturation, 1.0F, MathHelper.clamp(pz - 1.0F, 0.0F, 1.0F))
        );
    }

    public static long pack(ColoredLightCorner[] corners) {
        int high = ColoredLightPacking.pack4(
                corners[0].packed,
                corners[1].packed,
                corners[2].packed,
                corners[3].packed
        );
        int low = ColoredLightPacking.pack4(
                corners[4].packed,
                corners[5].packed,
                corners[6].packed,
                corners[7].packed
        );
        return (long) high << 32 | low;
    }

    private static int pack4(int a, int b, int c, int d) {
        return (a << 24) | (b << 16) | (c << 8) | d;
    }
}
