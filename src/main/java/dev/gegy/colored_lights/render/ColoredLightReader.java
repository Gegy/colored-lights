package dev.gegy.colored_lights.render;

import net.minecraft.client.MinecraftClient;

public interface ColoredLightReader {
    ColoredLightReader INSTANCE = (ColoredLightReader) MinecraftClient.getInstance().worldRenderer;

    void read(double x, double y, double z, ColorConsumer consumer);
}
