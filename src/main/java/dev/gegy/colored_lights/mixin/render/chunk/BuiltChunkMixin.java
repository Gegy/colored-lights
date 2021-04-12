package dev.gegy.colored_lights.mixin.render.chunk;

import dev.gegy.colored_lights.ColoredLightPacking;
import dev.gegy.colored_lights.ColoredLightPoint;
import dev.gegy.colored_lights.render.ColoredLightBuiltChunk;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(ChunkBuilder.BuiltChunk.class)
public class BuiltChunkMixin implements ColoredLightBuiltChunk {
    private int chunkLightGeneration = -1;
    private ColoredLightPoint[] chunkLightColors;
    private final int[] packedChunkLightColors = new int[2];

    @Inject(method = "clear", at = @At("HEAD"))
    private void clear(CallbackInfo ci) {
        this.updateChunkLight(-1, null);
    }

    @Override
    public void updateChunkLight(int generation, ColoredLightPoint[] corners) {
        this.chunkLightGeneration = generation;
        this.chunkLightColors = corners;

        if (corners != null) {
            int[] colors = this.packedChunkLightColors;

            colors[0] = ColoredLightPacking.packHigh(
                    corners[0].asPacked(),
                    corners[1].asPacked(),
                    corners[2].asPacked(),
                    corners[3].asPacked()
            );
            colors[1] = ColoredLightPacking.packLow(
                    corners[4].asPacked(),
                    corners[5].asPacked(),
                    corners[6].asPacked(),
                    corners[7].asPacked()
            );
        } else {
            Arrays.fill(this.packedChunkLightColors, ColoredLightPacking.DEFAULT);
        }
    }

    @Nullable
    @Override
    public ColoredLightPoint[] getChunkLightColors() {
        return this.chunkLightColors;
    }

    @Override
    public int[] getPackedChunkLightColors() {
        return this.packedChunkLightColors;
    }

    @Override
    public int getChunkLightGeneration() {
        return this.chunkLightGeneration;
    }
}
