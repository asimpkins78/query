package simpkins.query.iterator;

import simpkins.query.JoinType;
import simpkins.query.Tuple;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class JoinIterator<L, R> implements Iterator<Tuple<L, R>> {
    private Iterator<L> leftSource;
    private Iterator<R> rightSource;
    private JoinType joinType;
    private BiPredicate<L, R> matcher;
    private Integer rightSourceSize;
    private boolean isNextReady = false;
    private Tuple<L, R> next = null;
    private boolean isLeftReady = false;
    private L left = null;
    private boolean isLeftIncluded = false;
    private int rightIndex = 0;
    private Map<Integer, R> rightByIndex = null;
    private Set<Integer> rightIndexesIncluded = null;

    public <K> JoinIterator(Iterator<L> leftSource, JoinType joinType, Iterator<R> rightSource, Function<L, K> leftSelector, Function<R, K> rightSelector, Integer rightSourceSize) {
        this(leftSource, joinType, rightSource, (l, r) -> Objects.equals(leftSelector.apply(l), rightSelector.apply(r)), rightSourceSize);
    }

    public JoinIterator(Iterator<L> leftSource, JoinType joinType, Iterator<R> rightSource, BiPredicate<L, R> matcher, Integer rightSourceSize) {
        this.leftSource = leftSource;
        this.rightSource = rightSource;
        this.joinType = joinType;
        this.matcher = matcher;
        this.rightSourceSize = rightSourceSize;
    }

    private Map<Integer, R> getRightByIndex() {
        if (rightByIndex == null) {
            rightByIndex = rightSourceSize != null ? new LinkedHashMap<>(rightSourceSize) : new LinkedHashMap<>();
            for (int i = 0; rightSource.hasNext(); i++)
                rightByIndex.put(i, rightSource.next());
        }
        return rightByIndex;
    }

    private Set<Integer> getRightIndexesIncluded() {
        if (rightIndexesIncluded == null)
            rightIndexesIncluded = new HashSet<>();
        return rightIndexesIncluded;
    }

    private boolean rightHasNext() {
        return rightIndex < getRightByIndex().size();
    }

    private R rightNext() {
        return getRightByIndex().get(rightIndex++);
    }

    @Override
    public boolean hasNext() {
        if (isNextReady)
            return true;

        while (isLeftReady || leftSource.hasNext()) {
            if (!isLeftReady) {
                left = leftSource.next();
                isLeftIncluded = false;
                isLeftReady = true;
            }

            while (rightHasNext()) {
                R right = rightNext();
                if (matcher.test(left, right)) {
                    isLeftIncluded = true;
                    if (joinType.isRight())
                        getRightIndexesIncluded().add(rightIndex - 1);
                    next = Tuple.create(left, right);
                    return isNextReady = true;
                }
            }

            rightIndex = 0;
            isLeftReady = false;
            if (joinType.isLeft() && !isLeftIncluded) {
                next = Tuple.create(left, null);
                return isNextReady = true;
            }
        }

        while (joinType.isRight() && rightHasNext()) {
            R right = rightNext();
            if (!getRightIndexesIncluded().contains(rightIndex - 1)) {
                next = Tuple.create(null, right);
                return isNextReady = true;
            }
        }

        return false;
    }

    @Override
    public Tuple<L, R> next() {
        if (!isNextReady && !hasNext())
            throw new NoSuchElementException();
        isNextReady = false;
        return next;
    }
}
