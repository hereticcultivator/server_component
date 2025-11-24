package com.comeon.component.mapper

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select

@Mapper
interface TestMapper {
    @Select("SELECT VERSION() as version")
    fun getVersion(): String
    
    @Select("SELECT DATABASE() as database")
    fun getDatabase(): String?
}


