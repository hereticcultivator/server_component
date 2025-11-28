package com.comeon.component.exception

import com.comeon.component.common.Result
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import kotlin.Exception

@ControllerAdvice(basePackages = ["com.comeon.component"])
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(Exception::class)
    @ResponseBody
    fun error(e: Exception): Result<Nothing> {
        logger.error("系统异常", e)
        return Result.error("系统异常", code = 500)
    }

    @ExceptionHandler(CustomException::class)
    @ResponseBody
    fun customError(e: CustomException): Result<Nothing> {
        logger.error("自定义错误", e)
        return Result.error(e.message, code = e.code)
    }
    
    /**
     * 处理Bean Validation验证错误
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseBody
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): Result<List<Map<String, String>>> {
        val errors = ex.bindingResult.allErrors.map { error ->
            val fieldName = (error as? FieldError)?.field ?: error.objectName
            val errorMessage = error.defaultMessage ?: "验证失败"
            mapOf("field" to fieldName, "message" to errorMessage)
        }
        
        logger.warn("参数验证失败: $errors")
        return Result.error("参数验证失败", code = 400, data = errors)
    }
}