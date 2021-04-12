package dev.gegy.colored_lights.chunk;

import dev.gegy.colored_lights.ColoredLightPoint;

public interface ColoredLightChunkSection {
    ColoredLightPoint getColoredLightPoint(int x, int y, int z);

    int getColoredLightGeneration();
}
