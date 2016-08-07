package com.simpkins.query;

import simpkins.query.iterator.OrderByIterator;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings({"UnusedDeclaration", "Convert2Diamond"})
public class OrderedQuery<T> extends Query<T> {
    protected Iterable<T> preOrderingSource;
    protected List<Ordering<T, ? extends Comparable<?>>> orderings;

    @SafeVarargs
    protected OrderedQuery(Supplier<Integer> sizeSupplier, Consumer<Iterable> sourceReset, Iterable<T> preOrderingSource, Ordering<T, ? extends Comparable<?>>... orderings) {
        this(sizeSupplier, sourceReset, preOrderingSource, Arrays.asList(orderings));
    }

    protected OrderedQuery(Supplier<Integer> sizeSupplier, Consumer<Iterable> sourceReset, Iterable<T> preOrderingSource, List<Ordering<T, ? extends Comparable<?>>> orderings) {
        super(sizeSupplier, sourceReset, () -> new OrderByIterator<T>(preOrderingSource.iterator(), orderings, sizeSupplier.get()));
        this.preOrderingSource = preOrderingSource;
        this.orderings = orderings;
    }

    public <S extends Comparable<S>> OrderedQuery<T> thenBy(Function<T, S> selector) {
        return stackOrdering(new Ordering<T, S>(selector, false, false));
    }

    public <S extends Comparable<S>> OrderedQuery<T> thenByNullsFirst(Function<T, S> selector) {
        return stackOrdering(new Ordering<T, S>(selector, false, true));
    }

    public <S extends Comparable<S>> OrderedQuery<T> thenBy(Comparator<T> comparator) {
        return stackOrdering(new Ordering<T, S>(comparator, false));
    }

    public <S extends Comparable<S>> OrderedQuery<T> thenByDescending(Function<T, S> selector) {
        return stackOrdering(new Ordering<T, S>(selector, true, false));
    }

    public <S extends Comparable<S>> OrderedQuery<T> thenByDescendingNullsLast(Function<T, S> selector) {
        return stackOrdering(new Ordering<T, S>(selector, true, true));
    }

    public <S extends Comparable<S>> OrderedQuery<T> thenByDescending(Comparator<T> comparator) {
        return stackOrdering(new Ordering<T, S>(comparator, true));
    }

    private <S extends Comparable<S>> OrderedQuery<T> stackOrdering(Ordering<T, S> newOrdering) {
        return new OrderedQuery<T>(getSizeSupplier(), getSourceReset(), preOrderingSource, from(orderings).combine(newOrdering).toList());
    }
}
