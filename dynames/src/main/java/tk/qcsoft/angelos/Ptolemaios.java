package tk.qcsoft.angelos;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

/**
 * Created by Channing Qiu on 2019/6/26 16:35.
 */
public final class Ptolemaios {

    private static ServiceLoader<Dynames> services = ServiceLoader.load(Dynames.class);

    private static String delimiter = "~";

    private static String impossible_value = "impossible_value";

    /**
     * Set the delimiter for cache key
     * This method should be call at first
     *
     * @param newDelimiter
     */
    private static void setDelimiter(String newDelimiter){
        delimiter = newDelimiter;
        kaKuNouKo.invalidateAll();
    }

    /**
     * Set the impossible_value
     * This method should be call before using cache
     *
     * @param newImpossibleValue
     */
    private static void setImpossibleValue(String newImpossibleValue){
        impossible_value = newImpossibleValue;
        kaKuNouKo.invalidateAll();
    }

    /**
     * Refresh cache
     *
     */
    public static void refresh(){
        kaKuNouKo.invalidateAll();
    }

    private static LoadingCache<String,Dynames> kaKuNouKo = Caffeine
            .newBuilder().maximumSize(500).expireAfterWrite(5, TimeUnit.DAYS)
            .build(Ptolemaios::getDynames);

    private void Ptolemaios(){}

    /**
     * Find a Dynames by target infos
     *
     * @param infos target infos
     * @param <T> input type of Dynames
     * @param <D> return type of Dynames
     * @return Optional of Dynames
     */
    @SuppressWarnings("unchecked")
    public static <T,D> Optional<Dynames<T,D>> findDynames(String...infos){
        return Optional.ofNullable(kaKuNouKo.get(data2Key(infos)));
    }

    /**
     * Find a Dynames by target infos
     *
     * @param infos target infos
     * @param <T> input type of Dynames
     * @param <D> return type of Dynames
     * @exception NoDynamesException throw NoDynamesException when not found
     * @return Dynames
     */
    @SuppressWarnings("unchecked")
    public static <T,D> Dynames<T,D> launchDynames(String...infos){
        return Ptolemaios.<T,D>findDynames(infos).orElseThrow(NoDynamesException::new);
    }

    /**
     * Find a Dynames by loader.
     *
     * @param infos target infos
     * @return Dynames
     */
    @SuppressWarnings("unchecked")
    private static <T,D> Dynames<T,D> getDynames(String infos){
        services.reload();
        Iterator<Dynames> iterator = services.iterator();

        Dynames<T,D> oo = null;
        int weight = -1;

        while (iterator.hasNext()){
            Dynames<T,D> curD = iterator.next();
            if(curD.canAttack(key2Data(infos)) && curD.getWeight() > weight){
                oo = curD;
                weight = oo.getWeight();
            }
        }

        return oo;
    }

    /**
     * Transform target infos into cache key
     *
     * @param infos target infos
     * @return cache key
     */
    private static String data2Key(String...infos){
        StringBuilder sb = new StringBuilder();
        Arrays.stream(infos).forEach(s -> {
            sb.append(s.length()>0?s:impossible_value);
            sb.append(delimiter);
        });
        return sb.substring(0,sb.length()-1);
    }

    /**
     * Transform key into target
     *
     * @param key cache key
     * @return target infos
     */
    private static String[] key2Data(String key){
        return key.split(delimiter);
    }

    private static class NoDynamesException extends RuntimeException {
        NoDynamesException(){
            super("can not found Any Dynames!");
        }
    }
}
