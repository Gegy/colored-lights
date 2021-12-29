package dev.gegy.colored_lights.provider;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

public interface BlockLightColorProvider {
    @Nullable
    Vec3f get(BlockState state);
}
