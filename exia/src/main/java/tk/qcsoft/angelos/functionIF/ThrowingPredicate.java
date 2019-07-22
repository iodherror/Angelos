package tk.qcsoft.angelos.functionIF;

import tk.qcsoft.angelos.wrapper.ExceptionWrapper;

import java.util.function.Predicate;

/**
 * Created by QC on 2019/7/19 11:18.
 */
@FunctionalInterface
public interface ThrowingPredicate<T, E extends Exception> {
    boolean test(T t) throws E;

    /**
     * wrap the checked exception to unchecked exception
     *
     * @param <E> the type of Exception that may be thrown
     *
     * @return {@code true} if the input argument matches the predicate,
     */
    default <E extends Exception> boolean wrapTest(T t) {
        Predicate<T> predicate =
        i -> {
            try {
                return this.test(i);
            } catch (RuntimeException rex){
                throw rex;
            }catch (Exception ex) {
                throw new ExceptionWrapper(ex);
            }
        };
        return predicate.test(t);
    }
}
