package com.comeon.component.common

data class Result<T>(
    val code: Int,                    // 业务状态码（数字）
    val message: String,              // 响应消息
    val data: T? = null,              // 响应数据（泛型）
    val success: Boolean = true,      // 是否成功
    val timestamp: Long = System.currentTimeMillis()  // 时间戳
) {
    companion object {
        fun <T> success(
            data: T? = null, 
            message: String = "success"
        ) = Result(
            code = 200, 
            message = message, 
            data = data, 
            success = true
        )
        
        fun <T> error(
            message: String, 
            code: Int = 500,
            data: T? = null
        ) = Result(
            code = code,
            message = message,
            data = data,
            success = false
        )
    }
}