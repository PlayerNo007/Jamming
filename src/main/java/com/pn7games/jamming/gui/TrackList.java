package com.pn7games.jamming.gui;

import com.pn7games.jamming.Jamming;
import com.pn7games.jamming.client.JammingConfig;
import com.pn7games.jamming.client.JammingPlayer;
import com.pn7games.jamming.music.TrackInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.List;

// The scrollable list of tracks, in the vanilla style (like the resource pack or world lists).
// Row: [star] title / artist ............ dimension (hover: biomes)
// Click the star = favorite, the artist = show only that artist, the dimension = show only that dimension,
// anywhere else = play. The playing track gets the vanilla selection outline.
public class TrackList extends ObjectSelectionList<TrackList.Row> {

    public static final int ROW_HEIGHT = 24;
    private static final int MAX_ROW_WIDTH = 310;
    private static final Identifier STAR = Jamming.id("icon/star");
    private static final Identifier STAR_EMPTY = Jamming.id("icon/star_empty");
    private static final int STAR_AREA = 18; // clickable width on the left

    // Vanilla text colors
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFFA0A0A0;
    private static final int DARK_GRAY = 0xFF707070;
    private static final int YELLOW = 0xFFFFFF55;

    // What the screen wants to happen when something in a row is clicked.
    public interface Listener {
        void onArtistClicked(String artist);

        void onDimensionClicked(String dimension);

        void onFavoriteChanged();
    }

    private final Listener listener;

    public TrackList(Minecraft client, int width, int height, int y, Listener listener) {
        super(client, width, height, y, ROW_HEIGHT);
        this.listener = listener;
    }

    public void setTracks(List<TrackInfo> tracks) {
        double scroll = scrollAmount();
        replaceEntries(tracks.stream().map(Row::new).toList());
        setScrollAmount(scroll); // keep the position (e.g. after starring a track)
    }

    @Override
    public int getRowWidth() {
        return Math.min(MAX_ROW_WIDTH, getWidth() - 40);
    }

    // No real "selection": vanilla setSelected() also scrolls the list to the selected row,
    // which made the list jump back to the playing track while you scrolled.
    // The playing track gets a vanilla-looking outline instead (drawn by the row itself).
    @Override
    protected boolean entriesCanBeSelected() {
        return false;
    }

    public class Row extends ObjectSelectionList.Entry<Row> {

        private final TrackInfo track;

        public Row(TrackInfo track) {
            this.track = track;
        }

        // ---- Layout helpers (shared by drawing and clicking, so they always match) ----

        private int textLeft() {
            return getContentX() + STAR_AREA + 2;
        }

        private int dimensionLeft(Font font) {
            return getContentRight() - font.width(track.dimensionLabel()) - 4;
        }

        private boolean overStar(double mouseX) {
            return mouseX < getContentX() + STAR_AREA;
        }

        private boolean overArtist(Font font, double mouseX, double mouseY) {
            int top = getContentY() + 12;
            return !track.artist().isEmpty()
                    && mouseY >= top && mouseY < top + font.lineHeight
                    && mouseX >= textLeft() && mouseX < textLeft() + font.width(track.artist());
        }

        private boolean overDimension(Font font, double mouseX, double mouseY) {
            int top = getContentY() + 3;
            return mouseX >= dimensionLeft(font) && mouseY >= top && mouseY < top + font.lineHeight;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float delta) {
            Font font = Minecraft.getInstance().font;
            boolean playing = track.id().equals(JammingPlayer.currentTrack());
            boolean favorite = JammingConfig.isFavorite(track.id());
            int left = getContentX();
            int top = getContentY();
            int bottom = getContentBottom();

            if (playing) {
                // Same look as the vanilla selection box: white border, black inside.
                graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), WHITE);
                graphics.fill(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, 0xFF000000);
            }

            // Star: gold for favorites; an outline on hover so you know you can click it.
            if (favorite || hovered) {
                int starColor = favorite ? YELLOW : overStar(mouseX) ? WHITE : DARK_GRAY;
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, favorite ? STAR : STAR_EMPTY,
                        left + 4, top + (bottom - top - 9) / 2, 9, 9, starColor);
            }

            // Dimension on the right, underlined on hover (like a vanilla link).
            // Hovering it also shows a tooltip with the biomes / situations where the track plays.
            boolean dimensionHovered = hovered && overDimension(font, mouseX, mouseY);
            graphics.text(font, link(track.dimensionLabel(), dimensionHovered), dimensionLeft(font), top + 3, DARK_GRAY, false);
            if (dimensionHovered && !track.places().isEmpty()) {
                graphics.setComponentTooltipForNextFrame(font, placesTooltip(), mouseX, mouseY);
            }

            // Title (cut to fit) and artist below it.
            int textLeft = textLeft();
            int maxWidth = dimensionLeft(font) - 8 - textLeft;
            String prefix = playing ? "♪ " : ""; // ♪ in front of the playing track
            String title = font.plainSubstrByWidth(prefix + track.title(), maxWidth);
            int titleColor = playing ? YELLOW : WHITE;
            if (track.artist().isEmpty()) {
                graphics.text(font, title, textLeft, top + (bottom - top - font.lineHeight) / 2 + 1, titleColor, false);
            } else {
                graphics.text(font, title, textLeft, top + 2, titleColor, false);
                boolean artistHovered = hovered && overArtist(font, mouseX, mouseY);
                String artist = font.plainSubstrByWidth(track.artist(), maxWidth);
                graphics.text(font, link(artist, artistHovered), textLeft, top + 12, GRAY, false);
            }
        }

        // "Plays in:" + one line per biome / situation
        private List<Component> placesTooltip() {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable("jamming.screen.plays_in").withStyle(ChatFormatting.YELLOW));
            track.places().forEach(place -> lines.add(Component.literal(" " + place).withStyle(ChatFormatting.GRAY)));
            return lines;
        }

        private static MutableComponent link(String text, boolean hovered) {
            MutableComponent component = Component.literal(text);
            return hovered ? component.withStyle(ChatFormatting.UNDERLINE) : component;
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            Minecraft client = Minecraft.getInstance();
            client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            Font font = client.font;
            if (overStar(event.x())) {
                JammingConfig.toggleFavorite(track.id());
                listener.onFavoriteChanged();
            } else if (overArtist(font, event.x(), event.y())) {
                listener.onArtistClicked(track.artist());
            } else if (overDimension(font, event.x(), event.y())) {
                listener.onDimensionClicked(track.dimensions().getFirst());
            } else {
                JammingPlayer.play(client, track.id());
            }
            return true;
        }

        @Override
        public Component getNarration() {
            return Component.literal(track.artist().isEmpty() ? track.title() : track.artist() + " - " + track.title());
        }
    }
}
