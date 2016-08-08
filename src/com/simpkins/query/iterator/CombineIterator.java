package com.simpkins.query.iterator;

import java.util.Iterator;

public class CombineIterator<T> implements Iterator<T> {
    private Iterator<? extends T> source;
    private Iterator<? extends T> additions;
    private Integer insertIndex = null;
    private int index = 0;

    public CombineIterator(Iterator<? extends T> source, Iterator<? extends T> additions) {
        this.source = source;
        this.additions = additions;
    }

    public CombineIterator(Iterator<? extends T> source, Iterator<? extends T> additions, Integer insertIndex) {
        this.source = source;
        this.additions = additions;
        this.insertIndex = insertIndex;
    }

    @Override
    public boolean hasNext() {
        return source.hasNext() || additions.hasNext();
    }

    @Override
    public T next() {
        if (insertIndex == null || !source.hasNext())
            return source.hasNext() ? source.next() : additions.next();
        if (insertIndex <= index && additions.hasNext())
            return additions.next();
        index++;
        return source.next();
    }
}

