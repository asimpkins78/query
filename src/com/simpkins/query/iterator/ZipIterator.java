package com.simpkins.query.iterator;

import com.simpkins.query.JoinType;
import com.simpkins.query.Tuple;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ZipIterator<L, R> implements Iterator<Tuple<L, R>> {
    private Iterator<L> leftSource;
    private JoinType joinType;
    private Iterator<R> rightSource;

    public ZipIterator(Iterator<L> leftSource, JoinType joinType, Iterator<R> rightSource) {
        this.leftSource = leftSource;
        this.joinType = joinType;
        this.rightSource = rightSource;
    }

    @Override
    public boolean hasNext() {
        switch (joinType) {
            case INNER: return leftSource.hasNext() && rightSource.hasNext();
            case LEFT: return leftSource.hasNext();
            case RIGHT: return rightSource.hasNext();
            case OUTER: return leftSource.hasNext() || rightSource.hasNext();
            default: throw new RuntimeException("Unsupported joinType: " + joinType);
        }
    }

    @Override
    public Tuple<L, R> next() {
        if (!hasNext())
            throw new NoSuchElementException();
        return Tuple.create(leftSource.hasNext() ? leftSource.next() : null, rightSource.hasNext() ? rightSource.next() : null);
    }
}
