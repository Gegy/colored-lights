package dev.gegy.colored_lights.mixin.render.chunk;

import net.minecraft.client.render.BuiltChunkStorage;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BuiltChunkStorage.class)
public interface BuiltChunkStorageAccess {
    @Invoker("getRenderedChunk")
    ChunkBuilder.BuiltChunk getBuiltChunk(BlockPos pos);
}
