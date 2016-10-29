package multicolumn;

import java.util.Objects;

class Tuple<H extends Comparable<H>, T> implements Comparable<Tuple<H, T>> {
    public H head;
    public T tail;

    Tuple(H head, T tail) {
        this.head = head;
        this.tail = tail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return Objects.equals(head, tuple.head); // Don't include tail, 'consistent' with compareTo.
    }

    @Override
    public int hashCode() {
        return Objects.hash(head);
    }

    @Override
    public String toString() {
        return head + " : " + tail;
    }

    @Override
    public int compareTo(Tuple<H, T> o) {
        return head.compareTo(o.head);
    }
}