package cn.qcsoft.angelos.util.exception

/**
 * Created by QC on 2018/7/19 16:54.
 */
class BusinessException : Exception {
    private val bizMessage: String

    constructor(msg: String) : super(msg) {
        bizMessage = msg
    }

    constructor(msg: String, ex: Exception) : super(msg) {
        bizMessage = msg
        this.initCause(ex)
    }

    fun getBizMessage():String{
        return bizMessage
    }
}
