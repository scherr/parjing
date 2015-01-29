package parjing;

import static parjing.Parser.*;

public class TestArithmeticInteger {
    private Parser<Integer> natural, expr, term, factor;
    {
        natural = digits(Integer::valueOf);
        expr    = lazy(() ->
                        term.then(
                                symbol('+').then(
                                        expr,
                                        (a, b) -> b
                                ).or(
                                        empty(null)
                                ),
                                (a, b) -> b == null ? a : a + b
                        )
        );
        term    = lazy(() ->
                        factor.then(
                                symbol('*').then(
                                        term,
                                        (a, b) -> b
                                ).or(
                                        empty(null)
                                ),
                                (a, b) -> b == null ? a : a * b
                        )
        );
        factor  = lazy(() ->
                        symbol('(').then(
                                expr,
                                (a, b) -> b
                        ).then(
                                symbol(')'),
                                (a, b) -> a
                        ).or(natural)
        );
    }

    public static void main(String[] args) {
        TestArithmeticInteger t = new TestArithmeticInteger();
        System.out.println(t.expr.parse("2*3+4"));
    }
}
