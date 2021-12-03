package dev.gegy.colored_lights;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.block.Block;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

public final class BlockLightColorLoader implements SimpleResourceReloadListener<BlockLightColors> {
    public static final BlockLightColorLoader INSTANCE = new BlockLightColorLoader();

    private BlockLightColorLoader() {
    }

    @Override
    public CompletableFuture<BlockLightColors> load(ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return loadColors(manager);
            } catch (IOException e) {
                ColoredLights.LOGGER.error("Failed to load colored light mappings", e);
                return new BlockLightColors();
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> apply(BlockLightColors colors, ResourceManager manager, Profiler profiler, Executor executor) {
        BlockLightColors.instance = colors;
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(ColoredLights.ID, "light_colors");
    }

    private static BlockLightColors loadColors(ResourceManager manager) throws IOException {
        var baseColors = new BlockLightColors();
        var overrideColors = new BlockLightColors();

        for (var resource : manager.getAllResources(new Identifier(ColoredLights.ID, "light_colors.json"))) {
            try (var input = resource.getInputStream()) {
                var root = JsonParser.parseReader(new InputStreamReader(input)).getAsJsonObject();

                boolean replace = JsonHelper.getBoolean(root, "replace", false);
                var mappings = JsonHelper.getObject(root, "colors");

                if (replace) {
                    baseColors = new BlockLightColors();
                    parseColorMappings(mappings, baseColors::putColor);
                } else {
                    parseColorMappings(mappings, overrideColors::putColor);
                }
            } catch (JsonSyntaxException e) {
                ColoredLights.LOGGER.error("Failed to parse colored light mappings at {}", resource.getId(), e);
            }
        }

        baseColors.putAllColors(overrideColors);

        return baseColors;
    }

    private static void parseColorMappings(JsonObject mappings, BiConsumer<Block, Vec3f> consumer) throws JsonSyntaxException {
        for (var entry : mappings.entrySet()) {
            var blockId = new Identifier(entry.getKey());
            var block = Registry.BLOCK.getOrEmpty(blockId).orElse(null);
            if (block == null) {
                continue;
            }

            var color = parseColor(JsonHelper.asString(entry.getValue(), "color value"));
            consumer.accept(block, color);
        }
    }

    private static Vec3f parseColor(String string) {
        if (!string.startsWith("#")) {
            throw new JsonSyntaxException("Invalid color! Expected hex string in format #ffffff");
        }

        try {
            int color = Integer.parseInt(string.substring(1), 16);
            int red = (color >> 16) & 0xFF;
            int green = (color >> 8) & 0xFF;
            int blue = color & 0xFF;
            return new Vec3f(red / 255.0F, green / 255.0F, blue / 255.0F);
        } catch (NumberFormatException e) {
            throw new JsonSyntaxException("Malformed hex string", e);
        }
    }
}
