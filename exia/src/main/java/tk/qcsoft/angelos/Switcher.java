package tk.qcsoft.angelos;

import tk.qcsoft.angelos.functionIF.ThrowingBiFunction;
import tk.qcsoft.angelos.functionIF.ThrowingConsumer;
import tk.qcsoft.angelos.functionIF.ThrowingFunction;
import tk.qcsoft.angelos.functionIF.ThrowingSupplier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Created by Channing Qiu on 2019/5/31 15:57.
 */
public interface Switcher<T,R>{


    /**
     * Get the wrapped obj
     *
     * @return wrapped obj
     */
    @SuppressWarnings("unchecked")
    default T get(){
        return (T) this;
    }

    default R getDefaultValue(){
        return null;
    }

    <N> Switcher<T,R> setRule(ThrowingBiFunction<T, N, Boolean, ? extends Exception> rule);

    <N> ThrowingBiFunction<T, N, Boolean,? extends Exception> getRule();

    Map<Object, Object[]> getCases();

    <N> Switcher<T,R> eCase(ThrowingFunction<T, R, ? extends Exception> fun, N... params);
    <N> Switcher<T,R> eCaseC(ThrowingConsumer<T, ? extends Exception> fun, N... params);

    /**
     * Wrap obj with Switcher
     *
     * @param <T> the type of the wrapped obj
     * @param value wrapped obj
     *
     * @return Switcher obj
     */
    static <T,R> Switcher<T,R> of(T value) {
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
    static <T,K> Switcher<T,K> of(T value, K defaultValue) {
        return new Switcher<T,K>() {

            private T instance;
            private K defaultValue;
            private ThrowingBiFunction<T, ?, Boolean,? extends Exception> rule;
            private Map<Object,Object[]> caseMap;

            Switcher<T,K> init(T instance, K defaultValue) {
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
            public K getDefaultValue(){
                return (K)defaultValue;
            }

            @Override
            public <N> Switcher<T,K> setRule(ThrowingBiFunction<T, N, Boolean, ? extends Exception> newRule) {
                rule = newRule;
                return this;
            }

            @Override
            public ThrowingBiFunction<T, ?, Boolean,? extends Exception> getRule() {
                return rule;
            }

            @Override
            public Map<Object, Object[]> getCases() {
                return caseMap;
            }

            @Override
            public <N> Switcher<T,K> eCase(ThrowingFunction<T, K, ? extends Exception> fun, N... params) {
                caseMap.put(fun,params);
                return this;
            }

            @Override
            public <N> Switcher<T,K> eCaseC(ThrowingConsumer<T, ? extends Exception> fun, N... params) {
                caseMap.put(fun,params);
                return this;
            }

        }.init(value,defaultValue);

    }

    /**
     * Wrap obj with Switcher
     *
     * @param <T> the type of the wrapped obj
     * @param <K> the type of default value
     * @param value wrapped obj
     * @param defaultValue the supplier of default value when match no case
     *
     * @return Switcher obj
     */
    static <T,K> Switcher<T,K> of(T value, ThrowingSupplier<K,? extends Exception> defaultValue) {
        return new Switcher<T,K>() {

            private T instance;
            private ThrowingSupplier<K,? extends Exception> defaultValue;
            private ThrowingBiFunction<T, ?, Boolean,? extends Exception> rule;
            private Map<Object,Object[]> caseMap;

            Switcher<T,K> init(T instance, ThrowingSupplier<K,? extends Exception> defaultValue) {
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
            public K getDefaultValue(){
                return (K)defaultValue.wrapGet();
            }

            @Override
            public <N> Switcher<T,K> setRule(ThrowingBiFunction<T, N, Boolean, ? extends Exception> newRule) {
                rule = newRule;
                return this;
            }

            @Override
            public <N> ThrowingBiFunction<T, N, Boolean,? extends Exception> getRule() {
                return (ThrowingBiFunction<T, N, Boolean,? extends Exception>)rule;
            }

            @Override
            public Map<Object, Object[]> getCases() {
                return caseMap;
            }

            @Override
            public <N> Switcher<T,K> eCase(ThrowingFunction<T, K, ? extends Exception> fun, N... params) {
                caseMap.put(fun,params);
                return this;
            }

            @Override
            public <N> Switcher<T,K> eCaseC(ThrowingConsumer<T, ? extends Exception> fun, N... params) {
                caseMap.put(fun,params);
                return this;
            }

        }.init(value,defaultValue);

    }

    default R run(){

        Optional ret =
                getCases().entrySet().stream().filter(supplierEntry ->
                        Stream.of(supplierEntry.getValue()).anyMatch(
                                t->getRule().wrapApply(get(),t)
                        )
                ).findFirst();
        if(!ret.isPresent()) return Optional.ofNullable(getDefaultValue())
                .orElseThrow(() -> new RuntimeException("Match no case and no default value"));
        Object run = (((Map.Entry<Object,T[]>)ret.get())).getKey();
        if(run instanceof ThrowingFunction) return (R) ((ThrowingFunction) run).wrapApply(get());
        if(run instanceof ThrowingConsumer) ((ThrowingConsumer) run).wrapAccept(get());
        return null;
    }

}

