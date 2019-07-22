package tk.qcsoft.angelos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Channing Qiu on 2019/6/24 16:59.
 */
public interface Dynames<D,R>  {

    /**
     * Fire and return the result
     *
     * @param data input data
     * @return result
     */
    R fire(D data);

    /**
     * Define the target of Dynames
     *
     * @return target information
     */
    Object[][] initTargets();

    /**
     * Determine whether the target can be attacked by this Dynames
     *
     * @param target target information
     * @return true or false
     */
    default boolean canAttack(String... target){

        return array2List(initTargets()).stream().anyMatch(
                targetInfos -> {
                    if(targetInfos.size() != target.length) return false;
                    for (int i = 0; i < target.length; i++) {
                        if(!targetInfos.get(i).test(target[i])) return false;
                    }
                    return true;
                }
        );
    }

    /**
     * Define the weight of Dynames
     * Higher weight will give priority to attack
     *
     * @return weight
     */
    default int getWeight(){
        return 0;
    }

    /**
     * Transform String array to List
     *
     * @param targetInfo target information
     * @return target information
     */
    default List<LinkedList<TargetRule>> array2List(Object[][] targetInfo){

        return Arrays.stream(targetInfo).collect(
                ArrayList::new,
                (lists, objects) -> {
                    LinkedList<TargetRule> list = new LinkedList<>();
                    Arrays.stream(objects).forEach(o -> {
                        if(o instanceof java.lang.String){
                            list.add(TargetRule.SAME((String)o));
                        } else if(o instanceof TargetRule) {
                            list.add((TargetRule)o);
                        } else{
                            throw new UnsupportedOperationException("Unsupported class:"+o.getClass());
                        }
                    });
                    lists.add(list);
                },
                ArrayList::addAll
        );
    }

}

