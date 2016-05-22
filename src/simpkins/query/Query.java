package simpkins.query;

import simpkins.query.iterator.*;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.*;

/**
 * Query is an Iterable that wraps an Iterable (or array) as its source and provides a variety of methods to manipulate
 * or transform the contents of its source.  Wherever possible, Query methods will return a new Query object that
 * contains the previous Query object as its source.  This creates a chain of Query objects and provides for deferred
 * execution.  Only when a method is called that requires a concrete result will any of the chained Query objects
 * begin to execute, and only for as long as needed to acquire the result.
 *
 * The root Query source is normally defined when the root Query is created, but it is also possible to create a
 * sourceless Query (see the parameterless constructor or the static of() method).  The source can be later assigned
 * or reassigned with the fromThis() method before a result is requested.
 */
@SuppressWarnings({"UnusedDeclaration", "Convert2Diamond", "JavaDoc"})
public class Query<T> implements Iterable<T> {
    protected Iterable<T> source = null;

    @Override
    public Iterator<T> iterator() {
        if (source == null)
            throw new RuntimeException("The root query source is null and must be set to a valid " +
                    "array or iterable at either creation time or later through the fromThis() method.");
        return source.iterator();
    }

    //
    // Size tracking
    //

    // Used to track the query size when possible.  The root size will be set if the root source is an array,
    // collection, or another query with a size.  That size will then be modified while only size-predictable
    // operations are performed.  If a root size cannot be obtained from the root source or an operation with an
    // unpredictable size is used then the sizeSupplier will return null.
    protected Supplier<Integer> sizeSupplier = null;

    // Fetch the sizeSupplier lazily.  Since chained Query objects have their sizeSupplier passed down and assigned at
    // creation, only root Query objects will have their sizeSupplier initialized here.
    protected Supplier<Integer> getSizeSupplier() {
        if (sizeSupplier == null)
            sizeSupplier = () -> findSize(source);
        return sizeSupplier;
    }

    //
    // Late source assignment
    //

    // Executed when the fromThis() method is called.  In the root Query this is initialized to replace the source
    // field with the contents of the Iterable.  All chained Query objects will be passed this same Consumer.
    protected Consumer<Iterable> sourceReset = null;

    // Fetch the sourceReset lazily.  Since chained Query objects have their sourceReset passed down and assigned at
    // creation, only root Query objects will have their sourceReset initialized here.
    protected Consumer<Iterable> getSourceReset() {
        if (sourceReset == null)
            //noinspection unchecked
            return source -> this.source = (Iterable<T>)source;
        return sourceReset;
    }

    /**
     * Assigns or reassigns the root source.  All chained Query objects sharing the same root Query will be affected.
     * The items passed in must be of the same type as the root Query or a ClassCastException will occur.  It didn't
     * seem worth handling another generic type for Query just to handle this side feature.
     */
    public Query<T> fromThis(Object... items) {
        getSourceReset().accept(Arrays.asList(items));
        return this;
    }

    /**
     * Assigns or reassigns the root source.  All chained Query objects sharing the same root Query will be affected.
     * The source Iterable passed in must contain the same type as the root Query or a ClassCastException will occur.
     * It didn't seem worth handling another generic type for Query just to handle this side feature.
     */
    public Query<T> fromThis(Iterable source) {
        getSourceReset().accept(Objects.requireNonNull(source));
        return this;
    }

    //
    // Constructors
    //

    /**
     * No source is initially defined, but a source must be defined later through the fromThis() method before a
     * concrete result can be obtained.
     */
    public Query() {
    }

    @SafeVarargs
    public Query(T... items) {
        this.source = Arrays.asList(items);
    }

    public Query(Iterable<T> source) {
        this.source = Objects.requireNonNull(source);
    }

    // Java params doesn't work well with primitive arrays, so there is a specific constructor for each.
    // ArrayCollection will box each primitive as it's iterated.

    public Query(boolean[] source) {
        this.source = new ArrayCollection<T>(source);
    }

    public Query(byte[] source) {
        this.source = new ArrayCollection<T>(source);
    }

    public Query(short[] source) {
        this.source = new ArrayCollection<T>(source);
    }

    public Query(int[] source) {
        this.source = new ArrayCollection<T>(source);
    }

    public Query(long[] source) {
        this.source = new ArrayCollection<T>(source);
    }

    public Query(float[] source) {
        this.source = new ArrayCollection<T>(source);
    }

    public Query(double[] source) {
        this.source = new ArrayCollection<T>(source);
    }

    public Query(char[] source) {
        this.source = new ArrayCollection<T>(source);
    }

    // All chained Query objects use this constructor so that the sizeSupplier and sourceReset can be passed down from
    // the root Query object.  This should only be used internally.
    protected Query(Supplier<Integer> sizeSupplier, Consumer<Iterable> sourceReset, Iterable<T> source) {
        this.sizeSupplier = sizeSupplier;
        this.sourceReset = sourceReset;
        this.source = source;
    }

    //
    // Static Constructors
    //

    /**
     * No source is initially defined, but a source must be defined later through the fromThis() method before a
     * concrete result can be obtained.
     */
    public static <T> Query<T> of() {
        return new Query<T>();
    }

    /**
     * No source is initially defined, but a source must be defined later through the fromThis() method before a
     * concrete result can be obtained.
     */
    public static <T> Query<T> of(Class<T> type) {
        return new Query<T>();
    }

    @SafeVarargs
    public static <T> Query<T> from(T... items) {
        return new Query<T>(items);
    }

    public static <T> Query<T> from(Iterable<T> source) {
        return new Query<T>(source);
    }

    // Java params doesn't work well with primitive arrays, so there is a specific static constructor for each.
    // ArrayCollection will box each primitive as it's iterated.

    public static Query<Boolean> from(boolean[] source) {
        return new Query<Boolean>(source);
    }

    public static Query<Byte> from(byte[] source) {
        return new Query<Byte>(source);
    }

    public static Query<Short> from(short[] source) {
        return new Query<Short>(source);
    }

    public static Query<Integer> from(int[] source) {
        return new Query<Integer>(source);
    }

    public static Query<Long> from(long[] source) {
        return new Query<Long>(source);
    }

    public static Query<Float> from(float[] source) {
        return new Query<Float>(source);
    }

    public static Query<Double> from(double[] source) {
        return new Query<Double>(source);
    }

    public static Query<Character> from(char[] source) {
        return new Query<Character>(source);
    }

    //
    // Restriction Operators
    //

    /**
     * Filters the source down to only items where the provided condition is true.
     */
    public Query<T> where(Predicate<T> condition) {
        return new Query<T>(unknownSize, getSourceReset(), () -> new WhereIterator<T>(iterator(), condition));
    }

    /**
     * Filters the source down to only items where the provided condition is true.  The index of each item in the
     * source is included to be used in the condition.
     */
    public Query<T> whereByIndex(BiPredicate<T, Integer> condition) {
        return new Query<T>(unknownSize, getSourceReset(), () -> new WhereIterator<T>(iterator(), new Predicate<T>() {
            int i = 0;
            public boolean test(T t) {
                return condition.test(t, i++);
            }
        }));
    }

    /**
     * Filters the source down to only items where the provided condition is true.  The context of each item in the
     * source is included to be used in the condition.
     */
    public Query<T> whereByContext(BiPredicate<T, QueryContext<T>> condition) {
        QueryContextController<T> contextController = new QueryContextController<>(iterator());
        return new Query<T>(unknownSize, getSourceReset(), () -> new WhereIterator<T>(contextController, t -> condition.test(t, contextController)));
    }

    /**
     * Filters the source down to only items that equal one of the provided items.
     */
    @SafeVarargs
    public final Query<T> whereIn(T... items) {
        return whereIn(Arrays.asList(items));
    }

    /**
     * Filters the source down to only items that equal one of the provided items.
     */
    public Query<T> whereIn(Iterable<? extends T> container) {
        Objects.requireNonNull(container);
        return new Query<T>(unknownSize, getSourceReset(), () -> new WhereInIterator<T, T>(iterator(), container, selectSelf, true));
    }

    /**
     * Filters the source down to only items that do not equal one of the provided items.
     */
    @SafeVarargs
    public final Query<T> whereNotIn(T... items) {
        return whereNotIn(Arrays.asList(items));
    }

    /**
     * Filters the source down to only items that do not equal one of the provided items.
     */
    public Query<T> whereNotIn(Iterable<? extends T> container) {
        Objects.requireNonNull(container);
        return new Query<T>(unknownSize, getSourceReset(), () -> new WhereInIterator<T, T>(iterator(), container, selectSelf, false));
    }

    /**
     * Filters the source down only items that can be cast to the provided type and then casts them to that type.
     */
    public <S> Query<S> ofType(Class<S> type) {
        Objects.requireNonNull(type);
        return new Query<S>(unknownSize, getSourceReset(), () -> {
            WhereIterator<T> whereIterator = new WhereIterator<T>(iterator(), t -> type.isInstance(t));
            return new SelectIterator<T, S>(whereIterator, t -> type.cast(t));
        });
    }

    //
    // Partitioning Operators
    //

    /**
     * Discards a number of items from the beginning of the source as defined by the amount parameter.  The amount
     * must not be a negative number.  An amount greater than the size of the source will return an empty Query.
     */
    public Query<T> skip(int amount) {
        if (amount < 0)
            throw new RuntimeException("Skip amount cannot be less than zero.");
        return new Query<T>(getPartitioningSupplier(0, amount - 1, false), getSourceReset(), () -> new SkipIterator<T>(iterator(), new Predicate<T>() {
            int i = 0;
            public boolean test(T t) {
                return amount > i++;
            }
        }));
    }

    /**
     * Discards a number of items from the beginning of the source until an item is found where the given condition is
     * not true.  That item and all subsequent items (if any) are retained.
     */
    public Query<T> skipWhile(Predicate<T> condition) {
        return new Query<T>(unknownSize, getSourceReset(), () -> new SkipIterator<T>(iterator(), condition));
    }

