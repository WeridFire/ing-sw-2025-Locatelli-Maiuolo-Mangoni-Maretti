package it.polimi.ingsw.util;

import java.io.Serializable;

public class Pair<T extends Serializable> implements Serializable {

    private final T first;
    private final T second;

    public Pair(T first, T second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }
}
