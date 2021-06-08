package dev.gegy.colored_lights.mixin.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.colored_lights.ColoredLightCorner;
import dev.gegy.colored_lights.ColoredLights;
import dev.gegy.colored_lights.mixin.render.chunk.BuiltChunkStorageAccess;
import dev.gegy.colored_lights.render.ChunkLightColorUpdater;
import dev.gegy.colored_lights.render.ColorConsumer;
import dev.gegy.colored_lights.render.ColoredLightBuiltChunk;
import dev.gegy.colored_lights.render.ColoredLightEntityRenderContext;
import dev.gegy.colored_lights.render.ColoredLightReader;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BuiltChunkStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
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
public class WorldRendererMixin implements ColoredLightReader {
    @Shadow
    private ClientWorld world;
    @Shadow
    private BuiltChunkStorage chunks;

    private final ChunkLightColorUpdater chunkLightColorUpdater = new ChunkLightColorUpdater();

    private final BlockPos.Mutable readBlockPos = new BlockPos.Mutable();
    private GlUniform chunkLightColors;

    private long lastChunkLightColors;

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
        var shader = RenderSystem.getShader();
        this.chunkLightColors = ColoredLights.CHUNK_LIGHT_COLORS.get(shader);
        this.lastChunkLightColors = 0;
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
        var chunkLightColors = this.chunkLightColors;
        if (chunkLightColors == null) return;

        long colors = ((ColoredLightBuiltChunk) builtChunk).getPackedChunkLightColors();
        if (this.lastChunkLightColors != colors) {
            this.lastChunkLightColors = colors;

            int colorsHigh = (int) (colors >>> 32);
            int colorsLow = (int) colors;
            chunkLightColors.set(colorsHigh, colorsLow);
            chunkLightColors.upload();
        }
    }

    @Inject(method = "renderLayer", at = @At("RETURN"))
    private void finishRenderLayer(RenderLayer layer, MatrixStack transform, double cameraX, double cameraY, double cameraZ, Matrix4f projection, CallbackInfo ci) {
        this.lastChunkLightColors = 0;

        var chunkLightColors = this.chunkLightColors;
        if (chunkLightColors != null) {
            chunkLightColors.set(0, 0);
        }
    }

    @Inject(method = "render", at = @At("INVOKE"))
    private void beforeRender(MatrixStack transform, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmap, Matrix4f projection, CallbackInfo ci) {
        float skyBrightness = this.world.method_23783(tickDelta);
        ColoredLightEntityRenderContext.setGlobal(skyBrightness);
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
        this.read(entityX, entityY, entityZ, ColoredLightEntityRenderContext::set);
    }

    @Inject(method = "renderEntity", at = @At("RETURN"))
    private void afterRenderEntity(
            Entity entity, double cameraX, double cameraY, double cameraZ,
            float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci
    ) {
        ColoredLightEntityRenderContext.end();
    }

    @Override
    public void read(double x, double y, double z, ColorConsumer consumer) {
        var readBlockPos = this.readBlockPos.set(x, y, z);
        var chunk = ((BuiltChunkStorageAccess) this.chunks).getBuiltChunk(readBlockPos);
        if (chunk == null) {
            return;
        }

        var corners = ((ColoredLightBuiltChunk) chunk).getChunkLightColors();
        if (corners != null) {
            BlockPos origin = chunk.getOrigin();
            float localX = (float) (x - origin.getX()) / 16.0F;
            float localY = (float) (y - origin.getY()) / 16.0F;
            float localZ = (float) (z - origin.getZ()) / 16.0F;
            ColoredLightCorner.mix(corners, localX, localY, localZ, consumer);
        }
    }
}