    /**
     * Discards a number of items from the beginning of the source until an item is found where the given condition is
     * not true.  That item and all subsequent items (if any) are retained.  The index of each item in the source is
     * included to be used in the condition.
     */
    public Query<T> skipWhileByIndex(BiPredicate<T, Integer> condition) {
        return new Query<T>(unknownSize, getSourceReset(), () -> new SkipIterator<T>(iterator(), new Predicate<T>() {
            int i = 0;
            public boolean test(T t) {
                return condition.test(t, i++);
            }
        }));
    }

    /**
     * Discards a number of items from the beginning of the source until an item is found where the given condition is
     * not true.  That item and all subsequent items (if any) are retained.  The context of each item in the source is
     * included to be used in the condition.
     */
    public Query<T> skipWhileByContext(BiPredicate<T, QueryContext<T>> condition) {
        QueryContextController<T> contextController = new QueryContextController<>(iterator());
        return new Query<T>(unknownSize, getSourceReset(), () -> new SkipIterator<T>(contextController, t -> condition.test(t, contextController)));
    }

    /**
     * Retains a number of items from the beginning of the source as defined by the amount parameter.  All subsequent
     * items are discarded.  The amount must not be a negative number.  An amount greater than the size of the source
     * will return an identical Query.
     */
    public Query<T> take(int amount) {
        if (amount < 0)
            throw new RuntimeException("Take amount cannot be less than zero.");
        return new Query<T>(getPartitioningSupplier(0, amount - 1, true), getSourceReset(), () -> new TakeIterator<T>(iterator(), new Predicate<T>() {
            int i = 0;
            public boolean test(T t) {
                return amount > i++;
            }
        }));
    }

    /**
     * Retains a number of items from the beginning of the source until an item is found where the given condition is
     * not true.  That item and all subsequent items (if any) are discarded.
     */
    public Query<T> takeWhile(Predicate<T> condition) {
        return new Query<T>(unknownSize, getSourceReset(), () -> new TakeIterator<T>(iterator(), condition));
    }

    /**
     * Retains a number of items from the beginning of the source until an item is found where the given condition is
     * not true.  That item and all subsequent items (if any) are discarded.  The index of each item in the source is
     * included to be used in the condition.
     */
    public Query<T> takeWhileByIndex(BiPredicate<T, Integer> condition) {
        return new Query<T>(unknownSize, getSourceReset(), () -> new TakeIterator<T>(iterator(), new Predicate<T>() {
            int i = 0;
            public boolean test(T t) {
                return condition.test(t, i++);
            }
        }));
    }

    /**
     * Retains a number of items from the beginning of the source until an item is found where the given condition is
     * not true.  That item and all subsequent items (if any) are discarded.  The context of each item in the source is
     * included to be used in the condition.
     */
    public Query<T> takeWhileByContext(BiPredicate<T, QueryContext<T>> condition) {
        QueryContextController<T> contextController = new QueryContextController<>(iterator());
        return new Query<T>(unknownSize, getSourceReset(), () -> new TakeIterator<T>(contextController, t -> condition.test(t, contextController)));
    }

    /**
     * Discards a number of items defined by the amount beginning at the startIndex.  Items before or after this range
     * (if any) are retained.  The startIndex and amount parameters cannot be negative.  An amount of 0 will cause no
     * changes.  Any part of the range that falls outside the length of the source will be ignored.
     */
    public Query<T> exclude(int startIndex, int amount) {
        if (startIndex < 0)
            throw new RuntimeException("Exclude startIndex cannot be less than zero.");
        if (amount < 0)
            throw new RuntimeException("Exclude amount cannot be less than zero.");
        if (amount == 0)
            return new Query<T>(getSizeSupplier(), getSourceReset(), this);
        return excludeBetween(startIndex, startIndex + amount - 1);
    }

    /**
     * Discards a number of items at or between the startIndex and endIndex.  Items before or after this range (if any)
     * are retained.  The startIndex and endIndex parameters cannot be negative, and the endIndex cannot be less than
     * the startIndex.  Any part of the range that falls outside the length of the source will be ignored.
     */
    public Query<T> excludeBetween(int startIndex, int endIndex) {
        if (startIndex < 0)
            throw new RuntimeException("ExcludeBetween startIndex cannot be less than zero.");
        if (endIndex < 0)
            throw new RuntimeException("ExcludeBetween endIndex cannot be less than zero.");
        if (endIndex < startIndex)
            throw new RuntimeException("ExcludeBetween endIndex cannot be less than startIndex.");
        return new Query<T>(getPartitioningSupplier(startIndex, endIndex, false), getSourceReset(), () -> new ExcludeIterator<T>(iterator(), startIndex, endIndex));
    }

    //
    // Ordering Operators
    //

    /**
     * Re-orders the source in ascending order based on T as a Comparable.  If T is not a Comparable then a dummy
     * Comparable is used and source is only re-ordered with nulls last.  An OrderedQuery is returned which is an
     * extension of Query with additional thenBy() methods that allow for sub-ordering.
     */
    public <S extends Comparable<S>> OrderedQuery<T> order() {
        return new OrderedQuery<T>(getSizeSupplier(), getSourceReset(), () -> iterator(), new Ordering<T, S>(false, false));
    }

    /**
     * Re-orders the source in ascending order based on T as a Comparable.  If T is not a Comparable then a dummy
     * Comparable is used and source is only re-ordered with nulls first.  An OrderedQuery is returned which is an
     * extension of Query with additional thenBy() methods that allow for sub-ordering.
     */
    public <S extends Comparable<S>> OrderedQuery<T> orderNullsFirst() {
        return new OrderedQuery<T>(getSizeSupplier(), getSourceReset(), () -> iterator(), new Ordering<T, S>(false, true));
    }

    /**
     * Re-orders the source in descending order based on T as a Comparable.  If T is not a Comparable then a dummy
     * Comparable is used and source is only re-ordered with nulls first.  An OrderedQuery is returned which is an
     * extension of Query with additional thenBy() methods that allow for sub-ordering.
     */
    public <S extends Comparable<S>> OrderedQuery<T> orderDescending() {
        return new OrderedQuery<T>(getSizeSupplier(), getSourceReset(), () -> iterator(), new Ordering<T, S>(true, false));
    }

    /**
     * Re-orders the source in descending order based on T as a Comparable.  If T is not a Comparable then a dummy
     * Comparable is used and source is only re-ordered with nulls last.  An OrderedQuery is returned which is an
     * extension of Query with additional thenBy() methods that allow for sub-ordering.
     */
    public <S extends Comparable<S>> OrderedQuery<T> orderDescendingNullsLast() {
        return new OrderedQuery<T>(getSizeSupplier(), getSourceReset(), () -> iterator(), new Ordering<T, S>(true, true));
    }

    /**
     * Re-orders the source in ascending order based on the Comparable provided by the selector.  Null values are
     * placed last.  An OrderedQuery is returned which is an extension of Query with additional thenBy() methods that
     * allow for sub-ordering.
     */
    public <S extends Comparable<S>> OrderedQuery<T> orderBy(Function<T, S> selector) {
        return new OrderedQuery<T>(getSizeSupplier(), getSourceReset(), () -> iterator(), new Ordering<T, S>(selector, false, false));
    }

    /**
     * Re-orders the source in ascending order based on the Comparable provided by the selector.  Null values are
     * placed first.  An OrderedQuery is returned which is an extension of Query with additional thenBy() methods that
     * allow for sub-ordering.
     */
    public <S extends Comparable<S>> OrderedQuery<T> orderByNullsFirst(Function<T, S> selector) {
        return new OrderedQuery<T>(getSizeSupplier(), getSourceReset(), () -> iterator(), new Ordering<T, S>(selector, false, true));
    }

    /**
     * Re-orders the source based on the provided comparator.  An OrderedQuery is returned which is an extension of
     * Query with additional thenBy() methods that allow for sub-ordering.
     */
    public <S extends Comparable<S>> OrderedQuery<T> orderBy(Comparator<T> comparator) {
        return new OrderedQuery<T>(getSizeSupplier(), getSourceReset(), () -> iterator(), new Ordering<T, S>(comparator, false));
    }

    /**
     * Re-orders the source in descending order based on the Comparable provided by the selector.  Null values are
     * placed first.  An OrderedQuery is returned which is an extension of Query with additional thenBy() methods that
     * allow for sub-ordering.
     */
    public <S extends Comparable<S>> OrderedQuery<T> orderByDescending(Function<T, S> selector) {
        return new OrderedQuery<T>(getSizeSupplier(), getSourceReset(), () -> iterator(), new Ordering<T, S>(selector, true, false));
    }

    /**
     * Re-orders the source in descending order based on the Comparable provided by the selector.  Null values are
     * placed last.  An OrderedQuery is returned which is an extension of Query with additional thenBy() methods that
     * allow for sub-ordering.
     */
    public <S extends Comparable<S>> OrderedQuery<T> orderByDescendingNullsLast(Function<T, S> selector) {
        return new OrderedQuery<T>(getSizeSupplier(), getSourceReset(), () -> iterator(), new Ordering<T, S>(selector, true, true));
    }

    /**
     * Re-orders the source based on an inversion of the provided comparator.  An OrderedQuery is returned which is an
     * extension of Query with additional thenBy() methods that allow for sub-ordering.
     */
    public <S extends Comparable<S>> OrderedQuery<T> orderByDescending(Comparator<T> comparator) {
        return new OrderedQuery<T>(getSizeSupplier(), getSourceReset(), () -> iterator(), new Ordering<T, S>(comparator, true));
    }

    /**
     * Reverses the ordering of the source.
     */
    public Query<T> reverse() {
        return new Query<T>(getSizeSupplier(), getSourceReset(), () -> new OrderByIterator<T>(iterator(), Collections::reverse, getSizeSupplier().get()));
    }

