package parjing;

import java.util.Optional;

import static parjing.Parser.*;

public class TestArithmeticIntegerFinalTrick {
    private static class Natural {
        final private static Parser<Integer> it = digits(Integer::valueOf);
    }
    private static class Expr {
        final private static Parser<Integer> it = lazy(() ->
                        Term.it.then(
                                symbol('+').then(
                                        Expr.it,
                                        (a, b) -> b
                                ).or(
                                        empty(null)
                                ),
                                (a, b) -> b == null ? a : a + b
                        )
        );
    }
    private static class Term {
        final private static Parser<Integer> it = lazy(() ->
                        Factor.it.then(
                                symbol('*').then(
                                        Term.it,
                                        (a, b) -> b
                                ).or(
                                        empty(null)
                                ),
                                (a, b) -> b == null ? a : a * b
                        )
        );
    }
    private static class Factor {
        final private static Parser<Integer> it = lazy(() ->
                        symbol('(').then(
                                Expr.it,
                                (a, b) -> b
                        ).then(
                                symbol(')'),
                                (a, b) -> a
                        ).or(Natural.it)
        );
    }

    public static void main(String[] args) {
        Optional<Integer> r = null;

        for (int i = 0; i < 10000; i++) {
            r = Expr.it.parse("2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+(4+2*3+4)+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4");
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            r = Expr.it.parse("2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+(4+2*3+4)+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4+2*3+4");
        }
        System.out.println(System.currentTimeMillis() - start);
        System.out.println(r);
    }
}
