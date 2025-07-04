package it.polimi.ingsw.model.shipboard.tiles.exceptions;

import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.model.shipboard.tiles.ContainerTile;

import java.util.Set;
import java.util.stream.Collectors;

public class UnsupportedLoadableItemException extends Exception {

    public UnsupportedLoadableItemException(Set<LoadableType> types) {
        super("Loadable of type " + types + " is not supported in this operation.");
    }

    public UnsupportedLoadableItemException(Set<LoadableType> types, Set<LoadableType> allowedTypes) {
        super("Loadable of type " + types.stream()
                                        .filter((t) -> !allowedTypes.contains(t))
                                        .collect(Collectors.toSet())
                + " is not supported in this operation. Valid types: " + allowedTypes);
    }

    public UnsupportedLoadableItemException(Set<LoadableType> types, ContainerTile tile) {
        this(types, tile.getAllowedItems());
    }

    public UnsupportedLoadableItemException(LoadableType type, ContainerTile tile) {
        this(Set.of(type), tile.getAllowedItems());
    }

    public UnsupportedLoadableItemException(LoadableType type, Set<LoadableType> allowedTypes) {
        this(Set.of(type), allowedTypes);
    }
}
