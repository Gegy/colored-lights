package dev.gegy.colored_lights.chunk;

import dev.gegy.colored_lights.BlockLightColors;
import dev.gegy.colored_lights.ColoredLightValue;
import net.minecraft.block.BlockState;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.chunk.ChunkSection;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class ChunkColoredLightSampler {
    private static final int OCTANT_COUNT = 2;

    private static final ColoredLightValue[] EMPTY = Util.make(
            new ColoredLightValue[OCTANT_COUNT * OCTANT_COUNT * OCTANT_COUNT],
            points -> Arrays.fill(points, ColoredLightValue.NO)
    );

    private static final int SAMPLE_SIZE = 3;
    private static final int SAMPLE_COUNT = (16 + SAMPLE_SIZE - 1) / SAMPLE_SIZE;

    private static int index(int x, int y, int z, int size) {
        return (y * size + z) * size + x;
    }

    private static int sampleIndex(int x, int y, int z) {
        return index(x, y, z, SAMPLE_COUNT);
    }

    public static int octantIndex(int x, int y, int z) {
        return index(x, y, z, OCTANT_COUNT);
    }

    public static ColoredLightValue[] sampleCorners(ChunkSection section) {
        // To properly weight lights, we would need to sample across the neighbor chunks and compute color per block
        // before summing all together. This is too expensive for our compromise solution, so instead we just sample
        // within this chunk at a lower resolution.

        ColoredLightValue[] samples = takeSamples(section);
        if (samples == null) {
            return EMPTY;
        }

        ColoredLightValue[] result = new ColoredLightValue[OCTANT_COUNT * OCTANT_COUNT * OCTANT_COUNT];
        for (int i = 0; i < result.length; i++) {
            result[i] = new ColoredLightValue();
        }

        for (int sampleY = 0; sampleY < SAMPLE_COUNT; sampleY++) {
            int octantY = (sampleY * SAMPLE_SIZE) >> 3;

            for (int sampleZ = 0; sampleZ < SAMPLE_COUNT; sampleZ++) {
                int octantZ = (sampleZ * SAMPLE_SIZE) >> 3;

                for (int sampleX = 0; sampleX < SAMPLE_COUNT; sampleX++) {
                    int octantX = (sampleX * SAMPLE_SIZE) >> 3;

                    ColoredLightValue block = samples[sampleIndex(sampleX, sampleY, sampleZ)];
                    if (block == null) {
                        continue;
                    }

                    // a block can only have a maximum light level of 15, so we don't want to weight it more than that
                    if (block.weight > 15.0F) {
                        block.scale(15.0F / block.weight);
                    }

                    ColoredLightValue octant = result[octantIndex(octantX, octantY, octantZ)];
                    octant.add(block);
                }
            }
        }

        return result;
    }

    @Nullable
    private static ColoredLightValue[] takeSamples(ChunkSection section) {
        ColoredLightValue[] samples = null;

        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    BlockState state = section.getBlockState(x, y, z);
                    int luminance = state.getLuminance();
                    if (luminance != 0) {
                        if (samples == null) {
                            samples = new ColoredLightValue[SAMPLE_COUNT * SAMPLE_COUNT * SAMPLE_COUNT];
                        }

                        Vec3f color = BlockLightColors.forBlock(state);
                        addLightSourceSamples(samples, x, y, z, luminance, color.getX(), color.getY(), color.getZ());
                    }
                }
            }
        }

        return samples;
    }

    private static void addLightSourceSamples(
            ColoredLightValue[] samples, int lightBlockX, int lightBlockY, int lightBlockZ,
            int luminance, float red, float green, float blue
    ) {
        int lightX = lightBlockX / SAMPLE_SIZE;
        int lightY = lightBlockY / SAMPLE_SIZE;
        int lightZ = lightBlockZ / SAMPLE_SIZE;

        int blockRadius = luminance - 1;
        int radius = (blockRadius + SAMPLE_SIZE - 1) / SAMPLE_SIZE;

        int minY = Math.max(lightY - radius, 0);
        int maxY = Math.min(lightY + radius, SAMPLE_COUNT - 1);

        for (int y = minY; y <= maxY; y++) {
            int distanceY = Math.abs(y - lightY);

            int radiusZ = radius - distanceY;
            int minZ = Math.max(lightZ - radiusZ, 0);
            int maxZ = Math.min(lightZ + radiusZ, SAMPLE_COUNT - 1);

            for (int z = minZ; z <= maxZ; z++) {
                int distanceYZ = Math.abs(z - lightZ) + distanceY;

                int radiusX = radius - distanceYZ;
                int minX = Math.max(lightX - radiusX, 0);
                int maxX = Math.min(lightX + radiusX, SAMPLE_COUNT - 1);

                for (int x = minX; x <= maxX; x++) {
                    int distance = Math.abs(x - lightX) + distanceYZ;

                    int idx = sampleIndex(x, y, z);
                    ColoredLightValue sample = samples[idx];
                    if (sample == null) {
                        samples[idx] = sample = new ColoredLightValue();
                    }

                    int blockDistance = (distance - 1) * SAMPLE_SIZE + 1;
                    sample.add(red, green, blue, luminance - blockDistance);
                }
            }
        }
    }
}
