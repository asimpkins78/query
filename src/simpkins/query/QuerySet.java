package simpkins.query;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.*;

/**
 * QuerySet is an extension of LinkedHashSet with extra features including convenient access to most Query methods.
 */
@SuppressWarnings({"UnusedDeclaration", "Convert2Diamond"})
public class QuerySet<T> extends LinkedHashSet<T> {

    //
    // Constructors
    //

    public QuerySet() {
    }

    @SafeVarargs
    public QuerySet(T... items) {
        super(items.length);
        addAll(Arrays.asList(items));
    }

    // This constructor can be inefficient if the iterable is very large due to the inability to set an initial
    // capacity for the QuerySet.
    public QuerySet(Iterable<? extends T> items) {
        addAll(items);
    }

    public QuerySet(Collection<? extends T> items) {
        super(items);
    }

    // Java params doesn't work well with primitive arrays, so there is a specific constructor for each.

    public QuerySet(boolean[] items) {
        super(items.length);
        for (Boolean item : items)
            //noinspection unchecked
            add((T)item);
    }

    public QuerySet(byte[] items) {
        super(items.length);
        for (Byte item : items)
            //noinspection unchecked
            add((T)item);
    }

    public QuerySet(short[] items) {
        super(items.length);
        for (Short item : items)
            //noinspection unchecked
            add((T)item);
    }

    public QuerySet(int[] items) {
        super(items.length);
        for (Integer item : items)
            //noinspection unchecked
            add((T)item);
    }

    public QuerySet(long[] items) {
        super(items.length);
        for (Long item : items)
            //noinspection unchecked
            add((T)item);
    }

    public QuerySet(float[] items) {
        super(items.length);
        for (Float item : items)
            //noinspection unchecked
            add((T)item);
    }

    public QuerySet(double[] items) {
        super(items.length);
        for (Double item : items)
            //noinspection unchecked
            add((T)item);
    }

    public QuerySet(char[] items) {
        super(items.length);
        for (Character item : items)
            //noinspection unchecked
            add((T)item);
    }

    //
    // Static Constructors
    //

    @SafeVarargs
    public static <T> QuerySet<T> of(T... items) {
        return new QuerySet<T>(items);
    }

    // This constructor can be inefficient if the iterable is very large due to the inability to set an initial
    // capacity for the QuerySet.
    public static <T> QuerySet<T> of(Iterable<? extends T> source) {
        return new QuerySet<T>(source);
    }

    public static <T> QuerySet<T> of(Collection<? extends T> source) {
        return new QuerySet<T>(source);
    }

    // Java params doesn't work well with primitive arrays, so there is a specific constructor for each.

    public static QuerySet<Boolean> of(boolean[] items) {
        return new QuerySet<Boolean>(items);
    }

    public static QuerySet<Byte> of(byte[] items) {
        return new QuerySet<Byte>(items);
    }

    public static QuerySet<Short> of(short[] items) {
        return new QuerySet<Short>(items);
    }

    public static QuerySet<Integer> of(int[] items) {
        return new QuerySet<Integer>(items);
    }

    public static QuerySet<Long> of(long[] items) {
        return new QuerySet<Long>(items);
    }

    public static QuerySet<Float> of(float[] items) {
        return new QuerySet<Float>(items);
    }

    public static QuerySet<Double> of(double[] items) {
        return new QuerySet<Double>(items);
    }

    public static QuerySet<Character> of(char[] items) {
        return new QuerySet<Character>(items);
    }

