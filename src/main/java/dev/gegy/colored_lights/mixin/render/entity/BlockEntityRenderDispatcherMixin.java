package dev.gegy.colored_lights.mixin.render.entity;

import dev.gegy.colored_lights.render.ColoredLightEntityRenderContext;
import dev.gegy.colored_lights.render.ColoredLightReader;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
    @Inject(
            method = "render(Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
            at = @At("HEAD")
    )
    private static <T extends BlockEntity> void beforeRender(
            BlockEntityRenderer<T> renderer, T entity, float tickDelta,
            MatrixStack matrices, VertexConsumerProvider consumers,
            CallbackInfo ci
    ) {
        var pos = entity.getPos();
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        ColoredLightReader.INSTANCE.read(x, y, z, ColoredLightEntityRenderContext::set);
    }

    @Inject(
            method = "render(Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
            at = @At("RETURN")
    )
    private static <T extends BlockEntity> void afterRender(
            BlockEntityRenderer<T> renderer, T entity, float tickDelta,
            MatrixStack matrices, VertexConsumerProvider consumers,
            CallbackInfo ci
    ) {
        ColoredLightEntityRenderContext.end();
    }
}
