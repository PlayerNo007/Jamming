package com.pn7games.jamming;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Shared constants. Jamming is client-only, so there is no "main" entrypoint: everything starts in JammingClient.
public final class Jamming {

    public static final String MOD_ID = "jamming";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private Jamming() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
