package dev.gegy.colored_lights.chunk;

import dev.gegy.colored_lights.ColoredLightValue;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.WorldView;

public interface ColoredLightChunkSection {
    ColoredLightValue getColoredLightPoint(WorldView world, ChunkSectionPos sectionPos, int x, int y, int z);

    int getColoredLightGeneration();
}
