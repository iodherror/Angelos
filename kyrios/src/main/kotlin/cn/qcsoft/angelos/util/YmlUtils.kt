@file:Suppress("UNCHECKED_CAST")
package cn.qcsoft.angelos.util

import org.ho.yaml.Yaml
import java.io.File
import kotlin.collections.HashMap


/**
 * Created by QC on 2018/9/10 16:28.
 */
class YmlUtils

private constructor(vararg fileNames: String) {

    private val root: Map<String, Any>

    init {
        var map: Map<String, Any> = HashMap(0)
        for (fileName in fileNames) {
            val conf = this.getRoot(fileName)
            map = this.merge(map, conf)
        }
        root = map
    }

    /**
     * @param key
     * @return
     */
    private fun <T> getValue(key: String): T {
        return root[key] as T ?: throw RuntimeException(
                "The value corresponding to the key is not set.key[$key]")
    }

    /**
     * @param key
     * @return
     */
    fun getOrDefault(key: String,value :Any): Any {

        return root[key] ?: value
    }


    /**
     *
     * @param key
     * @return
     */
    operator fun <T> get(key: String): T {
        return getValue<T>(key)
    }

    /**
     * @param resouce
     * @return
     */
    private fun getRoot(resouce: String): Map<String, Any> {
        try {
            return File(resouce).exists().takeIf { it }.run {
                YmlUtils::class.java.getResourceAsStream(resouce).use {
                    Yaml.load(it) as Map<String, Any>
                }
            }
        }catch (ex:Exception){}
        return HashMap()

    }

    /**
     * @param map1
     * @param map2
     * @return
     */
    private fun merge(map1: Map<String, Any>,
                      map2: Map<String, Any>): Map<String, Any> {

        val merged = HashMap(map1)

        for (key in map2.keys) {
            if (merged.containsKey(key)) {
                throw RuntimeException(String.format(
                        "The key overlaps. [%s]", key))
            }
            merged[key] = map2[key]
        }

        return merged
    }

    companion object {
        /**
         *
         * @return
         */
        val kyrios = YmlUtils("/kyrios.yml")
    }
}