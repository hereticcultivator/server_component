package com.comeon.component

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ComeonApplication

fun main(args: Array<String>) {
    runApplication<ComeonApplication>(*args)
}
