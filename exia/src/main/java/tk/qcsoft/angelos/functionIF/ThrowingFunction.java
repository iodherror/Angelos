package tk.qcsoft.angelos.functionIF;

import tk.qcsoft.angelos.wrapper.ExceptionWrapper;

import java.util.function.Function;

/**
 * Created by QC on 2019/7/19 11:18.
 */
@FunctionalInterface
public interface ThrowingFunction<T, K, E extends Exception> {
    K apply(T t) throws E;

    /**
     * wrap the checked exception to unchecked exception
     *
     * @param t the input argument
     * @param <E> the type of Exception that may be thrown
     *
     * @return result
     */
    default <E extends Exception> K wrapApply(T t) {
        Function<T,K> function =
         i -> {
            try {
                return this.apply(i);
            } catch (RuntimeException rex){
                throw rex;
            }catch (Exception ex) {
                throw new ExceptionWrapper(ex);
            }
        };
        return function.apply(t);
    }
}
