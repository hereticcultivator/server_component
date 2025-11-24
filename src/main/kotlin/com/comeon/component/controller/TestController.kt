package com.comeon.component.controller

import com.comeon.component.common.Result
import com.comeon.component.service.AdminService
import com.comeon.component.service.DatabaseTestService
import jakarta.annotation.Resource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {

    @Resource
    lateinit var adminService: AdminService

    @Resource
    lateinit var databaseTestService: DatabaseTestService

    @GetMapping("/hello")
    fun hello(): Result<Map<String, String>> {
        //测试全局异常
//        val s = 1/0
        return Result.success(
                data = mapOf(
                        "llj" to "hello"
                )
        )
//         return Result.error(message = "错误")
    }

    @GetMapping("/admin")
    fun admin(name: String): Result<Any> {
        val admin = adminService.admin(name)
        return Result.success(data = admin)
    }

    @GetMapping("/db/test")
    fun testDatabase(): Result<Any> {
        val connectionInfo = databaseTestService.testConnection()
        return Result.success(data = connectionInfo)
    }

}