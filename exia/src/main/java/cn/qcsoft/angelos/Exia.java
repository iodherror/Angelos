package cn.qcsoft.angelos;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Channing Qiu on 2018/8/9 11:14.
 */
public interface Exia<T>{

    String STRING_TEMPLATE_DEFAULT = "\\$\\{.+?}";
    Pattern PATTERN_DEFAULT = Pattern.compile(STRING_TEMPLATE_DEFAULT);

    @SuppressWarnings("unchecked")
    default T get(){
        return (T) this;
    }

    static <T> Exia<T> of(T value) {
        return new Exia<T>() {

            private T instance;

            Exia<T> setInstance(T instance) {
                this.instance = instance;
                return this;
            }

            @Override
            public T get(){
                return instance;
            }

        }.setInstance(value);
    }
    default <E extends Exception> T init(ThrowingConsumer<T, E> applier){
        throwingConsumerWrapper(applier).accept(get());
        return get();
    }

    default <E extends Exception> Exia<T> apply(ThrowingConsumer<T, E> applier) {
        throwingConsumerWrapper(applier).accept(get());
        return this;
    }

    default <K,E extends Exception> K run(ThrowingFunction<T, K, E> runner) {
        return throwingFunctionWrapper(runner).apply(get());
    }


    default <R extends Exception,E extends Exception> Exia<T> takeApply(
            ThrowingPredicate<T, R> predicate, ThrowingConsumer<T, E> applier) {
        if(throwingPredicateWrapper(predicate).test(get())) throwingConsumerWrapper(applier).accept(get());
        return this;
    }

    default <E extends Exception> Optional<T> takeIf(ThrowingPredicate<T, E> predicate) {
        if(throwingPredicateWrapper(predicate).test(get())) return Optional.of(get());
        return Optional.empty();
    }

    default <K,R extends Exception,E extends Exception> Optional<K> takeRun(
            ThrowingPredicate<T, R> predicate, ThrowingFunction<T, K, E> runner) {
        if(throwingPredicateWrapper(predicate).test(get()))
            return Optional.of(throwingFunctionWrapper(runner).apply(get()));
        return Optional.empty();
    }

    default <K> K retryableRun(
            BiPredicate<K, Optional<Exception>> needRetry, Function<T, K> runner, int retryTimes, long interval) {

        K ret = null;
        boolean needRun = true;
        int i = 0;
        Exception ex;
        while (needRun){
            ex = null;
            try {
               ret = runner.apply(get());
            }catch (Exception e){
               ex = e;
            }
            needRun = needRetry.test(ret,Optional.ofNullable(ex));

            if(needRun && i < retryTimes ) {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    throw new ExceptionWrapper(e);
                }
            }
            if( ++i > retryTimes) needRun = false;
        }
        return ret;
    }

    default <E extends Exception> Consumer<T> throwingConsumerWrapper(
            ThrowingConsumer<T, E> throwingConsumer) {
        return i -> {
            try {
                throwingConsumer.accept(i);
            }catch (RuntimeException rex){
                throw rex;
            }catch (Exception ex) {
                throw new ExceptionWrapper(ex);
            }
        };
    }
    default <K,E extends Exception> Function<T,K> throwingFunctionWrapper(
            ThrowingFunction<T, K, E> throwingFunction) {
        return i -> {
            try {
                return throwingFunction.apply(i);
            } catch (RuntimeException rex){
                throw rex;
            }catch (Exception ex) {
                throw new ExceptionWrapper(ex);
            }
        };
    }

    default <E extends Exception> Predicate<T> throwingPredicateWrapper(
            ThrowingPredicate<T, E> throwingPredicate) {
        return i -> {
            try {
                return throwingPredicate.test(i);
            } catch (RuntimeException rex){
                throw rex;
            }catch (Exception ex) {
                throw new ExceptionWrapper(ex);
            }
        };
    }

    default String getStringByCol(String input) {

        Matcher m = PATTERN_DEFAULT.matcher(input);
        StringBuffer str = new StringBuffer();
        Map<String, Field> map = Arrays.stream(get().getClass().getDeclaredFields())
                .collect(Collectors.toMap(Field::getName, o -> o));
        while (m.find()) {
            map.keySet().stream()
                .filter(m.group().substring(2, m.group().length() - 1)::equals)
                .findAny()
                .ifPresent(
                    o -> {
                        try {
                            map.get(o).setAccessible(true);
                            m.appendReplacement(str, map.get(o).get(get()).toString());
                        } catch (NullPointerException ex) {
                            m.appendReplacement(str, "null");
                        } catch (Exception ignored) {
                            //do nothing
                        }
                    }
            );
        }
        m.appendTail(str);
        return str.toString();
    }

    default String getStringByGetter(String input) {

        Matcher m = PATTERN_DEFAULT.matcher(input);
        StringBuffer str = new StringBuffer();
        Map<String, Method> map = Arrays.stream(get().getClass().getDeclaredMethods())
                .collect(Collectors.toMap(Method::getName, o -> o));
        while (m.find()) {
            map.keySet().stream()
                    .filter(m.group().substring(2, m.group().length() - 1)::equals)
                    .findAny()
                    .ifPresent(
                            o -> {
                                try {
                                    map.get(o).setAccessible(true);
                                    m.appendReplacement(str, map.get(o).invoke(get()).toString());
                                } catch (NullPointerException ex) {
                                    m.appendReplacement(str, "null");
                                } catch (Exception ignored) {
                                    //do nothing
                                }
                            }
                    );
        }
        m.appendTail(str);
        return str.toString();
    }

    final class ExceptionWrapper extends RuntimeException {

        private static final String MSG_PREFIX = "函数执行过程中发生异常:";

        ExceptionWrapper(Exception ex){
            super(MSG_PREFIX + ex.getMessage());
            this.initCause(ex);
        }
    }

    @FunctionalInterface
    interface ThrowingConsumer<T, E extends Exception> {
        void accept(T t) throws E;
    }
    @FunctionalInterface
    interface ThrowingFunction<T, K, E extends Exception> {
        K apply(T t) throws E;
    }
    @FunctionalInterface
    interface ThrowingPredicate<T, E extends Exception> {
        boolean test(T t) throws E;
    }
}

