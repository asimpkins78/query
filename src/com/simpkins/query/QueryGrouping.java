package com.simpkins.query;

import java.util.Arrays;
import java.util.Collection;

@SuppressWarnings({"UnusedDeclaration", "Convert2Diamond"})
public class QueryGrouping<K, V> extends QueryList<V> {
    private K key;

    @SafeVarargs
    public QueryGrouping(K key, V... items) {
        this(key, Arrays.asList(items));
    }

    public QueryGrouping(K key, Iterable<? extends V> items) {
        super(items);
        this.key = key;
    }

    public QueryGrouping(K key, Collection<? extends V> items) {
        super(items);
        this.key = key;
    }

    public K getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "QueryGrouping{key=" + key + ", size=" + size() + ", values=[" + toString(", ") + "]}";
    }
}
