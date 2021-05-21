package dev.gegy.colored_lights.mixin.render.entity;

import dev.gegy.colored_lights.render.ColoredLightEntityRenderContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Redirect(
            method = "renderBakedItemQuads",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumer;quad(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/model/BakedQuad;FFFII)V")
    )
    private void renderQuad(VertexConsumer consumer, MatrixStack.Entry transform, BakedQuad quad, float red, float green, float blue, int light, int overlay) {
        var ctx = ColoredLightEntityRenderContext.get();
        if (ctx != null) {
            float factor = ctx.getLightColorFactor(light);
            red *= MathHelper.lerp(factor, 1.0F, ctx.red);
            green *= MathHelper.lerp(factor, 1.0F, ctx.green);
            blue *= MathHelper.lerp(factor, 1.0F, ctx.blue);
        }

        consumer.quad(transform, quad, red, green, blue, light, overlay);
    }
}
