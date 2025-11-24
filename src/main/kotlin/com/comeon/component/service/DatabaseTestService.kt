package com.comeon.component.service

import com.comeon.component.mapper.TestMapper
import org.springframework.stereotype.Service

@Service
class DatabaseTestService(
    private val testMapper: TestMapper
) {
    fun testConnection(): Map<String, Any?> {
        return mapOf(
            "version" to testMapper.getVersion(),
            "database" to testMapper.getDatabase(),
            "status" to "connected"
        )
    }
}


