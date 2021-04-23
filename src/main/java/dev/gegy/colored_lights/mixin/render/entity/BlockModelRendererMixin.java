package dev.gegy.colored_lights.mixin.render.entity;

import dev.gegy.colored_lights.render.ColoredLightEntityRenderContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockModelRenderer.class)
public class BlockModelRendererMixin {
    @Redirect(
            method = "renderQuad(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/model/BakedQuad;FFFFIIIII)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumer;quad(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/model/BakedQuad;[FFFF[IIZ)V")
    )
    private void renderQuad(VertexConsumer consumer, MatrixStack.Entry transform, BakedQuad quad, float[] brightnesses, float red, float green, float blue, int[] lights, int overlay, boolean useQuadColorData) {
        ColoredLightEntityRenderContext ctx = ColoredLightEntityRenderContext.get();
        if (ctx != null) {
            // we're very lazy and don't respect smooth lighting
            float factor = ctx.getLightColorFactor(lights[0]);
            red *= MathHelper.lerp(factor, 1.0F, ctx.red);
            green *= MathHelper.lerp(factor, 1.0F, ctx.green);
            blue *= MathHelper.lerp(factor, 1.0F, ctx.blue);
        }

        consumer.quad(transform, quad, brightnesses, red, green, blue, lights, overlay, useQuadColorData);
    }
}
