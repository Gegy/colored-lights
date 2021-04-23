package dev.gegy.colored_lights.chunk;

import dev.gegy.colored_lights.ColoredLightValue;

public interface ColoredLightChunkSection {
    ColoredLightValue getColoredLightPoint(int x, int y, int z);

    int getColoredLightGeneration();
}