    /**
     * Shuffles the ordering of the source.
     */
    public Query<T> shuffle() {
        return new Query<T>(getSizeSupplier(), getSourceReset(), () -> new OrderByIterator<T>(iterator(), Collections::shuffle, getSizeSupplier().get()));
    }

    //
    // Combining Operators
    //

    /**
     * Appends the provided additions at the end of the source.
     */
    @SafeVarargs
    public final Query<T> combine(T... additions) {
        return combine(Arrays.asList(additions));
    }

    /**
     * Appends the provided additions at the end of the source.
     */
    public Query<T> combine(Iterable<? extends T> additions) {
        Objects.requireNonNull(additions);
        return new Query<T>(getCombineSupplier(additions), getSourceReset(), () -> new CombineIterator<T>(iterator(), additions.iterator()));
    }

    /**
     * Appends the source at the end of the provided target.
     */
    @SafeVarargs
    public final Query<T> combineAfter(T... target) {
        return combineAfter(Arrays.asList(target));
    }

    /**
     * Appends the source at the end of the provided target.
     */
    public Query<T> combineAfter(Iterable<? extends T> target) {
        Objects.requireNonNull(target);
        return new Query<T>(getCombineSupplier(target), getSourceReset(), () -> new CombineIterator<T>(target.iterator(), iterator()));
    }

    /**
     * Combines the source with the provided insertions with the insertions inserted into the source at the provided
     * insertIndex.  The insertIndex cannot be negative.  If the insertIndex is outside the scope of the source then
     * the insertions will appended immediately after the source.
     */
    @SafeVarargs
    public final Query<T> insert(int insertIndex, T... insertions) {
        if (insertIndex < 0)
            throw new RuntimeException("Insert insertIndex cannot be less than zero.");
        return insert(insertIndex, Arrays.asList(insertions));
    }

    /**
     * Combines the source with the provided insertions with the insertions inserted into the source at the provided
     * insertIndex.  The insertIndex cannot be negative.  If the insertIndex is outside the scope of the source then
     * the insertions will appended immediately after the source.
     */
    public Query<T> insert(int insertIndex, Iterable<? extends T> insertions) {
        if (insertIndex < 0)
            throw new RuntimeException("Insert insertIndex cannot be less than zero.");
        Objects.requireNonNull(insertions);
        return new Query<T>(getCombineSupplier(insertions), getSourceReset(), () -> new CombineIterator<T>(iterator(), insertions.iterator(), insertIndex));
    }

    /**
     * Combines the source with the provided target with the source inserted into the target at the provided
     * insertIndex.  The insertIndex cannot be negative.  If the insertIndex is outside the scope of the target then
     * the source will appended immediately after the target.
     */
    @SafeVarargs
    public final Query<T> insertInto(int insertIndex, T... target) {
        if (insertIndex < 0)
            throw new RuntimeException("InsertInto insertIndex cannot be less than zero.");
        return insertInto(insertIndex, Arrays.asList(target));
    }

    /**
     * Combines the source with the provided target with the source inserted into the target at the provided
     * insertIndex.  The insertIndex cannot be negative.  If the insertIndex is outside the scope of the target then
     * the source will appended immediately after the target.
     */
    public Query<T> insertInto(int insertIndex, Iterable<? extends T> target) {
        if (insertIndex < 0)
            throw new RuntimeException("InsertInto insertIndex cannot be less than zero.");
        Objects.requireNonNull(target);
        return new Query<T>(getCombineSupplier(target), getSourceReset(), () -> new CombineIterator<T>(target.iterator(), iterator(), insertIndex));
    }

    //
    // Set Operators
    //

    /**
     * Filters the source down to a set that are distinct according to T's equals() implementation.
     */
    public Query<T> distinct() {
        return distinct(selectSelf);
    }

    /**
     * Filters the source down to a set that are distinct according to the results of the provided selector.
     */
    public <S> Query<T> distinct(Function<T, S> selector) {
        return new Query<T>(unknownSize, getSourceReset(), () -> new DistinctIterator<T, S>(iterator(), selector));
    }

    /**
     * Filters the source down to a set that are distinct according to the provided matcher.
     */
    public Query<T> distinct(BiPredicate<T, T> matcher) {
        return new Query<T>(unknownSize, getSourceReset(), () -> new DistinctIterator<T, T>(iterator(), matcher));
    }

    /**
     * Filters the source down to a set that are distinct and not present in the provided exceptions according to T's
     * equals() implementation.
     */
    @SafeVarargs
    public final Query<T> except(T... exceptions) {
        return except(Arrays.asList(exceptions));
    }

    /**
     * Filters the source down to a set that are distinct and not present in the provided exceptions according to T's
     * equals() implementation.
     */
    public Query<T> except(Iterable<? extends T> exceptions) {
        return except(exceptions, selectSelf);
    }

    /**
     * Filters the source down to a set that are distinct and not present in the provided exceptions according to the
     * results of the provided selector.
     */
    public <S> Query<T> except(Iterable<? extends T> exceptions, Function<T, S> selector) {
        Objects.requireNonNull(exceptions);
        return new Query<T>(unknownSize, getSourceReset(), () -> {
            WhereInIterator<T, S> whereInIterator = new WhereInIterator<T, S>(iterator(), exceptions, selector, false);
            return new DistinctIterator<T, S>(whereInIterator, selector);
        });
    }

    /**
     * Filters the source down to a set that are distinct and not present in the provided exceptions according to the
     * provided matcher.
     */
    public Query<T> except(Iterable<? extends T> exceptions, BiPredicate<T, T> matcher) {
        Objects.requireNonNull(exceptions);
        return new Query<T>(unknownSize, getSourceReset(), () -> {
            WhereInIterator<T, T> whereInIterator = new WhereInIterator<T, T>(iterator(), exceptions, matcher, false);
            return new DistinctIterator<T, T>(whereInIterator, matcher);
        });
    }

    /**
     * Filters the source down to a set that are distinct and present in the provided intersections according to T's
     * equals() implementation.
     */
    @SafeVarargs
    public final Query<T> intersect(T... intersections) {
        return intersect(Arrays.asList(intersections));
    }

    /**
     * Filters the source down to a set that are distinct and present in the provided intersections according to T's
     * equals() implementation.
     */
    public Query<T> intersect(Iterable<? extends T> intersections) {
        return intersect(intersections, selectSelf);
    }

    /**
     * Filters the source down to a set that are distinct and present in the provided intersections according to the
     * results of the provided selector.
     */
    public <S> Query<T> intersect(Iterable<? extends T> intersections, Function<T, S> selector) {
        Objects.requireNonNull(intersections);
        return new Query<T>(unknownSize, getSourceReset(), () -> {
            WhereInIterator<T, S> whereInIterator = new WhereInIterator<T, S>(iterator(), intersections, selector, true);
            return new DistinctIterator<T, S>(whereInIterator, selector);
        });
    }

    /**
     * Filters the source down to a set that are distinct and present in the provided intersections according to the
     * provided matcher.
     */
    public Query<T> intersect(Iterable<? extends T> intersections, BiPredicate<T, T> matcher) {
        Objects.requireNonNull(intersections);
        return new Query<T>(unknownSize, getSourceReset(), () -> {
            WhereInIterator<T, T> whereInIterator = new WhereInIterator<T, T>(iterator(), intersections, matcher, true);
            return new DistinctIterator<T, T>(whereInIterator, matcher);
        });
    }

    /**
     * Combines the source with the unions and filters them down to a set that are distinct according to T's equals
     * implementation.
     */
    @SafeVarargs
    public final Query<T> union(T... unions) {
        return union(Arrays.asList(unions));
    }

    /**
     * Combines the source with the unions and filters them down to a set that are distinct according to T's equals
     * implementation.
     */
    public Query<T> union(Iterable<? extends T> unions) {
        return union(unions, selectSelf);
    }

    /**
     * Combines the source with the unions and filters them down to a set that are distinct according to the results
     * of the provided selector.
     */
    public <S> Query<T> union(Iterable<? extends T> unions, Function<T, S> selector) {
        Objects.requireNonNull(unions);
        return new Query<T>(unknownSize, getSourceReset(), () -> {
            CombineIterator<T> combineIterator = new CombineIterator<T>(iterator(), unions.iterator());
            return new DistinctIterator<T, S>(combineIterator, selector);
        });
    }

    /**
     * Combines the source with the unions and filters them down to a set that are distinct according to the
     * provided matcher.
     */
    public Query<T> union(Iterable<? extends T> unions, BiPredicate<T, T> matcher) {
        Objects.requireNonNull(unions);
        return new Query<T>(unknownSize, getSourceReset(), () -> {
            CombineIterator<T> combineIterator = new CombineIterator<T>(iterator(), unions.iterator());
            return new DistinctIterator<T, T>(combineIterator, matcher);
        });
    }

    //
    // Projection Operators
    //

