package dev.gegy.colored_lights.resource;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ResourcePatchManager {
    public static final ResourcePatchManager INSTANCE = new ResourcePatchManager();

    private static final Logger LOGGER = LogManager.getLogger();

    private final Multimap<Identifier, ResourcePatch> patches = HashMultimap.create();

    private ResourcePatchManager() {
    }

    public void add(Identifier id, ResourcePatch patch) {
        this.patches.put(id, patch);
    }

    @NotNull
    public InputStream patch(Identifier id, InputStream input) {
        var patches = this.patches.get(id);
        if (patches.isEmpty()) {
            return input;
        }

        try {
            var bytes = IOUtils.toByteArray(input);
            for (ResourcePatch patch : patches) {
                bytes = patch.apply(bytes);
            }

            return new ByteArrayInputStream(bytes);
        } catch (IOException e) {
            LOGGER.error("Failed to load bytes for patching for resource: '{}'", id, e);
            return propagateException(e);
        }
    }

    private static InputStream propagateException(IOException exception) {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                throw exception;
            }
        };
    }
}
