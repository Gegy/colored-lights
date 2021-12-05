package dev.gegy.colored_lights.resource.shader;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.gegy.colored_lights.resource.ResourcePatchManager;
import net.minecraft.client.gl.GlShader;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.Program;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
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

        this.addResourcePatch(shader, patch, Program.Type.VERTEX);
        this.addResourcePatch(shader, patch, Program.Type.FRAGMENT);
    }

    private void addResourcePatch(String shader, ShaderPatch patch, Program.Type type) {
        var location = new Identifier("shaders/core/" + shader + type.getFileExtension());

        ResourcePatchManager.INSTANCE.add(location, bytes -> {
            String source = new String(bytes, StandardCharsets.UTF_8);
            source = patch.applyToSource(source, type);

            return source.getBytes(StandardCharsets.UTF_8);
        });
    }

    public static void startPatching(String shader) {
        INSTANCE.activePatches.set(INSTANCE.patches.get(shader));
    }

    public static void stopPatching() {
        INSTANCE.activePatches.remove();
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
