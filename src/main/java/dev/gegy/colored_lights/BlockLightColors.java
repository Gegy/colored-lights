package dev.gegy.colored_lights;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

public final class BlockLightColors {
    public static final Vec3f WHITE = new Vec3f(1.0F, 1.0F, 1.0F);

    private static final JsonParser JSON_PARSER = new JsonParser();

    private static BlockLightColors instance = new BlockLightColors();

    private final Map<Block, Vec3f> blockToColor = new Reference2ObjectOpenHashMap<>();

    public static void init() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleResourceReloadListener<BlockLightColors>() {
            @Override
            public CompletableFuture<BlockLightColors> load(ResourceManager manager, Profiler profiler, Executor executor) {
                return CompletableFuture.supplyAsync(() -> {
                    BlockLightColors colors = new BlockLightColors();

                    try {
                        for (Resource resource : manager.getAllResources(new Identifier(ColoredLights.ID, "light_colors.json"))) {
                            try (InputStream input = resource.getInputStream()) {
                                JsonObject root = JSON_PARSER.parse(new InputStreamReader(input)).getAsJsonObject();
                                if(root.get("replace") != null) {
                                    if (JsonHelper.getBoolean(root, "replace")) {
                                        colors = new BlockLightColors();
                                    }
                                }

                                JsonObject mappings = JsonHelper.getObject(root, "colors");
                                parseColorMappings(mappings, colors.blockToColor::put);
                            } catch (JsonSyntaxException e) {
                                ColoredLights.LOGGER.error("Failed to parse colored light mappings at {}", resource.getId(), e);
                            }
                        }
                    } catch (IOException e) {
                        ColoredLights.LOGGER.error("Failed to load colored light mappings", e);
                    }

                    return colors;
                }, executor);
            }

            @Override
            public CompletableFuture<Void> apply(BlockLightColors data, ResourceManager manager, Profiler profiler, Executor executor) {
                instance = data;
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public Identifier getFabricId() {
                return new Identifier(ColoredLights.ID, "light_colors");
            }
        });
    }

    private static void parseColorMappings(JsonObject mappings, BiConsumer<Block, Vec3f> consumer) throws JsonSyntaxException {
        for (Map.Entry<String, JsonElement> entry : mappings.entrySet()) {
            Identifier blockId = new Identifier(entry.getKey());
            Block block = Registry.BLOCK.getOrEmpty(blockId).orElse(null);
            if (block == null) {
                continue;
            }

            Vec3f color = parseColor(JsonHelper.asString(entry.getValue(), "color value"));
            consumer.accept(block, color);
        }
    }

    private static Vec3f parseColor(String string) {
        if (!string.startsWith("#")) {
            throw new JsonSyntaxException("Invalid color! Expected format #ffffff");
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

    public static Vec3f forBlock(BlockState state) {
        Vec3f color = instance.blockToColor.get(state.getBlock());
        return color != null ? color : WHITE;
    }
}
