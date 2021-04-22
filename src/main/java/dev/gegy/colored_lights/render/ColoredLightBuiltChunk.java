package dev.gegy.colored_lights.render;

import dev.gegy.colored_lights.ColoredLightPoint;
import dev.gegy.colored_lights.chunk.ColoredLightChunkSection;
import org.jetbrains.annotations.Nullable;

public interface ColoredLightBuiltChunk {
    void updateChunkLight(int generation, @Nullable ColoredLightPoint[] corners);

    long getPackedChunkLightColors();

    @Nullable
    ColoredLightPoint[] getChunkLightColors();

    int getChunkLightGeneration();

    default boolean isLightOutdated(ColoredLightChunkSection section) {
        return this.getChunkLightGeneration() != section.getColoredLightGeneration();
    }
}
