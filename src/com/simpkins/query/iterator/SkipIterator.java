package com.simpkins.query.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class SkipIterator<T> implements Iterator<T> {
    private Iterator<T> source;
    private Predicate<T> condition;
    private boolean isDoneSkipping = false;
    private boolean isNextReady = false;
    private T next = null;

    public SkipIterator(Iterator<T> source, Predicate<T> condition) {
        this.source = source;
        this.condition = condition;
    }

    @Override
    public boolean hasNext() {
        if (isNextReady)
            return true;
        if (!source.hasNext())
            return false;
        if (isDoneSkipping) {
            if (source.hasNext()) {
                next = source.next();
                return isNextReady = true;
            }
            return isNextReady = false;
        }
        while (source.hasNext()) {
            next = source.next();
            if (!condition.test(next))
                return isDoneSkipping = isNextReady = true;
        }
        return false;
    }

    @Override
    public T next() {
        if (!isNextReady && !hasNext())
            throw new NoSuchElementException();
        isNextReady = false;
        return next;
    }
}
