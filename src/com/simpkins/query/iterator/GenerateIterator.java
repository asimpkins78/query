package com.simpkins.query.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class GenerateIterator<T> implements Iterator<T> {
    private Integer iterations;
    private Predicate<T> isDone;
    private T seed;
    private BiFunction<T, Integer, T> generator;
    private int index = 0;
    private boolean isNextReady = false;
    private boolean isNextLast = false;

    public GenerateIterator(int iterations, T seed, BiFunction<T, Integer, T> generator) {
        this.iterations = iterations;
        this.seed = seed;
        this.generator = generator;
    }

    public GenerateIterator(Predicate<T> isDone, T seed, BiFunction<T, Integer, T> generator) {
        this.isDone = isDone;
        this.seed = seed;
        this.generator = generator;
    }

    @Override
    public boolean hasNext() {
        if (iterations != null)
            return iterations > index;
        if (isNextReady)
            return true;
        if (isNextLast)
            return false;
        seed = generator.apply(seed, index++);
        isNextReady = true;
        return isNextLast = isDone.test(seed);
    }

    @Override
    public T next() {
        if (iterations != null) {
            if (!hasNext())
                throw new NoSuchElementException();
            return seed = generator.apply(seed, index++);
        }
        if (!isNextReady && !hasNext())
            throw new NoSuchElementException();
        isNextReady = false;
        return seed;
    }
}
