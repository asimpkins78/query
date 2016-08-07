package simpkins.query.iterator;

import simpkins.query.JoinType;
import simpkins.query.QueryList;
import simpkins.query.Tuple;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class GroupJoinIterator<L, R> implements Iterator<Tuple<L, QueryList<R>>> {
    private Iterator<L> leftSource;
    private Iterator<R> rightSource;
    private JoinType joinType;
    private BiPredicate<L, R> matcher;
    private Integer rightSourceSize;
    private boolean isNextReady = false;
    private Tuple<L, QueryList<R>> next = null;
    private Map<Integer, R> rightByIndex = null;
    private Set<Integer> rightIndexesIncluded = null;
    private boolean isRightDone = false;

    public <K> GroupJoinIterator(Iterator<L> leftSource, JoinType joinType, Iterator<R> rightSource, Function<L, K> leftSelector, Function<R, K> rightSelector, Integer rightSourceSize) {
        this(leftSource, joinType, rightSource, (l, r) -> Objects.equals(leftSelector.apply(l), rightSelector.apply(r)), rightSourceSize);
    }

    public GroupJoinIterator(Iterator<L> leftSource, JoinType joinType, Iterator<R> rightSource, BiPredicate<L, R> matcher, Integer rightSourceSize) {
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

    @Override
    public boolean hasNext() {
        if (isNextReady)
            return true;

        while (leftSource.hasNext()) {
            L left = leftSource.next();

            QueryList<R> rightMatches = new QueryList<>();
            for (int i = 0; i < getRightByIndex().size(); i++) {
                R right = getRightByIndex().get(i);
                if (matcher.test(left, right)) {
                    rightMatches.add(right);
                    if (joinType.isRight())
                        getRightIndexesIncluded().add(i);
                }
            }

            if (!rightMatches.isEmpty() || joinType.isLeft()) {
                next = Tuple.create(left, rightMatches);
                return isNextReady = true;
            }
        }

        if (joinType.isRight() && !isRightDone) {
            QueryList<R> rights = new QueryList<>();
            for (int i = 0; i < getRightByIndex().size(); i++)
                if (!getRightIndexesIncluded().contains(i))
                    rights.add(getRightByIndex().get(i));
            isRightDone = true;
            if (!rights.isEmpty()) {
                next = Tuple.create(null, rights);
                return isNextReady = true;
            }
        }

        return false;
    }

    @Override
    public Tuple<L, QueryList<R>> next() {
        if (!isNextReady && !hasNext())
            throw new NoSuchElementException();
        isNextReady = false;
        return next;
    }
}
