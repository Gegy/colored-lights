package dev.gegy.colored_lights.mixin.render;

import com.mojang.blaze3d.platform.TextureUtil;
import dev.gegy.colored_lights.render.shader.ShaderPatchManager;
import net.minecraft.client.gl.GLImportProcessor;
import net.minecraft.client.gl.Program;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.InputStream;

@Mixin(Program.class)
public class ProgramMixin {
    @Redirect(method = "loadProgram", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/TextureUtil;readResourceAsString(Ljava/io/InputStream;)Ljava/lang/String;"))
    private static String readSource(
            InputStream input,
            Program.Type type, String name, InputStream stream, String domain, GLImportProcessor loader
    ) {
        var source = TextureUtil.readResourceAsString(input);
        source = ShaderPatchManager.applySourcePatches(source, type);
        return source;
    }
}
