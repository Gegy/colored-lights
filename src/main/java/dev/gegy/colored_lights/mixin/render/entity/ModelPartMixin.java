package dev.gegy.colored_lights.mixin.render.entity;

import dev.gegy.colored_lights.render.ColoredLightEntityRenderContext;
import net.minecraft.client.model.ModelPart;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value = ModelPart.class, priority = 800)
public abstract class ModelPartMixin {

    @ModifyArgs(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;renderCuboids(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"))
    private void modArgs(Args args){
        ColoredLightEntityRenderContext ctx = ColoredLightEntityRenderContext.get();
        if (ctx != null){
            int light = args.<Integer>get(2);
            float red = args.<Float>get(4);
            float green = args.<Float>get(5);
            float blue = args.<Float>get(6);
            float factor = ctx.getLightColorFactor(light);
            args.set(4, red * MathHelper.lerp(factor, 1.0f, ctx.red));
            args.set(5, green * MathHelper.lerp(factor, 1.0f, ctx.green));
            args.set(6, blue * MathHelper.lerp(factor, 1.0f, ctx.blue));
        }
    }
}
