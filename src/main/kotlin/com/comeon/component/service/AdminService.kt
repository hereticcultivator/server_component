package com.comeon.component.service

import com.comeon.component.exception.CustomException
import org.springframework.stereotype.Service

@Service
class AdminService {

    fun admin(name: String): String {
        if (name == "admin") return "admin" else throw CustomException("账号异常")
    }

}