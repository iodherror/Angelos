package tk.qcsoft.angelos.util;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by QC on 2019/1/11 10:39.
 */
public class YamlUtils {

    private YamlUtils(){}

    private static Yaml yaml = new Yaml();
    private static final String CONFIG_NAME = "config.yml";

    private static Map<String, Map> yamlCache = new HashMap<>();

    public static synchronized Map loadFile(String fileName) {
        URL url = YamlUtils.class.getResource("/"+fileName);
        if(Objects.isNull(url)) return new HashMap();
        try(FileInputStream fis = new FileInputStream(url.getFile())){
            Map config = yaml.load(fis);
            yamlCache.put(fileName,config);
            return config;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap();
    }

    public static <T> T getConfig(String key){
        return getCustomConfig(CONFIG_NAME,key);
    }
    public static <T> T getConfigOrDefault(String key,T defalutValue){
        return Optional.ofNullable(YamlUtils.<T>getCustomConfig(CONFIG_NAME,key)).orElse(defalutValue);
    }

    public static <T> T getCustomConfig(String configFileName,String key){
        Map target = yamlCache.getOrDefault(configFileName,
                loadFile(configFileName));

        if(target.isEmpty()) return null;

        if(target.containsKey(key)){
            return (T)target.get(key);
        }

        Object obj=null;
        if(key.indexOf('.')>0){
            String[] keys = key.split("\\.");
            obj = yamlCache.get(configFileName);
            for (String curKey : keys) {
                if(Objects.isNull(obj))break;
                obj = ((Map)obj).get(curKey);
            }
        }
        return (T)obj;
    }

    public static <T> T getCustomConfigOrDefault(String configFileName,String key,T defaultValue){
        return Optional.ofNullable(YamlUtils.<T>getCustomConfig(configFileName,key)).orElse(defaultValue);
    }

}
