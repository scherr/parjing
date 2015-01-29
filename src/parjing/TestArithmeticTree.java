package parjing;

import java.util.Optional;

import static parjing.Parser.*;

abstract class Tree {
    static class Add extends Tree {
        final Tree left;
        final Tree right;

        Add(Tree left, Tree right) {
            this.left = left;
            this.right = right;
        }
    }

    static class Mul extends Tree {
        final Tree left;
        final Tree right;

        Mul(Tree left, Tree right) {
            this.left = left;
            this.right = right;
        }
    }

    static class Lit extends Tree {
        final int value;

        Lit(int value) {
            this.value = value;
        }
    }
}

public class TestArithmeticTree {
    private Parser<Tree> natural, expr, term, factor;
    {
        natural = digits(a -> new Tree.Lit(Integer.valueOf(a)));
        expr    = lazy(() ->
                        term.then(
                                symbol('+').then(
                                        expr,
                                        (a, b) -> b
                                ).or(
                                        empty(null)
                                ),
                                (a, b) -> b == null ? a : new Tree.Add(a, b)
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
                                (a, b) -> b == null ? a : new Tree.Mul(a, b)
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
        TestArithmeticTree t = new TestArithmeticTree();
        Optional<Tree> r = t.expr.parse("2*3+4*(4+5)");
        System.out.println(r);
    }
}
