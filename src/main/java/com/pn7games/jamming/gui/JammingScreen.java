package com.pn7games.jamming.gui;

import com.pn7games.jamming.client.JammingConfig;
import com.pn7games.jamming.client.JammingConfig.SortMode;
import com.pn7games.jamming.music.MusicLibrary;
import com.pn7games.jamming.music.TrackFilter;
import com.pn7games.jamming.music.TrackInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

// Jamming's main menu, in the vanilla style (like Options or the resource pack screen).
//   title + "now playing"
//   [prev][play][next][loop]  [Music: 58%          ]
//   [All][Here][Favorites]    [Group: All          ]
//   [Search...            ] [Sort: Title] [Gap: ...]
//   track list (full width, vanilla background)
//   [Done]
public class JammingScreen extends Screen implements TrackList.Listener {

    private static final int ROW_WIDTH = 320; // width of the control rows
    private static final int H = 20;          // vanilla button height
    private static final int GAP = 4;
    private static final String ALL_DIMENSIONS = "";

    private enum View { ALL, HERE, FAVORITES }

    @Nullable
    private final Screen parent; // where "Done" goes back to (the title screen, or null = back to the game)

    private final List<TrackInfo> tracks = new ArrayList<>();
    private Set<Identifier> hereTracks = Set.of();
    private View view = View.ALL;
    private String dimension = ALL_DIMENSIONS;
    private TrackFilter filter = TrackFilter.parse("");

    private final List<Button> viewButtons = new ArrayList<>();
    private CycleButton<String> dimensionButton;
    private EditBox searchBox;
    private TrackList list;

