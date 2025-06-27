package it.polimi.ingsw.view.gui.utils;

import javafx.scene.paint.Color;

import java.util.*;
import java.util.stream.Collectors;

public class Colors {

    private static final List<String> colors = List.of("red", "blue", "green",
            "black", "white", "yellow", "cyan", "magenta");
    /*
    List.of("red", "blue", "green",
            "black", "white", "yellow", "cyan", "magenta", "gray", "darkgray", "lightgray",
            "orange", "purple", "pink", "brown"
    );
     */

    private static String fromNameToRGBA(final String colorName, float alpha) {
        Color color = Color.web(colorName);
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);
        return String.format(Locale.US, "rgba(%d,%d,%d,%.2f)", r, g, b, alpha);
    }

    /**
     * @param alpha the desired alpha component as in rgbA (in [0; 1]) for the generated colors
     * @return targetQuantity different random colors, or less if forbiddenColors remove to many possibilities.
     */
    public static List<String> getRandomColors(int targetQuantity, List<String> forbiddenColors, float alpha) {
        if (targetQuantity < 1) targetQuantity = 1;

        List<String> availableColors = new ArrayList<>(colors);
        forbiddenColors.forEach(fc -> availableColors.remove(fc.toLowerCase()));

        if (targetQuantity > availableColors.size()) targetQuantity = availableColors.size();

        Collections.shuffle(availableColors);

        return availableColors.stream()
                .limit(targetQuantity)
                .map(sCol -> fromNameToRGBA(sCol, alpha))
                .collect(Collectors.toList());
    }
}
