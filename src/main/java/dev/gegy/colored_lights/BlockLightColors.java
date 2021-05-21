package dev.gegy.colored_lights;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Vec3f;

import java.util.Map;

public final class BlockLightColors {
    public static final Vec3f WHITE = new Vec3f(1.0F, 1.0F, 1.0F);

    static BlockLightColors instance = new BlockLightColors();

    private final Map<Block, Vec3f> blockToColor = new Reference2ObjectOpenHashMap<>();

    public static Vec3f forBlock(BlockState state) {
        var color = instance.blockToColor.get(state.getBlock());
        return color != null ? color : WHITE;
    }

    void putColor(Block block, Vec3f color) {
        this.blockToColor.put(block, color);
    }

    void putAllColors(BlockLightColors colors) {
        this.blockToColor.putAll(colors.blockToColor);
    }
}
