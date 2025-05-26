package it.polimi.ingsw.util;

import java.io.Serializable;
import java.util.Collection;

public class Pair<T extends Serializable> implements Serializable {

    private final T first;
    private final T second;

    public Pair(T first, T second) {
        if (first == null || second == null) {
            throw new NullPointerException();
        }
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }

    public boolean contains(T item) {
        return first.equals(item) || second.equals(item);
    }

    public boolean isIn(Collection<T> items) {
        return items.contains(first) && items.contains(second);
    }
}