    public JammingScreen(@Nullable Screen parent) {
        super(Component.translatable("jamming.screen.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        Minecraft client = Minecraft.getInstance();
        loadTracks(client);
        viewButtons.clear();

        int left = (width - ROW_WIDTH) / 2;
        int right = left + ROW_WIDTH;

        // Row 1: play controls + music volume
        int y = 40;
        MusicControls.create(client, left, y).forEach(this::addRenderableWidget);
        int sliderX = left + MusicControls.WIDTH + GAP * 2;
        addRenderableWidget(new VolumeSlider(sliderX, y, right - sliderX, H));

        // Row 2: views + dimension
        y += H + GAP;
        int x = left;
        int[] viewWidths = {50, 50, 86};
        for (View v : View.values()) {
            Button button = Button.builder(Component.translatable("jamming.screen.view." + v.name().toLowerCase(Locale.ROOT)),
                    b -> setView(v)).bounds(x, y, viewWidths[v.ordinal()], H).build();
            viewButtons.add(addRenderableWidget(button));
            x += viewWidths[v.ordinal()] + GAP;
        }
        updateViewButtons();
        List<String> dimensions = new ArrayList<>();
        dimensions.add(ALL_DIMENSIONS);
        dimensions.addAll(dimensionsInOrder());
        dimensionButton = addRenderableWidget(CycleButton.<String>builder(
                        d -> d.isEmpty() ? Component.translatable("jamming.screen.dimension.all") : Component.literal(d),
                        () -> dimension)
                .withValues(dimensions)
                .create(x + GAP, y, right - x - GAP, H, Component.translatable("jamming.screen.dimension"),
                        (button, value) -> {
                            dimension = value;
                            refreshList();
                        }));

        // Row 3: search + sort + gap
        y += H + GAP;
        int smallWidth = 76;
        int searchWidth = ROW_WIDTH - 2 * (smallWidth + GAP);
        searchBox = new EditBox(font, left + 1, y + 1, searchWidth - 2, H - 2, Component.translatable("jamming.screen.search"));
        searchBox.setHint(Component.translatable("jamming.screen.search"));
        searchBox.setMaxLength(64);
        searchBox.setTooltip(Tooltip.create(searchHelp()));
        searchBox.setResponder(text -> {
            filter = TrackFilter.parse(text);
            refreshList();
        });
        addRenderableWidget(searchBox);
        addRenderableWidget(CycleButton.<SortMode>builder(
                        mode -> Component.translatable("jamming.screen.sort." + mode.name().toLowerCase(Locale.ROOT)),
                        JammingConfig::sort)
                .withValues(SortMode.values())
                .displayOnlyValue()
                .create(left + searchWidth + GAP, y, smallWidth, H, Component.translatable("jamming.screen.sort"),
                        (button, mode) -> {
                            JammingConfig.setSort(mode);
                            sortTracks();
                            refreshList();
                        }));
        addRenderableWidget(CycleButton.<Integer>builder(JammingScreen::gapLabel, JammingConfig::gapSeconds)
                .withValues(JammingConfig.GAP_STEPS)
                .displayOnlyValue()
                .create(right - smallWidth, y, smallWidth, H, Component.translatable("jamming.screen.gap"),
                        (button, seconds) -> JammingConfig.setGapSeconds(seconds)));

        // The list fills the space down to the footer, full width like vanilla lists.
        y += H + 6;
        int footerTop = height - 32;
        list = addRenderableWidget(new TrackList(client, width, footerTop - y, y, this));
        refreshList();

        // Footer: Done
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
                .bounds(width / 2 - 75, height - 26, 150, H).build());
    }

    // Hover help for the search box. One Component; "\n" makes new lines in a tooltip.
    //   Search tracks              (yellow)
    //   Type part of a title...    (white)
    //   @  artist   @C418          (aqua sign, gray example)
    //   ...
    private static Component searchHelp() {
        return Component.empty()
                .append(Component.translatable("jamming.search_help.title").withStyle(ChatFormatting.YELLOW))
                .append("\n")
                .append(Component.translatable("jamming.search_help.text"))
                .append("\n\n")
                .append(helpLine("@", "jamming.search_help.artist", "@C418"))
                .append("\n")
                .append(helpLine("#", "jamming.search_help.place", "#nether  #cherry"))
                .append("\n\n")
                .append(Component.translatable("jamming.search_help.combine").withStyle(ChatFormatting.GRAY))
                .append("\n")
                .append(Component.literal("  calm @C418 #overworld").withStyle(ChatFormatting.GRAY))
                .append("\n\n")
                .append(Component.translatable("jamming.search_help.click").withStyle(ChatFormatting.DARK_GRAY));
    }

    // "@  artist   @C418"
    private static Component helpLine(String sign, String key, String example) {
        return Component.literal(sign + "  ").withStyle(ChatFormatting.AQUA)
                .append(Component.translatable(key).withStyle(ChatFormatting.WHITE))
                .append(Component.literal("   " + example).withStyle(ChatFormatting.GRAY));
    }

    private void setView(View v) {
        view = v;
        updateViewButtons();
        refreshList();
    }

    // Vanilla style for "tabs": the selected one is greyed out (inactive).
    private void updateViewButtons() {
        for (int i = 0; i < viewButtons.size(); i++) {
            viewButtons.get(i).active = i != view.ordinal();
        }
    }

    // "Vanilla", "0s", "30s", "2m"...
    private static Component gapLabel(int seconds) {
        if (seconds == JammingConfig.VANILLA_GAP) {
            return Component.translatable("jamming.screen.gap.vanilla");
        }
        return seconds < 60
                ? Component.translatable("jamming.screen.gap.seconds", seconds)
                : Component.translatable("jamming.screen.gap.minutes", seconds / 60);
    }

    // Overworld, Nether, End, Menu, Disc, then mods.
    private List<String> dimensionsInOrder() {
        return tracks.stream().flatMap(track -> track.dimensions().stream()).distinct()
                .sorted(TrackInfo::compareDimensions)
                .toList();
    }

    // Snapshot of all tracks when the menu opens.
    private void loadTracks(Minecraft client) {
        tracks.clear();
        for (Identifier id : MusicLibrary.allTracks()) {
            tracks.add(TrackInfo.of(id));
        }
        sortTracks();
        // "Here" = the tracks that fit where you are right now (biome, dimension, menu...).
        Music situational = client.getSituationalMusic();
        hereTracks = situational == null ? Set.of()
                : MusicLibrary.tracksFor(situational.sound().value().location());
    }

    private void sortTracks() {
        Comparator<TrackInfo> byTitle = Comparator.comparing(TrackInfo::title, String.CASE_INSENSITIVE_ORDER);
        Comparator<TrackInfo> order = switch (JammingConfig.sort()) {
            case TITLE -> byTitle;
            case ARTIST -> Comparator.comparing(TrackInfo::artist, String.CASE_INSENSITIVE_ORDER).thenComparing(byTitle);
            case GROUP -> Comparator.comparing((TrackInfo t) -> t.dimensions().getFirst(), TrackInfo::compareDimensions)
                    .thenComparing(byTitle);
        };
        tracks.sort(order);
    }

    private void refreshList() {
        if (list == null) {
            return;
        }
        Predicate<TrackInfo> inView = switch (view) {
            case ALL -> track -> true;
            case HERE -> track -> hereTracks.contains(track.id());
            case FAVORITES -> track -> JammingConfig.isFavorite(track.id());
        };
        list.setTracks(tracks.stream()
                .filter(inView)
                .filter(track -> dimension.isEmpty() || track.dimensions().contains(dimension))
                .filter(filter::test)
                .toList());
    }

    // ---- TrackList.Listener ----

    @Override
    public void onArtistClicked(String artist) {
        searchBox.setValue("@" + artist); // the responder updates the filter and the list
    }

    @Override
    public void onDimensionClicked(String clicked) {
        dimension = clicked.equals(dimension) ? ALL_DIMENSIONS : clicked;
        dimensionButton.setValue(dimension);
        refreshList();
    }

    @Override
    public void onFavoriteChanged() {
        if (view == View.FAVORITES) {
            refreshList(); // un-starred tracks leave the Favorites view right away
        }
    }

    // ---- Drawing ----

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        int center = width / 2;
        graphics.centeredText(font, title, center, 6, 0xFFFFFFFF);
        graphics.centeredText(font, MusicControls.title(), center, 18, 0xFFFFFF55);
        graphics.centeredText(font, MusicControls.subtitle(), center, 28, 0xFFA0A0A0);

        // Footer: how many tracks are shown + search tip
        Component count = Component.translatable("jamming.screen.count", list.children().size(), tracks.size());
        graphics.text(font, count, 8, height - 20, 0xFFA0A0A0, false);
        Component tip = Component.translatable("jamming.screen.tip");
        graphics.text(font, tip, width - 8 - font.width(tip), height - 20, 0xFF707070, false);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().gui.setScreen(parent);
    }

    // Don't pause the game: in singleplayer a pause screen would also pause the world.
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
