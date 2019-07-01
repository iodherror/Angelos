package tk.qcsoft.angelos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    String[][] initTargets();

    /**
     * Determine whether the target can be attacked by this Dynames
     *
     * @param target target information
     * @return true or false
     */
    default boolean canAttack(String... target){

        return array2List(initTargets()).stream().anyMatch(targetInfos ->
                targetInfos.size() == target.length
            && Arrays.stream(target).allMatch(targetInfos::contains)
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
    default List<List<String>> array2List(String[][] targetInfo){

        return Arrays.stream(targetInfo).collect(
                    ArrayList::new,
                    (lists, strings) ->
                        lists.add(Arrays.stream(strings).collect(Collectors.toList())),
                    ArrayList::addAll
                );
    }

}

