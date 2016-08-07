package com.simpkins.query;

import java.util.Objects;

/**
 * Represents an operation that accepts no input arguments and returns no
 * result. Unlike most other functional interfaces, {@code Executer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #execute()}.
 *
 * @since 1.8
 */
@FunctionalInterface
public interface Executer {
    /**
     * Performs this operation.
     */
    void execute();

    /**
     * Returns a composed {@code Executer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code Executer } that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default Executer andThen(Executer after) {
        Objects.requireNonNull(after);
        return () -> { execute(); after.execute(); };
    }
}
