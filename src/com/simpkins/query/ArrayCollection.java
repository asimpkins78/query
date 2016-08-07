package com.simpkins.query;

import simpkins.query.iterator.ArrayIterator;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.function.Supplier;

/**
 * A simple read-only Collection wrapper for arrays.  This is particularly useful for primitive arrays since they don't
 * work well with Arrays.asList().
 */
@SuppressWarnings({"UnusedDeclaration", "Convert2Diamond"})
public class ArrayCollection<T> extends AbstractCollection<T> {
    private Supplier<ArrayIterator<T>> iteratorSupplier;
    private int size;

    public ArrayCollection(T[] array) {
        this(() -> new ArrayIterator<T>(array), array.length);
    }

    public ArrayCollection(boolean[] array) {
        this(() -> new ArrayIterator<T>(array), array.length);
    }

    public ArrayCollection(byte[] array) {
        this(() -> new ArrayIterator<T>(array), array.length);
    }

    public ArrayCollection(short[] array) {
        this(() -> new ArrayIterator<T>(array), array.length);
    }

    public ArrayCollection(int[] array) {
        this(() -> new ArrayIterator<T>(array), array.length);
    }

    public ArrayCollection(long[] array) {
        this(() -> new ArrayIterator<T>(array), array.length);
    }

    public ArrayCollection(float[] array) {
        this(() -> new ArrayIterator<T>(array), array.length);
    }

    public ArrayCollection(double[] array) {
        this(() -> new ArrayIterator<T>(array), array.length);
    }

    public ArrayCollection(char[] array) {
        this(() -> new ArrayIterator<T>(array), array.length);
    }

    private ArrayCollection(Supplier<ArrayIterator<T>> iteratorSupplier, int size) {
        this.iteratorSupplier = iteratorSupplier;
        this.size = size;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public Iterator<T> iterator() {
        return iteratorSupplier.get();
    }

    @Override
    public int size() {
        return size;
    }
}
