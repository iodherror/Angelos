package tk.qcsoft.angelos.util

import org.slf4j.helpers.MessageFormatter

/**
 * Created by QC on 2018/9/14 16:33.
 */
class StrUtils {
    companion object {
        fun format(input: String, vararg obj: Any): String {
            return MessageFormatter.arrayFormat(input, obj).message
        }
    }
}