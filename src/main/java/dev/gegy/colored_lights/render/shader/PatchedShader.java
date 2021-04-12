package dev.gegy.colored_lights.render.shader;

import net.minecraft.client.gl.GlUniform;
import org.jetbrains.annotations.Nullable;

public interface PatchedShader {
    @Nullable
    GlUniform getPatchedUniform(PatchedUniform uniform);
}
