package com.simpkins.query.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ExcludeIterator<T> implements Iterator<T> {
    private Iterator<T> source;
    private int startIndex;
    private int endIndex;
    private int index = 0;
    private boolean isNextReady = false;
    private T next = null;

    public ExcludeIterator(Iterator<T> source, int startIndex, int endIndex) {
        this.source = source;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    public boolean hasNext() {
        if (isNextReady)
            return true;
        while (source.hasNext() && startIndex <= index && index <= endIndex) {
            index++;
            next = source.next();
        }
        if (!source.hasNext())
            return false;
        index++;
        next = source.next();
        return isNextReady = true;
    }

    @Override
    public T next() {
        if (!isNextReady && !hasNext())
            throw new NoSuchElementException();
        isNextReady = false;
        return next;
    }
}
