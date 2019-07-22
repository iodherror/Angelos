package tk.qcsoft.angelos.functionIF;

import tk.qcsoft.angelos.wrapper.ExceptionWrapper;

import java.util.function.Consumer;

/**
 * Created by QC on 2019/7/19 11:13.
 */
@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {
    void accept(T t) throws E;

    /**
     * wrap the checked exception to unchecked exception
     *
     * @param <E> the type of Exception that may be thrown by the {@code throwingConsumer}
     */
    default <E extends Exception> void wrapAccept(T t) {
        Consumer<T> consumer =
            i -> {
               try {
                   this.accept(i);
               }catch (RuntimeException rex){
                   throw rex;
               }catch (Exception ex) {
                   throw new ExceptionWrapper(ex);
               }
            };
        consumer.accept(t);
    }
}

