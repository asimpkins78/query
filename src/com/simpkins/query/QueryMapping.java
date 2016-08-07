package com.simpkins.query;

public class QueryMapping<K, V> {
    private K key;
    private V value;

    public QueryMapping(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "QueryMapping{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}
