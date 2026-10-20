package com.pn7games.jamming.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pn7games.jamming.Jamming;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Things Jamming remembers between game starts. Saved as config/jamming.json (easy to read and edit).
public final class JammingConfig {

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("jamming.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Data data = new Data();

    // The JSON shape. Gson fills these fields by name.
    private static final class Data {
        Set<String> favorites = new LinkedHashSet<>();
        String sort = SortMode.TITLE.name();
        boolean pauseForJukebox = true;
        int gapSeconds = VANILLA_GAP;
    }

    // Silence between songs. VANILLA_GAP = let the game decide (10-20 minutes by default).
    public static final int VANILLA_GAP = -1;
    public static final List<Integer> GAP_STEPS = List.of(VANILLA_GAP, 0, 15, 30, 60, 120, 300);

    public enum SortMode {
        TITLE, ARTIST, GROUP;

        public SortMode next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    private JammingConfig() {
    }

    public static void load() {
        if (!Files.exists(FILE)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(FILE)) {
            Data loaded = GSON.fromJson(reader, Data.class);
            if (loaded != null) {
                data = loaded;
            }
        } catch (IOException | RuntimeException e) {
            // A broken file must not crash the game: log it and keep the defaults.
            Jamming.LOGGER.warn("Could not read {}, using defaults", FILE, e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            Jamming.LOGGER.warn("Could not save {}", FILE, e);
        }
    }

    public static boolean isFavorite(Identifier track) {
        return data.favorites.contains(track.toString());
    }

    public static void toggleFavorite(Identifier track) {
        if (!data.favorites.remove(track.toString())) {
            data.favorites.add(track.toString());
        }
        save();
    }

    public static SortMode sort() {
        try {
            return SortMode.valueOf(data.sort);
        } catch (IllegalArgumentException | NullPointerException e) {
            return SortMode.TITLE;
        }
    }

    public static void setSort(SortMode mode) {
        data.sort = mode.name();
        save();
    }

    public static boolean pauseForJukebox() {
        return data.pauseForJukebox;
    }

    public static int gapSeconds() {
        return data.gapSeconds;
    }

    public static void setGapSeconds(int seconds) {
        data.gapSeconds = seconds;
        save();
    }
}
