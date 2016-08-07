package com.simpkins.query;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.*;

/**
 * QueryGroup is an extension of LinkedHashMap with extra features including convenient access to many Query methods.
 * QueryGroup is a map where each key maps to a QueryList of values or Map<K, QueryList<V>>.
 */
@SuppressWarnings({"UnusedDeclaration", "Convert2Diamond"})
public class QueryGroup<K, V> extends LinkedHashMap<K, QueryList<V>> implements Iterable<QueryGrouping<K, V>> {
    
    public QueryGroup() {
    }

    public QueryGroup(int initialCapacity) {
        super(initialCapacity);
    }

    @SafeVarargs
    public QueryGroup(QueryGrouping<? extends K, ? extends V>... groupings) {
        super(groupings.length);
        for (QueryGrouping<? extends K, ? extends V> grouping : groupings)
            put(grouping.getKey(), new QueryList<V>(grouping));
    }

    // This constructor can be inefficient if the iterable is very large due to the inability to set an initial
    // capacity for the QueryGroup.
    public QueryGroup(Iterable<QueryGrouping<? extends K, ? extends V>> groupings) {
        for (QueryGrouping<? extends K, ? extends V> grouping : groupings)
            put(grouping.getKey(), new QueryList<V>(grouping));
    }

    public QueryGroup(Collection<QueryGrouping<? extends K, ? extends V>> groupings) {
        super(groupings.size());
        for (QueryGrouping<? extends K, ? extends V> grouping : groupings)
            put(grouping.getKey(), new QueryList<V>(grouping));
    }

    public QueryGroup(Map<? extends K, ? extends QueryList<? extends V>> items) {
        super(items.size());
        for (Map.Entry<? extends K, ? extends QueryList<? extends V>> entry : items.entrySet())
            put(entry.getKey(), new QueryList<V>(entry.getValue()));
    }

    public QueryGroup(Iterable<? extends V> items, Function<V, K> keySelector) {
        selectAndPut(items, keySelector, v -> v);
    }

    public <T> QueryGroup(Iterable<T> items, Function<T, K> keySelector, Function<T, V> valueSelector) {
        selectAndPut(items, keySelector, valueSelector);
    }

    private <T> void selectAndPut(Iterable<? extends T> items, Function<T, K> keySelector, Function<T, V> valueSelector) {
        for (T item : items) {
            K key = keySelector.apply(item);
            if (!containsKey(key))
                put(key, new QueryList<V>());
            get(key).add(valueSelector.apply(item));
        }
    }

    /**
     * A static constructor where the valueSelector projects an iterable of which each are combined into a flattened
     * result.
     */
    public static <T, K, V> QueryGroup<K, V> byMany(Iterable<T> items, Function<T, K> keySelector, Function<T, ? extends Iterable<V>> valueSelector) {
        QueryGroup<K, V> result = new QueryGroup<K, V>();
        for (T item : items) {
            K key = keySelector.apply(item);
            if (!result.containsKey(key))
                result.put(key, new QueryList<V>());
            result.get(key).addAll(valueSelector.apply(item));
        }
        return result;
    }

