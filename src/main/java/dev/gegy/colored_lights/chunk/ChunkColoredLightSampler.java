package dev.gegy.colored_lights.chunk;

import dev.gegy.colored_lights.BlockLightColors;
import dev.gegy.colored_lights.ColoredLightPoint;
import net.minecraft.block.BlockState;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.chunk.ChunkSection;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ChunkColoredLightSampler {
    private static final int SIZE = 2;

    private static final ColoredLightPoint[] EMPTY = Util.make(
            new ColoredLightPoint[SIZE * SIZE * SIZE],
            points -> Arrays.fill(points, ColoredLightPoint.NO)
    );

    public static int index(int x, int y, int z) {
        return (y * SIZE + z) * SIZE + x;
    }

    public static ColoredLightPoint[] sample(ChunkSection section) {
        List<BlockPos> lights = collectLights(section);
        if (lights == null) {
            return EMPTY;
        }

        ColoredLightPoint[] result = new ColoredLightPoint[SIZE * SIZE * SIZE];

        for (int y = 0; y < SIZE; y++) {
            for (int z = 0; z < SIZE; z++) {
                for (int x = 0; x < SIZE; x++) {
                    result[index(x, y, z)] = sampleOctant(section, x, y, z, lights);
                }
            }
        }

        return result;
    }

    private static ColoredLightPoint sampleOctant(ChunkSection section, int octantX, int octantY, int octantZ, List<BlockPos> lights) {
        // To properly weight lights, we would need to sample across the neighbor chunks and compute color per block
        // before summing all together. This is too expensive for our compromise solution, so instead we just sample
        // within this octant at a lower resolution.

        int minX = octantX << 3;
        int minY = octantY << 3;
        int minZ = octantZ << 3;
        int maxX = minX + 7;
        int maxY = minY + 7;
        int maxZ = minZ + 7;

        ColoredLightPoint result = new ColoredLightPoint();
        ColoredLightPoint block = new ColoredLightPoint();

        int step = 3;

        for (int y = minY; y <= maxY; y += step) {
            for (int z = minZ; z <= maxZ; z += step) {
                for (int x = minX; x <= maxX; x += step) {
                    block.reset();

                    for (BlockPos lightPos : lights) {
                        int lightX = lightPos.getX();
                        int lightY = lightPos.getY();
                        int lightZ = lightPos.getZ();

                        BlockState state = section.getBlockState(lightX, lightY, lightZ);

                        int distance = Math.abs(lightX - x) + Math.abs(lightY - y) + Math.abs(lightZ - z);
                        int receivedLight = state.getLuminance() - distance;
                        if (receivedLight > 0) {
                            Vec3f color = BlockLightColors.forBlock(state);
                            block.add(color.getX(), color.getY(), color.getZ(), receivedLight);
                        }
                    }

                    // a block can only have a maximum light level of 15, so we don't want to weight it more than that
                    if (block.weight > 15.0F) {
                        block.scale(15.0F / block.weight);
                    }

                    result.add(block);
                }
            }
        }

        return result;
    }

    @Nullable
    private static List<BlockPos> collectLights(ChunkSection section) {
        List<BlockPos> lights = null;

        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    BlockState state = section.getBlockState(x, y, z);
                    if (state.getLuminance() != 0) {
                        if (lights == null) {
                            lights = new ArrayList<>(4);
                        }
                        lights.add(new BlockPos(x, y, z));
                    }
                }
            }
        }

        return lights;
    }
}
