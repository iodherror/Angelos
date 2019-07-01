package tk.qcsoft.angelos;

/**
 * Created by QC on 2019/6/27 17:49.
 */
class NoDynamesException extends RuntimeException {
    NoDynamesException(){
        super("can not found Any Dynames!");
    }
}
