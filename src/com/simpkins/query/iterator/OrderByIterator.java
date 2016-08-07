package simpkins.query.iterator;

import simpkins.query.Ordering;

import java.util.*;
import java.util.function.Consumer;

public class OrderByIterator<T> implements Iterator<T> {
    private Iterator<T> source;
    private List<Ordering<T, ? extends Comparable<?>>> orderings;
    private Consumer<List<T>> sorter;
    private Integer sourceSize;
    private int index = 0;
    private List<T> orderedList = null;

    public OrderByIterator(Iterator<T> source, List<Ordering<T, ? extends Comparable<?>>> orderings, Integer sourceSize) {
        this.source = source;
        this.orderings = orderings;
        this.sourceSize = sourceSize;
    }

    public OrderByIterator(Iterator<T> source, Consumer<List<T>> sorter, Integer sourceSize) {
        this.source = source;
        this.sorter = sorter;
        this.sourceSize = sourceSize;
    }

    public List<T> getOrderedList() {
        if (orderedList == null) {
            orderedList = sourceSize != null ? new ArrayList<>(sourceSize) : new ArrayList<>();
            while (source.hasNext())
                orderedList.add(source.next());
            if (sorter != null) {
                sorter.accept(orderedList);
            }
            else {
                for (int i = orderings.size() - 1; i >= 0; i--)
                    Collections.sort(orderedList, orderings.get(i).toComparator());
            }
        }
        return orderedList;
    }

    @Override
    public boolean hasNext() {
        return orderedList != null ? orderedList.size() > index : source.hasNext();
    }

    @Override
    public T next() {
        if (!hasNext())
            throw new NoSuchElementException();
        return getOrderedList().get(index++);
    }
}