    /**
     * Transforms the source to the projection defined by the provided selector.
     */
    public <S> Query<S> select(Function<T, S> selector) {
        return new Query<S>(getSizeSupplier(), getSourceReset(), () -> new SelectIterator<T, S>(iterator(), selector));
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  The index of each item in the source
     * is included to be used in the selector.
     */
    public <S> Query<S> selectByIndex(BiFunction<T, Integer, S> selector) {
        return new Query<S>(getSizeSupplier(), getSourceReset(), () -> new SelectIterator<T, S>(iterator(), new Function<T, S>() {
            int i = 0;
            public S apply(T t) {
                return selector.apply(t, i++);
            }
        }));
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  The context of each item in the source
     * is included to be used in the selector.
     */
    public <S> Query<S> selectByContext(BiFunction<T, QueryContext<T>, S> selector) {
        QueryContextController<T> contextController = new QueryContextController<>(iterator());
        return new Query<S>(getSizeSupplier(), getSourceReset(), () -> new SelectIterator<T, S>(contextController, t -> selector.apply(t, contextController)));
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  Each projection is an iterable which
     * are all combined into a flattened result.
     */
    public <S> Query<S> selectMany(Function<T, ? extends Iterable<S>> selector) {
        return new Query<S>(unknownSize, getSourceReset(), () -> new SelectManyIterator<T, S>(iterator(), selector));
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  Each projection is an iterable which
     * are all combined into a flattened result.  The index of each item in the source is included to be used in the
     * selector.
     */
    public <S> Query<S> selectManyByIndex(BiFunction<T, Integer, ? extends Iterable<S>> selector) {
        return new Query<S>(unknownSize, getSourceReset(), () -> new SelectManyIterator<T, S>(iterator(), new Function<T, Iterable<S>>() {
            int i = 0;
            public Iterable<S> apply(T t) {
                return selector.apply(t, i++);
            }
        }));
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  Each projection is an iterable which
     * are all combined into a flattened result.  The context of each item in the source is included to be used in the
     * selector.
     */
    public <S> Query<S> selectManyByContext(BiFunction<T, QueryContext<T>, ? extends Iterable<S>> selector) {
        QueryContextController contextController = new QueryContextController<>(iterator());
        return new Query<S>(unknownSize, getSourceReset(), () -> new SelectManyIterator<T, S>(contextController, t -> selector.apply(t, contextController)));
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  Each projection is an array which
     * are all combined into a flattened result.
     */
    public <S> Query<S> selectManyArray(Function<T, S[]> selector) {
        return new Query<S>(unknownSize, getSourceReset(), () -> new SelectManyIterator<T, S>(iterator(), t -> Arrays.asList(selector.apply(t))));
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  Each projection is an array which
     * are all combined into a flattened result.  The index of each item in the source is included to be used in the
     * selector.
     */
    public <S> Query<S> selectManyArrayByIndex(BiFunction<T, Integer, S[]> selector) {
        return new Query<S>(unknownSize, getSourceReset(), () -> new SelectManyIterator<T, S>(iterator(), new Function<T, Iterable<S>>() {
            int i = 0;
            public Iterable<S> apply(T t) {
                return Arrays.asList(selector.apply(t, i++));
            }
        }));
    }

    /**
     * Transforms the source to the projection defined by the provided selector.  Each projection is an array which
     * are all combined into a flattened result.  The index of each item in the source is included to be used in the
     * selector.
     */
    public <S> Query<S> selectManyArrayByContext(BiFunction<T, QueryContext<T>, S[]> selector) {
        QueryContextController<T> contextController = new QueryContextController<>(iterator());
        return new Query<S>(unknownSize, getSourceReset(), () -> new SelectManyIterator<T, S>(contextController, t -> Arrays.asList(selector.apply(t, contextController))));
    }

    /**
     * Groups and returns items from the source that share the same key obtained by the provided keySelector into a
     * QueryGrouping.  Unlike groupBy() this returns a new Query, not a QueryGroup, which provides deferred execution
     * but none of the underlying Map functionality of QueryGroup.
     */
    public <K> Query<QueryGrouping<K, T>> selectGrouped(Function<T, K> keySelector) {
        return selectGrouped(keySelector, selectSelf);
    }

    /**
     * Groups and returns items from the source that share the same key obtained by the provided keySelector into a
     * QueryGrouping.  The items in the QueryGroup and transformed to the projection defined by the valueSelector.
     * Unlike groupBy() this returns a new Query, not a QueryGroup, which provides deferred execution but none of the
     * underlying Map functionality of QueryGroup.
     */
    public <K, V> Query<QueryGrouping<K, V>> selectGrouped(Function<T, K> keySelector, Function<T, V> valueSelector) {
        return new Query<QueryGrouping<K, V>>(unknownSize, getSourceReset(), () -> new GroupByIterator<>(iterator(), keySelector, valueSelector));
    }

    /**
     * Casts each item in the source to the provided type.
     */
    public <S> Query<S> cast(Class<S> type) {
        Objects.requireNonNull(type);
        return new Query<S>(getSizeSupplier(), getSourceReset(), () -> new SelectIterator<T, S>(iterator(), t -> type.cast(t)));
    }

    //
    // Application Operators
    //

    /**
     * Executes the provided action on each item in the source and then returns that item.
     */
    public Query<T> pipe(Consumer<T> action) {
        return new Query<T>(getSizeSupplier(), getSourceReset(), () -> new SelectIterator<T, T>(iterator(), t -> { action.accept(t); return t; }));
    }

    /**
     * Executes the provided action on each item in the source and then returns that item.  The index of each item in
     * the source is included to be used in the action.
     */
    public Query<T> pipeByIndex(BiConsumer<T, Integer> action) {
        return new Query<T>(getSizeSupplier(), getSourceReset(), () -> new SelectIterator<T, T>(iterator(), new Function<T, T>() {
            int i = 0;
            public T apply(T t) {
                action.accept(t, i++);
                return t;
            }
        }));
    }

    /**
     * Executes the provided action on each item in the source and then returns that item.  The context of each item in
     * the source is included to be used in the action.
     */
    public Query<T> pipeByContext(BiConsumer<T, QueryContext<T>> action) {
        QueryContextController contextController = new QueryContextController<>(iterator());
        return new Query<T>(getSizeSupplier(), getSourceReset(), () -> new SelectIterator<T, T>(contextController, t -> { action.accept(t, contextController); return t; }));
    }

    /**
     * Executes the provided action on each item in the source.  The index of each item in the source is included to be
     * used in the action.
     */
    public void forEachByIndex(BiConsumer<T, Integer> action) {
        int i = 0;
        for (T item : this)
            action.accept(item, i++);
    }

    /**
     * Executes the provided action on each item in the source.  The context of each item in the source is included to be
     * used in the action.
     */
    public void forEachByContext(BiConsumer<T, QueryContext<T>> action) {
        QueryContextController<T> context = new QueryContextController<>(iterator());
        while (context.hasNext())
            action.accept(context.next(), context);
    }

    //
    // Join Operators
    //

    /**
     * Inner joins the source as left with the provided rightItems into a Tuple wherever the projection of a left
     * item's leftSelector equals the projection of right item's rightSelector.
     */
    public <R, K> Query<Tuple<T, R>> join(Iterable<R> rightItems, Function<T, K> leftSelector, Function<R, K> rightSelector) {
        return join(JoinType.INNER, rightItems, leftSelector, rightSelector);
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into a Tuple wherever the
     * projection of a left item's leftSelector equals the projection of a right item's rightSelector.
     */
    public <R, K> Query<Tuple<T, R>> join(JoinType joinType, Iterable<R> rightItems, Function<T, K> leftSelector, Function<R, K> rightSelector) {
        Objects.requireNonNull(joinType);
        Objects.requireNonNull(rightItems);
        return new Query<Tuple<T, R>>(unknownSize, getSourceReset(), () -> new JoinIterator<T, R>(iterator(), joinType, rightItems.iterator(), leftSelector, rightSelector, findSize(rightItems)));
    }

    /**
     * Inner joins the source as left with the provided rightItems into a Tuple wherever items from either source
     * match by the provided matcher.
     */
    public <R> Query<Tuple<T, R>> join(Iterable<R> rightItems, BiPredicate<T, R> matcher) {
        return join(JoinType.INNER, rightItems, matcher);
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into a Tuple wherever
     * items from either source match by the provided matcher.
     */
    public <R> Query<Tuple<T, R>> join(JoinType joinType, Iterable<R> rightItems, BiPredicate<T, R> matcher) {
        Objects.requireNonNull(joinType);
        Objects.requireNonNull(rightItems);
        return new Query<Tuple<T, R>>(unknownSize, getSourceReset(), () -> new JoinIterator<T, R>(iterator(), joinType, rightItems.iterator(), matcher, findSize(rightItems)));
    }

    /**
     * Inner joins the source as left with the provided rightItems into the joiner projection wherever the projection
     * of a left item's leftSelector equals the projection of right item's rightSelector.
     */
    public <K, R, S> Query<S> join(Iterable<R> rightItems, Function<T, K> leftSelector, Function<R, K> rightSelector, BiFunction<T, R, S> joiner) {
        return join(JoinType.INNER, rightItems, leftSelector, rightSelector, joiner);
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into the joiner projection
     * wherever the projection of a left item's leftSelector equals the projection of right item's rightSelector.
     */
    public <K, R, S> Query<S> join(JoinType joinType, Iterable<R> rightItems, Function<T, K> leftSelector, Function<R, K> rightSelector, BiFunction<T, R, S> joiner) {
        Objects.requireNonNull(joinType);
        Objects.requireNonNull(rightItems);
        return new Query<S>(unknownSize, getSourceReset(), () -> {
            JoinIterator<T, R> joinIterator = new JoinIterator<T, R>(iterator(), joinType, rightItems.iterator(), leftSelector, rightSelector, findSize(rightItems));
            return new SelectIterator<Tuple<T, R>, S>(joinIterator, tr -> joiner.apply(tr.getItem1(), tr.getItem2()));
        });
    }

    /**
     * Inner joins the source as left with the provided rightItems into the joiner projection wherever items from
     * either source match by the provided matcher.
     */
    public <R, S> Query<S> join(Iterable<R> rightItems, BiPredicate<T, R> matcher, BiFunction<T, R, S> joiner) {
        return join(JoinType.INNER, rightItems, matcher, joiner);
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into the joiner projection
     * wherever items from either source match by the provided matcher.
     */
    public <R, S> Query<S> join(JoinType joinType, Iterable<R> rightItems, BiPredicate<T, R> matcher, BiFunction<T, R, S> joiner) {
        Objects.requireNonNull(joinType);
        Objects.requireNonNull(rightItems);
        return new Query<S>(unknownSize, getSourceReset(), () -> {
            JoinIterator<T, R> joinIterator = new JoinIterator<T, R>(iterator(), joinType, rightItems.iterator(), matcher, findSize(rightItems));
            return new SelectIterator<Tuple<T, R>, S>(joinIterator, tr -> joiner.apply(tr.getItem1(), tr.getItem2()));
        });
    }

    /**
     * Inner joins the source as left with the provided rightItems into a Tuple wherever the projection of a left
     * item's leftSelector equals the projection of right item's rightSelector.  Joined right items are grouped in a
     * QueryList for each left item.
     */
    public <R, K> Query<Tuple<T, QueryList<R>>> groupJoin(Iterable<R> rightItems, Function<T, K> leftSelector, Function<R, K> rightSelector) {
        return groupJoin(JoinType.INNER, rightItems, leftSelector, rightSelector);
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into a Tuple wherever the
     * projection of a left item's leftSelector equals the projection of a right item's rightSelector.  Joined right
     * items are grouped in a QueryList for each left item.
     */
    public <R, K> Query<Tuple<T, QueryList<R>>> groupJoin(JoinType joinType, Iterable<R> rightItems, Function<T, K> leftSelector, Function<R, K> rightSelector) {
        Objects.requireNonNull(joinType);
        Objects.requireNonNull(rightItems);
        return new Query<Tuple<T, QueryList<R>>>(getGroupJoinSupplier(joinType), getSourceReset(), () -> new GroupJoinIterator<T, R>(iterator(), joinType, rightItems.iterator(), leftSelector, rightSelector, findSize(rightItems)));
    }

    /**
     * Inner joins the source as left with the provided rightItems into a Tuple wherever items from either source
     * match by the provided matcher.  Joined right items are grouped in a QueryList for each left item.
     */
    public <R> Query<Tuple<T, QueryList<R>>> groupJoin(Iterable<R> rightItems, BiPredicate<T, R> matcher) {
        return groupJoin(JoinType.INNER, rightItems, matcher);
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into a Tuple wherever
     * items from either source match by the provided matcher.  Joined right items are grouped in a QueryList for each
     * left item.
     */
    public <R> Query<Tuple<T, QueryList<R>>> groupJoin(JoinType joinType, Iterable<R> rightItems, BiPredicate<T, R> matcher) {
        Objects.requireNonNull(joinType);
        Objects.requireNonNull(rightItems);
        return new Query<Tuple<T, QueryList<R>>>(getGroupJoinSupplier(joinType), getSourceReset(), () -> new GroupJoinIterator<T, R>(iterator(), joinType, rightItems.iterator(), matcher, findSize(rightItems)));
    }

    /**
     * Inner joins the source as left with the provided rightItems into the joiner projection wherever the projection
     * of a left item's leftSelector equals the projection of right item's rightSelector.  Joined right items are
     * grouped in a QueryList for each left item.
     */
    public <K, R, S> Query<S> groupJoin(Iterable<R> rightItems, Function<T, K> leftSelector, Function<R, K> rightSelector, BiFunction<T, QueryList<R>, S> joiner) {
        return groupJoin(JoinType.INNER, rightItems, leftSelector, rightSelector, joiner);
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into the joiner projection
     * wherever the projection of a left item's leftSelector equals the projection of right item's rightSelector.
     * Joined right items are grouped in a QueryList for each left item.
     */
    public <K, R, S> Query<S> groupJoin(JoinType joinType, Iterable<R> rightItems, Function<T, K> leftSelector, Function<R, K> rightSelector, BiFunction<T, QueryList<R>, S> joiner) {
        Objects.requireNonNull(joinType);
        Objects.requireNonNull(rightItems);
        return new Query<S>(getGroupJoinSupplier(joinType), getSourceReset(), () -> {
            GroupJoinIterator<T, R> groupJoinIterator = new GroupJoinIterator<T, R>(iterator(), joinType, rightItems.iterator(), leftSelector, rightSelector, findSize(rightItems));
            return new SelectIterator<Tuple<T, QueryList<R>>, S>(groupJoinIterator, tr -> joiner.apply(tr.getItem1(), tr.getItem2()));
        });
    }

    /**
     * Inner joins the source as left with the provided rightItems into the joiner projection wherever items from
     * either source match by the provided matcher.  Joined right items are grouped in a QueryList for each left item.
     */
    public <R, S> Query<S> groupJoin(Iterable<R> rightItems, BiPredicate<T, R> matcher, BiFunction<T, QueryList<R>, S> joiner) {
        return groupJoin(JoinType.INNER, rightItems, matcher, joiner);
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into the joiner projection
     * wherever items from either source match by the provided matcher.  Joined right items are grouped in a QueryList
     * for each left item.
     */
    public <R, S> Query<S> groupJoin(JoinType joinType, Iterable<R> rightItems, BiPredicate<T, R> matcher, BiFunction<T, QueryList<R>, S> joiner) {
        Objects.requireNonNull(joinType);
        Objects.requireNonNull(rightItems);
        return new Query<S>(getGroupJoinSupplier(joinType), getSourceReset(), () -> {
            GroupJoinIterator<T, R> groupJoinIterator = new GroupJoinIterator<T, R>(iterator(), joinType, rightItems.iterator(), matcher, findSize(rightItems));
            return new SelectIterator<Tuple<T, QueryList<R>>, S>(groupJoinIterator, tr -> joiner.apply(tr.getItem1(), tr.getItem2()));
        });
    }

    /**
     * Inner joins the source as left with the provided rightItems into a Tuple where the left and right items share
     * the same index.
     */
    @SafeVarargs
    public final <R> Query<Tuple<T, R>> zip(R... rightItems) {
        return zip(JoinType.INNER, Arrays.asList(rightItems));
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into a Tuple where the
     * left and right items share the same index.
     */
    @SafeVarargs
    public final <R> Query<Tuple<T, R>> zip(JoinType joinType, R... rightItems) {
        return zip(joinType, Arrays.asList(rightItems));
    }

    /**
     * Inner joins the source as left with the provided rightItems into a Tuple where the left and right items share
     * the same index.
     */
    public <R> Query<Tuple<T, R>> zip(Iterable<R> rightItems) {
        return zip(JoinType.INNER, rightItems);
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into a Tuple where the
     * left and right items share the same index.
     */
    public <R> Query<Tuple<T, R>> zip(JoinType joinType, Iterable<R> rightItems) {
        Objects.requireNonNull(joinType);
        Objects.requireNonNull(rightItems);
        return new Query<Tuple<T, R>>(getZipSupplier(joinType, rightItems), getSourceReset(), () -> new ZipIterator<T, R>(iterator(), joinType, rightItems.iterator()));
    }

    /**
     * Inner joins the source as left with the provided rightItems into the zipper projection where the left and right
     * items share the same index.
     */
    public <R, S> Query<S> zip(Iterable<R> rightItems, BiFunction<T, R, S> zipper) {
        return zip(JoinType.INNER, rightItems, zipper);
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into the zipper projection
     * where the left and right items share the same index.
     */
    public <R, S> Query<S> zip(JoinType joinType, Iterable<R> rightItems, BiFunction<T, R, S> zipper) {
        return zipByIndex(joinType, rightItems, (t, r, i) -> zipper.apply(t, r));
    }

    /**
     * Inner joins the source as left with the provided rightItems into the zipper projection where the left and right
     * items share the same index.  The index of each join is included to be used in the zipper.
     */
    public <R, S> Query<S> zipByIndex(Iterable<R> rightItems, TriFunction<T, R, Integer, S> zipper) {
        return zipByIndex(JoinType.INNER, rightItems, zipper);
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into the zipper projection
     * where the left and right items share the same index.  The index of each join is included to be used in the
     * zipper.
     */
    public <R, S> Query<S> zipByIndex(JoinType joinType, Iterable<R> rightItems, TriFunction<T, R, Integer, S> zipper) {
        Objects.requireNonNull(joinType);
        Objects.requireNonNull(rightItems);
        return new Query<S>(getZipSupplier(joinType, rightItems), getSourceReset(), () -> {
            ZipIterator<T, R> zipIterator = new ZipIterator<T, R>(iterator(), joinType, rightItems.iterator());
            return new SelectIterator<Tuple<T, R>, S>(zipIterator, new Function<Tuple<T, R>, S>() {
                int i = 0;
                public S apply(Tuple<T, R> tr) {
                    return zipper.apply(tr.getItem1(), tr.getItem2(), i++);
                }
            });
        });
    }

    /**
     * Inner joins the source as left with the provided rightItems into the zipper projection where the left and right
     * items share the same index.  The context of each join is included to be used in the zipper.
     */
    public <R, S> Query<S> zipByContext(Iterable<R> rightItems, TriFunction<T, R, QueryContext<Tuple<T, R>>, S> zipper) {
        return zipByContext(JoinType.INNER, rightItems, zipper);
    }

    /**
     * Joins according to the given joinType the source as left with the provided rightItems into the zipper projection
     * where the left and right items share the same index.  The context of each join is included to be used in the
     * zipper.
     */
    public <R, S> Query<S> zipByContext(JoinType joinType, Iterable<R> rightItems, TriFunction<T, R, QueryContext<Tuple<T, R>>, S> zipper) {
        Objects.requireNonNull(joinType);
        Objects.requireNonNull(rightItems);
        return new Query<S>(getZipSupplier(joinType, rightItems), getSourceReset(), () -> {
            ZipIterator<T, R> zipIterator = new ZipIterator<T, R>(iterator(), joinType, rightItems.iterator());
            QueryContextController<Tuple<T, R>> contextController = new QueryContextController<>(zipIterator);
            return new SelectIterator<Tuple<T, R>, S>(contextController, tr -> zipper.apply(tr.getItem1(), tr.getItem2(), contextController));
        });
    }

    //
    // Conversion Operators
    //

    /**
     * Returns the query result as a QueryMap with the key defined by the keySelector and the value the item itself.
     * Each key selected must be unique or the mapping will fail.
     */
    public <K> QueryMap<K, T> map(Function<T, K> keySelector) {
        return map(keySelector, selectSelf);
    }

    /**
     * Returns the query result as a QueryMap with the key defined by the keySelector and the value defined by the
     * valueSelector.  Each key selected must be unique or the mapping will fail.
     */
    public <K, V> QueryMap<K, V> map(Function<T, K> keySelector, Function<T, V> valueSelector) {
        if (getSizeSupplier().get() != null)
            return new QueryMap<K, V>(this.asCollection(), keySelector, valueSelector);
        return new QueryMap<K, V>(this, keySelector, valueSelector);
    }

    /**
     * Returns the query result as a QueryGroup with the key defined by the keySelector and the values defined by the
     * valueSelector.  The valueSelector must target an iterable whose items are all combined into a flattened result.
     * Each key selected must be unique or the mapping will fail.
     */
    public <K, V> QueryGroup<K, V> mapMany(Function<T, K> keySelector, Function<T, ? extends Iterable<V>> valueSelector) {
        return QueryMap.many(this, keySelector, valueSelector);
    }

    /**
     * Returns the query result as a QueryGroup with the key defined by the keySelector and the values defined by the
     * valueSelector.  The valueSelector must target an array whose items are all combined into a flattened result.
     * Each key selected must be unique or the mapping will fail.
     */
    public <K, V> QueryGroup<K, V> mapManyArray(Function<T, K> keySelector, Function<T, V[]> valueSelector) {
        return QueryMap.manyArray(this, keySelector, valueSelector);
    }

    /**
     * Returns the query result as a QueryGroup with the key defined by the keySelector and the values as the items
     * themselves.
     */
    public <K> QueryGroup<K, T> groupBy(Function<T, K> keySelector) {
        return groupBy(keySelector, selectSelf);
    }

    /**
     * Returns the query result as a QueryGroup with the key defined by the keySelector and the values defined by the
     * valueSelector.
     */
    public <K, V> QueryGroup<K, V> groupBy(Function<T, K> keySelector, Function<T, V> valueSelector) {
        return new QueryGroup<K, V>(this, keySelector, valueSelector);
    }

    /**
     * Returns the query result as a QueryGroup with the key defined by the keySelector and the values defined by the
     * valueSelector.  The valueSelector must target an iterable whose items are all combined into a flattened result.
     */
    public <K, V> QueryGroup<K, V> groupByMany(Function<T, K> keySelector, Function<T, ? extends Iterable<V>> valueSelector) {
        return QueryGroup.byMany(this, keySelector, valueSelector);
    }

    /**
     * Returns the query result as a QueryGroup with the key defined by the keySelector and the values defined by the
     * valueSelector.  The valueSelector must target an array whose items are all combined into a flattened result.
     */
    public <K, V> QueryGroup<K, V> groupByManyArray(Function<T, K> keySelector, Function<T, V[]> valueSelector) {
        return QueryGroup.byManyArray(this, keySelector, valueSelector);
    }

    /**
     * Returns the query result as a QueryList.
     */
    public QueryList<T> toList() {
        if (getSizeSupplier().get() != null)
            return new QueryList<T>(this.asCollection());
        return to(new QueryList<T>());
    }

    /**
     * Returns the query result as a QueryList of the provided type.
     */
    public <S> QueryList<S> toList(Class<S> type) {
        return cast(type).toList();
    }

    /**
     * Returns the query result as a QuerySet.
     */
    public QuerySet<T> toSet() {
        if (getSizeSupplier().get() != null)
            return new QuerySet<T>(this.asCollection());
        return to(new QuerySet<T>());
    }

    /**
     * Returns the query result as a QuerySet of the provided type.
     */
    public <S> QuerySet<S> toSet(Class<S> type) {
        return cast(type).toSet();
    }

    /**
     * Returns the query result as the provided collection.
     */
    public <C extends Collection<? super T>> C to(C collection) {
        Objects.requireNonNull(collection);
        for (T item : this)
            collection.add(item);
        return collection;
    }

    /**
     * Returns the query result as an array of the provided type.
     */
    @SuppressWarnings("unchecked")
    public <S> S[] toArray(Class<S> type) {
        Objects.requireNonNull(type);
        Integer size = getSizeSupplier().get();
        if (size != null) {
            S[] array = (S[])Array.newInstance(type, size);
            int index = 0;
            for (T item : this)
                array[index++] = (S)item;
            return array;
        }
        List<S> list = toList(type);
        S[] array = (S[])Array.newInstance(type, list.size());
        return list.toArray(array);
    }

    /**
     * Returns the query result as an Object[] array.
     */
    public Object[] toArray() {
        Integer size = getSizeSupplier().get();
        if (size != null) {
            Object[] array = (Object[])Array.newInstance(Object.class, size);
            int index = 0;
            for (T item : this)
                array[index++] = item;
            return array;
        }
        return toList().toArray();
    }

    //
    // Element Operators
    //

    /**
     * Returns the first result of the query, or if none are found then a NoResultException exception is thrown.
     */
    public T first() {
        return first(anything);
    }

    /**
     * Returns the first result of the query that matches the provided condition, or if none are found then a
     * NoResultException exception is thrown.
     */
    public T first(Predicate<T> condition) {
        T first = firstOrNull(condition);
        if (first == null)
            throw new RuntimeException("Sequence contains no elements.");
        return first;
    }

    /**
     * Returns the first result of the query or null if none are found.
     */
    public T firstOrNull() {
        return firstOrNull(anything);
    }

    /**
     * Returns the first result of the query that matches the provided condition or null if none are found.
     */
    public T firstOrNull(Predicate<T> condition) {
        return firstOr(condition, null);
    }

    /**
     * Returns the first result of the query or null if none are found, wrapped in an Optional.
     */
    public Optional<T> firstOptional() {
        return firstOptional(anything);
    }

    /**
     * Returns the first result of the query that matches the provided condition or null if none are found, wrapped
     * in an Optional.
     */
    public Optional<T> firstOptional(Predicate<T> condition) {
        return Optional.ofNullable(firstOrNull(condition));
    }

    /**
     * Returns the first result of the query or the provided alternate if none are found.
     */
    public T firstOr(T alternate) {
        return firstOr(anything, alternate);
    }

    /**
     * Returns the first result of the query that matches the provided condition or the provided alternate if none are
     * found.
     */
    public T firstOr(Predicate<T> condition, T alternate) {
        for (T item : this)
            if (condition.test(item))
                return item;
        return alternate;
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
            throw new RuntimeException("Sequence contains no matching elements.");
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
        T last = alternate;
        for (T item : this)
            if (condition.test(item))
                last = item;
        return last;
    }

    /**
     * Returns the single result of the query, or if none are found then a NoResultException exception is thrown.  If
     * more than one result is found a NonUniqueResultException exception is thrown.
     */
    public T single() {
        return single(anything);
    }

    /**
     * Returns the single result of the query that matches the provided condition, or if none are found then a
     * NoResultException exception is thrown.  If more than one result is found a NonUniqueResultException exception is
     * thrown.
     */
    public T single(Predicate<T> condition) {
        T single = singleOrNull(condition);
        if (single == null)
            throw new RuntimeException("Sequence contains no matching elements.");
        return single;
    }

    /**
     * Returns the single result of the query or null if none are found.  If more than one result is found a
     * NonUniqueResultException exception is thrown.
     */
    public T singleOrNull() {
        return singleOrNull(anything);
    }

    /**
     * Returns the single result of the query that matches the provided condition or null if none are found.  If more
     * than one result is found a NonUniqueResultException exception is thrown.
     */
    public T singleOrNull(Predicate<T> condition) {
        return singleOr(condition, null);
    }

    /**
     * Returns the single result of the query or null if none are found, wrapped in an Optional.  If more than one
     * result is found a NonUniqueResultException exception is thrown.
     */
    public Optional<T> singleOptional() {
        return singleOptional(anything);
    }

    /**
     * Returns the single result of the query that matches the provided condition or null if none are found, wrapped
     * in an Optional.  If more than one result is found a NonUniqueResultException exception is thrown.
     */
    public Optional<T> singleOptional(Predicate<T> condition) {
        return Optional.ofNullable(singleOrNull(condition));
    }

    /**
     * Returns the single result of the query or the alternate if none are found.  If more than one result is found a
     * NonUniqueResultException exception is thrown.
     */
    public T singleOr(T alternate) {
        return singleOr(anything, alternate);
    }

    /**
     * Returns the single result of the query that matches the provided condition or the alternate if none are found.
     * If more than one result is found a NonUniqueResultException exception is thrown.
     */
    public T singleOr(Predicate<T> condition, T alternate) {
        int count = 0;
        T match = null;
        for (T item : this) {
            if (condition.test(item)) {
                count++;
                match = item;
            }
        }
        if (count > 1)
            throw new RuntimeException("Count: " + count);
        return count == 0 ? alternate : match;
    }

    /**
     * Returns the result at the provided index, or an IndexOutOfBoundsException is thrown if the index is out of range.
     */
    public T elementAt(int index) {
        T elementAt = elementAtOrNull(index);
        if (elementAt == null)
            throw new IndexOutOfBoundsException("No element at index: " + index);
        return elementAt;
    }

    /**
     * Returns the result at the provided index or null if the index is out of range.
     */
    public T elementAtOrNull(int index) {
        return elementAtOr(index, null);
    }

    /**
     * Returns the result at the provided index or null if the index is out of range, wrapped in an optional.
     */
    public Optional<T> elementAtOptional(int index) {
        return Optional.ofNullable(elementAtOrNull(index));
    }

    /**
     * Returns the result at the provided index or the alternate if the index is out of range.
     */
    public T elementAtOr(int index, T alternate) {
        int i = 0;
        for (T item : this)
            if (index == i++)
                return item;
        return alternate;
    }

    //
    // Quantifying Operators
    //

    /**
     * Returns true if there are any results, otherwise false.
     */
    public boolean any() {
        return any(anything);
    }

    /**
     * Returns true if any result is equal to the provided item, otherwise false.
     */
    public boolean any(T item) {
        return any(equalsThis.apply(item));
    }

    /**
     * Returns true if there are any results after applying the provided condition, otherwise false.
     */
    public boolean any(Predicate<T> condition) {
        for (T item : this)
            if (condition.test(item))
                return true;
        return false;
    }

    /**
     * Returns true if all results are equal to the provided item, otherwise false.
     */
    public boolean all(T item) {
        return all(equalsThis.apply(item));
    }

    /**
     * Returns true if all results pass the provided condition, otherwise false.
     */
    public boolean all(Predicate<T> condition) {
        for (T item : this)
            if (!condition.test(item))
                return false;
        return true;
    }

    /**
     * Return true if there are no results, otherwise false.
     */
    public boolean none() {
        return none(anything);
    }

    /**
     * Returns true if none of the results are equal to the provided item, otherwise false.
     */
    public boolean none(T item) {
        return none(equalsThis.apply(item));
    }

    /**
     * Returns true if none of the results pass the provided condition, otherwise false.
     */
    public boolean none(Predicate<T> condition) {
        for (T item : this)
            if (condition.test(item))
                return false;
        return true;
    }

    //
    // Aggregate operators
    //

    /**
     * Aggregates each item in the source according to the provided aggregation from a null seed.
     */
    public <A> A aggregate(BiFunction<A, T, A> aggregation) {
        return aggregate(null, aggregation);
    }

    /**
     * Aggregates each item in the source according to the provided aggregation from a provided seed.
     */
    public <A> A aggregate(A seed, BiFunction<A, T, A> aggregation) {
        return aggregateByIndex(seed, (a, t, i) -> aggregation.apply(a, t));
    }

    /**
     * Aggregates each item in the source according to the provided aggregation from a null seed.  The index of each
     * item in the source is included to be used in the aggregation.
     */
    public <A> A aggregateByIndex(TriFunction<A, T, Integer, A> aggregation) {
        return aggregateByIndex(null, aggregation);
    }

    /**
     * Aggregates each item in the source according to the provided aggregation from a provided seed.  The index of
     * each item in the source is included to be used in the aggregation.
     */
    public <A> A aggregateByIndex(A seed, TriFunction<A, T, Integer, A> aggregation) {
        A result = seed;
        int i = 0;
        for (T item : this)
            result = aggregation.apply(result, item, i++);
        return result;
    }

    /**
     * Aggregates each item in the source according to the provided aggregation from a null seed.  The context of each
     * item in the source is included to be used in the aggregation.
     */
    public <A> A aggregateByContext(TriFunction<A, T, QueryContext<T>, A> aggregation) {
        return aggregateByContext(null, aggregation);
    }

    /**
     * Aggregates each item in the source according to the provided aggregation from a provided seed.  The context of
     * each item in the source is included to be used in the aggregation.
     */
    public <A> A aggregateByContext(A seed, TriFunction<A, T, QueryContext<T>, A> aggregation) {
        A result = seed;
        QueryContextController<T> context = new QueryContextController<>(iterator());
        while (context.hasNext())
            result = aggregation.apply(result, context.next(), context);
        return result;
    }

    /**
     * Returns the number of items in the result.
     */
    public int count() {
        Integer size = getSizeSupplier().get();
        return size != null ? size : count(anything);
    }

    /**
     * Returns the number of items in the result that match the provided condition.
     */
    public int count(Predicate<T> condition) {
        int count = 0;
        for (T item : this)
            if (condition.test(item))
                count++;
        return count;
    }

    /**
     * Returns the sum of the non-null items in the result.  The items must be a Number type or an exception will be thrown.
     */
    public BigDecimal sum() {
        return sum(selfAsNumber);
    }

    /**
     * Returns the sum of the non-null projections of the provided selector for each item in the result.
     */
    public BigDecimal sum(Function<T, ? extends Number> selector) {
        BigDecimal sum = BigDecimal.ZERO;
        for (T item : this) {
            Number number = selector.apply(item);
            if (number != null)
                sum = sum.add(new BigDecimal(number.toString()));
        }
        return sum;
    }

    /**
     * Returns the average or mean of the non-null items in the result.  The items must be a Number type or an
     * exception will be thrown.  The default scale will be used for the result with HALF_UP rounding.
     */
    public BigDecimal average() {
        return average(null, null, selfAsNumber);
    }

    /**
     * Returns the average or mean of the non-null items in the result.  The items must be a Number type or an
     * exception will be thrown.  The provided scale and rounding will be used for the result or the default
     * scale or HALF_UP rounding wherever null is provided.
     */
    public BigDecimal average(Integer scale, RoundingMode roundingMode) {
        return average(scale, roundingMode, selfAsNumber);
    }

    /**
     * Returns the average or mean of the non-null projections of the provided selector for each item in the result.
     * The default scale will be used for the result with HALF_UP rounding.
     */
    public BigDecimal average(Function<T, ? extends Number> selector) {
        return average(null, null, selector);
    }

    /**
     * Returns the average or mean of the non-null projections of the provided selector for each item in the result.
     * The provided scale and rounding will be used for the result or the default scale or HALF_UP rounding wherever
     * null is provided.
     */
    public BigDecimal average(Integer scale, RoundingMode roundingMode, Function<T, ? extends Number> selector) {
        if (roundingMode == null)
            roundingMode = RoundingMode.HALF_UP;
        int count = 0;
        BigDecimal sum = null;
        for (T item : this) {
            Number number = selector.apply(item);
            if (number != null) {
                BigDecimal current = new BigDecimal(number.toString());
                sum = sum == null ? current : sum.add(current);
                count++;
            }
        }
        return sum == null ? null : scale == null
                ? sum.divide(new BigDecimal(count), roundingMode)
                : sum.divide(new BigDecimal(count), scale, roundingMode);
    }

    /**
     * Returns the median of the non-null items in the result.  The items must be a Number type or an exception will be
     * thrown.  The default scale will be used for the result with HALF_UP rounding.
     */
    public BigDecimal median() {
        return median(null, null, selfAsNumber);
    }

    /**
     * Returns the median of the non-null items in the result.  The items must be a Number type or an exception will be
     * thrown.  The provided scale and rounding will be used for the result or the default scale or HALF_UP rounding
     * wherever null is provided.
     */
    public BigDecimal median(Integer scale, RoundingMode roundingMode) {
        return median(scale, roundingMode, selfAsNumber);
    }

    /**
     * Returns the median of the non-null projections of the provided selector for each item in the result.  The
     * default scale will be used for the result with HALF_UP rounding.
     */
    public BigDecimal median(Function<T, ? extends Number> selector) {
        return median(null, null, selector);
    }

    /**
     * Returns the median of the non-null projections of the provided selector for each item in the result.  The
     * provided scale and rounding will be used for the result or the default scale or HALF_UP rounding wherever
     * null is provided.
     */
    public BigDecimal median(Integer scale, RoundingMode roundingMode, Function<T, ? extends Number> selector) {
        if (roundingMode == null)
            roundingMode = RoundingMode.HALF_UP;
        List<BigDecimal> list = new ArrayList<BigDecimal>();
        for (T item : this) {
            Number number = selector.apply(item);
            if (number != null)
                list.add(new BigDecimal(number.toString()));
        }
        if (list.isEmpty())
            return null;
        Collections.sort(list);
        BigDecimal middle = list.get(list.size() / 2);
        if (list.size() % 2 == 1)
            return middle;
        BigDecimal middleLow = list.get((list.size() - 1) / 2);
        return scale == null
                ? middle.add(middleLow).divide(new BigDecimal(2), roundingMode)
                : middle.add(middleLow).divide(new BigDecimal(2), scale, roundingMode);
    }

    /**
     * Returns the lowest value non-null item in the result.  The item must be a Number type or an exception will be
     * thrown.
     */
    public BigDecimal min() {
        return min(selfAsNumber);
    }

    /**
     * Returns the lowest value of the non-null projections of the provided selector for each item in the result.
     */
    public BigDecimal min(Function<T, ? extends Number> selector) {
        BigDecimal minimum = null;
        for (T item : this) {
            Number number = selector.apply(item);
            if (number != null) {
                BigDecimal current = new BigDecimal(number.toString());
                minimum = minimum == null ? current : minimum.min(current);
            }
        }
        return minimum;
    }

    /**
     * Returns the highest value non-null item in the result.  The item must be a Number type or an exception will be
     * thrown.
     */
    public BigDecimal max() {
        return max(selfAsNumber);
    }

    /**
     * Returns the highest value of the non-null projections of the provided selector for each item in the result.
     */
    public BigDecimal max(Function<T, ? extends Number> selector) {
        BigDecimal maximum = null;
        for (T item : this) {
            Number number = selector.apply(item);
            if (number != null) {
                BigDecimal current = new BigDecimal(number.toString());
                maximum = maximum == null ? current : maximum.max(current);
            }
        }
        return maximum;
    }

    //
    // Other operators
    //

    /**
     * Executes the provided doThis function if the provided condition is true.  Otherwise returns a new Query with
     * this Query as the source, effectively doing nothing.
     */
    public Query<T> when(boolean condition, Function<Query<T>, Query<T>> doThis) {
        return condition ? doThis.apply(this) : new Query<T>(getSizeSupplier(), getSourceReset(), this);
    }

    /**
     * Returns true if the length of the provided items is equal to the length of the source and each item in both
     * sequences is equal to the item sharing its index.
     */
    @SafeVarargs
    public final boolean sequenceEqual(T... items) {
        return sequenceEqual(Arrays.asList(items));
    }

    /**
     * Returns true if the length of the provided items is equal to the length of the source and each item in both
     * sequences is equal to the item sharing its index.
     */
    public boolean sequenceEqual(Iterable<? extends T> items) {
        return sequenceEqual(items, Objects::equals);
    }

    /**
     * Returns true if the length of the provided items is equal to the length of the source and each item in both
     * sequences is equal to the item sharing its index according to the provided matcher.
     */
    public <R> boolean sequenceEqual(Iterable<R> items, BiPredicate<T, R> matcher) {
        return sequenceEqualByIndex(items, (t, r, i) -> matcher.test(t, r));
    }

    /**
     * Returns true if the length of the provided items is equal to the length of the source and each item in both
     * sequences is equal to the item sharing its index according to the provided matcher.  The index of each item in
     * the source is included to be used in the condition.
     */
    public <R> boolean sequenceEqualByIndex(Iterable<R> items, TriPredicate<T, R, Integer> matcher) {
        Objects.requireNonNull(items);
        Integer size = getSizeSupplier().get();
        Integer rightSize = findSize(items);
        if (size != null && rightSize != null && !size.equals(rightSize))
            return false;

        Iterator<T> leftIterator = iterator();
        Iterator<R> rightIterator = items.iterator();
        for (int i = 0; leftIterator.hasNext() && rightIterator.hasNext(); i++) {
            if (!matcher.test(leftIterator.next(), rightIterator.next(), i))
                return false;
            if (leftIterator.hasNext() != rightIterator.hasNext())
                return false;
        }
        return true;
    }

    /**
     * Returns string by converting each item in the source to a String and separating them with the provided separator.
     */
    public String toString(String separator) {
        Objects.requireNonNull(separator);
        List<String> strings = new ArrayList<String>();
        for (T item : this)
            strings.add(item == null ? "null" : item.toString());
        return String.join(separator, strings);
    }

    @Override
    public String toString() {
        return "Query{source=" + (source != null ? source : "null") + "}";
    }

    //
    // Generation Operators
    //

    /**
     * Returns an empty Query.
     */
    public static <T> Query<T> empty() {
        return new Query<T>(() -> new Iterator<T>() {
            @Override
            public boolean hasNext() { return false; }
            @Override
            public T next() { throw new NoSuchElementException(); }
        });
    }

    /**
     * Returns a Query with a source of sequential Integers that begin at the provided start and continue for the
     * provided length.  If the length is negative then the sequence will be descending from the start.
     */
    public static Query<Integer> range(int start, int length) {
        if (length == 0)
            return Query.empty();
        return new Query<Integer>(() -> new RangeIterator(start, start + length + (length < 0 ? +1 : -1)));
    }

    /**
     * Returns a Query with a source of sequential Integers that begin at the provided start and continue until the
     * provided end.  If the end is less than the start then the sequence will be descending.
     */
    public static Query<Integer> rangeBetween(int start, int end) {
        return new Query<Integer>(() -> new RangeIterator(start, end));
    }

    /**
     * Generates a Query with a source of the provided value repeated the number of provided iterations.
     */
    public static <T> Query<T> generate(int iterations, T value) {
        if (iterations < 0)
            throw new RuntimeException("Generate iterations cannot be less than zero.");
        return new Query<T>(() -> new GenerateIterator<T>(iterations, null, (t, i) -> value));
    }

    /**
     * Generates a Query with a source of items built by the provided generator and a null seed for the number of
     * provided iterations.
     */
    public static <T> Query<T> generate(int iterations, Function<T, T> generator) {
        if (iterations < 0)
            throw new RuntimeException("Generate iterations cannot be less than zero.");
        return new Query<T>(() -> new GenerateIterator<T>(iterations, null, (t, i) -> generator.apply(t)));
    }

    /**
     * Generates a Query with a source of items built by the provided generator and the provided seed for the number of
     * provided iterations.
     */
    public static <T> Query<T> generate(int iterations, T seed, Function<T, T> generator) {
        if (iterations < 0)
            throw new RuntimeException("Generate iterations cannot be less than zero.");
        return new Query<T>(() -> new GenerateIterator<T>(iterations, seed, (t, i) -> generator.apply(t)));
    }

    /**
     * Generates a Query with a source of items built by the provided generator and a null seed until the provided
     * isDone predicate is true.
     */
    public static <T> Query<T> generate(Function<T, T> generator, Predicate<T> isDone) {
        return new Query<T>(() -> new GenerateIterator<T>(isDone, null, (t, i) -> generator.apply(t)));
    }

    /**
     * Generates a Query with a source of items built by the provided generator and the provided seed until the
     * provided  isDone predicate is true.
     */
    public static <T> Query<T> generate(T seed, Function<T, T> generator, Predicate<T> isDone) {
        return new Query<T>(() -> new GenerateIterator<T>(isDone, seed, (t, i) -> generator.apply(t)));
    }

    /**
     * Generates a Query with a source of items built by the provided generator and a null seed for the number of
     * provided iterations.  The index of each item in the source is included to be used in the generator.
     */
    public static <T> Query<T> generateByIndex(int iterations, BiFunction<T, Integer, T> generator) {
        if (iterations < 0)
            throw new RuntimeException("Generate iterations cannot be less than zero.");
        return new Query<T>(() -> new GenerateIterator<T>(iterations, null, generator));
    }

    /**
     * Generates a Query with a source of items built by the provided generator and a provided seed for the number of
     * provided iterations.  The index of each item in the source is included to be used in the generator.
     */
    public static <T> Query<T> generateByIndex(int iterations, T seed, BiFunction<T, Integer, T> generator) {
        if (iterations < 0)
            throw new RuntimeException("Generate iterations cannot be less than zero.");
        return new Query<T>(() -> new GenerateIterator<T>(iterations, seed, generator));
    }

    /**
     * Generates a Query with a source of items built by the provided generator and a null seed until the provided
     * isDone predicate is true.  The index of each item in the source is included to be used in the generator.
     */
    public static <T> Query<T> generateByIndex(BiFunction<T, Integer, T> generator, Predicate<T> isDone) {
        return new Query<T>(() -> new GenerateIterator<T>(isDone, null, generator));
    }

    /**
     * Generates a Query with a source of items built by the provided generator and a provided seed until the provided
     * isDone predicate is true.  The index of each item in the source is included to be used in the generator.
     */
    public static <T> Query<T> generateByIndex(T seed, BiFunction<T, Integer, T> generator, Predicate<T> isDone) {
        return new Query<T>(() -> new GenerateIterator<T>(isDone, seed, generator));
    }

    //
    // Function Helpers
    //

    // Tries to find the size of the given iterable by checking if it is a Collection, Map, or another Query.
    static Integer findSize(Iterable iterable) {
        Integer size = null;
        if (iterable instanceof Collection)
            size = ((Collection)iterable).size();
        else if (iterable instanceof Map)
            size = ((Map)iterable).size();
        else if (iterable instanceof Query)
            size = (Integer)((Query)iterable).getSizeSupplier().get();
        return size;
    }

    private Function<T, T> selectSelf = t -> t;

    private BiFunction<T, Integer, T> selectSelfByIndex = (t, i) -> t;

    private Predicate<T> anything = t -> true;

    private Function<T, Predicate<T>> equalsThis = x -> y -> Objects.equals(x, y);

    private Function<T, Number> selfAsNumber = x -> {
        if (x instanceof Number)
            return (Number)x;
        throw new RuntimeException("Cannot execute numeric aggregation directly because " + (x != null ? x.getClass().getSimpleName() : "T") + " is not a Number.");
    };

    private Supplier<Integer> unknownSize = () -> null;

    // Used to build a sizeSupplier for skip/take/exclude operations by modifying the previous size when available.
    private Supplier<Integer> getPartitioningSupplier(int startIndex, int endIndex, boolean isTaking) {
        return () -> {
            Integer size = getSizeSupplier().get();
            if (size == null)
                return null;
            int prefix = Math.min(startIndex, size);
            int length = Math.max(endIndex - startIndex + 1, 0);
            if (isTaking)
                return Math.min(size - prefix, length);
            int suffix = Math.max(size - (prefix + length), 0);
            return prefix + suffix;
        };
    }

    // Used to build a sizeSupplier for combine/insert operations by adding the sizes together when available.
    private Supplier<Integer> getCombineSupplier(Iterable additions) {
        return () -> {
            Integer size = getSizeSupplier().get();
            if (size == null)
                return null;
            Integer additionsSize = findSize(additions);
            return additionsSize != null ? size + additionsSize : null;
        };
    }

    // Used to build a sizeSupplier for group join operations based on the join type and the left size when available.
    private Supplier<Integer> getGroupJoinSupplier(JoinType joinType) {
        return () -> joinType == JoinType.LEFT ? getSizeSupplier().get() : null;
    }

    // Used to build a sizeSupplier for zip operations based on the join type and the left and right sizes when
    // available.
    private Supplier<Integer> getZipSupplier(JoinType joinType, Iterable rightItems) {
        return () -> {
            Integer leftSize = getSizeSupplier().get();
            if (joinType == JoinType.LEFT)
                return leftSize;
            Integer rightSize = findSize(rightItems);
            if (joinType == JoinType.RIGHT)
                return rightSize;
            if (leftSize != null && rightSize != null) {
                if (joinType == JoinType.INNER)
                    return Math.min(leftSize, rightSize);
                if (joinType == JoinType.OUTER)
                    return Math.max(leftSize, rightSize);
            }
            return null;
        };
    }

    // wraps the query as a collection to easily pass size information.  if the query has an unknown size then an
    // exception is likely to be thrown.
    private Collection<T> asCollection() {
        return new AbstractCollection<T>() {
            @Override
            @SuppressWarnings("NullableProblems")
            public Iterator<T> iterator() {
                return Query.this.iterator();
            }

            @Override
            public int size() {
                Integer size = getSizeSupplier().get();
                if (size == null)
                    throw new RuntimeException("Unexpected use of query.asCollection() when query has no size!");
                return size;
            }
        };
    }
}
