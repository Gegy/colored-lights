package dev.gegy.colored_lights.provider;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.Vec3f;

import java.util.ArrayList;
import java.util.List;

public final class BlockLightColors {
    public static final Vec3f WHITE = new Vec3f(1.0F, 1.0F, 1.0F);

    private static final List<BlockLightColorProvider> providers = new ArrayList<>();
    private static Lookup lookup = state -> WHITE;

    public static void registerProvider(BlockLightColorProvider provider) {
        BlockLightColors.providers.add(provider);
        BlockLightColors.rebuildLookup();
    }

    private static void rebuildLookup() {
        var providers = BlockLightColors.providers.toArray(BlockLightColorProvider[]::new);
        BlockLightColors.lookup = Lookup.build(providers);
    }

    public static Vec3f forBlock(BlockState state) {
        return BlockLightColors.lookup.get(state);
    }

    private interface Lookup {
        static Lookup build(BlockLightColorProvider... providers) {
            if (providers.length == 1) {
                return new SingleProviderLookup(providers[0]);
            } else {
                return new CompositeProviderLookup(providers);
            }
        }

        Vec3f get(BlockState state);
    }

    private record SingleProviderLookup(BlockLightColorProvider provider) implements Lookup {
        @Override
        public Vec3f get(BlockState state) {
            var color = this.provider.get(state);
            return color != null ? color : WHITE;
        }
    }

    private record CompositeProviderLookup(BlockLightColorProvider[] providers) implements Lookup {
        @Override
        public Vec3f get(BlockState state) {
            for (var provider : this.providers) {
                var color = provider.get(state);
                if (color != null) {
                    return color;
                }
            }
            return WHITE;
        }
    }
}
