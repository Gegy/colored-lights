package dev.gegy.colored_lights.render.shader;

import net.minecraft.client.gl.GlShader;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.Shader;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public final class PatchedUniform {
    final String name;
    final Type type;
    final Consumer<GlUniform> reset;

    PatchedUniform(String name, Type type, Consumer<GlUniform> reset) {
        this.name = name;
        this.type = type;
        this.reset = reset;
    }

    public static PatchedUniform ofInt(String name, int value) {
        return new PatchedUniform(name, Type.INT, u -> u.set(value));
    }

    public static PatchedUniform ofFloat(String name, float value) {
        return new PatchedUniform(name, Type.FLOAT, u -> u.set(value));
    }

    public static PatchedUniform ofInt2(String name, int x, int y) {
        return new PatchedUniform(name, Type.INT2, u -> u.set(x, y));
    }

    public String getName() {
        return this.name;
    }

    @Nullable
    public GlUniform get(Shader shader) {
        if (shader instanceof PatchedShader patchedShader) {
            return patchedShader.getPatchedUniform(this);
        }
        return null;
    }

    public GlUniform toGlUniform(GlShader shader) {
        var uniform = new GlUniform(this.name, this.type.glType, this.type.count, shader);
        this.reset.accept(uniform);
        return uniform;
    }

    public enum Type {
        INT(GlUniform.field_32038, "int", 1),
        FLOAT(GlUniform.field_32042, "float", 1),
        INT2(GlUniform.field_32039, "ivec2", 2);

        public final int glType;
        public final String glslType;
        public final int count;

        Type(int glType, String glslType, int count) {
            this.glType = glType;
            this.glslType = glslType;
            this.count = count;
        }
    }
}