    // Uses reflection to set the initial capacity.  This is a work around due to the problem of having a params
    // constructor for an Integer typed QuerySet.
    public static <T> QuerySet<T> initialCapacity(int value) {
        if (value < 0)
            throw new IllegalArgumentException("Illegal Capacity: " + value);
        QuerySet<T> set = new QuerySet<T>();
        try {
            Field map = set.getClass().getSuperclass().getSuperclass().getDeclaredField("map");
            map.setAccessible(true);
            map.set(set, new LinkedHashMap<>(value, .75f));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return set;
    }

    //
    // Extra Set Methods
    //

    /**
     * Overload of the addAll() that takes a params array.
     */
    @SafeVarargs
    public final boolean addAll(T... items) {
        return addAll(Arrays.asList(items));
    }

    /**
     * Overload of the addAll() that takes an iterable.
     */
    public boolean addAll(Iterable<? extends T> items) {
        return addAll(Query.from(items).toList());
    }

    /**
     * Returns this QuerySet if not empty or the alternates if empty.
     */
    @SafeVarargs
    public final QuerySet<T> orIfEmpty(T... alternates) {
        return orIfEmpty(Arrays.asList(alternates));
    }

    /**
     * Returns this QuerySet if not empty or the alternates if empty.
     */
    public QuerySet<T> orIfEmpty(Iterable<? extends T> alternates) {
        if (!isEmpty())
            return this;
        Integer size = Query.findSize(alternates);
        QuerySet<T> set = size != null
                ? QuerySet.initialCapacity(((Collection)alternates).size())
                : new QuerySet<T>();
        set.addAll(alternates);
        return set;
    }

    //
    // Restriction Operators
    //

    /**
     * Filters the source down to only items where the provided condition is true.
     */
    public Query<T> where(Predicate<T> condition) {
        return query().where(condition);
    }

    /**
     * Filters the source down to only items where the provided condition is true.  The index of each item in the
     * source is included to be used in the condition.
     */
    public Query<T> whereByIndex(BiPredicate<T, Integer> condition) {
        return query().whereByIndex(condition);
    }

    /**
     * Filters the source down to only items where the provided condition is true.  The context of each item in the
     * source is included to be used in the condition.
     */
    public Query<T> whereByContext(BiPredicate<T, QueryContext<T>> condition) {
        return query().whereByContext(condition);
    }

    /**
     * Filters the source down to only items that equal one of the provided items.
     */
    @SafeVarargs
    public final Query<T> whereIn(T... items) {
        return query().whereIn(items);
    }

    /**
     * Filters the source down to only items that equal one of the provided items.
     */
    public Query<T> whereIn(Iterable<? extends T> container) {
        return query().whereIn(container);
    }

    /**
     * Filters the source down to only items that do not equal one of the provided items.
     */
    @SafeVarargs
    public final Query<T> whereNotIn(T... items) {
        return query().whereNotIn(items);
    }

    /**
     * Filters the source down to only items that do not equal one of the provided items.
     */
    public Query<T> whereNotIn(Iterable<? extends T> container) {
        return query().whereNotIn(container);
    }

    /**
     * Filters the source down only items that can be cast to the provided type and then casts them to that type.
     */
    public <S> Query<S> ofType(Class<S> type) {
        return query().ofType(type);
    }

    //
    // Partitioning Operators
    //

    /**
     * Discards a number of items from the beginning of the source as defined by the amount parameter.  The amount
     * must not be a negative number.  An amount greater than the size of the source will return an empty Query.
     */
    public Query<T> skip(int amount) {
        return query().skip(amount);
    }

    /**
     * Discards a number of items from the beginning of the source until an item is found where the given condition is
     * not true.  That item and all subsequent items (if any) are retained.
     */
    public Query<T> skipWhile(Predicate<T> condition) {
        return query().skipWhile(condition);
    }

    /**
     * Discards a number of items from the beginning of the source until an item is found where the given condition is
     * not true.  That item and all subsequent items (if any) are retained.  The index of each item in the source is
     * included to be used in the condition.
     */
    public Query<T> skipWhileByIndex(BiPredicate<T, Integer> condition) {
        return query().skipWhileByIndex(condition);
    }

    /**
     * Discards a number of items from the beginning of the source until an item is found where the given condition is
     * not true.  That item and all subsequent items (if any) are retained.  The context of each item in the source is
     * included to be used in the condition.
     */
    public Query<T> skipWhileByContext(BiPredicate<T, QueryContext<T>> condition) {
        return query().skipWhileByContext(condition);
    }

    /**
     * Retains a number of items from the beginning of the source as defined by the amount parameter.  All subsequent
     * items are discarded.  The amount must not be a negative number.  An amount greater than the size of the source
     * will return an identical Query.
     */
    public Query<T> take(int amount) {
        return query().take(amount);
    }

    /**
     * Retains a number of items from the beginning of the source until an item is found where the given condition is
     * not true.  That item and all subsequent items (if any) are discarded.
     */
    public Query<T> takeWhile(Predicate<T> condition) {
        return query().takeWhile(condition);
    }

    /**
     * Retains a number of items from the beginning of the source until an item is found where the given condition is
     * not true.  That item and all subsequent items (if any) are discarded.  The index of each item in the source is
     * included to be used in the condition.
     */
    public Query<T> takeWhileByIndex(BiPredicate<T, Integer> condition) {
        return query().takeWhileByIndex(condition);
    }

    /**
     * Retains a number of items from the beginning of the source until an item is found where the given condition is
     * not true.  That item and all subsequent items (if any) are discarded.  The context of each item in the source is
     * included to be used in the condition.
     */
    public Query<T> takeWhileByContext(BiPredicate<T, QueryContext<T>> condition) {
        return query().takeWhileByContext(condition);
    }

    /**
     * Discards a number of items defined by the amount beginning at the startIndex.  Items before or after this range
     * (if any) are retained.  The startIndex and amount parameters cannot be negative.  An amount of 0 will cause no
     * changes.  Any part of the range that falls outside the length of the source will be ignored.
     */
    public Query<T> exclude(int startIndex, int amount) {
        return query().exclude(startIndex, amount);
    }

    /**
     * Discards a number of items at or between the startIndex and endIndex.  Items before or after this range (if any)
     * are retained.  The startIndex and endIndex parameters cannot be negative, and the endIndex cannot be less than
     * the startIndex.  Any part of the range that falls outside the length of the source will be ignored.
     */
    public Query<T> excludeBetween(int startIndex, int endIndex) {
        return query().exclude(startIndex, endIndex);
    }

    //
    // Ordering Operators
    //

    /**
     * Re-orders the source in ascending order based on T as a Comparable.  If T is not a Comparable then a dummy
     * Comparable is used and source is only re-ordered with nulls last.  An OrderedQuery is returned which is an
     * extension of Query with additional thenBy() methods that allow for sub-ordering.
     */
    public OrderedQuery<T> order() {
        return query().order();
    }

    /**
     * Re-orders the source in ascending order based on T as a Comparable.  If T is not a Comparable then a dummy
     * Comparable is used and source is only re-ordered with nulls first.  An OrderedQuery is returned which is an
     * extension of Query with additional thenBy() methods that allow for sub-ordering.
     */
    public OrderedQuery<T> orderNullsFirst() {
        return query().orderNullsFirst();
    }

    /**
     * Re-orders the source in descending order based on T as a Comparable.  If T is not a Comparable then a dummy
     * Comparable is used and source is only re-ordered with nulls first.  An OrderedQuery is returned which is an
     * extension of Query with additional thenBy() methods that allow for sub-ordering.
     */
    public OrderedQuery<T> orderDescending() {
        return query().orderDescending();
    }

    /**
     * Re-orders the source in descending order based on T as a Comparable.  If T is not a Comparable then a dummy
     * Comparable is used and source is only re-ordered with nulls last.  An OrderedQuery is returned which is an
     * extension of Query with additional thenBy() methods that allow for sub-ordering.
     */
    public OrderedQuery<T> orderDescendingNullsLast() {
        return query().orderDescendingNullsLast();
    }

    /**
     * Re-orders the source in ascending order based on the Comparable provided by the selector.  Null values are
     * placed last.  An OrderedQuery is returned which is an extension of Query with additional thenBy() methods that
     * allow for sub-ordering.
     */
    public <S extends Comparable<S>> OrderedQuery<T> orderBy(Function<T, S> selector) {
        return query().orderBy(selector);
    }

    /**
     * Re-orders the source in ascending order based on the Comparable provided by the selector.  Null values are
     * placed first.  An OrderedQuery is returned which is an extension of Query with additional thenBy() methods that
     * allow for sub-ordering.
     */
    public <S extends Comparable<S>> OrderedQuery<T> orderByNullsFirst(Function<T, S> selector) {
        return query().orderByNullsFirst(selector);
    }

    /**
     * Re-orders the source based on the provided comparator.  An OrderedQuery is returned which is an extension of
     * Query with additional thenBy() methods that allow for sub-ordering.
     */
    public OrderedQuery<T> orderBy(Comparator<T> comparator) {
        return query().orderBy(comparator);
    }

    /**
     * Re-orders the source in descending order based on the Comparable provided by the selector.  Null values are
     * placed first.  An OrderedQuery is returned which is an extension of Query with additional thenBy() methods that
     * allow for sub-ordering.
     */
    public <S extends Comparable<S>> OrderedQuery<T> orderByDescending(Function<T, S> selector) {
        return query().orderByDescending(selector);
    }

    /**
     * Re-orders the source in descending order based on the Comparable provided by the selector.  Null values are
     * placed last.  An OrderedQuery is returned which is an extension of Query with additional thenBy() methods that
     * allow for sub-ordering.
     */
    public <S extends Comparable<S>> OrderedQuery<T> orderByDescendingNullsLast(Function<T, S> selector) {
        return query().orderByDescendingNullsLast(selector);
    }

    /**
     * Re-orders the source based on an inversion of the provided comparator.  An OrderedQuery is returned which is an
     * extension of Query with additional thenBy() methods that allow for sub-ordering.
     */
    public OrderedQuery<T> orderByDescending(Comparator<T> comparator) {
        return query().orderByDescending(comparator);
    }

    /**
     * Reverses the ordering of the source.
     */
    public Query<T> reverse() {
        return query().reverse();
    }

    /**
     * Shuffles the ordering of the source.
     */
    public Query<T> shuffle() {
        return query().shuffle();
    }

    //
    // Combining Operators
    //

    /**
     * Appends the provided additions at the end of the source.
     */
    @SafeVarargs
    public final Query<T> combine(T... additions) {
        return query().combine(additions);
    }

    /**
     * Appends the provided additions at the end of the source.
     */
    public Query<T> combine(Iterable<? extends T> additions) {
        return query().combine(additions);
    }

    /**
     * Appends the source at the end of the provided target.
     */
    @SafeVarargs
    public final Query<T> combineAfter(T... target) {
        return query().combineAfter(target);
    }

    /**
     * Appends the source at the end of the provided target.
     */
    public Query<T> combineAfter(Iterable<? extends T> target) {
        return query().combineAfter(target);
    }

    /**
     * Combines the source with the provided insertions with the insertions inserted into the source at the provided
     * insertIndex.  The insertIndex cannot be negative.  If the insertIndex is outside the scope of the source then
     * the insertions will appended immediately after the source.
     */
    @SafeVarargs
    public final Query<T> insert(int insertIndex, T... insertions) {
        return query().insert(insertIndex, insertions);
    }

    /**
     * Combines the source with the provided insertions with the insertions inserted into the source at the provided
     * insertIndex.  The insertIndex cannot be negative.  If the insertIndex is outside the scope of the source then
     * the insertions will appended immediately after the source.
     */
    public Query<T> insert(int insertIndex, Iterable<? extends T> insertions) {
        return query().insert(insertIndex, insertions);
    }

    /**
     * Combines the source with the provided target with the source inserted into the target at the provided
     * insertIndex.  The insertIndex cannot be negative.  If the insertIndex is outside the scope of the target then
     * the source will appended immediately after the target.
     */
    @SafeVarargs
    public final Query<T> insertInto(int insertIndex, T... target) {
        return query().insertInto(insertIndex, target);
    }

    /**
     * Combines the source with the provided target with the source inserted into the target at the provided
     * insertIndex.  The insertIndex cannot be negative.  If the insertIndex is outside the scope of the target then
     * the source will appended immediately after the target.
     */
    public Query<T> insertInto(int insertIndex, Iterable<? extends T> target) {
        return query().insertInto(insertIndex, target);
    }

    //
    // Set Operators
    //

    /**
     * Filters the source down to a set that are distinct according to T's equals() implementation.
     */
    public Query<T> distinct() {
        return query().distinct();
    }

    /**
     * Filters the source down to a set that are distinct according to the results of the provided selector.
     */
    public <S> Query<T> distinct(Function<T, S> selector) {
        return query().distinct(selector);
    }

    /**
     * Filters the source down to a set that are distinct according to the provided matcher.
     */
    public Query<T> distinct(BiPredicate<T, T> matcher) {
        return query().distinct(matcher);
    }

    /**
     * Filters the source down to a set that are distinct and not present in the provided exceptions according to T's
     * equals() implementation.
     */
    @SafeVarargs
    public final Query<T> except(T... exceptions) {
        return query().except(exceptions);
    }

    /**
     * Filters the source down to a set that are distinct and not present in the provided exceptions according to T's
     * equals() implementation.
     */
    public Query<T> except(Iterable<? extends T> exceptions) {
        return query().except(exceptions);
    }

    /**
     * Filters the source down to a set that are distinct and not present in the provided exceptions according to the
     * results of the provided selector.
     */
    public <S> Query<T> except(Iterable<? extends T> exceptions, Function<T, S> selector) {
        return query().except(exceptions, selector);
    }

    /**
     * Filters the source down to a set that are distinct and not present in the provided exceptions according to the
     * provided matcher.
     */
    public Query<T> except(Iterable<? extends T> exceptions, BiPredicate<T, T> matcher) {
        return query().except(exceptions, matcher);
    }

    /**
     * Filters the source down to a set that are distinct and present in the provided intersections according to T's
     * equals() implementation.
     */
    @SafeVarargs
    public final Query<T> intersect(T... intersections) {
        return query().intersect(intersections);
    }

    /**
     * Filters the source down to a set that are distinct and present in the provided intersections according to T's
     * equals() implementation.
     */
    public Query<T> intersect(Iterable<? extends T> intersections) {
        return query().intersect(intersections);
    }

    /**
     * Filters the source down to a set that are distinct and present in the provided intersections according to the
     * results of the provided selector.
     */
    public <S> Query<T> intersect(Iterable<? extends T> intersections, Function<T, S> selector) {
        return query().intersect(intersections, selector);
    }

    /**
     * Filters the source down to a set that are distinct and present in the provided intersections according to the
     * provided matcher.
     */
    public Query<T> intersect(Iterable<? extends T> intersections, BiPredicate<T, T> matcher) {
        return query().intersect(intersections, matcher);
    }

    /**
     * Combines the source with the unions and filters them down to a set that are distinct according to T's equals
     * implementation.
     */
    @SafeVarargs
    public final Query<T> union(T... unions) {
        return query().union(unions);
    }

    /**
     * Combines the source with the unions and filters them down to a set that are distinct according to T's equals
     * implementation.
     */
    public Query<T> union(Iterable<? extends T> unions) {
        return query().union(unions);
    }

    /**
     * Combines the source with the unions and filters them down to a set that are distinct according to the results
     * of the provided selector.
     */
    public <S> Query<T> union(Iterable<? extends T> unions, Function<T, S> selector) {
        return query().union(unions, selector);
    }

    /**
     * Combines the source with the unions and filters them down to a set that are distinct according to the
     * provided matcher.
     */
    public Query<T> union(Iterable<? extends T> unions, BiPredicate<T, T> matcher) {
        return query().union(unions, matcher);
    }

    //
    // Projection Operators
    //

    /**
     * Transforms the source to the projection defined by the provided selector.
     */
    public <S> Query<S> select(Function<T, S> selector) {
        return query().select(selector);
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  The index of each item in the source
     * is included to be used in the selector.
     */
    public <S> Query<S> selectByIndex(BiFunction<T, Integer, S> selector) {
        return query().selectByIndex(selector);
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  The context of each item in the source
     * is included to be used in the selector.
     */
    public <S> Query<S> selectByContext(BiFunction<T, QueryContext<T>, S> selector) {
        return query().selectByContext(selector);
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  Each projection is an iterable which
     * are all combined into a flattened result.
     */
    public <S> Query<S> selectMany(Function<T, ? extends Iterable<S>> selector) {
        return query().selectMany(selector);
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  Each projection is an iterable which
     * are all combined into a flattened result.  The index of each item in the source is included to be used in the
     * selector.
     */
    public <S> Query<S> selectManyByIndex(BiFunction<T, Integer, ? extends Iterable<S>> selector) {
        return query().selectManyByIndex(selector);
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  Each projection is an iterable which
     * are all combined into a flattened result.  The context of each item in the source is included to be used in the
     * selector.
     */
    public <S> Query<S> selectManyByContext(BiFunction<T, QueryContext<T>, ? extends Iterable<S>> selector) {
        return query().selectManyByContext(selector);
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  Each projection is an array which
     * are all combined into a flattened result.
     */
    public <S> Query<S> selectManyArray(Function<T, S[]> selector) {
        return query().selectManyArray(selector);
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  Each projection is an array which
     * are all combined into a flattened result.  The index of each item in the source is included to be used in the
     * selector.
     */
    public <S> Query<S> selectManyArrayByIndex(BiFunction<T, Integer, S[]> selector) {
        return query().selectManyArrayByIndex(selector);
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  Each projection is an array which
     * are all combined into a flattened result.  The context of each item in the source is included to be used in the
     * selector.
     */
    public <S> Query<S> selectManyArrayByContext(BiFunction<T, QueryContext<T>, S[]> selector) {
        return query().selectManyArrayByContext(selector);
    }

    /**
     * Groups and returns items from the source that share the same key obtained by the provided keySelector into a
     * QueryGrouping.  Unlike groupBy() this returns a new Query, not a QueryGroup, which provides deferred execution
     * but none of the underlying Map functionality of QueryGroup.
     */
    public <K> Query<QueryGrouping<K, T>> selectGrouped(Function<T, K> keySelector) {
        return query().selectGrouped(keySelector);
    }

    /**
     * Groups and returns items from the source that share the same key obtained by the provided keySelector into a
     * QueryGrouping.  The items in the QueryGroup and transformed to the projection defined by the valueSelector.
     * Unlike groupBy() this returns a new Query, not a QueryGroup, which provides deferred execution but none of the
     * underlying Map functionality of QueryGroup.
     */
    public <K, V> Query<QueryGrouping<K, V>> selectGrouped(Function<T, K> keySelector, Function<T, V> valueSelector) {
        return query().selectGrouped(keySelector, valueSelector);
    }

    /**
     * Casts each item in the source to the provided type.
     */
    public <S> Query<S> cast(Class<S> type) {
        return query().cast(type);
    }

    //
    // Application Operators
    //

    /**
     * Executes the provided action on each item in the source and then returns that item.
     */
    public Query<T> pipe(Consumer<T> action) {
        return query().pipe(action);
    }

    /**
     * Executes the provided action on each item in the source and then returns that item.  The index of each item in
     * the source is included to be used in the action.
     */
    public Query<T> pipeByIndex(BiConsumer<T, Integer> action) {
        return query().pipeByIndex(action);
    }

    /**
     * Executes the provided action on each item in the source.  The index of each item in the source is included to be
     * used in the action.
     */
    public void forEachByIndex(BiConsumer<T, Integer> action) {
        query().forEachByIndex(action);
    }

    /**
     * Executes the provided action on each item in the source.  The context of each item in the source is included to be
     * used in the action.
     */
    public void forEachByContext(BiConsumer<T, QueryContext<T>> action) {
        query().forEachByContext(action);
    }

    //
    // Join Operators
    //

    /**
     * Inner joins the source as left with the provided rightItems into a Tuple wherever the projection of a left
     * item's leftSelector equals the projection of right item's rightSelector.
     */
    public <R, K> Query<Tuple<T, R>> join(Iterable<R> rightItems, Function<T, K> leftSelector, Function<R, K> rightSelector) {
        return query().join(rightItems, leftSelector, rightSelector);
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into a Tuple wherever the
     * projection of a left item's leftSelector equals the projection of a right item's rightSelector.
     */
    public <R, K> Query<Tuple<T, R>> join(JoinType joinType, Iterable<R> rightItems, Function<T, K> leftSelector, Function<R, K> rightSelector) {
        return query().join(joinType, rightItems, leftSelector, rightSelector);
    }

    /**
     * Inner joins the source as left with the provided rightItems into a Tuple wherever item's from either source
     * match by the provided matcher.
     */
    public <R> Query<Tuple<T, R>> join(Iterable<R> rightItems, BiPredicate<T, R> matcher) {
        return query().join(rightItems, matcher);
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into a Tuple wherever
     * item's from either source match by the provided matcher.
     */
    public <R> Query<Tuple<T, R>> join(JoinType joinType, Iterable<R> rightItems, BiPredicate<T, R> matcher) {
        return query().join(joinType, rightItems, matcher);
    }

    /**
     * Inner joins the source as left with the provided rightItems into the joiner projection wherever the projection
     * of a left item's leftSelector equals the projection of right item's rightSelector.
     */
    public <K, R, S> Query<S> join(Iterable<R> rightItems, Function<T, K> leftSelector, Function<R, K> rightSelector, BiFunction<T, R, S> joiner) {
        return query().join(rightItems, leftSelector, rightSelector, joiner);
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into the joiner projection
     * wherever the projection of a left item's leftSelector equals the projection of right item's rightSelector.
     */
    public <K, R, S> Query<S> join(JoinType joinType, Iterable<R> rightItems, Function<T, K> leftSelector, Function<R, K> rightSelector, BiFunction<T, R, S> joiner) {
        return query().join(joinType, rightItems, leftSelector, rightSelector, joiner);
    }

    /**
     * Inner joins the source as left with the provided rightItems into the joiner projection wherever item's from
     * either source match by the provided matcher.
     */
    public <R, S> Query<S> join(Iterable<R> rightItems, BiPredicate<T, R> matcher, BiFunction<T, R, S> joiner) {
        return query().join(rightItems, matcher, joiner);
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into the joiner projection
     * wherever item's from either source match by the provided matcher.
     */
    public <R, S> Query<S> join(JoinType joinType, Iterable<R> rightItems, BiPredicate<T, R> matcher, BiFunction<T, R, S> joiner) {
        return query().join(joinType, rightItems, matcher, joiner);
    }

    /**
     * Inner joins the source as left with the provided rightItems into a Tuple wherever the projection of a left
     * item's leftSelector equals the projection of right item's rightSelector.  Joined right items are grouped in a
     * QueryList for each left item.
     */
    public <R, K> Query<Tuple<T, QueryList<R>>> groupJoin(Iterable<R> rightItems, Function<T, K> leftSelector, Function<R, K> rightSelector) {
        return query().groupJoin(rightItems, leftSelector, rightSelector);
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into a Tuple wherever the
     * projection of a left item's leftSelector equals the projection of a right item's rightSelector.  Joined right
     * items are grouped in a QueryList for each left item.
     */
    public <R, K> Query<Tuple<T, QueryList<R>>> groupJoin(JoinType joinType, Iterable<R> rightItems, Function<T, K> leftSelector, Function<R, K> rightSelector) {
        return query().groupJoin(joinType, rightItems, leftSelector, rightSelector);
    }

    /**
     * Inner joins the source as left with the provided rightItems into a Tuple wherever item's from either source
     * match by the provided matcher.  Joined right items are grouped in a QueryList for each left item.
     */
    public <R> Query<Tuple<T, QueryList<R>>> groupJoin(Iterable<R> rightItems, BiPredicate<T, R> matcher) {
        return query().groupJoin(rightItems, matcher);
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into a Tuple wherever
     * item's from either source match by the provided matcher.  Joined right items are grouped in a QueryList for each
     * left item.
     */
    public <R> Query<Tuple<T, QueryList<R>>> groupJoin(JoinType joinType, Iterable<R> rightItems, BiPredicate<T, R> matcher) {
        return query().groupJoin(joinType, rightItems, matcher);
    }

    /**
     * Inner joins the source as left with the provided rightItems into the joiner projection wherever the projection
     * of a left item's leftSelector equals the projection of right item's rightSelector.  Joined right items are
     * grouped in a QueryList for each left item.
     */
    public <K, R, S> Query<S> groupJoin(Iterable<R> rightItems, Function<T, K> leftSelector, Function<R, K> rightSelector, BiFunction<T, QueryList<R>, S> joiner) {
        return query().groupJoin(rightItems, leftSelector, rightSelector, joiner);
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into the joiner projection
     * wherever the projection of a left item's leftSelector equals the projection of right item's rightSelector.
     * Joined right items are grouped in a QueryList for each left item.
     */
    public <K, R, S> Query<S> groupJoin(JoinType joinType, Iterable<R> rightItems, Function<T, K> leftSelector, Function<R, K> rightSelector, BiFunction<T, QueryList<R>, S> joiner) {
        return query().groupJoin(joinType, rightItems, leftSelector, rightSelector, joiner);
    }

    /**
     * Inner joins the source as left with the provided rightItems into the joiner projection wherever item's from
     * either source match by the provided matcher.  Joined right items are grouped in a QueryList for each left item.
     */
    public <R, S> Query<S> groupJoin(Iterable<R> rightItems, BiPredicate<T, R> matcher, BiFunction<T, QueryList<R>, S> joiner) {
        return query().groupJoin(rightItems, matcher, joiner);
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into the joiner projection
     * wherever item's from either source match by the provided matcher.  Joined right items are grouped in a QueryList
     * for each left item.
     */
    public <R, S> Query<S> groupJoin(JoinType joinType, Iterable<R> rightItems, BiPredicate<T, R> matcher, BiFunction<T, QueryList<R>, S> joiner) {
        return query().groupJoin(joinType, rightItems, matcher, joiner);
    }

    /**
     * Inner joins the source as left with the provided rightItems into a Tuple where the left and right items share
     * the same index.
     */
    @SafeVarargs
    public final <R> Query<Tuple<T, R>> zip(R... rightItems) {
        return query().zip(rightItems);
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into a Tuple where the
     * left and right items share the same index.
     */
    @SafeVarargs
    public final <R> Query<Tuple<T, R>> zip(JoinType joinType, R... rightItems) {
        return query().zip(joinType, rightItems);
    }

    /**
     * Inner joins the source as left with the provided rightItems into a Tuple where the left and right items share
     * the same index.
     */
    public <R> Query<Tuple<T, R>> zip(Iterable<R> rightItems) {
        return query().zip(rightItems);
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into a Tuple where the
     * left and right items share the same index.
     */
    public <R> Query<Tuple<T, R>> zip(JoinType joinType, Iterable<R> rightItems) {
        return query().zip(joinType, rightItems);
    }

    /**
     * Inner joins the source as left with the provided rightItems into the zipper projection where the left and right
     * items share the same index.
     */
    public <R, S> Query<S> zip(Iterable<R> rightItems, BiFunction<T, R, S> zipper) {
        return query().zip(rightItems, zipper);
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into the zipper projection
     * where the left and right items share the same index.
     */
    public <R, S> Query<S> zip(JoinType joinType, Iterable<R> rightItems, BiFunction<T, R, S> zipper) {
        return query().zip(joinType, rightItems, zipper);
    }

    /**
     * Inner joins the source as left with the provided rightItems into the zipper projection where the left and right
     * items share the same index.  The index of each join is included to be used in the zipper.
     */
    public <R, S> Query<S> zipByIndex(Iterable<R> rightItems, TriFunction<T, R, Integer, S> zipper) {
        return query().zipByIndex(rightItems, zipper);
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into the zipper projection
     * where the left and right items share the same index.  The index of each join is included to be used in the
     * zipper.
     */
    public <R, S> Query<S> zipByIndex(JoinType joinType, Iterable<R> rightItems, TriFunction<T, R, Integer, S> zipper) {
        return query().zipByIndex(joinType, rightItems, zipper);
    }

    /**
     * Inner joins the source as left with the provided rightItems into the zipper projection where the left and right
     * items share the same index.  The context of each join is included to be used in the zipper.
     */
    public <R, S> Query<S> zipByContext(Iterable<R> rightItems, TriFunction<T, R, QueryContext<Tuple<T, R>>, S> zipper) {
        return query().zipByContext(rightItems, zipper);
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into the zipper projection
     * where the left and right items share the same index.  The context of each join is included to be used in the
     * zipper.
     */
    public <R, S> Query<S> zipByContext(JoinType joinType, Iterable<R> rightItems, TriFunction<T, R, QueryContext<Tuple<T, R>>, S> zipper) {
        return query().zipByContext(joinType, rightItems, zipper);
    }

    //
    // Conversion Operators
    //

    /**
     * Wraps the QuerySet in a Query.
     */
    public Query<T> query() {
        return new Query<T>(this);
    }

    /**
     * Returns the query result as a QueryMap with the key defined by the keySelector and the value the item itself.
     * Each key selected must be unique or the mapping will fail.
     */
    public <K> QueryMap<K, T> map(Function<T, K> keySelector) {
        return query().map(keySelector);
    }

    /**
     * Returns the query result as a QueryMap with the key defined by the keySelector and the value defined by the
     * valueSelector.  Each key selected must be unique or the mapping will fail.
     */
    public <K, V> QueryMap<K, V> map(Function<T, K> keySelector, Function<T, V> valueSelector) {
        return query().map(keySelector, valueSelector);
    }

    /**
     * Returns the query result as a QueryGroup with the key defined by the keySelector and the values defined by the
     * valueSelector.  The valueSelector must target an iterable whose items are all combined into a flattened result.
     * Each key selected must be unique or the mapping will fail.
     */
    public <K, V> QueryGroup<K, V> mapMany(Function<T, K> keySelector, Function<T, ? extends Iterable<V>> valueSelector) {
        return query().mapMany(keySelector, valueSelector);
    }

    /**
     * Returns the query result as a QueryGroup with the key defined by the keySelector and the values defined by the
     * valueSelector.  The valueSelector must target an array whose items are all combined into a flattened result.
     * Each key selected must be unique or the mapping will fail.
     */
    public <K, V> QueryGroup<K, V> mapManyArray(Function<T, K> keySelector, Function<T, V[]> valueSelector) {
        return query().mapManyArray(keySelector, valueSelector);
    }

    /**
     * Returns the query result as a QueryGroup with the key defined by the keySelector and the values as the items
     * themselves.
     */
    public <K> QueryGroup<K, T> groupBy(Function<T, K> keySelector) {
        return query().groupBy(keySelector);
    }

    /**
     * Returns the query result as a QueryGroup with the key defined by the keySelector and the values defined by the
     * valueSelector.
     */
    public <K, V> QueryGroup<K, V> groupBy(Function<T, K> keySelector, Function<T, V> valueSelector) {
        return query().groupBy(keySelector, valueSelector);
    }

    /**
     * Returns the query result as a QueryGroup with the key defined by the keySelector and the values defined by the
     * valueSelector.  The valueSelector must target an iterable whose items are all combined into a flattened result.
     */
    public <K, V> QueryGroup<K, V> groupByMany(Function<T, K> keySelector, Function<T, ? extends Iterable<V>> valueSelector) {
        return query().groupByMany(keySelector, valueSelector);
    }

    /**
     * Returns the query result as a QueryGroup with the key defined by the keySelector and the values defined by the
     * valueSelector.  The valueSelector must target an array whose items are all combined into a flattened result.
     */
    public <K, V> QueryGroup<K, V> groupByManyArray(Function<T, K> keySelector, Function<T, V[]> valueSelector) {
        return query().groupByManyArray(keySelector, valueSelector);
    }

    /**
     * Returns the query result as a QueryList.
     */
    public QueryList<T> toList() {
        return new QueryList<T>(this);
    }

    /**
     * Returns the query result as a QueryList of the provided type.
     */
    public <S> QueryList<S> toList(Class<S> type) {
        return query().toList(type);
    }

    /**
     * Returns the query result as a QuerySet of the provided type.
     */
    public <S> QuerySet<S> toSet(Class<S> type) {
        return query().toSet(type);
    }

    /**
     * Returns the query result as the provided collection.
     */
    public <C extends Collection<? super T>> C to(C collection) {
        return query().to(collection);
    }

    /**
     * Returns the query result as an array of the provided type.
     */
    public <S> S[] toArray(Class<S> type) {
        Objects.requireNonNull(type);
        @SuppressWarnings({"unchecked"})
        S[] array = (S[])Array.newInstance(type, size());
        return toArray(array);
    }

    //
    // Element Operators
    //

    private Predicate<T> anything = t -> true;

    /**
     * Returns the first result of the query, or if none are found then a NoResultException exception is thrown.
     */
    public T first() {
        return query().first();
    }

    /**
     * Returns the first result of the query that matches the provided condition, or if none are found then a
     * NoResultException exception is thrown.
     */
    public T first(Predicate<T> condition) {
        return query().first(condition);
    }

    /**
     * Returns the first result of the query or null if none are found.
     */
    public T firstOrNull() {
        return query().firstOrNull();
    }

    /**
     * Returns the first result of the query that matches the provided condition or null if none are found.
     */
    public T firstOrNull(Predicate<T> condition) {
        return query().firstOrNull(condition);
    }

    /**
     * Returns the first result of the query or null if none are found, wrapped in an Optional.
     */
    public Optional<T> firstOptional() {
        return query().firstOptional();
    }

    /**
     * Returns the first result of the query that matches the provided condition or null if none are found, wrapped
     * in an Optional.
     */
    public Optional<T> firstOptional(Predicate<T> condition) {
        return query().firstOptional(condition);
    }

    /**
     * Returns the first result of the query or the provided alternate if none are found.
     */
    public T firstOr(T alternate) {
        return query().firstOr(alternate);
    }

    /**
     * Returns the first result of the query that matches the provided condition or the provided alternate if none are
     * found.
     */
    public T firstOr(Predicate<T> condition, T alternate) {
        return query().firstOr(condition, alternate);
    }

    /**
     * Returns the last result of the query, or if none are found then a NoResultException exception is thrown.
     */
    public T last() {
        return last(anything);
    }

    /**
     * Returns the last result of the query that matches the provided condition, or if none are found then a
     * NoResultException exception is thrown.
     */
    public T last(Predicate<T> condition) {
        T last = lastOrNull(condition);
        if (last == null)
            throw new RuntimeException("Sequence contains no elements.");
        return last;
    }

    /**
     * Returns the last result of the query or null if none are found.
     */
    public T lastOrNull() {
        return lastOrNull(anything);
    }

    /**
     * Returns the last result of the query that matches the provided condition or null if none are found.
     */
    public T lastOrNull(Predicate<T> condition) {
        return lastOr(condition, null);
    }

    /**
     * Returns the last result of the query or null if none are found, wrapped in an Optional.
     */
    public Optional<T> lastOptional() {
        return lastOptional(anything);
    }

    /**
     * Returns the last result of the query that matches the provided condition or null if none are found, wrapped
     * in an Optional.
     */
    public Optional<T> lastOptional(Predicate<T> condition) {
        return Optional.ofNullable(lastOrNull(condition));
    }

    /**
     * Returns the last result of the query or the provided alternate if none are found.
     */
    public T lastOr(T alternate) {
        return lastOr(anything, alternate);
    }

    /**
     * Returns the last result of the query that matches the provided condition or the provided alternate if none are
     * found.
     */
    public T lastOr(Predicate<T> condition, T alternate) {
        for (int i = size() - 1; i >= 0; i--) {
            T item = elementAt(i);
            if (condition.test(item))
                return item;
        }
        return alternate;
    }

    /**
     * Returns the single result of the query, or if none are found then a NoResultException exception is thrown.  If
     * more than one result is found a NonUniqueResultException exception is thrown.
     */
    public T single() {
        return query().single();
    }

    /**
     * Returns the single result of the query that matches the provided condition, or if none are found then a
     * NoResultException exception is thrown.  If more than one result is found a NonUniqueResultException exception is
     * thrown.
     */
    public T single(Predicate<T> condition) {
        return query().single(condition);
    }

    /**
     * Returns the single result of the query or null if none are found.  If more than one result is found a
     * NonUniqueResultException exception is thrown.
     */
    public T singleOrNull() {
        return query().singleOrNull();
    }

    /**
     * Returns the single result of the query that matches the provided condition or null if none are found.  If more
     * than one result is found a NonUniqueResultException exception is thrown.
     */
    public T singleOrNull(Predicate<T> condition) {
        return query().singleOrNull(condition);
    }

    /**
     * Returns the single result of the query or null if none are found, wrapped in an Optional.  If more than one
     * result is found a NonUniqueResultException exception is thrown.
     */
    public Optional<T> singleOptional() {
        return query().singleOptional();
    }

    /**
     * Returns the single result of the query that matches the provided condition or null if none are found, wrapped
     * in an Optional.  If more than one result is found a NonUniqueResultException exception is thrown.
     */
    public Optional<T> singleOptional(Predicate<T> condition) {
        return query().singleOptional(condition);
    }

    /**
     * Returns the single result of the query or the alternate if none are found.  If more than one result is found a
     * NonUniqueResultException exception is thrown.
     */
    public T singleOr(T alternate) {
        return query().singleOr(alternate);
    }

    /**
     * Returns the single result of the query that matches the provided condition or the alternate if none are found.
     * If more than one result is found a NonUniqueResultException exception is thrown.
     */
    public T singleOr(Predicate<T> condition, T alternate) {
        return query().singleOr(condition, alternate);
    }

    /**
     * Returns the result at the provided index, or an IndexOutOfBoundsException is thrown if the index is out of range.
     */
    public T elementAt(int index) {
        return query().elementAt(index);
    }

    /**
     * Returns the result at the provided index or null if the index is out of range.
     */
    public T elementAtOrNull(int index) {
        return query().elementAtOrNull(index);
    }

    /**
     * Returns the result at the provided index or null if the index is out of range, wrapped in an optional.
     */
    public Optional<T> elementAtOptional(int index) {
        return query().elementAtOptional(index);
    }

    /**
     * Returns the result at the provided index or the alternate if the index is out of range.
     */
    public T elementAtOr(int index, T alternate) {
        return query().elementAtOr(index, alternate);
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
     * Returns true if any result is equal to the provided item, otherwise false.
     */
    public boolean any(T item) {
        return query().any(item);
    }

    /**
     * Returns true if there are any results after applying the provided condition, otherwise false.
     */
    public boolean any(Predicate<T> condition) {
        return query().any(condition);
    }

    /**
     * Returns true if all results are equal to the provided item, otherwise false.
     */
    public boolean all(T item) {
        return query().all(item);
    }

    /**
     * Returns true if all results pass the provided condition, otherwise false.
     */
    public boolean all(Predicate<T> condition) {
        return query().all(condition);
    }

    /**
     * Return true if there are no results, otherwise false.
     */
    public boolean none() {
        return query().none();
    }

    /**
     * Returns true if none of the results are equal to the provided item, otherwise false.
     */
    public boolean none(T item) {
        return query().none(item);
    }

    /**
     * Returns true if none of the results pass the provided condition, otherwise false.
     */
    public boolean none(Predicate<T> condition) {
        return query().none(condition);
    }

    //
    // Aggregate operators
    //

    /**
     * Aggregates each item in the source according to the provided aggregation from a null seed.
     */
    public <A> A aggregate(BiFunction<A, T, A> aggregation) {
        return query().aggregate(aggregation);
    }

    /**
     * Aggregates each item in the source according to the provided aggregation from a provided seed.
     */
    public <A> A aggregate(A seed, BiFunction<A, T, A> aggregation) {
        return query().aggregate(seed, aggregation);
    }

    /**
     * Aggregates each item in the source according to the provided aggregation from a null seed.  The index of each
     * item in the source is included to be used in the aggregation.
     */
    public <A> A aggregateByIndex(TriFunction<A, T, Integer, A> aggregation) {
        return query().aggregateByIndex(aggregation);
    }

    /**
     * Aggregates each item in the source according to the provided aggregation from a provided seed.  The index of
     * each item in the source is included to be used in the aggregation.
     */
    public <A> A aggregateByIndex(A seed, TriFunction<A, T, Integer, A> aggregation) {
        return query().aggregateByIndex(seed, aggregation);
    }

    /**
     * Aggregates each item in the source according to the provided aggregation from a null seed.  The context of each
     * item in the source is included to be used in the aggregation.
     */
    public <A> A aggregateByContext(TriFunction<A, T, QueryContext<T>, A> aggregation) {
        return query().aggregateByContext(aggregation);
    }

    /**
     * Aggregates each item in the source according to the provided aggregation from a provided seed.  The context of
     * each item in the source is included to be used in the aggregation.
     */
    public <A> A aggregateByContext(A seed, TriFunction<A, T, QueryContext<T>, A> aggregation) {
        return query().aggregateByContext(seed, aggregation);
    }

    /**
     * Returns the sum of the non-null items in the result.  The items must be a Number type or an exception will be thrown.
     */
    public BigDecimal sum() {
        return query().sum();
    }

    /**
     * Returns the sum of the non-null projections of the provided selector for each item in the result.
     */
    public BigDecimal sum(Function<T, ? extends Number> selector) {
        return query().sum(selector);
    }

    /**
     * Returns the average or mean of the non-null items in the result.  The items must be a Number type or an
     * exception will be thrown.  The default scale will be used for the result with HALF_UP rounding.
     */
    public BigDecimal average() {
        return query().average();
    }

    /**
     * Returns the average or mean of the non-null items in the result.  The items must be a Number type or an
     * exception will be thrown.  The provided scale and rounding will be used for the result or the default
     * scale or HALF_UP rounding wherever null is provided.
     */
    public BigDecimal average(Integer scale, RoundingMode roundingMode) {
        return query().average(scale, roundingMode);
    }

    /**
     * Returns the average or mean of the non-null projections of the provided selector for each item in the result.
     * The default scale will be used for the result with HALF_UP rounding.
     */
    public BigDecimal average(Function<T, ? extends Number> selector) {
        return query().average(selector);
    }

    /**
     * Returns the average or mean of the non-null projections of the provided selector for each item in the result.
     * The provided scale and rounding will be used for the result or the default scale or HALF_UP rounding wherever
     * null is provided.
     */
    public BigDecimal average(Integer scale, RoundingMode roundingMode, Function<T, ? extends Number> selector) {
        return query().average(scale, roundingMode, selector);
    }

    /**
     * Returns the median of the non-null items in the result.  The items must be a Number type or an exception will be
     * thrown.  The default scale will be used for the result with HALF_UP rounding.
     */
    public BigDecimal median() {
        return query().median();
    }

    /**
     * Returns the median of the non-null items in the result.  The items must be a Number type or an exception will be
     * thrown.  The provided scale and rounding will be used for the result or the default scale or HALF_UP rounding
     * wherever null is provided.
     */
    public BigDecimal median(Integer scale, RoundingMode roundingMode) {
        return query().median(scale, roundingMode);
    }

    /**
     * Returns the median of the non-null projections of the provided selector for each item in the result.  The
     * default scale will be used for the result with HALF_UP rounding.
     */
    public BigDecimal median(Function<T, ? extends Number> selector) {
        return query().median(selector);
    }

    /**
     * Returns the median of the non-null projections of the provided selector for each item in the result.  The
     * provided scale and rounding will be used for the result or the default scale or HALF_UP rounding wherever
     * null is provided.
     */
    public BigDecimal median(Integer scale, RoundingMode roundingMode, Function<T, ? extends Number> selector) {
        return query().median(scale, roundingMode, selector);
    }

    /**
     * Returns the lowest value non-null item in the result.  The item must be a Number type or an exception will be
     * thrown.
     */
    public BigDecimal min() {
        return query().min();
    }

    /**
     * Returns the lowest value of the non-null projections of the provided selector for each item in the result.
     */
    public BigDecimal min(Function<T, ? extends Number> selector) {
        return query().min(selector);
    }

    /**
     * Returns the highest value non-null item in the result.  The item must be a Number type or an exception will be
     * thrown.
     */
    public BigDecimal max() {
        return query().max();
    }

    /**
     * Returns the highest value of the non-null projections of the provided selector for each item in the result.
     */
    public BigDecimal max(Function<T, ? extends Number> selector) {
        return query().max(selector);
    }

    //
    // Other operators
    //

    /**
     * Executes the provided doThis function if the provided condition is true.  Otherwise returns a new Query with
     * this Query as the source, effectively doing nothing.
     */
    public Query<T> when(boolean condition, Function<Query<T>, Query<T>> doThis) {
        return query().when(condition, doThis);
    }

    /**
     * Returns true if the length of the provided items is equal to the length of the source and each item in both
     * sequences is equal to the item sharing its index.
     */
    @SafeVarargs
    public final boolean sequenceEqual(T... items) {
        return query().sequenceEqual(items);
    }

    /**
     * Returns true if the length of the provided items is equal to the length of the source and each item in both
     * sequences is equal to the item sharing its index.
     */
    public boolean sequenceEqual(Iterable<? extends T> items) {
        return query().sequenceEqual(items);
    }

    /**
     * Returns true if the length of the provided items is equal to the length of the source and each item in both
     * sequences is equal to the item sharing its index according to the provided matcher.
     */
    public <R> boolean sequenceEqual(Iterable<R> items, BiPredicate<T, R> matcher) {
        return query().sequenceEqual(items, matcher);
    }

    /**
     * Returns true if the length of the provided items is equal to the length of the source and each item in both
     * sequences is equal to the item sharing its index according to the provided matcher.  The index of each item in
     * the source is included to be used in the condition.
     */
    public <R> boolean sequenceEqualByIndex(Iterable<R> items, TriPredicate<T, R, Integer> matcher) {
        return query().sequenceEqualByIndex(items, matcher);
    }

    /**
     * Returns string by converting each item in the source to a String and separating them with the provided separator.
     */
    public String toString(String separator) {
        return query().toString(separator);
    }

    @Override
    public String toString() {
        return "QuerySet{size=" + size() + ", values=" + super.toString() + "}";
    }
}
