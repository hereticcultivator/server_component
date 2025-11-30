package com.comeon.component.game.hungry.menu.controller

import com.comeon.component.common.Result
import com.comeon.component.game.hungry.menu.dto.ParseRequest
import com.comeon.component.game.hungry.menu.dto.ParseResult
import com.comeon.component.game.hungry.menu.service.ShareParseService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/game/hungry/parse")
class ShareParseController(
    private val shareParseService: ShareParseService
) {

    /**
     * 解析分享链接
     * 无需鉴权
     */
    @PostMapping("/share")
    fun parseShare(@RequestBody request: ParseRequest): ResponseEntity<Result<ParseResult>> {
        val result = shareParseService.parse(request.text)
        return ResponseEntity.ok(Result.success(data = result))
    }
}

