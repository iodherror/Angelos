package tk.qcsoft.angelos.wrapper;

/**
 * Created by QC on 2019/7/19 11:19.
 */
public final class ExceptionWrapper extends RuntimeException {

    private static final String MSG_PREFIX = "Exception found in function:";

    public ExceptionWrapper(Exception ex){
        super(MSG_PREFIX + ex.getMessage());
        this.initCause(ex);
    }
}
