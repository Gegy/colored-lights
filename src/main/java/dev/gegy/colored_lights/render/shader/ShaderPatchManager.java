package dev.gegy.colored_lights.render.shader;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.gl.GlShader;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.Program;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.BiConsumer;

public final class ShaderPatchManager {
    public static final ShaderPatchManager INSTANCE = new ShaderPatchManager();

    private final Multimap<String, ShaderPatch> patches = HashMultimap.create();

    private final ThreadLocal<Collection<ShaderPatch>> activePatches = new ThreadLocal<>();

    private ShaderPatchManager() {
    }

    public void add(String shader, ShaderPatch patch) {
        this.patches.put(shader, patch);
    }

    public static void startPatching(String shader) {
        INSTANCE.activePatches.set(INSTANCE.patches.get(shader));
    }

    public static void stopPatching() {
        INSTANCE.activePatches.remove();
    }

    public static String applySourcePatches(String source, Program.Type type) {
        var activePatches = getActivePatches();
        if (activePatches != null && !activePatches.isEmpty()) {
            for (ShaderPatch patch : activePatches) {
                source = patch.applyToSource(source, type);
            }
        }

        return source;
    }

    public static void applyUniformPatches(GlShader shader, BiConsumer<PatchedUniform, GlUniform> consumer) {
        var activePatches = getActivePatches();
        if (activePatches != null) {
            for (ShaderPatch patch : activePatches) {
                patch.addUniforms(shader, consumer);
            }
        }
    }

    @Nullable
    private static Collection<ShaderPatch> getActivePatches() {
        return INSTANCE.activePatches.get();
    }
}
