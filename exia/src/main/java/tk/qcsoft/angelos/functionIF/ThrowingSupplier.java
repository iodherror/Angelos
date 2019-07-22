package tk.qcsoft.angelos.functionIF;

import tk.qcsoft.angelos.wrapper.ExceptionWrapper;

import java.util.function.Supplier;

/**
 * Created by QC on 2019/7/19 11:18.
 */
@FunctionalInterface
public interface ThrowingSupplier<K, E extends Exception> {
    K get() throws E;

    /**
     * wrap the checked exception to unchecked exception
     *
     * @param <E> the type of Exception that may be thrown
     *
     * @return result
     */
    default <E extends Exception> K wrapGet() {
        Supplier<K> supplier =
                () -> {
                    try {
                        return this.get();
                    } catch (RuntimeException rex){
                        throw rex;
                    }catch (Exception ex) {
                        throw new ExceptionWrapper(ex);
                    }
                };
        return supplier.get();
    }
}
