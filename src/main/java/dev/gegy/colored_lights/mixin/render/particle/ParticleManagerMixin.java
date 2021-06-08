package dev.gegy.colored_lights.mixin.render.particle;

import dev.gegy.colored_lights.render.ColorConsumer;
import dev.gegy.colored_lights.render.ColoredLightReader;
import dev.gegy.colored_lights.render.particle.ColoredParticleVertexConsumer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {
    @Shadow
    protected ClientWorld world;

    private final ColoredParticleVertexConsumer coloredParticleVertexConsumer = new ColoredParticleVertexConsumer();
    private final ColorConsumer coloredLightSetter = this.coloredParticleVertexConsumer::setLightColor;

    @Inject(
            method = "renderParticles",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleTextureSheet;begin(Lnet/minecraft/client/render/BufferBuilder;Lnet/minecraft/client/texture/TextureManager;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void beforeRenderParticleSheet(
            MatrixStack matrices, VertexConsumerProvider.Immediate immediate,
            LightmapTextureManager lightmap, Camera camera, float tickDelta,
            CallbackInfo ci,
            MatrixStack modelView,
            Iterator<ParticleTextureSheet> sheetIterator, ParticleTextureSheet sheet, Iterable<Particle> particles,
            Tessellator tessellator, BufferBuilder bufferBuilder
    ) {
        float skyLight = this.world.method_23783(tickDelta);
        this.coloredParticleVertexConsumer.setup(bufferBuilder, skyLight);
    }

    @Redirect(
            method = "renderParticles",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;buildGeometry(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/Camera;F)V")
    )
    private void renderParticle(Particle particle, VertexConsumer consumer, Camera camera, float tickDelta) {
        var box = particle.getBoundingBox();
        ColoredLightReader.INSTANCE.read(box.minX, box.minY, box.minZ, this.coloredLightSetter);

        particle.buildGeometry(this.coloredParticleVertexConsumer, camera, tickDelta);
    }

    @Inject(method = "renderParticles", at = @At("RETURN"))
    private void afterRenderParticles(
            MatrixStack matrices, VertexConsumerProvider.Immediate immediate,
            LightmapTextureManager lightmap, Camera camera, float tickDelta,
            CallbackInfo ci
    ) {
        this.coloredParticleVertexConsumer.close();
    }
}