    /**
     * A static constructor where the valueSelector projects an array of which each are combined into a flattened
     * result.
     */
    public static <T, K, V> QueryGroup<K, V> byManyArray(Iterable<T> items, Function<T, K> keySelector, Function<T, V[]> valueSelector) {
        return byMany(items, keySelector, x -> Arrays.asList(valueSelector.apply(x)));
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
    public Query<QueryList<V>> valuesQuery() {
        return new Query<QueryList<V>>(super.values());
    }

    /**
     * Iterates through each values list to see if any contain the provided value.
     */
    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean containsValue(Object value) {
        for (K key : keySet())
            if (get(key).contains(value))
                return true;
        return false;
    }

    /**
     * Returns the QueryList for the provided key or an empty QueryList if not present.
     */
    @Override
    public QueryList<V> get(Object key) {
        QueryList<V> results = super.get(key);
        return results != null ? results : new QueryList<V>();
    }

    /**
     * Returns a Query of each QueryList of values for each provided key.
     */
    @SafeVarargs
    public final Query<QueryList<V>> get(K... keys) {
        return get(Arrays.asList(keys));
    }

    /**
     * Returns a Query of each QueryList of values for each provided key.
     */
    public Query<QueryList<V>> get(Iterable<? extends K> keys) {
        return Query.from(keys).select(x -> get(x));
    }

    /**
     * Returns a Query of each value in the QueryList of each provided key.
     */
    @SafeVarargs
    public final Query<V> getMany(K... keys) {
        return getMany(Arrays.asList(keys));
    }

    /**
     * Returns a Query of each value in the QueryList of each provided key.
     */
    public Query<V> getMany(Iterable<? extends K> keys) {
        return Query.from(keys).selectMany(x -> get(x));
    }

    //
    // Restriction Operators
    //

    private Function<QueryGrouping<K, V>, K> getKey = x -> x.getKey();
    private Function<QueryGrouping<K, V>, Iterable<V>> getValue = x -> x;

    /**
     * Filters the source down to only items where the provided condition is true.
     */
    public QueryGroup<K, V> where(Predicate<QueryGrouping<K, V>> condition) {
        return query().where(condition).groupByMany(getKey, getValue);
    }

    /**
     * Filters the source down to only items where the provided condition is true.  The index of each item in the
     * source is included to be used in the condition.
     */
    public QueryGroup<K, V> whereByIndex(BiPredicate<QueryGrouping<K, V>, Integer> condition) {
        return query().whereByIndex(condition).groupByMany(getKey, getValue);
    }

    /**
     * Filters the source down to only items where the provided condition is true.  The context of each item in the
     * source is included to be used in the condition.
     */
    public QueryGroup<K, V> whereByContext(BiPredicate<QueryGrouping<K, V>, QueryContext<QueryGrouping<K, V>>> condition) {
        return query().whereByContext(condition).groupByMany(getKey, getValue);
    }

    //
    // Ordering Operators
    //

    /**
     * Re-orders the source in ascending order based on the Comparable provided by the selector.  Null values are
     * placed last.  An OrderedQuery is returned which is an extension of Query with additional thenBy() methods that
     * allow for sub-ordering.
     */
    public <S extends Comparable<S>> QueryGroup<K, V> orderBy(Function<QueryGrouping<K, V>, S> selector) {
        return query().orderBy(selector).mapMany(getKey, getValue);
    }

    /**
     * Re-orders the source based on the provided comparator.  An OrderedQuery is returned which is an extension of
     * Query with additional thenBy() methods that allow for sub-ordering.
     */
    public QueryGroup<K, V> orderBy(Comparator<QueryGrouping<K, V>> comparator) {
        return query().orderBy(comparator).mapMany(getKey, getValue);
    }

    /**
     * Re-orders the source in descending order based on the Comparable provided by the selector.  Null values are
     * placed first.  An OrderedQuery is returned which is an extension of Query with additional thenBy() methods that
     * allow for sub-ordering.
     */
    public <S extends Comparable<S>> QueryGroup<K, V> orderByDescending(Function<QueryGrouping<K, V>, S> selector) {
        return query().orderByDescending(selector).mapMany(getKey, getValue);
    }

    /**
     * Re-orders the source based on an inversion of the provided comparator.  An OrderedQuery is returned which is an
     * extension of Query with additional thenBy() methods that allow for sub-ordering.
     */
    public QueryGroup<K, V> orderByDescending(Comparator<QueryGrouping<K, V>> comparator) {
        return query().orderByDescending(comparator).mapMany(getKey, getValue);
    }

    /**
     * Reverses the ordering of the source.
     */
    public QueryGroup<K, V> reverse() {
        return query().reverse().mapMany(getKey, getValue);
    }

    /**
     * Shuffles the ordering of the source.
     */
    public QueryGroup<K, V> shuffle() {
        return query().shuffle().mapMany(getKey, getValue);
    }

    //
    // Projection Operators
    //

    /**
     * Transforms the source to the projection defined by the provided selector.
     */
    public <S> Query<S> select(Function<QueryGrouping<K, V>, S> selector) {
        return query().select(selector);
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  The index of each item in the source
     * is included to be used in the selector.
     */
    public <S> Query<S> selectByIndex(BiFunction<QueryGrouping<K, V>, Integer, S> selector) {
        return query().selectByIndex(selector);
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  The context of each item in the source
     * is included to be used in the selector.
     */
    public <S> Query<S> selectByContext(BiFunction<QueryGrouping<K, V>, QueryContext<QueryGrouping<K, V>>, S> selector) {
        return query().selectByContext(selector);
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  Each projection is an iterable which
     * are all combined into a flattened result.
     */
    public <S> Query<S> selectMany(Function<QueryGrouping<K, V>, ? extends Iterable<S>> selector) {
        return query().selectMany(selector);
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  Each projection is an iterable which
     * are all combined into a flattened result.  The index of each item in the source is included to be used in the
     * selector.
     */
    public <S> Query<S> selectManyByIndex(BiFunction<QueryGrouping<K, V>, Integer, ? extends Iterable<S>> selector) {
        return query().selectManyByIndex(selector);
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  Each projection is an iterable which
     * are all combined into a flattened result.  The context of each item in the source is included to be used in the
     * selector.
     */
    public <S> Query<S> selectManyByContext(BiFunction<QueryGrouping<K, V>, QueryContext<QueryGrouping<K, V>>, ? extends Iterable<S>> selector) {
        return query().selectManyByContext(selector);
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  Each projection is an array which
     * are all combined into a flattened result.
     */
    public <S> Query<S> selectManyArray(Function<QueryGrouping<K, V>, S[]> selector) {
        return query().selectManyArray(selector);
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  Each projection is an array which
     * are all combined into a flattened result.  The index of each item in the source is included to be used in the
     * selector.
     */
    public <S> Query<S> selectManyArrayByIndex(BiFunction<QueryGrouping<K, V>, Integer, S[]> selector) {
        return query().selectManyArrayByIndex(selector);
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  Each projection is an array which
     * are all combined into a flattened result.  The context of each item in the source is included to be used in the
     * selector.
     */
    public <S> Query<S> selectManyArrayByContext(BiFunction<QueryGrouping<K, V>, QueryContext<QueryGrouping<K, V>>, S[]> selector) {
        return query().selectManyArrayByContext(selector);
    }

    /**
     * Groups and returns items from the source that share the same key obtained by the provided keySelector into a
     * QueryGrouping.  Unlike groupBy() this returns a new Query, not a QueryGroup, which provides deferred execution
     * but none of the underlying Map functionality of QueryGroup.
     */
    public <K2> Query<QueryGrouping<K2, QueryGrouping<K, V>>> selectGrouped(Function<QueryGrouping<K, V>, K2> keySelector) {
        return query().selectGrouped(keySelector);
    }

    /**
     * Groups and returns items from the source that share the same key obtained by the provided keySelector into a
     * QueryGrouping.  The items in the QueryGroup and transformed to the projection defined by the valueSelector.
     * Unlike groupBy() this returns a new Query, not a QueryGroup, which provides deferred execution but none of the
     * underlying Map functionality of QueryGroup.
     */
    public <K2, V2> Query<QueryGrouping<K2, V2>> selectGrouped(Function<QueryGrouping<K, V>, K2> keySelector, Function<QueryGrouping<K, V>, V2> valueSelector) {
        return query().selectGrouped(keySelector, valueSelector);
    }

    //
    // Application Operators
    //

    /**
     * Executes the provided action on each item in the source and then returns that item.
     */
    public QueryGroup<K, V> pipe(Consumer<QueryGrouping<K, V>> consumer) {
        return query().pipe(consumer).mapMany(getKey, getValue);
    }

    /**
     * Executes the provided action on each item in the source and then returns that item.  The index of each item in
     * the source is included to be used in the action.
     */
    public QueryGroup<K, V> pipeByIndex(BiConsumer<QueryGrouping<K, V>, Integer> consumer) {
        return query().pipeByIndex(consumer).mapMany(getKey, getValue);
    }

    /**
     * Executes the provided action on each item in the source.  The index of each item in the source is included to be
     * used in the action.
     */
    public void forEachByIndex(BiConsumer<QueryGrouping<K, V>, Integer> action) {
        query().forEachByIndex(action);
    }

    /**
     * Executes the provided action on each item in the source.  The context of each item in the source is included to be
     * used in the action.
     */
    public void forEachByContext(BiConsumer<QueryGrouping<K, V>, QueryContext<QueryGrouping<K, V>>> action) {
        query().forEachByContext(action);
    }

    //
    // Conversion Operators
    //

    /**
     * Wraps the QueryGroup in a Query.
     */
    public Query<QueryGrouping<K, V>> query() {
        return new Query<QueryGrouping<K, V>>(this);
    }

    /**
     * Returns the query result as a QueryMap with the key defined by the keySelector and the value the item itself.
     * Each key selected must be unique or the mapping will fail.
     */
    public <K2> QueryMap<K2, QueryGrouping<K, V>> map(Function<QueryGrouping<K, V>, K2> keySelector) {
        return query().map(keySelector);
    }

    /**
     * Returns the query result as a QueryMap with the key defined by the keySelector and the value defined by the
     * valueSelector.  Each key selected must be unique or the mapping will fail.
     */
    public <K2, V2> QueryMap<K2, V2> map(Function<QueryGrouping<K, V>, K2> keySelector, Function<QueryGrouping<K, V>, V2> valueSelector) {
        return query().map(keySelector, valueSelector);
    }

    /**
     * Returns the query result as a QueryGroup with the key defined by the keySelector and the values defined by the
     * valueSelector.  The valueSelector must target an iterable whose items are all combined into a flattened result.
     * Each key selected must be unique or the mapping will fail.
     */
    public <K2, V2> QueryGroup<K2, V2> mapMany(Function<QueryGrouping<K, V>, K2> keySelector, Function<QueryGrouping<K, V>, ? extends Iterable<V2>> valueSelector) {
        return query().mapMany(keySelector, valueSelector);
    }

    /**
     * Returns the query result as a QueryGroup with the key defined by the keySelector and the values defined by the
     * valueSelector.  The valueSelector must target an array whose items are all combined into a flattened result.
     * Each key selected must be unique or the mapping will fail.
     */
    public <K2, V2> QueryGroup<K2, V2> mapManyArray(Function<QueryGrouping<K, V>, K2> keySelector, Function<QueryGrouping<K, V>, V2[]> valueSelector) {
        return query().mapManyArray(keySelector, valueSelector);
    }

    /**
     * Returns the query result as a QueryGroup with the key defined by the keySelector and the values as the items
     * themselves.
     */
    public <K2> QueryGroup<K2, QueryGrouping<K, V>> groupBy(Function<QueryGrouping<K, V>, K2> keySelector) {
        return query().groupBy(keySelector);
    }

    /**
     * Returns the query result as a QueryGroup with the key defined by the keySelector and the values defined by the
     * valueSelector.
     */
    public <K2, V2> QueryGroup<K2, V2> groupBy(Function<QueryGrouping<K, V>, K2> keySelector, Function<QueryGrouping<K, V>, V2> valueSelector) {
        return query().groupBy(keySelector, valueSelector);
    }

    /**
     * Returns the query result as a QueryGroup with the key defined by the keySelector and the values defined by the
     * valueSelector.  The valueSelector must target an iterable whose items are all combined into a flattened result.
     */
    public <K2, V2> QueryGroup<K2, V2> groupByMany(Function<QueryGrouping<K, V>, K2> keySelector, Function<QueryGrouping<K, V>, ? extends Iterable<V2>> valueSelector) {
        return query().groupByMany(keySelector, valueSelector);
    }

    /**
     * Returns the query result as a QueryGroup with the key defined by the keySelector and the values defined by the
     * valueSelector.  The valueSelector must target an array whose items are all combined into a flattened result.
     */
    public <K2, V2> QueryGroup<K2, V2> groupByManyArray(Function<QueryGrouping<K, V>, K2> keySelector, Function<QueryGrouping<K, V>, V2[]> valueSelector) {
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
    public boolean any(Predicate<QueryGrouping<K, V>> condition) {
        return query().any(condition);
    }

    /**
     * Returns true if all results pass the provided condition, otherwise false.
     */
    public boolean all(Predicate<QueryGrouping<K, V>> condition) {
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
    public boolean none(Predicate<QueryGrouping<K, V>> condition) {
        return query().none(condition);
    }

    //
    // Aggregate operators
    //

    /**
     * Aggregates each item in the source according to the provided aggregation from a null seed.
     */
    public <A> A aggregate(BiFunction<A, QueryGrouping<K, V>, A> aggregation) {
        return query().aggregate(aggregation);
    }

    /**
     * Aggregates each item in the source according to the provided aggregation from a provided seed.
     */
    public <A> A aggregate(A seed, BiFunction<A, QueryGrouping<K, V>, A> aggregation) {
        return query().aggregate(seed, aggregation);
    }

    /**
     * Aggregates each item in the source according to the provided aggregation from a null seed.  The index of each
     * item in the source is included to be used in the aggregation.
     */
    public <A> A aggregateByIndex(TriFunction<A, QueryGrouping<K, V>, Integer, A> aggregation) {
        return query().aggregateByIndex(aggregation);
    }

    /**
     * Aggregates each item in the source according to the provided aggregation from a provided seed.  The index of
     * each item in the source is included to be used in the aggregation.
     */
    public <A> A aggregateByIndex(A seed, TriFunction<A, QueryGrouping<K, V>, Integer, A> aggregation) {
        return query().aggregateByIndex(seed, aggregation);
    }

    /**
     * Aggregates each item in the source according to the provided aggregation from a null seed.  The context of each
     * item in the source is included to be used in the aggregation.
     */
    public <A> A aggregateByContext(TriFunction<A, QueryGrouping<K, V>, QueryContext<QueryGrouping<K, V>>, A> aggregation) {
        return query().aggregateByContext(aggregation);
    }

    /**
     * Aggregates each item in the source according to the provided aggregation from a provided seed.  The context of
     * each item in the source is included to be used in the aggregation.
     */
    public <A> A aggregateByContext(A seed, TriFunction<A, QueryGrouping<K, V>, QueryContext<QueryGrouping<K, V>>, A> aggregation) {
        return query().aggregateByContext(seed, aggregation);
    }

    /**
     * Returns the sum of the non-null projections of the provided selector for each item in the result.
     */
    public BigDecimal sum(Function<QueryGrouping<K, V>, ? extends Number> selector) {
        return query().sum(selector);
    }

    /**
     * Returns the average or mean of the non-null projections of the provided selector for each item in the result.
     * The default scale will be used for the result with HALF_UP rounding.
     */
    public BigDecimal average(Function<QueryGrouping<K, V>, ? extends Number> selector) {
        return query().average(selector);
    }

    /**
     * Returns the average or mean of the non-null projections of the provided selector for each item in the result.
     * The provided scale and rounding will be used for the result or the default scale or HALF_UP rounding wherever
     * null is provided.
     */
    public BigDecimal average(Integer scale, RoundingMode roundingMode, Function<QueryGrouping<K, V>, ? extends Number> selector) {
        return query().average(scale, roundingMode, selector);
    }

    /**
     * Returns the median of the non-null projections of the provided selector for each item in the result.  The
     * default scale will be used for the result with HALF_UP rounding.
     */
    public BigDecimal median(Function<QueryGrouping<K, V>, ? extends Number> selector) {
        return query().median(selector);
    }

    /**
     * Returns the median of the non-null projections of the provided selector for each item in the result.  The
     * provided scale and rounding will be used for the result or the default scale or HALF_UP rounding wherever
     * null is provided.
     */
    public BigDecimal median(Integer scale, RoundingMode roundingMode, Function<QueryGrouping<K, V>, ? extends Number> selector) {
        return query().median(scale, roundingMode, selector);
    }

    /**
     * Returns the lowest value of the non-null projections of the provided selector for each item in the result.
     */
    public BigDecimal min(Function<QueryGrouping<K, V>, ? extends Number> selector) {
        return query().min(selector);
    }

    /**
     * Returns the highest value of the non-null projections of the provided selector for each item in the result.
     */
    public BigDecimal max(Function<QueryGrouping<K, V>, ? extends Number> selector) {
        return query().max(selector);
    }

    @Override
    public Iterator<QueryGrouping<K, V>> iterator() {
        return new Iterator<QueryGrouping<K, V>>() {
            private Iterator<Map.Entry<K, QueryList<V>>> iterator = entrySet().iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public QueryGrouping<K, V> next() {
                Map.Entry<K, QueryList<V>> entry = iterator.next();
                return new QueryGrouping<K, V>(entry.getKey(), entry.getValue());
            }
        };
    }

    @Override
    public String toString() {
        return "QueryGroup{size=" + size() + ", values={" + query().select(x -> x != null ? x.getKey() + "=[" + x.toString(", ") + "]" : "null").toString(", ") + "}";
    }
}
