package it.polimi.ingsw.view.gui.helpers;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Utility class for loading and caching image assets as {@link ImageView} instances.
 * Images are loaded from the resource path and cached.
 */
public class AssetHandler {

    /** Cache mapping texture names to loaded {@link Image} objects. */
    private static final Map<String, Image> imageCache = new HashMap<>();

    /** Default width and height. */
    private static final double DEFAULT_WIDTH = 100;
    private static final double DEFAULT_HEIGHT = 100;
    private static final boolean DEFAULT_PRESERVE_RATIO = true;

    /**
     * Loads an image from a resource path and wraps it in an {@link ImageView}.
     * @param textureName texture relative to resources root, e.g. "GT-new_tiles_16_for web.jpg"
     *                    (automatically detects the correct path)
     * @param preserveRatio whether to preserve aspect ratio
     * @return ImageView containing the image
     */
    public static ImageView loadImage(String textureName, boolean preserveRatio) {
        ImageView view = new ImageView(loadRawImage(textureName));
        view.setPreserveRatio(preserveRatio);
        return view;
    }

    /**
     * Loads an image from resources and returns it raw.
     * @param textureName e.g. "GT-new_tiles_16_for web.jpg" (automatically detects the correct path)
     * @return Image
     */
    public static Image loadRawImage(String textureName) {
        return imageCache.computeIfAbsent(textureName, key ->
                new Image(Objects.requireNonNull(AssetHandler.class.getResourceAsStream(Path.of(textureName))))
        );
    }

    /**
     * Pre-loads tile textures listed in {@link Path#LIST_PATH}.
     * The file must contain one texture file name per line.
     */
    public static void preLoadTextures() {
        new Thread(() -> {
            List<String> textures = new ArrayList<>();

            try (InputStream is = AssetHandler.class.getResourceAsStream(Path.LIST_PATH);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(is)))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) {
                        textures.add(line.trim());
                    }
                }

            } catch (IOException e) {
                System.err.println("Failed to load tile texture list: " + e.getMessage());
                return;
            }

            for (String texture : textures) {
                try {
                    loadRawImage(texture);
                    System.out.println("Loaded asset: " + texture);
                } catch (Exception e) {
                    System.err.println("Failed to load asset " + texture + ": " + e.getMessage());
                }
            }

            System.out.println("Loaded " + textures.size() + " assets.");
        }).start();
    }

    public static ImageView loadImage(String name, double size) {
        return loadImage(name, size, size);
    }

    public static ImageView loadImage(String name, double width, double height) {
        ImageView image = loadImage(name);
        image.setFitWidth(width);
        image.setFitHeight(height);
        return image;
    }

    public static ImageView loadImage(String name) {
        return loadImage(name, DEFAULT_PRESERVE_RATIO);
    }
}
