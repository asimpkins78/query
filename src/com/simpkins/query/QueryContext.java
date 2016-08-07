package com.simpkins.query;

public interface QueryContext<T> {
    int getIndex();
    boolean isFirst();
    boolean hasPrevious();
    T getPrevious();
    boolean isLast();
    boolean hasNext();
    T getNext();
}
