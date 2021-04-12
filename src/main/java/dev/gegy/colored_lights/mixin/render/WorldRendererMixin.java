package dev.gegy.colored_lights.mixin.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.colored_lights.ColoredLightPoint;
import dev.gegy.colored_lights.ColoredLights;
import dev.gegy.colored_lights.mixin.render.chunk.BuiltChunkStorageAccess;
import dev.gegy.colored_lights.render.ChunkLightColorUpdater;
import dev.gegy.colored_lights.render.ColoredLightBuiltChunk;
import dev.gegy.colored_lights.render.ColoredLightEntityRenderContext;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BuiltChunkStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Set;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Shadow
    private ClientWorld world;
    @Shadow
    private BuiltChunkStorage chunks;

    private final ChunkLightColorUpdater chunkLightColorUpdater = new ChunkLightColorUpdater();

    private final BlockPos.Mutable entityBlockPos = new BlockPos.Mutable();
    private GlUniform chunkLightColors;

    @Inject(method = "scheduleChunkRender", at = @At("HEAD"))
    private void scheduleChunkRender(int x, int y, int z, boolean important, CallbackInfo ci) {
        this.chunkLightColorUpdater.rerenderChunk(this.world, this.chunks, x, y, z);
    }

    @Inject(
            method = "setupTerrain",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk;cancelRebuild()V"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void setupChunk(
            Camera camera, Frustum frustum, boolean hasForcedFrustum, int frame, boolean spectator, CallbackInfo ci,
            Vec3d cameraPos, BlockPos cameraBlockPos,
            Set<ChunkBuilder.BuiltChunk> chunksToRebuild, ObjectListIterator<WorldRenderer.ChunkInfo> chunkIterator,
            WorldRenderer.ChunkInfo chunkInfo, ChunkBuilder.BuiltChunk builtChunk
    ) {
        this.chunkLightColorUpdater.updateChunk(this.world, builtChunk);
    }

    @Inject(
            method = "updateChunks",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk;cancelRebuild()V"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void updateChunk(
            long limitTime, CallbackInfo ci,
            long startTime, int count, Iterator<ChunkBuilder.BuiltChunk> chunkIterator, ChunkBuilder.BuiltChunk chunk
    ) {
        this.chunkLightColorUpdater.updateChunk(this.world, chunk);
    }

    @Inject(method = "renderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;startDrawing()V", shift = At.Shift.AFTER))
    private void prepareRenderLayer(RenderLayer layer, MatrixStack transform, double cameraX, double cameraY, double cameraZ, Matrix4f projection, CallbackInfo ci) {
        Shader shader = RenderSystem.getShader();
        this.chunkLightColors = ColoredLights.CHUNK_LIGHT_COLORS.get(shader);
    }

    @Inject(
            method = "renderLayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/GlUniform;set(FFF)V"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void prepareRenderChunk(
            RenderLayer layer, MatrixStack transform, double cameraX, double cameraY, double cameraZ, Matrix4f projection, CallbackInfo ci,
            boolean opaque, ObjectListIterator<WorldRenderer.ChunkInfo> chunkIterator,
            VertexFormat vertexFormat, Shader shader, GlUniform chunkOffset, boolean renderedChunk,
            WorldRenderer.ChunkInfo chunk, ChunkBuilder.BuiltChunk builtChunk, VertexBuffer buffer, BlockPos origin
    ) {
        GlUniform chunkLightColors = this.chunkLightColors;
        if (chunkLightColors != null) {
            int[] colors = ((ColoredLightBuiltChunk) builtChunk).getPackedChunkLightColors();
            chunkLightColors.method_35650(colors[0], colors[1]);
            chunkLightColors.upload();
        }
    }

    @Inject(
            method = "renderEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;render(Lnet/minecraft/entity/Entity;DDDFFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void beforeRenderEntity(
            Entity entity, double cameraX, double cameraY, double cameraZ,
            float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci,
            double entityX, double entityY, double entityZ, float entityYaw
    ) {
        BlockPos.Mutable entityBlockPos = this.entityBlockPos.set(entityX, entityY, entityZ);
        ChunkBuilder.BuiltChunk chunk = ((BuiltChunkStorageAccess) this.chunks).getBuiltChunk(entityBlockPos);
        if (chunk == null) {
            return;
        }

        ColoredLightPoint[] corners = ((ColoredLightBuiltChunk) chunk).getChunkLightColors();
        if (corners != null) {
            ColoredLightPoint.mix(corners, entityX, entityY, entityZ, ColoredLightEntityRenderContext::set);
        }
    }

    @Inject(method = "renderEntity", at = @At("RETURN"))
    private void afterRenderEntity(
            Entity entity, double cameraX, double cameraY, double cameraZ,
            float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci
    ) {
        ColoredLightEntityRenderContext.end();
    }
}
