package tk.qcsoft.angelos;

import tk.qcsoft.angelos.constant.Operation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by QC on 2019/7/22 15:16.
 */
public class TargetRule {

    private Operation operation;
    private ArrayList<String> valueList;

    private TargetRule() {

    }

    private TargetRule(Operation operation,String value) {
        Objects.requireNonNull(value);
        this.valueList = new ArrayList<>();
        valueList.add(value);
        this.operation = operation;
    }

    private TargetRule(Operation operation, String... value) {
        Objects.requireNonNull(value);
        this.valueList = new ArrayList<>();
        valueList.addAll(Arrays.stream(value).collect(Collectors.toList()));
        this.operation = operation;
    }

    private TargetRule(Operation operation) {
        this.operation = operation;
    }

    public Boolean test(String target) {
        if (operation.equals(Operation.SAME)) return valueList.get(0).equals(target);
        if (operation.equals(Operation.LIKE)) return target.length() > 0 && target.contains(valueList.get(0));
        if (operation.equals(Operation.NOT)) return !valueList.get(0).equals(target);
        if (operation.equals(Operation.ANY)) return true;
        if (operation.equals(Operation.IN)) return valueList.contains(target);
        if (operation.equals(Operation.NOT_IN)) return !valueList.contains(target);
        throw new UnsupportedOperationException("invalid operation:" + operation.name());
    }

    public static TargetRule SAME(String value) {
        return new TargetRule(Operation.SAME,value);
    }

    public static TargetRule LIKE(String value) {
        return new TargetRule(Operation.LIKE,value);
    }

    public static TargetRule NOT(String value) {
        return new TargetRule(Operation.NOT,value);
    }

    public static TargetRule ANY() {
        return new TargetRule(Operation.ANY);
    }
    public static TargetRule IN(String... value) {
        return new TargetRule( Operation.IN,value);
    }
    public static TargetRule NOT_IN(String... value) {
        return new TargetRule( Operation.NOT_IN,value);
    }
}