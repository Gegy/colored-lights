package dev.gegy.colored_lights.mixin.render.entity;

import dev.gegy.colored_lights.render.ColoredLightEntityRenderContext;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ModelPart.class)
public abstract class ModelPartMixin {
    @Shadow
    protected abstract void renderCuboids(MatrixStack.Entry entry, VertexConsumer writer, int light, int overlay, float red, float green, float blue, float alpha);

    @Redirect(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;renderCuboids(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V")
    )
    private void render(ModelPart part, MatrixStack.Entry entry, VertexConsumer writer, int light, int overlay, float red, float green, float blue, float alpha) {
        var ctx = ColoredLightEntityRenderContext.get();
        if (ctx != null) {
            float factor = ctx.getLightColorFactor(light);
            red *= MathHelper.lerp(factor, 1.0F, ctx.red);
            green *= MathHelper.lerp(factor, 1.0F, ctx.green);
            blue *= MathHelper.lerp(factor, 1.0F, ctx.blue);
        }

        this.renderCuboids(entry, writer, light, overlay, red, green, blue, alpha);
    }
}
