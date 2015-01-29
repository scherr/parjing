package parjing;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Parser<T> {
    public Parser<T> or(Parser<? extends T> parser) {
        return new Choice<>(this, parser);
    }

    public <U, V> Parser<V> then(Parser<U> parser, BiFunction<T, U, V> sequencer) {
        return new Sequence<>(this, parser, sequencer);
    }

    final public Optional<T> parse(String string) {
        Context<T> c = parse(Context.make(string, 0, null));
        if (c == null || c.position < c.string.length()) {
            return Optional.empty();
        }
        return Optional.ofNullable(c.value);
    }

    abstract protected <U> Context<T> parse(Context<U> context);

    public static <T> Parser<T> lazy(Supplier<Parser<T>> supplier) {
        return new Lazy<>(supplier);
    }
    public static <T> Parser<T> empty(Supplier<T> supplier) {
        return new Empty<>(supplier);
    }
    public static <T> Parser<T> symbols(String symbols, Supplier<T> supplier) {
        if (symbols.length() == 1) {
            return symbol(symbols.charAt(0), supplier);
        }
        return new Symbols<>(symbols, supplier);
    }
    public static <T> Parser<T> symbols(String symbols) {
        return symbols(symbols, null);
    }
    public static <T> Parser<T> symbol(char symbol, Supplier<T> supplier) {
        return new Symbol<>(symbol, supplier);
    }
    public static <T> Parser<T> symbol(char symbol) {
        return symbol(symbol, null);
    }
    public static <T> Parser<T> digits(Function<String, T> converter) {
        return new Digits<>(converter);
    }

    public static final class Context<T> {
        final String string;
        final int position;
        final T value;

        private Context(String string, int position, T value) {
            this.string = string;
            this.position = position;
            this.value = value;
        }

        public static <T> Context<T> make(String string, int position, T value) {
            return new Context<>(string, position, value);
        }
    }

    static class Lazy<T> extends Parser<T> {
        private final Supplier<Parser<T>> supplier;
        private Parser<T> parser;

        Lazy(Supplier<Parser<T>> supplier) {
            this.supplier = supplier;
        }

        protected <U> Context<T> parse(Context<U> context) {
            if (parser == null) {
                parser = supplier.get();
            }

            return parser.parse(context);
        }
    }

    static class Choice<T> extends Parser<T> {
        private final Parser<? extends T> first;
        private final Parser<? extends T> second;

        Choice(Parser<? extends T> first, Parser<? extends T> second) {
            this.first = first;
            this.second = second;
        }

        protected <S> Context<T> parse(Context<S> context) {
            Context<? extends T> c = first.parse(context);
            if (c == null) {
                c = second.parse(context);
            }

            return (Context<T>) c;
        }
    }

    static class Sequence<F, S, T> extends Parser<T> {
        private final Parser<F> first;
        private final Parser<S> second;
        private final BiFunction<F, S, T> sequencer;

        Sequence(Parser<F> first, Parser<S> second, BiFunction<F, S, T> sequencer) {
            this.first = first;
            this.second = second;
            this.sequencer = sequencer;
        }

        protected <U> Context<T> parse(Context<U> context) {
            Context<F> f = first.parse(context);
            if (f == null) {
                return null;
            }
            Context<S> s = second.parse(f);
            if (s == null) {
                return null;
            }
            return Context.make(s.string, s.position, sequencer.apply(f.value, s.value));
        }
    }

    static class Empty<T> extends Parser<T> {
        private final Supplier<T> supplier;

        Empty(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        protected <U> Context<T> parse(Context<U> context) {
            return Context.make(context.string, context.position, supplier == null ? null : supplier.get());
        }
    }

    static class Symbol<T> extends Parser<T> {
        private final char symbol;
        private final Supplier<T> supplier;

        Symbol(char symbol, Supplier<T> supplier) {
            this.symbol = symbol;
            this.supplier = supplier;
        }

        protected <U> Context<T> parse(Context<U> context) {
            if (context.position >= context.string.length() || context.string.charAt(context.position) != symbol) {
                return null;
            }
            return Context.make(context.string, context.position + 1, supplier == null ? null : supplier.get());
        }
    }

    static class Symbols<T> extends Parser<T> {
        private final String symbols;
        private final Supplier<T> supplier;

        Symbols(String symbols, Supplier<T> supplier) {
            this.symbols = symbols;
            this.supplier = supplier;
        }

        protected <U> Context<T> parse(Context<U> context) {
            int pos = context.position;
            for (int i = 0; i < symbols.length(); i++) {
                if (pos >= context.string.length() || symbols.charAt(i) != context.string.charAt(pos)) {
                    return null;
                }
                pos++;
            }
            return Context.make(context.string, pos, supplier == null ? null : supplier.get());
        }
    }

    static class Digits<T> extends Parser<T> {
        private final Function<String, T> converter;

        Digits(Function<String, T> converter) {
            this.converter = converter;
        }

        protected <U> Context<T> parse(Context<U> context) {
            int i = context.position;
            while (i < context.string.length()) {
                char c = context.string.charAt(i);
                switch (c) {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9': {
                        i++;
                        continue;
                    }
                }
                break;
            }

            if (i == context.position) {
                return null;
            } else {
                return Context.make(context.string, i, converter == null ? null : converter.apply(context.string.substring(context.position, i)));
            }
        }
    }
}
