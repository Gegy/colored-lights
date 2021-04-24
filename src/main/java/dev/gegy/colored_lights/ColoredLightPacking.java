package dev.gegy.colored_lights;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

public final class ColoredLightPacking {
    public static final int DEFAULT = 0;

    public static final int BITS = 8;
    public static final int COLOR_COUNT = 1 << BITS;

    private static final int SATURATION_LEVELS = 6;

    private static final int HUES_PER_SATURATION_LEVEL = (COLOR_COUNT - 1) / (SATURATION_LEVELS - 1);

    public static int pack(float hue, float saturation) {
        int saturationLevel = MathHelper.floor(saturation * SATURATION_LEVELS);
        saturationLevel = Math.min(saturationLevel, SATURATION_LEVELS - 1);

        if (saturationLevel <= 0) {
            return DEFAULT;
        }

        int hueIndex = Math.round(hue * HUES_PER_SATURATION_LEVEL) % HUES_PER_SATURATION_LEVEL;
        int saturationShift = (saturationLevel - 1) * HUES_PER_SATURATION_LEVEL;
        return hueIndex + saturationShift + 1;
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

        int saturationLevel = (color / HUES_PER_SATURATION_LEVEL) + 1;

        float hue = MathHelper.fractionalPart((float) color / HUES_PER_SATURATION_LEVEL);
        float saturation = (float) saturationLevel / (SATURATION_LEVELS - 1);

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
        return pack8(
                corners[0].packed,
                corners[1].packed,
                corners[2].packed,
                corners[3].packed,
                corners[4].packed,
                corners[5].packed,
                corners[6].packed,
                corners[7].packed
        );
    }

    private static long pack8(long a, long b, long c, long d, long e, long f, long g, long h) {
        return (a << 56) | (b << 48) | (c << 40) | (d << 32)
                | (e << 24) | (f << 16) | (g << 8) | h;
    }
}
