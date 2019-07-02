package tk.qcsoft.angelos;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by Channing Qiu on 2019/5/31 15:57.
 */
public interface Switcher<T>{


    /**
     * Get the wrapped obj
     *
     * @return wrapped obj
     */
    @SuppressWarnings("unchecked")
    default T get(){
        return (T) this;
    }

    default <K> K getDefaultValue(){
        return null;
    }

    <N> Switcher<T> setRule(BiFunction<T, N, Boolean> rule);

    <N> BiFunction<T, N, Boolean> getRule();

    Map<Function, Object[]> getCases();

    <A,N> Switcher<T> eCase(Function<T, A> fun, N... params);

    /**
     * Wrap obj with Switcher
     *
     * @param <T> the type of the wrapped obj
     * @param value wrapped obj
     *
     * @return Switcher obj
     */
    static <T> Switcher<T> of(T value) {
        return Switcher.of(value,null);
    }

    /**
     * Wrap obj with Switcher
     *
     * @param <T> the type of the wrapped obj
     * @param <K> the type of default value
     * @param value wrapped obj
     * @param defaultValue the default value when match no case
     *
     * @return Switcher obj
     */
    static <T,K> Switcher<T> of(T value, K defaultValue) {
        return new Switcher<T>() {

            private T instance;
            private K defaultValue;
            private BiFunction<T, Object, Boolean> rule;
            private Map<Function,Object[]> caseMap;

            Switcher<T> init(T instance, K defaultValue) {
                this.instance = instance;
                this.defaultValue = defaultValue;
                caseMap = new LinkedHashMap<>();
                rule = Objects::equals;
                return this;
            }

            @Override
            public T get(){
                return instance;
            }

            @Override
            public <K> K getDefaultValue(){
                return (K)defaultValue;
            }

            @Override
            public <N> Switcher<T> setRule(BiFunction<T, N, Boolean> newRule) {
                rule = (BiFunction<T, Object, Boolean>) newRule;
                return this;
            }

            @Override
            public <N> BiFunction<T, N, Boolean> getRule() {
                return (BiFunction<T, N, Boolean>)rule;
            }

            @Override
            public Map<Function, Object[]> getCases() {
                return caseMap;
            }

            @Override
            public <A,N> Switcher<T> eCase(Function<T,A> fun, N... params) {
                caseMap.put(fun,params);
                return this;
            }

        }.init(value,defaultValue);

    }

    default <K> K run(){

        Optional ret =
                getCases().entrySet().stream().filter(supplierEntry ->
                        Stream.of(supplierEntry.getValue()).anyMatch(
                                t->getRule().apply(get(),t)
                        )
                ).findFirst();
        if(!ret.isPresent()) return (K)Optional.ofNullable(getDefaultValue())
                .orElseThrow(() -> new RuntimeException("Match no case and no default value"));
        return (K)(((Map.Entry<Function,T[]>)ret.get())).getKey().apply(get());
    }

}

