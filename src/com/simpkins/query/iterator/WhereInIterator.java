package com.simpkins.query.iterator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class WhereInIterator<T, S> implements Iterator<T> {
    private Iterator<T> source;
    private Iterable<? extends T> container;
    private Function<T, S> selector;
    private BiPredicate<T, T> matcher;
    private boolean isInclusive;
    private Set<S> containerSet = null;
    private boolean isNextReady = false;
    private T next = null;

    public WhereInIterator(Iterator<T> source, Iterable<? extends T> container, Function<T, S> selector, boolean isInclusive) {
        this.source = source;
        this.container = container;
        this.selector = selector;
        this.isInclusive = isInclusive;
    }

    public WhereInIterator(Iterator<T> source, Iterable<? extends T> container, BiPredicate<T, T> matcher, boolean isInclusive) {
        this.source = source;
        this.container = container;
        this.matcher = matcher;
        this.isInclusive = isInclusive;
    }

    private Set<S> getContainerSet() {
        if (containerSet == null) {
            containerSet = new HashSet<>();
            for (T item : container)
                containerSet.add(selector.apply(item));
        }
        return containerSet;
    }

    @Override
    public boolean hasNext() {
        if (isNextReady)
            return true;
        if (!source.hasNext())
            return false;
        top:
        while (source.hasNext()) {
            next = source.next();
            if (selector != null) {
                if (getContainerSet().contains(selector.apply(next)) == isInclusive)
                    return isNextReady = true;
            }
            else if (isInclusive) {
                for (T item : container)
                    if (matcher.test(next, item))
                        return isNextReady = true;
            }
            else {
                for (T item : container)
                    if (matcher.test(next, item))
                        continue top;
                return isNextReady = true;
            }
        }
        return isNextReady = false;
    }

    @Override
    public T next() {
        if (!isNextReady && !hasNext())
            throw new NoSuchElementException();
        isNextReady = false;
        return next;
    }
}
