package simpkins.query;

import java.util.Comparator;
import java.util.function.Function;

public class Ordering<T, S extends Comparable<S>> {
    private Function<T, Comparable> selector;
    private Comparator<T> comparator;
    private boolean isDescending;
    private boolean reverseNulls;

    Ordering(boolean isDescending, boolean reverseNulls) {
        this.isDescending = isDescending;
        this.reverseNulls = reverseNulls;

        // Assume that T is a Comparable.  If the cast fails then use a dummy Comparable that does nothing.
        this.selector = new Function<T, Comparable>() {
            boolean isComparable = true;

            @Override
            public Comparable apply(T t) {
                try {
                    if (isComparable || t == null)
                        return (Comparable)t;
                } catch (ClassCastException e) {
                    // just handle the exception once
                    isComparable = false;
                }
                // dummy Comparable
                return x -> 0;
            }
        };
    }

    Ordering(Function<T, S> selector, boolean isDescending, boolean reverseNulls) {
        this.selector = x -> selector.apply(x);
        this.isDescending = isDescending;
        this.reverseNulls = reverseNulls;
    }

    Ordering(Comparator<T> comparator, boolean isDescending) {
        this.comparator = comparator;
        this.isDescending = isDescending;
    }

    public Comparator<T> toComparator() {
        if (comparator != null)
            return !isDescending ? comparator : new Comparator<T>() {
                @Override
                public int compare(T o1, T o2) {
                    return -1 * comparator.compare(o1, o2);
                }

                @Override
                @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
                public boolean equals(Object obj) {
                    return comparator.equals(obj);
                }
            };
        return (item1, item2) -> {
            Comparable select1 = selector.apply(item1);
            Comparable select2 = selector.apply(item2);
            if (select1 == null && select2 == null)
                return 0;
            if (select1 == null)
                return isDescending == reverseNulls ? 1 : -1;
            if (select2 == null)
                return isDescending == reverseNulls ? -1 : 1;
            //noinspection unchecked
            return isDescending ? select2.compareTo(select1) : select1.compareTo(select2);
        };
    }

    @Override
    public String toString() {
        return "Ordering{" +
                "selector=" + selector +
                ", comparator=" + comparator +
                ", isDescending=" + isDescending +
                ", reverseNulls=" + reverseNulls +
                '}';
    }
}
