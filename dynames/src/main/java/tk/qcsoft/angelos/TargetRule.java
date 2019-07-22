package tk.qcsoft.angelos;

import tk.qcsoft.angelos.constant.Operation;

import java.util.Objects;

/**
 * Created by QC on 2019/7/22 15:16.
 */
public class TargetRule {

    private Operation operation;
    private String value;

    private TargetRule() {

    }

    private TargetRule(String value, Operation operation) {
        Objects.requireNonNull(value);
        this.value = value;
        this.operation = operation;
    }

    private TargetRule(Operation operation) {
        this.operation = operation;
    }

    public Boolean test(String target) {
        if (operation.equals(Operation.SAME)) return value.equals(target);
        if (operation.equals(Operation.LIKE)) return target.length() > 0 && target.contains(value);
        if (operation.equals(Operation.NOT)) return !value.equals(target);
        if (operation.equals(Operation.ANY)) return true;
        throw new UnsupportedOperationException("invalid operation:" + operation.name());
    }

    public static TargetRule SAME(String value) {
        return new TargetRule(value, Operation.SAME);
    }

    public static TargetRule LIKE(String value) {
        return new TargetRule(value, Operation.LIKE);
    }

    public static TargetRule NOT(String value) {
        return new TargetRule(value, Operation.NOT);
    }

    public static TargetRule ANY() {
        return new TargetRule(Operation.ANY);
    }
}