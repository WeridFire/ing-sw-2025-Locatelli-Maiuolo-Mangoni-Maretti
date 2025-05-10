package it.polimi.ingsw.view.gui.helpers;

/**
 * Utility class for constructing file paths to asset resources based on texture name prefixes.
 * <p>
 * Determines the appropriate subdirectory (e.g., cardboard, cards, tiles) and prepends the base path.
 */
public class Path {

    /**
     * Enumeration of asset types, each mapping to its folder name.
     */
    public enum TYPE {
        /** Cardboard assets folder (e.g., boards, mats). */
        CARDBOARD,
        /** Card images folder. */
        CARD,
        /** Tile images folder. */
        TILE;

        @Override
        public String toString() {
            return switch (this) {
                case CARDBOARD -> "cardboard/";
                case CARD      -> "cards/";
                default        -> "tiles/";
            };
        }
    }

    /**
     * Base URI prefix for all asset files, relative to the application's assets directory.
     */
    private static final String BASE_PATH = "file:assets/";

    /**
     * Prefix indicating a cardboard asset name.
     */
    private static final String MINIMUM_DIFFERENT_PREFIX_CARDBOARD = "c";

    /**
     * Prefix indicating a card asset name.
     */
    private static final String MINIMUM_DIFFERENT_PREFIX_CARD = "GT-c";

    /**
     * Prefix indicating a tile asset name.
     */
    private static final String MINIMUM_DIFFERENT_PREFIX_TILE = "GT-n";

    /**
     * Constructs the full file path for the given texture name by
     * determining its asset type based on the name prefix.
     * <p>
     * If the texture name starts with the cardboard prefix, it is placed
     * under the cardboard folder; if it starts with the card prefix, under cards;
     * if it starts with the tile prefix, under tiles. Returns an empty string if
     * no known prefix matches.
     *
     * @param textureName the name of the texture (including its unique prefix)
     * @return the full URI string to load the asset, or empty if prefix unknown
     */
    public static String of(String textureName) {
        if (textureName.startsWith(MINIMUM_DIFFERENT_PREFIX_CARDBOARD)) {
            return BASE_PATH + TYPE.CARDBOARD.toString() + textureName;
        } else if (textureName.startsWith(MINIMUM_DIFFERENT_PREFIX_CARD)) {
            return BASE_PATH + TYPE.CARD.toString() + textureName;
        } else if (textureName.startsWith(MINIMUM_DIFFERENT_PREFIX_TILE)) {
            return BASE_PATH + TYPE.TILE.toString() + textureName;
        }
        return "";
    }
}
