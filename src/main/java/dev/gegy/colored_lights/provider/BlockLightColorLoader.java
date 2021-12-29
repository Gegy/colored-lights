package dev.gegy.colored_lights.provider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import dev.gegy.colored_lights.ColoredLights;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.block.BlockState;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.profiler.Profiler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class BlockLightColorLoader implements SimpleResourceReloadListener<BlockLightColorMap> {
    private final Consumer<BlockLightColorMap> colorConsumer;

    public BlockLightColorLoader(Consumer<BlockLightColorMap> colorConsumer) {
        this.colorConsumer = colorConsumer;
    }

    @Override
    public CompletableFuture<BlockLightColorMap> load(ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return loadColors(manager);
            } catch (IOException e) {
                ColoredLights.LOGGER.error("Failed to load colored light mappings", e);
                return new BlockLightColorMap();
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> apply(BlockLightColorMap colors, ResourceManager manager, Profiler profiler, Executor executor) {
        this.colorConsumer.accept(colors);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(ColoredLights.ID, "light_colors");
    }

    private static BlockLightColorMap loadColors(ResourceManager manager) throws IOException {
        var baseColors = new BlockLightColorMap();
        var overrideColors = new BlockLightColorMap();

        for (var resource : manager.getAllResources(new Identifier(ColoredLights.ID, "light_colors.json"))) {
            try (var input = resource.getInputStream()) {
                var root = JsonParser.parseReader(new InputStreamReader(input)).getAsJsonObject();

                boolean replace = JsonHelper.getBoolean(root, "replace", false);
                var mappings = JsonHelper.getObject(root, "colors");

                if (replace) {
                    baseColors = new BlockLightColorMap();
                    parseColorMappings(mappings, baseColors::put);
                } else {
                    parseColorMappings(mappings, overrideColors::put);
                }
            } catch (JsonSyntaxException e) {
                ColoredLights.LOGGER.error("Failed to parse colored light mappings at {}", resource.getId(), e);
            }
        }

        baseColors.putAll(overrideColors);

        return baseColors;
    }

    private static void parseColorMappings(JsonObject mappings, BiConsumer<BlockState, Vec3f> consumer) throws JsonSyntaxException {
        for (var entry : mappings.entrySet()) {
            var color = parseColor(JsonHelper.asString(entry.getValue(), "color value"));
            BlockReferenceParser.parse(entry.getKey(), state -> consumer.accept(state, color));
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
