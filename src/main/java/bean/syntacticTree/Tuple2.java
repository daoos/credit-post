package bean.syntacticTree;

/**
 * Created by hadoop on 17-4-26.
 */
public class Tuple2<E1, E2> {
    private E1 e1;
    private E2 e2;

    private Tuple2(E1 e1, E2 e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    public static <E1, E2> Tuple2<E1, E2> create(E1 e1, E2 e2) {
        return new Tuple2<E1, E2>(e1, e2);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Tuple2))
            return false;

        Tuple2 tuple = (Tuple2) o;
        return (e1.equals(tuple.e1) && e2.equals(tuple.e2));
    }

    @Override
    public int hashCode() {
        int hashCode = 7;
        hashCode += 31 * e1.hashCode();
        hashCode += 31 * e2.hashCode();
        return hashCode;
    }

    @Override
    public String toString() {
        return String.format("<%s,%s>", e1 != null? e1.toString(): "null",
                e2 != null? e2.toString(): "null");
    }
}
