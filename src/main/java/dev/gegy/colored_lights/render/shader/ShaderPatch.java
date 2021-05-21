package dev.gegy.colored_lights.render.shader;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.gl.GlShader;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.Program;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public final class ShaderPatch {
    private final Collection<PatchedUniform> uniforms;
    private final ShaderSourcePatcher vertex;
    private final ShaderSourcePatcher fragment;

    ShaderPatch(Collection<PatchedUniform> uniforms, ShaderSourcePatcher vertex, ShaderSourcePatcher fragment) {
        this.uniforms = uniforms;
        this.vertex = vertex;
        this.fragment = fragment;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void addUniforms(GlShader shader, BiConsumer<PatchedUniform, GlUniform> consumer) {
        for (var uniform : this.uniforms) {
            consumer.accept(uniform, uniform.toGlUniform(shader));
        }
    }

    public String applyToSource(String source, Program.Type type) {
        var patcher = type == Program.Type.VERTEX ? this.vertex : this.fragment;
        return patcher.apply(source);
    }

    public static final class Builder {
        private final Map<String, PatchedUniform> uniforms = new Object2ObjectLinkedOpenHashMap<>();
        private SourceBuilder vertex;
        private SourceBuilder fragment;

        Builder() {
        }

        void addUniform(PatchedUniform uniform) {
            this.uniforms.put(uniform.name, uniform);
        }

        public SourceBuilder vertex() {
            if (this.vertex == null) {
                this.vertex = new SourceBuilder(this);
            }
            return this.vertex;
        }

        public SourceBuilder fragment() {
            if (this.fragment == null) {
                this.fragment = new SourceBuilder(this);
            }
            return this.fragment;
        }

        public ShaderPatch build() {
            var vertex = this.vertex != null ? this.vertex.build() : ShaderSourcePatcher.NO;
            var fragment = this.fragment != null ? this.fragment.build() : ShaderSourcePatcher.NO;
            return new ShaderPatch(this.uniforms.values(), vertex, fragment);
        }
    }

    public static final class SourceBuilder {
        private final Builder parent;
        private final List<ShaderSourcePatcher> patchers = new ArrayList<>();

        SourceBuilder(Builder parent) {
            this.parent = parent;
        }

        public SourceBuilder addUniform(PatchedUniform uniform) {
            this.parent.addUniform(uniform);
            return this.declare("uniform " + uniform.type.glslType + " " + uniform.name + ";");
        }

        public SourceBuilder declare(String... declarations) {
            return this.patch(ShaderSourcePatcher.insertDeclarations(declarations));
        }

        public SourceBuilder insertBefore(Predicate<String> match, String... insert) {
            return this.patch(ShaderSourcePatcher.insertBefore(match, insert));
        }

        public SourceBuilder insertAfter(Predicate<String> match, String... insert) {
            return this.patch(ShaderSourcePatcher.insertAfter(match, insert));
        }

        public SourceBuilder wrapCall(String targetFunction, String wrapperFunction, String... additionalArguments) {
            return this.patch(ShaderSourcePatcher.wrapCall(targetFunction, wrapperFunction, additionalArguments));
        }

        public SourceBuilder patch(ShaderSourcePatcher patcher) {
            this.patchers.add(patcher);
            return this;
        }

        public Builder end() {
            return this.parent;
        }

        ShaderSourcePatcher build() {
            return source -> {
                for (ShaderSourcePatcher patcher : this.patchers) {
                    source = patcher.apply(source);
                }
                return source;
            };
        }
    }
}
