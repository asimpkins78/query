package com.simpkins.query;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.*;

/**
 * QueryMap is an extension of LinkedHashMap with extra features including convenient access to many Query methods.
 */
@SuppressWarnings({"UnusedDeclaration", "Convert2Diamond"})
public class QueryMap<K, V> extends LinkedHashMap<K, V> implements Iterable<QueryMapping<K, V>> {

    public QueryMap() {
    }

    public QueryMap(int initialCapacity) {
        super(initialCapacity);
    }

    @SafeVarargs
    public QueryMap(QueryMapping<? extends K, ? extends V>... mappings) {
        super(mappings.length);
        for (QueryMapping<? extends K, ? extends V> mapping : mappings)
            put(mapping.getKey(), mapping.getValue());
    }

    // This constructor can be inefficient if the iterable is very large due to the inability to set an initial
    // capacity for the QueryMap.
    public QueryMap(Iterable<QueryMapping<? extends K, ? extends V>> mappings) {
        for (QueryMapping<? extends K, ? extends V> mapping : mappings)
            put(mapping.getKey(), mapping.getValue());
    }

    public QueryMap(Collection<QueryMapping<? extends K, ? extends V>> mappings) {
        super(mappings.size());
        for (QueryMapping<? extends K, ? extends V> mapping : mappings)
            put(mapping.getKey(), mapping.getValue());
    }

    public QueryMap(Map<? extends K, ? extends V> items) {
        super(items);
    }

    // This constructor can be inefficient if the iterable is very large due to the inability to set an initial
    // capacity for the QueryMap.
    public QueryMap(Iterable<? extends V> items, Function<V, K> keySelector) {
        selectAndPut(items, keySelector, v -> v);
    }

    public QueryMap(Collection<? extends V> items, Function<V, K> keySelector) {
        super(items.size());
        selectAndPut(items, keySelector, v -> v);
    }

    // This constructor can be inefficient if the iterable is very large due to the inability to set an initial
    // capacity for the QueryMap.
    public <T> QueryMap(Iterable<T> items, Function<T, K> keySelector, Function<T, V> valueSelector) {
        selectAndPut(items, keySelector, valueSelector);
    }

    public <T> QueryMap(Collection<T> items, Function<T, K> keySelector, Function<T, V> valueSelector) {
        super(items.size());
        selectAndPut(items, keySelector, valueSelector);
    }

    private <T> void selectAndPut(Iterable<? extends T> items, Function<T, K> keySelector, Function<T, V> valueSelector) {
        for (T item : items) {
            K key = keySelector.apply(item);
            if (containsKey(key))
                throw new RuntimeException("keySelector returning duplicate key.");
            put(key, valueSelector.apply(item));
        }
    }

    /**
     * A static constructor where the valueSelector projects an iterable of which each are combined into a flattened
     * result.
     */
    public static <T, K, V> QueryGroup<K, V> many(Iterable<T> items, Function<T, K> keySelector, Function<T, ? extends Iterable<V>> valueSelector) {
        Integer size = Query.findSize(items);
        QueryGroup<K, V> result = size != null ? new QueryGroup<K, V>(size) : new QueryGroup<K, V>();
        for (T item : items) {
            K key = keySelector.apply(item);
            if (result.containsKey(key))
                throw new RuntimeException("keySelector returning duplicate key.");
            Iterable<V> values = valueSelector.apply(item);
            Integer valueSize = Query.findSize(values);
            QueryList<V> list = valueSize != null ? QueryList.initialCapacity(valueSize) : new QueryList<V>();
            list.addAll(values);
            result.put(key, list);
        }
        return result;
    }

    /**
     * A static constructor where the valueSelector projects an array of which each are combined into a flattened
     * result.
     */
    public static <T, K, V> QueryGroup<K, V> manyArray(Iterable<T> items, Function<T, K> keySelector, Function<T, V[]> valueSelector) {
        return many(items, keySelector, x -> Arrays.asList(valueSelector.apply(x)));
    }

    /**
     * Returns the keySet() wrapped as a Query.
     */
    public Query<K> keysQuery() {
        return new Query<K>(keySet());
    }

    /**
     * Returns the values() wrapped as a Query.
     */
    public Query<V> valuesQuery() {
        return new Query<V>(super.values());
    }

