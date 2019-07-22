package tk.qcsoft.angelos;

import tk.qcsoft.angelos.functionIF.ThrowingConsumer;
import tk.qcsoft.angelos.functionIF.ThrowingFunction;
import tk.qcsoft.angelos.functionIF.ThrowingPredicate;
import tk.qcsoft.angelos.wrapper.ExceptionWrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Channing Qiu on 2018/8/9 11:14.
 */
public interface Exia<T>{

    String STRING_TEMPLATE_DEFAULT = "\\$\\{.+?}";
    Pattern PATTERN_DEFAULT = Pattern.compile(STRING_TEMPLATE_DEFAULT);


    /**
     * Get the wrapped obj
     *
     * @return wrapped obj
     */
    @SuppressWarnings("unchecked")
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

            @SuppressWarnings("unchecked")
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
        applier.wrapAccept(get());
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
        applier.wrapAccept(get());
        return this;
    }

    /**
     * Run the wrapped obj by {@code runner} and return the result of it .
     *
     * @param <K> the type of the result of the functionIF
     * @param <E> the type of Exception that may be thrown
     * @param runner a java functionIF that its exception is wrapped up in RuntimeException .
     *
     * @return result of {@code runner}
     */
    default <K,E extends Exception> K run(ThrowingFunction<T, K, E> runner) {
        return runner.wrapApply(get());
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
        if(predicate.wrapTest(get())) applier.wrapAccept(get());
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
        if(predicate.wrapTest(get())) return Optional.of(get());
        return Optional.empty();
    }

    /**
     * Run the wrapped obj by {@code runner} when the {@code predicate} is true
     * and return the Optional of result,or else return {@code Optional.empty()}
     *
     * @param <K> the type of the result of the functionIF
     * @param <R> the type of Exception that may be thrown by the {@code predicate}
     * @param <E> the type of Exception that may be thrown by the {@code runner}
     * @param predicate a java predicate that its exception is wrapped up in RuntimeException .
     * @param runner a java functionIF that its exception is wrapped up in RuntimeException .
     *
     * @return Optional of result
     */
    default <K,R extends Exception,E extends Exception> Optional<K> takeRun(
            ThrowingPredicate<T, R> predicate, ThrowingFunction<T, K, E> runner) {
        if(predicate.wrapTest(get()))
            return Optional.of(runner.wrapApply(get()));
        return Optional.empty();
    }

    /**
     * Run the wrapped obj by {@code runner} when the {@code needRetry} is true
     * and return the Optional of result,or else return {@code Optional.empty()}
     *
     * @param <K> the type of the result of the functionIF
     * @param needRetry a java predicate that its exception is wrapped up in RuntimeException .
     * @param runner a java functionIF that its exception is wrapped up in RuntimeException .
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

}

