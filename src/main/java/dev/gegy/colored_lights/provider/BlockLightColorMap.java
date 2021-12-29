package dev.gegy.colored_lights.provider;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

public final class BlockLightColorMap implements BlockLightColorProvider {
    private final Reference2ObjectOpenHashMap<BlockState, Vec3f> blockToColor = new Reference2ObjectOpenHashMap<>();

    public void clear() {
        this.blockToColor.clear();
    }

    public void set(BlockLightColorMap map) {
        this.clear();
        this.putAll(map);
    }

    public void put(BlockState state, Vec3f color) {
        this.blockToColor.put(state, color);
    }

    public void putAll(BlockLightColorMap colors) {
        this.blockToColor.putAll(colors.blockToColor);
    }

    @Override
    @Nullable
    public Vec3f get(BlockState state) {
        return this.blockToColor.get(state);
    }
}
