package it.polimi.ingsw.view.gui.utils;

import it.polimi.ingsw.model.shipboard.TileCluster;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.util.Util;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.gui.components.ShipCell;
import it.polimi.ingsw.view.gui.components.ShipGrid;

import java.util.*;

public class ShipIntegrityProblemManager {
    private final HashMap<ShipCell, List<Integer>> mapToChoices;
    private final List<Set<Coordinates>> clusters;
    private final ShipGrid refShipGrid;
    private final List<String> colorsNames;
    private final String disabledColor;
    private int lastSelected;

    private static List<Set<Coordinates>> calculateClusters(String[] possibleOptions) {
        List<Set<Coordinates>> clusters = new ArrayList<>();
        for (String clusterStringANSIed : possibleOptions) {
            clusters.add(
                    new HashSet<>(
                            Coordinates.parseArray(ANSI.Helper.stripAnsi(clusterStringANSIed))
                    )
            );
        }
        return clusters;
    }

    public ShipIntegrityProblemManager(ShipGrid shipGrid, String[] possibleOptions) {
        mapToChoices = new HashMap<>();
        clusters = calculateClusters(possibleOptions);
        refShipGrid = shipGrid;
        colorsNames = Colors.getRandomColors(clusters.size(), List.of("white", "red", "black"), 0.40f);
        disabledColor = Colors.fromNameToRGBA("lightgray", 0.30f);
        lastSelected = 0;
    }

    public List<Set<Coordinates>> getClustersToKeep() {
        return Collections.unmodifiableList(clusters);
    }

    public void addCluster(Set<Coordinates> cluster, int index) {
        for (int i = clusters.size(); i < index; i++) {
            clusters.add(new HashSet<>());
        }
        if (index < clusters.size()) {
            clusters.get(index).addAll(cluster);
        } else {
            clusters.add(cluster);
        }
    }

    public void addShipCell(ShipCell cell, int clusterIndex) {
        if (!mapToChoices.containsKey(cell)) {
            mapToChoices.put(cell, new ArrayList<>());
        }
        mapToChoices.get(cell).add(clusterIndex);
    }

    public void highlightLastSelected() {
        performSelection(lastSelected);
    }

    public void unhighlightAll() {
        mapToChoices.keySet().forEach(cell -> {
            cell.setHighlight(null);
            cell.setOnMouseClicked(_ -> performSelection(cell));
        });
    }

    public void start() {
        mapToChoices.keySet().forEach(k -> k.setOnMouseClicked(_ -> performSelection(k)));
        performSelection(clusters.size() - 1);
    }

    private void performSelection(int clusterIndex) {
        // remove all highlights
        unhighlightAll();
        Set<Coordinates> coordinates = clusters.get(clusterIndex);
        // add other highlights
        for (int i = 0; i < clusters.size(); i++) {
            if (i != clusterIndex) {
                Set<Coordinates> coordinatesNotIntersected = new HashSet<>(clusters.get(i));
                coordinatesNotIntersected.removeAll(coordinates);
                refShipGrid.highlightCells(coordinatesNotIntersected, disabledColor);
            }
        }
        // add this highlight
        refShipGrid.highlightCells(coordinates, Util.getModularAt(colorsNames, clusterIndex));
        // update internal state
        lastSelected = clusterIndex;
    }

    private void performSelection(ShipCell cell) {
        List<Integer> choices = mapToChoices.get(cell);
        if (choices == null || choices.contains(lastSelected)) return;
        performSelection(choices.getLast());
    }

    public int getChoice() {
        System.out.println(" <<<<<<<<<< TEST >> choice: " + lastSelected);
        return lastSelected;
    }

}
