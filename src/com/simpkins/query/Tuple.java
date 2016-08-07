package com.simpkins.query;

public class Tuple<T1, T2> {
    private T1 item1;
    private T2 item2;

    public Tuple(T1 item1, T2 item2) {
        this.item1 = item1;
        this.item2 = item2;
    }

    public static <T1, T2> Tuple<T1, T2> create(T1 item1, T2 item2) {
        return new Tuple<>(item1, item2);
    }

    public T1 getItem1() {
        return item1;
    }

    public T2 getItem2() {
        return item2;
    }

    @Override
    public String toString() {
        return "Tuple{" +
                "item1=" + item1 +
                ", item2=" + item2 +
                '}';
    }
}
