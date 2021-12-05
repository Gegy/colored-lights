package dev.gegy.colored_lights.resource.shader;

import net.minecraft.client.gl.GlUniform;
import org.jetbrains.annotations.Nullable;

public interface PatchedShader {
    @Nullable
    GlUniform getPatchedUniform(PatchedUniform uniform);
}
