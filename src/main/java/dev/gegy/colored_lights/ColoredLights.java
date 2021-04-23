package dev.gegy.colored_lights;

import dev.gegy.colored_lights.render.shader.PatchedUniform;
import dev.gegy.colored_lights.render.shader.ShaderPatch;
import dev.gegy.colored_lights.render.shader.ShaderPatchManager;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ColoredLights implements ModInitializer {
    public static final String ID = "colored_lights";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final PatchedUniform CHUNK_LIGHT_COLORS = PatchedUniform.ofInt2("ChunkLightColors", 0, 0);

    @Override
    public void onInitialize() {
        BlockLightColors.init();

        // @formatter:off
        ShaderPatch chunkPatch = ShaderPatch.builder()
                .vertex()
                    .addUniform(CHUNK_LIGHT_COLORS)
                    .declare("#moj_import <colored_chunk_light.glsl>")
                    .wrapCall(
                            "minecraft_sample_lightmap", "apply_color_to_light",
                            "ChunkLightColors", "UV2", "Position", "Sampler2"
                    )
                    .end()
                .build();
        // @formatter:on

        String[] applyChunkColorTo = new String[] {
                "rendertype_solid",
                "rendertype_cutout",
                "rendertype_cutout_mipped",
                "rendertype_translucent",
                "rendertype_tripwire"
        };

        for (String shader : applyChunkColorTo) {
            ShaderPatchManager.INSTANCE.add(shader, chunkPatch);
        }
    }
}
