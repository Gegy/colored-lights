package dev.gegy.colored_lights.chunk;

import dev.gegy.colored_lights.BlockLightColors;
import dev.gegy.colored_lights.ColoredLightPoint;
import net.minecraft.block.BlockState;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.chunk.ChunkSection;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class ChunkColoredLightSampler {
    private static final int OCTANT_SIZE = 2;

    private static final ColoredLightPoint[] EMPTY = Util.make(
            new ColoredLightPoint[OCTANT_SIZE * OCTANT_SIZE * OCTANT_SIZE],
            points -> Arrays.fill(points, ColoredLightPoint.NO)
    );

    private static final int SAMPLE_STEP = 3;
    private static final int SAMPLE_SIZE = (16 + SAMPLE_STEP - 1) / SAMPLE_STEP;

    private static int index(int x, int y, int z, int size) {
        return (y * size + z) * size + x;
    }

    private static int sampleIndex(int x, int y, int z) {
        return index(x, y, z, SAMPLE_SIZE);
    }

    public static int octantIndex(int x, int y, int z) {
        return index(x, y, z, OCTANT_SIZE);
    }

    public static ColoredLightPoint[] sampleCorners(ChunkSection section) {
        // To properly weight lights, we would need to sample across the neighbor chunks and compute color per block
        // before summing all together. This is too expensive for our compromise solution, so instead we just sample
        // within this chunk at a lower resolution.

        ColoredLightPoint[] samples = takeSamples(section);
        if (samples == null) {
            return EMPTY;
        }

        ColoredLightPoint[] result = new ColoredLightPoint[OCTANT_SIZE * OCTANT_SIZE * OCTANT_SIZE];
        for (int i = 0; i < result.length; i++) {
            result[i] = new ColoredLightPoint();
        }

        for (int sampleY = 0; sampleY < SAMPLE_SIZE; sampleY++) {
            int octantY = (sampleY * SAMPLE_STEP) >> 3;

            for (int sampleZ = 0; sampleZ < SAMPLE_SIZE; sampleZ++) {
                int octantZ = (sampleZ * SAMPLE_STEP) >> 3;

                for (int sampleX = 0; sampleX < SAMPLE_SIZE; sampleX++) {
                    int octantX = (sampleX * SAMPLE_STEP) >> 3;

                    ColoredLightPoint block = samples[sampleIndex(sampleX, sampleY, sampleZ)];
                    if (block == null) {
                        continue;
                    }

                    // a block can only have a maximum light level of 15, so we don't want to weight it more than that
                    if (block.weight > 15.0F) {
                        block.scale(15.0F / block.weight);
                    }

                    ColoredLightPoint octant = result[octantIndex(octantX, octantY, octantZ)];
                    octant.add(block);
                }
            }
        }

        return result;
    }

    @Nullable
    private static ColoredLightPoint[] takeSamples(ChunkSection section) {
        ColoredLightPoint[] samples = null;

        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    BlockState state = section.getBlockState(x, y, z);
                    int luminance = state.getLuminance();
                    if (luminance != 0) {
                        if (samples == null) {
                            samples = new ColoredLightPoint[SAMPLE_SIZE * SAMPLE_SIZE * SAMPLE_SIZE];
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
            ColoredLightPoint[] samples, int lightBlockX, int lightBlockY, int lightBlockZ,
            int luminance, float red, float green, float blue
    ) {
        int lightX = lightBlockX / SAMPLE_STEP;
        int lightY = lightBlockY / SAMPLE_STEP;
        int lightZ = lightBlockZ / SAMPLE_STEP;

        int blockRadius = luminance - 1;
        int radius = (blockRadius + SAMPLE_STEP - 1) / SAMPLE_STEP;

        int minY = Math.max(lightY - radius, 0);
        int maxY = Math.min(lightY + radius, SAMPLE_SIZE - 1);

        for (int y = minY; y <= maxY; y++) {
            int distanceY = Math.abs(y - lightY);

            int radiusZ = radius - distanceY;
            int minZ = Math.max(lightZ - radiusZ, 0);
            int maxZ = Math.min(lightZ + radiusZ, SAMPLE_SIZE - 1);

            for (int z = minZ; z <= maxZ; z++) {
                int distanceYZ = Math.abs(z - lightZ) + distanceY;

                int radiusX = radius - distanceYZ;
                int minX = Math.max(lightX - radiusX, 0);
                int maxX = Math.min(lightX + radiusX, SAMPLE_SIZE - 1);

                for (int x = minX; x <= maxX; x++) {
                    int distance = Math.abs(x - lightX) + distanceYZ;

                    int idx = sampleIndex(x, y, z);
                    ColoredLightPoint sample = samples[idx];
                    if (sample == null) {
                        samples[idx] = sample = new ColoredLightPoint();
                    }

                    int blockDistance = (distance - 1) * SAMPLE_STEP + 1;
                    sample.add(red, green, blue, luminance - blockDistance);
                }
            }
        }
    }
}
