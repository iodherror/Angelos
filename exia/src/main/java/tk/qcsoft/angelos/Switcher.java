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

    Switcher<T> setRule(BiFunction<T, T, Boolean> rule);

    BiFunction<T, T, Boolean> getRule();

    Map<Function,T[]> getCases();

    <A> Switcher<T> eCase(Function<T, A> fun, T... params);

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
     * @param <K> the default Value when match no case
     * @param value wrapped obj
     *
     * @return Switcher obj
     */
    static <T,K> Switcher<T> of(T value, K defaultValue) {
        return new Switcher<T>() {

            private T instance;
            private K defaultValue;
            private BiFunction<T, T, Boolean> rule;
            private Map<Function,T[]> caseMap;

            Switcher<T> init(T instance,K defaultValue) {
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
            public Switcher<T> setRule(BiFunction<T, T, Boolean> newRule) {
                rule = newRule;
                return this;
            }

            @Override
            public BiFunction<T, T, Boolean> getRule() {
                return rule;
            }

            @Override
            public Map<Function,T[]> getCases() {
                return caseMap;
            }

            @Override
            public <A> Switcher<T> eCase(Function<T,A> fun,T... params) {
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

