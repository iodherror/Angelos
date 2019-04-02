package tk.qcsoft.angelos.util.exception

import org.slf4j.LoggerFactory
import java.util.*

import java.util.function.Supplier

/**
 * Created by QC on 2018/7/20 16:45.
 */
class ExceptionLauncherImpl : ExceptionLauncher {

    /**
     * throw BusinessException(@msg)
     */
    @Throws(BusinessException::class)
    override fun throwBusinessException(msg: String) {
        logger.debug(msg)
        throw BusinessException(msg)
    }

    /**
     * if @obj is null,throw BusinessException(@msg)
     */
    @Throws(BusinessException::class)
    override fun checkArgumentNotNull(obj: Any, msg: String) {
        if (Objects.isNull(obj)) throwBusinessException(msg)
    }

    /**
     * if @expression is false,throw BusinessException(@msg)
     */
    @Throws(BusinessException::class)
    override fun checkArgument(expression: Boolean, msg: String) {
        if (!expression) throwBusinessException(msg)
    }

    /**
     * if @return is neither null nor Empty,throw BusinessException(@Return)
     */
    @Throws(BusinessException::class)
    override fun checkArgument(check: Supplier<String?>) {
        val msg = check.get()
        msg?.takeIf { s -> s.isNotEmpty()  }?.apply { throwBusinessException(this) }
    }

    /**
     * throw BusinessException(@msg),
     */
    @Throws(BusinessException::class)
    override fun throwBusinessException(msg: String, ex: Exception) {
        logger.debug(msg)
        val businessException = BusinessException(msg)
        businessException.initCause(ex)
        throw businessException
    }

    /**
     * return msg for error ,null for success
     */
    @Throws(BusinessException::class)
    override fun checkArgument(expression: Boolean, msg: String, ex: Exception) {
        if (!expression) throwBusinessException(msg, ex)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExceptionLauncherImpl::class.java)
    }


}
