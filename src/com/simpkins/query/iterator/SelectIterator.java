package com.simpkins.query.iterator;

import java.util.Iterator;
import java.util.function.Function;

public class SelectIterator<T, S> implements Iterator<S> {
    private Iterator<T> source;
    private Function<T, S> selector;

    public SelectIterator(Iterator<T> source, Function<T, S> selector) {
        this.source = source;
        this.selector = selector;
    }

    @Override
    public boolean hasNext() {
        return source.hasNext();
    }

    @Override
    public S next() {
        return selector.apply(source.next());
    }
}

