package dev.gegy.colored_lights;

public final class ColoredLightValue {
    public static final ColoredLightValue NO = new ColoredLightValue();

    public float red;
    public float green;
    public float blue;
    public float weight;

    public ColoredLightValue(float red, float green, float blue, float weight) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.weight = weight;
    }

    public ColoredLightValue() {
        this(0.0F, 0.0F, 0.0F, 0.0F);
    }

    public void add(ColoredLightValue other) {
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

    public ColoredLightCorner asCorner() {
        return ColoredLightCorner.byPacked(this.asPacked());
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
}
