package simpkins.query.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class RangeIterator implements Iterator<Integer> {
    private int start;
    private int end;
    private boolean isDescending;
    private int index = 0;

    public RangeIterator(int start, int end) {
        this.start = start;
        this.end = end;
        this.isDescending = start > end;
    }

    @Override
    public boolean hasNext() {
        return isDescending ? start + index >= end : start + index <= end;
    }

    @Override
    public Integer next() {
        if (!hasNext())
            throw new NoSuchElementException();
        return start + ((isDescending ? index-- : index++));
    }
}
