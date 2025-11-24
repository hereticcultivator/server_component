package com.comeon.component.exception

class CustomException(override val message: String, val code: Int = 500) : RuntimeException(message)