    /**
     * Returns a QueryList of values for the provided keys.
     */
    @SafeVarargs
    public final QueryList<V> get(K... keys) {
        return get(Arrays.asList(keys));
    }

    /**
     * Returns a QueryList of values for the provided keys.
     */
    public QueryList<V> get(Iterable<? extends K> keys) {
        Integer size = Query.findSize(keys);
        QueryList<V> results = size != null
                ? QueryList.initialCapacity(((Collection)keys).size())
                : new QueryList<V>();
        for (K key : keys)
            results.add(super.get(key));
        return results;
    }

    //
    // Restriction Operators
    //

    private Function<QueryMapping<K, V>, K> getKey = x -> x.getKey();
    private Function<QueryMapping<K, V>, V> getValue = x -> x.getValue();

    /**
     * Filters the source down to only items where the provided condition is true.
     */
    public QueryMap<K, V> where(Predicate<QueryMapping<K, V>> condition) {
        return query().where(condition).map(getKey, getValue);
    }

    /**
     * Filters the source down to only items where the provided condition is true.  The index of each item in the
     * source is included to be used in the condition.
     */
    public QueryMap<K, V> whereByIndex(BiPredicate<QueryMapping<K, V>, Integer> condition) {
        return query().whereByIndex(condition).map(getKey, getValue);
    }

    /**
     * Filters the source down to only items where the provided condition is true.  The context of each item in the
     * source is included to be used in the condition.
     */
    public QueryMap<K, V> whereByContext(BiPredicate<QueryMapping<K, V>, QueryContext<QueryMapping<K, V>>> condition) {
        return query().whereByContext(condition).map(getKey, getValue);
    }

    //
    // Ordering Operators
    //

    /**
     * Re-orders the source in ascending order based on the Comparable provided by the selector.  Null values are
     * placed last.  An OrderedQuery is returned which is an extension of Query with additional thenBy() methods that
     * allow for sub-ordering.
     */
    public <S extends Comparable<S>> QueryMap<K, V> orderBy(Function<QueryMapping<K, V>, S> selector) {
        return query().orderBy(selector).map(getKey, getValue);
    }

    /**
     * Re-orders the source based on the provided comparator.  An OrderedQuery is returned which is an extension of
     * Query with additional thenBy() methods that allow for sub-ordering.
     */
    public QueryMap<K, V> orderBy(Comparator<QueryMapping<K, V>> comparator) {
        return query().orderBy(comparator).map(getKey, getValue);
    }

    /**
     * Re-orders the source in descending order based on the Comparable provided by the selector.  Null values are
     * placed first.  An OrderedQuery is returned which is an extension of Query with additional thenBy() methods that
     * allow for sub-ordering.
     */
    public <S extends Comparable<S>>QueryMap<K, V> orderByDescending(Function<QueryMapping<K, V>, S> selector) {
        return query().orderByDescending(selector).map(getKey, getValue);
    }

    /**
     * Re-orders the source based on an inversion of the provided comparator.  An OrderedQuery is returned which is an
     * extension of Query with additional thenBy() methods that allow for sub-ordering.
     */
    public QueryMap<K, V> orderByDescending(Comparator<QueryMapping<K, V>> comparator) {
        return query().orderByDescending(comparator).map(getKey, getValue);
    }

    /**
     * Reverses the ordering of the source.
     */
    public QueryMap<K, V> reverse() {
        return query().reverse().map(getKey, getValue);
    }

    /**
     * Shuffles the ordering of the source.
     */
    public QueryMap<K, V> shuffle() {
        return query().shuffle().map(getKey, getValue);
    }

    //
    // Projection Operators
    //

