package com.comeon.component.exception

import com.comeon.component.common.Result
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import kotlin.Exception

@ControllerAdvice("com.comeon.component.controller")
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(Exception::class)
    @ResponseBody
    fun error(e: Exception): Result {
        logger.error("系统异常", e)
        return Result.error("系统异常")
    }

    @ExceptionHandler(CustomException::class)
    @ResponseBody
    fun customError(e: CustomException): Result {
        logger.error("自定义错误", e)
        return Result.error(e.msg, e.code)
    }



}