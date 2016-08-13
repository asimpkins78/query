package simpkins.query.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class SelectManyIterator<T, S> implements Iterator<S> {
    private Iterator<T> source;
    private Function<T, ? extends Iterable<S>> selector;
    private boolean isCurrentReady = false;
    private Iterator<S> current = null;

    public SelectManyIterator(Iterator<T> source, Function<T, ? extends Iterable<S>> selector) {
        this.source = source;
        this.selector = selector;
    }

    @Override
    public boolean hasNext() {
        while (!isCurrentReady || !current.hasNext()) {
            if (!source.hasNext())
                return false;
            current = selector.apply(source.next()).iterator();
            isCurrentReady = true;
        }
        return current.hasNext();
    }

    @Override
    public S next() {
        if (!isCurrentReady && !hasNext())
            throw new NoSuchElementException();
        return current.next();
    }
}

