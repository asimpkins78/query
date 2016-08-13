package simpkins.query.iterator;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class DistinctIterator<T, S> implements Iterator<T> {
    private Iterator<T> source;
    private Function<T, S> selector;
    private BiPredicate<T, T> matcher;
    private List<T> iteratedList = null;
    private Set<S> iteratedSet = null;
    private boolean isNextReady = false;
    private T next = null;
    private S nextSelected = null;

    public DistinctIterator(Iterator<T> source, Function<T, S> selector) {
        this.source = source;
        this.selector = selector;
        this.iteratedSet = new HashSet<>();
    }

    public DistinctIterator(Iterator<T> source, BiPredicate<T, T> matcher) {
        this.source = source;
        this.matcher = matcher;
        this.iteratedList = new ArrayList<>();
    }

    @Override
    public boolean hasNext() {
        if (isNextReady)
            return true;
        if (!source.hasNext())
            return false;
        top:
        while (source.hasNext()) {
            next = source.next();
            if (selector != null) {
                nextSelected = selector.apply(next);
                if (iteratedSet.contains(nextSelected))
                    continue;
            }
            else {
                for (T previous : iteratedList)
                    if (matcher.test(next, previous))
                        continue top;
            }

            return isNextReady = true;
        }
        return isNextReady = false;
    }

    @Override
    public T next() {
        if (!isNextReady && !hasNext())
            throw new NoSuchElementException();
        isNextReady = false;
        if (selector != null)
            iteratedSet.add(nextSelected);
        else
            iteratedList.add(next);
        return next;
    }
}
