package com.comeon.component.exception

class CustomException(val msg: String, val code: String = "500") : RuntimeException() {}