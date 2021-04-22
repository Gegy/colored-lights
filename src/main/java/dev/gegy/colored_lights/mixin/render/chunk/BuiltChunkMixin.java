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

@Mixin(ChunkBuilder.BuiltChunk.class)
public class BuiltChunkMixin implements ColoredLightBuiltChunk {
    private int chunkLightGeneration = -1;
    private ColoredLightPoint[] chunkLightColors;
    private long packedChunkLightColors = 0;

    @Inject(method = "clear", at = @At("HEAD"))
    private void clear(CallbackInfo ci) {
        this.updateChunkLight(-1, null);
    }

    @Override
    public void updateChunkLight(int generation, ColoredLightPoint[] corners) {
        this.chunkLightGeneration = generation;
        this.chunkLightColors = corners;

        if (corners != null) {
            int high = ColoredLightPacking.packHigh(
                    corners[0].asPacked(),
                    corners[1].asPacked(),
                    corners[2].asPacked(),
                    corners[3].asPacked()
            );
            int low = ColoredLightPacking.packLow(
                    corners[4].asPacked(),
                    corners[5].asPacked(),
                    corners[6].asPacked(),
                    corners[7].asPacked()
            );

            this.packedChunkLightColors = (long) high << 32 | low;
        } else {
            this.packedChunkLightColors = ColoredLightPacking.DEFAULT;
        }
    }

    @Nullable
    @Override
    public ColoredLightPoint[] getChunkLightColors() {
        return this.chunkLightColors;
    }

    @Override
    public long getPackedChunkLightColors() {
        return this.packedChunkLightColors;
    }

    @Override
    public int getChunkLightGeneration() {
        return this.chunkLightGeneration;
    }
}