    /**
     * Transforms the source to the projection defined by the provided selector.
     */
    public <S> Query<S> select(Function<QueryMapping<K, V>, S> selector) {
        return query().select(selector);
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  The index of each item in the source
     * is included to be used in the selector.
     */
    public <S> Query<S> selectByIndex(BiFunction<QueryMapping<K, V>, Integer, S> selector) {
        return query().selectByIndex(selector);
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  The context of each item in the source
     * is included to be used in the selector.
     */
    public <S> Query<S> selectByContext(BiFunction<QueryMapping<K, V>, QueryContext<QueryMapping<K, V>>, S> selector) {
        return query().selectByContext(selector);
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  Each projection is an iterable which
     * are all combined into a flattened result.
     */
    public <S> Query<S> selectMany(Function<QueryMapping<K, V>, ? extends Iterable<S>> selector) {
        return query().selectMany(selector);
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  Each projection is an iterable which
     * are all combined into a flattened result.  The index of each item in the source is included to be used in the
     * selector.
     */
    public <S> Query<S> selectManyByIndex(BiFunction<QueryMapping<K, V>, Integer, ? extends Iterable<S>> selector) {
        return query().selectManyByIndex(selector);
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  Each projection is an iterable which
     * are all combined into a flattened result.  The context of each item in the source is included to be used in the
     * selector.
     */
    public <S> Query<S> selectManyByContext(BiFunction<QueryMapping<K, V>, QueryContext<QueryMapping<K, V>>, ? extends Iterable<S>> selector) {
        return query().selectManyByContext(selector);
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  Each projection is an array which
     * are all combined into a flattened result.
     */
    public <S> Query<S> selectManyArray(Function<QueryMapping<K, V>, S[]> selector) {
        return query().selectManyArray(selector);
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  Each projection is an array which
     * are all combined into a flattened result.  The index of each item in the source is included to be used in the
     * selector.
     */
    public <S> Query<S> selectManyArrayByIndex(BiFunction<QueryMapping<K, V>, Integer, S[]> selector) {
        return query().selectManyArrayByIndex(selector);
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  Each projection is an array which
     * are all combined into a flattened result.  The context of each item in the source is included to be used in the
     * selector.
     */
    public <S> Query<S> selectManyArrayByContext(BiFunction<QueryMapping<K, V>, QueryContext<QueryMapping<K, V>>, S[]> selector) {
        return query().selectManyArrayByContext(selector);
    }

    /**
     * Groups and returns items from the source that share the same key obtained by the provided keySelector into a
     * QueryGrouping.  Unlike groupBy() this returns a new Query, not a QueryGroup, which provides deferred execution
     * but none of the underlying Map functionality of QueryGroup.
     */
    public <K2> Query<QueryGrouping<K2, QueryMapping<K, V>>> selectGrouped(Function<QueryMapping<K, V>, K2> keySelector) {
        return query().selectGrouped(keySelector);
    }

    /**
     * Groups and returns items from the source that share the same key obtained by the provided keySelector into a
     * QueryGrouping.  The items in the QueryGroup and transformed to the projection defined by the valueSelector.
     * Unlike groupBy() this returns a new Query, not a QueryGroup, which provides deferred execution but none of the
     * underlying Map functionality of QueryGroup.
     */
    public <K2, V2> Query<QueryGrouping<K2, V2>> selectGrouped(Function<QueryMapping<K, V>, K2> keySelector, Function<QueryMapping<K, V>, V2> valueSelector) {
        return query().selectGrouped(keySelector, valueSelector);
    }

    //
    // Application Operators
    //

    /**
     * Executes the provided action on each item in the source and then returns that item.
     */
    public QueryMap<K, V> pipe(Consumer<QueryMapping<K, V>> action) {
        return query().pipe(action).map(getKey, getValue);
    }

    /**
     * Executes the provided action on each item in the source and then returns that item.  The index of each item in
     * the source is included to be used in the action.
     */
    public QueryMap<K, V> pipeByIndex(BiConsumer<QueryMapping<K, V>, Integer> action) {
        return query().pipeByIndex(action).map(getKey, getValue);
    }

    /**
     * Executes the provided action on each item in the source.  The index of each item in the source is included to be
     * used in the action.
     */
    public void forEachByIndex(BiConsumer<QueryMapping<K, V>, Integer> action) {
        query().forEachByIndex(action);
    }

    /**
     * Executes the provided action on each item in the source.  The context of each item in the source is included to be
     * used in the action.
     */
    public void forEachByContext(BiConsumer<QueryMapping<K, V>, QueryContext<QueryMapping<K, V>>> action) {
        query().forEachByContext(action);
    }

    //
    // Conversion Operators
    //

    /**
     * Wraps the QueryMap in a Query.
     */
    public Query<QueryMapping<K, V>> query() {
        return new Query<QueryMapping<K, V>>(this);
    }

    /**
     * Returns the query result as a QueryMap with the key defined by the keySelector and the value the item itself.
     * Each key selected must be unique or the mapping will fail.
     */
    public <K2> QueryMap<K2, QueryMapping<K, V>> map(Function<QueryMapping<K, V>, K2> keySelector) {
        return query().map(keySelector);
    }

    /**
     * Returns the query result as a QueryMap with the key defined by the keySelector and the value defined by the
     * valueSelector.  Each key selected must be unique or the mapping will fail.
     */
    public <K2, V2> QueryMap<K2, V2> map(Function<QueryMapping<K, V>, K2> keySelector, Function<QueryMapping<K, V>, V2> valueSelector) {
        return query().map(keySelector, valueSelector);
    }


    /**
     * Returns the query result as a QueryGroup with the key defined by the keySelector and the values defined by the
     * valueSelector.  The valueSelector must target an iterable whose items are all combined into a flattened result.
     * Each key selected must be unique or the mapping will fail.
     */
    public <K2, V2> QueryGroup<K2, V2> mapMany(Function<QueryMapping<K, V>, K2> keySelector, Function<QueryMapping<K, V>, ? extends Iterable<V2>> valueSelector) {
        return query().mapMany(keySelector, valueSelector);
    }

    /**
     * Returns the query result as a QueryGroup with the key defined by the keySelector and the values defined by the
     * valueSelector.  The valueSelector must target an array whose items are all combined into a flattened result.
     * Each key selected must be unique or the mapping will fail.
     */
    public <K2, V2> QueryGroup<K2, V2> mapManyArray(Function<QueryMapping<K, V>, K2> keySelector, Function<QueryMapping<K, V>, V2[]> valueSelector) {
        return query().mapManyArray(keySelector, valueSelector);
    }

    /**
     * Returns the query result as a QueryGroup with the key defined by the keySelector and the values as the items
     * themselves.
     */
    public <K2> QueryGroup<K2, QueryMapping<K, V>> groupBy(Function<QueryMapping<K, V>, K2> keySelector) {
        return query().groupBy(keySelector);
    }

    /**
     * Returns the query result as a QueryGroup with the key defined by the keySelector and the values defined by the
     * valueSelector.
     */
    public <K2, V2> QueryGroup<K2, V2> groupBy(Function<QueryMapping<K, V>, K2> keySelector, Function<QueryMapping<K, V>, V2> valueSelector) {
        return query().groupBy(keySelector, valueSelector);
    }

    /**
     * Returns the query result as a QueryGroup with the key defined by the keySelector and the values defined by the
     * valueSelector.  The valueSelector must target an iterable whose items are all combined into a flattened result.
     */
    public <K2, V2> QueryGroup<K2, V2> groupByMany(Function<QueryMapping<K, V>, K2> keySelector, Function<QueryMapping<K, V>, ? extends Iterable<V2>> valueSelector) {
        return query().groupByMany(keySelector, valueSelector);
    }

    /**
     * Returns the query result as a QueryGroup with the key defined by the keySelector and the values defined by the
     * valueSelector.  The valueSelector must target an array whose items are all combined into a flattened result.
     */
    public <K2, V2> QueryGroup<K2, V2> groupByManyArray(Function<QueryMapping<K, V>, K2> keySelector, Function<QueryMapping<K, V>, V2[]> valueSelector) {
        return query().groupByManyArray(keySelector, valueSelector);
    }

    //
    // Quantifying Operators
    //

    /**
     * Returns true if there are any results, otherwise false.
     */
    public boolean any() {
        return query().any();
    }

    /**
     * Returns true if there are any results after applying the provided condition, otherwise false.
     */
    public boolean any(Predicate<QueryMapping<K, V>> condition) {
        return query().any(condition);
    }

    /**
     * Returns true if all results pass the provided condition, otherwise false.
     */
    public boolean all(Predicate<QueryMapping<K, V>> condition) {
        return query().all(condition);
    }

    /**
     * Return true if there are no results, otherwise false.
     */
    public boolean none() {
        return query().none();
    }

    /**
     * Returns true if none of the results pass the provided condition, otherwise false.
     */
    public boolean none(Predicate<QueryMapping<K, V>> condition) {
        return query().none(condition);
    }

    //
    // Aggregate operators
    //

    /**
     * Aggregates each item in the source according to the provided aggregation from a null seed.
     */
    public <A> A aggregate(BiFunction<A, QueryMapping<K, V>, A> aggregation) {
        return query().aggregate(aggregation);
    }

    /**
     * Aggregates each item in the source according to the provided aggregation from a provided seed.
     */
    public <A> A aggregate(A seed, BiFunction<A, QueryMapping<K, V>, A> aggregation) {
        return query().aggregate(seed, aggregation);
    }

    /**
     * Aggregates each item in the source according to the provided aggregation from a null seed.  The index of each
     * item in the source is included to be used in the aggregation.
     */
    public <A> A aggregateByIndex(TriFunction<A, QueryMapping<K, V>, Integer, A> aggregation) {
        return query().aggregateByIndex(aggregation);
    }

    /**
     * Aggregates each item in the source according to the provided aggregation from a provided seed.  The index of
     * each item in the source is included to be used in the aggregation.
     */
    public <A> A aggregateByIndex(A seed, TriFunction<A, QueryMapping<K, V>, Integer, A> aggregation) {
        return query().aggregateByIndex(seed, aggregation);
    }

    /**
     * Aggregates each item in the source according to the provided aggregation from a null seed.  The context of each
     * item in the source is included to be used in the aggregation.
     */
    public <A> A aggregateByContext(TriFunction<A, QueryMapping<K, V>, QueryContext<QueryMapping<K, V>>, A> aggregation) {
        return query().aggregateByContext(aggregation);
    }

    /**
     * Aggregates each item in the source according to the provided aggregation from a provided seed.  The context of
     * each item in the source is included to be used in the aggregation.
     */
    public <A> A aggregateByContext(A seed, TriFunction<A, QueryMapping<K, V>, QueryContext<QueryMapping<K, V>>, A> aggregation) {
        return query().aggregateByContext(seed, aggregation);
    }

    /**
     * Returns the sum of the non-null projections of the provided selector for each item in the result.
     */
    public BigDecimal sum(Function<QueryMapping<K, V>, ? extends Number> selector) {
        return query().sum(selector);
    }

    /**
     * Returns the average or mean of the non-null projections of the provided selector for each item in the result.
     * The default scale will be used for the result with HALF_UP rounding.
     */
    public BigDecimal average(Function<QueryMapping<K, V>, ? extends Number> selector) {
        return query().average(selector);
    }

    /**
     * Returns the average or mean of the non-null projections of the provided selector for each item in the result.
     * The provided scale and rounding will be used for the result or the default scale or HALF_UP rounding wherever
     * null is provided.
     */
    public BigDecimal average(Integer scale, RoundingMode roundingMode, Function<QueryMapping<K, V>, ? extends Number> selector) {
        return query().average(scale, roundingMode, selector);
    }

    /**
     * Returns the median of the non-null projections of the provided selector for each item in the result.  The
     * default scale will be used for the result with HALF_UP rounding.
     */
    public BigDecimal median(Function<QueryMapping<K, V>, ? extends Number> selector) {
        return query().median(selector);
    }

    /**
     * Returns the median of the non-null projections of the provided selector for each item in the result.  The
     * provided scale and rounding will be used for the result or the default scale or HALF_UP rounding wherever
     * null is provided.
     */
    public BigDecimal median(Integer scale, RoundingMode roundingMode, Function<QueryMapping<K, V>, ? extends Number> selector) {
        return query().median(scale, roundingMode, selector);
    }

    /**
     * Returns the lowest value of the non-null projections of the provided selector for each item in the result.
     */
    public BigDecimal min(Function<QueryMapping<K, V>, ? extends Number> selector) {
        return query().min(selector);
    }

    /**
     * Returns the highest value of the non-null projections of the provided selector for each item in the result.
     */
    public BigDecimal max(Function<QueryMapping<K, V>, ? extends Number> selector) {
        return query().max(selector);
    }

    @Override
    public Iterator<QueryMapping<K, V>> iterator() {
        return new Iterator<QueryMapping<K, V>>() {
            private Iterator<Map.Entry<K, V>> iterator = entrySet().iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public QueryMapping<K, V> next() {
                Map.Entry<K, V> entry = iterator.next();
                return new QueryMapping<K, V>(entry.getKey(), entry.getValue());
            }
        };
    }

    @Override
    public String toString() {
        return "QueryMap{size=" + size() + ", values=" + super.toString() + "}";
    }
}
