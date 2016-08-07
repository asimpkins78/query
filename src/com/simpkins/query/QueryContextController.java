package com.simpkins.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class QueryContextController<T> implements QueryContext<T>, Iterator<T> {
    private Iterator<T> iterator;
    // we roll over to index=0 when the first object is fetched.
    private int index = -1;
    // stores the items we've already iterated through.
    private List<T> cache;

    protected QueryContextController(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    // don't create the cache unless we need to.
    private List<T> getCache() {
        if (cache == null)
            cache = new ArrayList<>();
        return cache;
    }

    // see if we've already cached the next item.
    private boolean hasNextInCache() {
        return cache != null && getCache().size() > index + 1;
    }

    // not in QueryContext interface because we only want the internal query tools to advance the index.
    @Override
    public T next() {
        T next = getNext();
        index++;
        return next;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public boolean isFirst() {
        return index == 0;
    }

    @Override
    public boolean hasPrevious() {
        return !isFirst();
    }

    @Override
    public T getPrevious() {
        if (isFirst())
            throw new NoSuchElementException();
        return cache.get(index - 1);
    }

    @Override
    public boolean isLast() {
        return !hasNext();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext() || hasNextInCache();
    }

    @Override
    public T getNext() {
        if (hasNextInCache()) {
            return getCache().get(index + 1);
        }
        else {
            T next = iterator.next();
            getCache().add(next);
            return next;
        }
    }
}
