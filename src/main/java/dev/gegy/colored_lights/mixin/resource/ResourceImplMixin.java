package dev.gegy.colored_lights.mixin.resource;

import dev.gegy.colored_lights.resource.ResourcePatchManager;
import net.minecraft.resource.ResourceImpl;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;

@Mixin(ResourceImpl.class)
public class ResourceImplMixin {
    @Shadow @Final private Identifier id;
    @Shadow @Final @Mutable private InputStream inputStream;

    @Unique
    private boolean colored_lights$patchedResource;

    @Inject(method = "getInputStream", at = @At("HEAD"))
    private void getInputStream(CallbackInfoReturnable<InputStream> ci) {
        if (!this.colored_lights$patchedResource) {
            this.colored_lights$patchedResource = true;
            this.inputStream = ResourcePatchManager.INSTANCE.patch(this.id, this.inputStream);
        }
    }
}
