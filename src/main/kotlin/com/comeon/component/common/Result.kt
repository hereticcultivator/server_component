package com.comeon.component.common

data class Result(
        val code: String,
        val data: Any?,
        val msg: String
) {
    companion object {
        fun success(data: Any? = null, msg: String = "success") = Result("200", data, msg)
        fun error(msg: String, code: String = "500") = Result(code, null, msg)
    }
}