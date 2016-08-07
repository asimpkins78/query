package simpkins.query.iterator;

import simpkins.query.QueryGrouping;

import java.util.*;
import java.util.function.Function;

public class GroupByIterator<T, K, V> implements Iterator<QueryGrouping<K, V>> {
    private Iterator<T> source;
    private Function<T, K> keySelector;
    private Function<T, V> valueSelector;
    private int index = 0;
    private List<QueryGrouping<K, T>> sourceByKeys = null;

    public GroupByIterator(Iterator<T> source, Function<T, K> keySelector, Function<T, V> valueSelector) {
        this.source = source;
        this.keySelector = keySelector;
        this.valueSelector = valueSelector;
    }

    private List<QueryGrouping<K, T>> getSourceByKeys() {
        if (sourceByKeys == null) {
            Map<K, List<T>> keysMap = new LinkedHashMap<>();
            while (source.hasNext()) {
                T next = source.next();
                K key = keySelector.apply(next);
                if (!keysMap.containsKey(key))
                    keysMap.put(key, new ArrayList<>());
                keysMap.get(key).add(next);
            }
            sourceByKeys = new ArrayList<>(keysMap.size());
            for (K key : keysMap.keySet())
                sourceByKeys.add(new QueryGrouping<>(key, keysMap.get(key)));
        }
        return sourceByKeys;
    }

    @Override
    public boolean hasNext() {
        return index < getSourceByKeys().size();
    }

    @Override
    public QueryGrouping<K, V> next() {
        if (!hasNext())
            throw new NoSuchElementException();
        QueryGrouping<K, T> nextGrouping = getSourceByKeys().get(index++);
        List<V> values = new ArrayList<>(nextGrouping.size());
        for (T item : nextGrouping)
            values.add(valueSelector.apply(item));
        return new QueryGrouping<>(nextGrouping.getKey(), values);
    }
}
