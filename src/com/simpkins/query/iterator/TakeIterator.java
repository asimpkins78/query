package com.simpkins.query.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class TakeIterator<T> implements Iterator<T> {
    private Iterator<T> source;
    private Predicate<T> condition;
    private boolean isDoneTaking = false;
    private boolean isNextReady = false;
    private T next = null;

    public TakeIterator(Iterator<T> source, Predicate<T> condition) {
        this.source = source;
        this.condition = condition;
    }

    @Override
    public boolean hasNext() {
        if (isNextReady)
            return true;
        if (isDoneTaking || !source.hasNext())
            return false;
        next = source.next();
        if (condition.test(next))
            return isNextReady = true;
        isDoneTaking = true;
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
