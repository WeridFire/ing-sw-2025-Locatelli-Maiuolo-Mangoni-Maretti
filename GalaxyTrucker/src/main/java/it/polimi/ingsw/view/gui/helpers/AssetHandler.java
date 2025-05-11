package it.polimi.ingsw.view.gui.helpers;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for loading and caching image assets as {@link ImageView} instances.
 * <p>
 * Images are loaded from the provided path or URL and stored in an internal cache
 * to avoid reloading the same image multiple times.
 */
public class AssetHandler {

    /** Cache mapping texture names to loaded {@link Image} objects. */
    private static final Map<String, Image> imageCache = new HashMap<>();

    /** Default width to apply when no explicit size is provided. */
    private static final double DEFAULT_WIDTH = 100;

    /** Default height to apply when no explicit size is provided. */
    private static final double DEFAULT_HEIGHT = 100;

    /** Default flag indicating whether to preserve aspect ratio. */
    private static final boolean DEFAULT_PRESERVE_RATIO = true;

    /**
     * Loads an image with the given texture name, preserving its aspect ratio according to the flag.
     * The image is cached after the first load.
     *
     * @param textureName     Path or URL of the texture to load
     * @param preserveRatio   whether to preserve the image aspect ratio when resizing
     * @return an {@link ImageView} displaying the loaded image
     */
    public static ImageView loadImage(String textureName, boolean preserveRatio) {
        String fullPath = Path.of(textureName);

        // Attempt to retrieve from cache
        Image image = imageCache.get(fullPath);

        // Load and cache if not present
        if (image == null) {
            image = new Image(fullPath);
            imageCache.put(fullPath, image);
        }

        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(preserveRatio);
        return imageView;
    }

    /**
     * Loads an image with the given name and sets both width and height to the specified size.
     * Preserves the aspect ratio by default.
     *
     * @param name  Path or URL of the texture to load
     * @param size  the width and height to apply
     * @return an {@link ImageView} with the specified dimensions
     */
    public static ImageView loadImage(String name, double size) {
        return loadImage(name, size, size);
    }

    /**
     * Loads an image with the given name, resizing it to the specified width and height.
     * Preserves the aspect ratio by default.
     *
     * @param name    Path or URL of the texture to load
     * @param width   the width to apply
     * @param height  the height to apply
     * @return an {@link ImageView} with the specified width and height
     */
    public static ImageView loadImage(String name, double width, double height) {
        ImageView image = loadImage(name);
        image.setFitWidth(width);
        image.setFitHeight(height);
        return image;
    }

    /**
     * Loads an image with the given name, using default dimensions and preserving aspect ratio.
     *
     * @param name  Path or URL of the texture to load
     * @return an {@link ImageView} with default size
     */
    public static ImageView loadImage(String name) {
        return loadImage(name, DEFAULT_PRESERVE_RATIO);
    }
}
