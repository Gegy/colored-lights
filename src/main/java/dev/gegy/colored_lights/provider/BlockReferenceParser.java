package dev.gegy.colored_lights.provider;

import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;

public final class BlockReferenceParser {
    public static void parse(String reference, Consumer<BlockState> consumer) {
        if (reference.indexOf('[') != -1) {
            parseBlockState(reference, consumer);
        } else {
            parseBlock(reference, consumer);
        }
    }

    private static void parseBlockState(String reference, Consumer<BlockState> consumer) {
        int propertiesIndex = reference.indexOf('[');
        if (propertiesIndex == -1 || !reference.endsWith("]")) {
            throw new JsonSyntaxException("Malformed block state reference: " + reference);
        }

        var blockReference = reference.substring(0, propertiesIndex);
        var propertiesReference = reference.substring(propertiesIndex + 1, reference.length() - 1);

        var block = parseBlock(blockReference);
        if (block != null) {
            var state = parseBlockStateProperties(blockReference, propertiesReference, block);
            consumer.accept(state);
        }
    }

    private static BlockState parseBlockStateProperties(String blockReference, String propertiesReference, Block block) {
        var state = block.getDefaultState();
        var states = block.getStateManager();

        var properties = parseProperties(propertiesReference);
        for (var entry : properties.entrySet()) {
            var property = states.getProperty(entry.getKey());
            if (property != null) {
                state = parseBlockStateProperty(state, property, entry.getValue());
            } else {
                throw new JsonSyntaxException("Missing block state property " + entry.getKey() + " on " + blockReference);
            }
        }

        return state;
    }

    private static <T extends Comparable<T>> BlockState parseBlockStateProperty(BlockState state, Property<T> property, String valueReference) {
        var value = property.parse(valueReference)
                .orElseThrow(() -> new JsonSyntaxException("Invalid property value " + valueReference + " for " + property + " on " + state.getBlock()));

        return state.with(property, value);
    }

    private static Map<String, String> parseProperties(String reference) {
        reference = reference.replaceAll(" ", "");

        var properties = new Object2ObjectArrayMap<String, String>();
        for (var declaration : reference.split(",")) {
            int equalsIdx = declaration.indexOf('=');
            if (equalsIdx == -1) {
                throw new JsonSyntaxException("Malformed block state property reference: " + declaration);
            }

            var key = declaration.substring(0, equalsIdx);
            var value = declaration.substring(equalsIdx + 1);
            properties.put(key, value);
        }

        return properties;
    }

    private static void parseBlock(String reference, Consumer<BlockState> consumer) {
        var block = parseBlock(reference);
        if (block != null) {
            block.getStateManager().getStates().forEach(consumer);
        }
    }

    @Nullable
    private static Block parseBlock(String reference) {
        if (Identifier.isValid(reference)) {
            var blockId = new Identifier(reference);
            return Registry.BLOCK.getOrEmpty(blockId).orElse(null);
        } else {
            throw new JsonSyntaxException("Malformed block identifier: " + reference);
        }
    }
}
