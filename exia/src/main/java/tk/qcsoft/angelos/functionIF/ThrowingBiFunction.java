package tk.qcsoft.angelos.functionIF;

import tk.qcsoft.angelos.wrapper.ExceptionWrapper;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by QC on 2019/7/19 11:18.
 */
@FunctionalInterface
public interface ThrowingBiFunction<T, N, K, E extends Exception> {
    K apply(T t,N n) throws E;

    /**
     * wrap the checked exception to unchecked exception
     *
     * @param <E> the type of Exception that may be thrown
     *
     * @return result
     */
    default <E extends Exception> K wrapApply(T t,N n) {
        BiFunction<T,N,K> biFunction =
                (i,j) -> {
                    try {
                        return this.apply(i,j);
                    } catch (RuntimeException rex){
                        throw rex;
                    }catch (Exception ex) {
                        throw new ExceptionWrapper(ex);
                    }
                };
        return biFunction.apply(t,n);
    }
}
