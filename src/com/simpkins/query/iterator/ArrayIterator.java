package com.simpkins.query.iterator;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayIterator<T> implements Iterator<T> {
    private T[] array = null;
    private Object primitiveArray = null;
    private int index = 0;

    public ArrayIterator(T[] array) {
        this.array = array;
    }

    public ArrayIterator(boolean[] array) {
        this.primitiveArray = array;
    }

    public ArrayIterator(byte[] array) {
        this.primitiveArray = array;
    }

    public ArrayIterator(short[] array) {
        this.primitiveArray = array;
    }

    public ArrayIterator(int[] array) {
        this.primitiveArray = array;
    }

    public ArrayIterator(long[] array) {
        this.primitiveArray = array;
    }

    public ArrayIterator(float[] array) {
        this.primitiveArray = array;
    }

    public ArrayIterator(double[] array) {
        this.primitiveArray = array;
    }

    public ArrayIterator(char[] array) {
        this.primitiveArray = array;
    }

    @Override
    public boolean hasNext() {
        return index < (array != null ? array.length : Array.getLength(primitiveArray));
    }

    @Override
    public T next() {
        if (!hasNext())
            throw new NoSuchElementException();
        //noinspection unchecked
        return array != null ? array[index++] : (T)Array.get(primitiveArray, index++);
    }
}
