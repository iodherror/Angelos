package tk.qcsoft.angelos;

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
    /**
     * Get the wrapped obj
     *
     * @return wrapped obj
     */
    default T get(){
        return (T) this;
    }

    /**
     * Wrap obj with Exia
     *
     * @param <T> the type of the wrapped obj
     * @param value wrapped obj
     *
     * @return Exia obj
     */
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

    /**
     * Initialize the wrapped obj by {@code applier} and return it .
     *
     * @param <E> the type of Exception that may be thrown
     * @param applier a java consumer that its exception is wrapped up in RuntimeException .
     *
     * @return wrapped obj
     */
    default <E extends Exception> T init(ThrowingConsumer<T, E> applier){
        throwingConsumerWrapper(applier).accept(get());
        return get();
    }

    /**
     * Apply the wrapped obj by {@code applier} and return the Exia of it .
     *
     * @param <E> the type of Exception that may be thrown
     * @param applier a java consumer that its exception is wrapped up in RuntimeException .
     *
     * @return Exia obj
     */
    default <E extends Exception> Exia<T> apply(ThrowingConsumer<T, E> applier) {
        throwingConsumerWrapper(applier).accept(get());
        return this;
    }

    /**
     * Run the wrapped obj by {@code runner} and return the result of it .
     *
     * @param <K> the type of the result of the function
     * @param <E> the type of Exception that may be thrown
     * @param runner a java function that its exception is wrapped up in RuntimeException .
     *
     * @return result of {@code runner}
     */
    default <K,E extends Exception> K run(ThrowingFunction<T, K, E> runner) {
        return throwingFunctionWrapper(runner).apply(get());
    }

    /**
     * Apply the wrapped obj by {@code applier} when the {@code predicate} is true
     * and return the Exia of it .
     *
     * @param <R> the type of Exception that may be thrown by the {@code predicate}
     * @param <E> the type of Exception that may be thrown by the {@code applier}
     * @param predicate a java predicate that its exception is wrapped up in RuntimeException .
     * @param applier a java consumer that its exception is wrapped up in RuntimeException .
     *
     * @return Exia obj
     */
    default <R extends Exception,E extends Exception> Exia<T> takeApply(
            ThrowingPredicate<T, R> predicate, ThrowingConsumer<T, E> applier) {
        if(throwingPredicateWrapper(predicate).test(get())) throwingConsumerWrapper(applier).accept(get());
        return this;
    }

    /**
     * when the {@code predicate} is true ,return the Optional of it ,or else return {@code Optional.empty()}
     *
     * @param <E> the type of Exception that may be thrown
     * @param predicate a java predicate that its exception is wrapped up in RuntimeException .
     *
     * @return Optional of the wrapped obj
     */
    default <E extends Exception> Optional<T> takeIf(ThrowingPredicate<T, E> predicate) {
        if(throwingPredicateWrapper(predicate).test(get())) return Optional.of(get());
        return Optional.empty();
    }

    /**
     * Run the wrapped obj by {@code runner} when the {@code predicate} is true
     * and return the Optional of result,or else return {@code Optional.empty()}
     *
     * @param <K> the type of the result of the function
     * @param <R> the type of Exception that may be thrown by the {@code predicate}
     * @param <E> the type of Exception that may be thrown by the {@code runner}
     * @param predicate a java predicate that its exception is wrapped up in RuntimeException .
     * @param runner a java function that its exception is wrapped up in RuntimeException .
     *
     * @return Optional of result
     */
    default <K,R extends Exception,E extends Exception> Optional<K> takeRun(
            ThrowingPredicate<T, R> predicate, ThrowingFunction<T, K, E> runner) {
        if(throwingPredicateWrapper(predicate).test(get()))
            return Optional.of(throwingFunctionWrapper(runner).apply(get()));
        return Optional.empty();
    }

    /**
     * Run the wrapped obj by {@code runner} when the {@code needRetry} is true
     * and return the Optional of result,or else return {@code Optional.empty()}
     *
     * @param <K> the type of the result of the function
     * @param needRetry a java predicate that its exception is wrapped up in RuntimeException .
     * @param runner a java function that its exception is wrapped up in RuntimeException .
     * @param retryTimes the max retry times when {@code needRetry} is true .
     * @param interval the interval when retry running .
     *
     * @return result
     */
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
                } catch (Exception e) {
                    throw new ExceptionWrapper(e);
                }
            }
            if( ++i > retryTimes) needRun = false;
        }
        return ret;
    }

    /**
     * wrap the checked exception to unchecked exception
     *
     * @param <E> the type of Exception that may be thrown by the {@code throwingConsumer}
     * @param throwingConsumer a custom consumer that defines some exception .
     *
     * @return a java consumer
     */
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

    /**
     * wrap the checked exception to unchecked exception
     *
     * @param <K> the type of the result of the function
     * @param <E> the type of Exception that may be thrown
     * @param throwingFunction a custom function that defines some exception .
     *
     * @return a java Function
     */
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

    /**
     * wrap the checked exception to unchecked exception
     *
     * @param <E> the type of Exception that may be thrown
     * @param throwingPredicate a custom predicate that defines some exception .
     *
     * @return a java predicate
     */
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

    /**
     * Use field name to Format {@code input} by the values of wrapped obj
     *
     * @param input input string
     *
     * @return String
     */
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

    /**
     * Use getter name to Format {@code input} by the values of wrapped obj
     *
     * @param input input string
     *
     * @return String
     */
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

