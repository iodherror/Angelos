package tk.qcsoft.angelos.util.exception

import java.util.function.Supplier

/**
 * Created by QC on 2018/7/20 16:44.
 */
interface ExceptionLauncher {
    @Throws(BusinessException::class)
    fun throwBusinessException(msg: String)

    @Throws(BusinessException::class)
    fun checkArgumentNotNull(obj: Any, msg: String)

    @Throws(BusinessException::class)
    fun checkArgument(expression: Boolean, msg: String)

    @Throws(BusinessException::class)
    fun checkArgument(check: Supplier<String?>)

    @Throws(BusinessException::class)
    fun throwBusinessException(msg: String, ex: Exception)

    @Throws(BusinessException::class)
    fun checkArgument(expression: Boolean, msg: String, ex: Exception)

}
