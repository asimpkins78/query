package simpkins.query;

public enum JoinType {
    INNER, LEFT, RIGHT, OUTER;

    public boolean isLeft() {
        return this == LEFT || this == OUTER;
    }

    public boolean isRight() {
        return this == RIGHT || this == OUTER;
    }
}
