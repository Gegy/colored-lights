package dev.gegy.colored_lights;

import net.minecraft.util.math.MathHelper;

public final class ColoredLightPoint {
    public static final ColoredLightPoint NO = new ColoredLightPoint();

    public float red;
    public float green;
    public float blue;
    public float weight;

    public ColoredLightPoint(float red, float green, float blue, float weight) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.weight = weight;
    }

    public ColoredLightPoint() {
        this(0.0F, 0.0F, 0.0F, 0.0F);
    }

    public static void mix(ColoredLightPoint[] corners, float x, float y, float z, ColorConsumer consumer) {
        consumer.accept(
                mix(corners, x, y, z, ColoredLightPoint::getRed),
                mix(corners, x, y, z, ColoredLightPoint::getGreen),
                mix(corners, x, y, z, ColoredLightPoint::getBlue)
        );
    }

    private static float mix(ColoredLightPoint[] corners, float x, float y, float z, GetComponent component) {
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

    public void add(ColoredLightPoint other) {
        this.red += other.red;
        this.green += other.green;
        this.blue += other.blue;
        this.weight += other.weight;
    }

    public void add(float red, float green, float blue, float weight) {
        this.red += red * weight;
        this.green += green * weight;
        this.blue += blue * weight;
        this.weight += weight;
    }

    public int asPacked() {
        if (this.weight <= 1e-3F) {
            return 0;
        }
        float weight = this.weight;
        return ColoredLightPacking.pack(this.red / weight, this.green / weight, this.blue / weight);
    }

    public void scale(float scale) {
        this.red *= scale;
        this.green *= scale;
        this.blue *= scale;
        this.weight *= scale;
    }

    public float getRed() {
        float weight = this.weight;
        return weight > 1e-3F ? this.red / weight : 1.0F;
    }

    public float getGreen() {
        float weight = this.weight;
        return weight > 1e-3F ? this.green / weight : 1.0F;
    }

    public float getBlue() {
        float weight = this.weight;
        return weight > 1e-3F ? this.blue / weight : 1.0F;
    }

    public boolean isDefault() {
        return this.weight <= 1e-3F
                || (this.getRed() >= 0.999F && this.getGreen() >= 0.999F && this.getBlue() >= 0.999F);
    }

    public interface ColorConsumer {
        void accept(float red, float green, float blue);
    }

    private interface GetComponent {
        float apply(ColoredLightPoint point);
    }
}
