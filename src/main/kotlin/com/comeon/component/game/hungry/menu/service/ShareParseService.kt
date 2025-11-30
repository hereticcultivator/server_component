package com.comeon.component.game.hungry.menu.service

import com.comeon.component.exception.CustomException
import com.comeon.component.game.hungry.menu.dto.JumpAction
import com.comeon.component.game.hungry.menu.dto.MetaInfo
import com.comeon.component.game.hungry.menu.dto.ParseResult
import com.comeon.component.game.hungry.menu.dto.ShopInfo
import com.comeon.component.game.hungry.menu.enums.ActionType
import com.comeon.component.game.hungry.menu.enums.Platform
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

@Service
class ShareParseService(
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(ShareParseService::class.java)

    data class NetworkInfo(val realUrl: String, val title: String)

    fun parse(text: String): ParseResult {
        logger.info("开始解析文本: $text")
        // 1. 提取 URL
        val rawUrl = extractUrl(text) ?: throw CustomException("未找到有效链接")

        // 2. 文本提取店名 (策略0-4)
        val nameFromText = extractNameFromText(text)
        logger.info("从文本提取店名: $nameFromText")

        // 3. 网络请求获取真实 URL 和 标题
        val networkInfo = resolveNetworkInfo(rawUrl)
        val realUrl = networkInfo.realUrl
        logger.info("获取到真实URL: $realUrl")
        
        // 4. 提取额外信息 (JD Scheme, ShopID)
        val jdScheme = extractJdScheme(realUrl)
        val taobaoShopId = extractTaobaoShopId(realUrl)
        val taobaoItemId = extractTaobaoItemId(realUrl)

        // 5. 网页标题清洗
        var shopNameFromWeb: String? = null
        if (networkInfo.title.isNotEmpty()) {
            val cleanedTitle = cleanTitle(networkInfo.title)
            if (cleanedTitle != "商家详情" && 
                cleanedTitle != "美团外卖" && 
                cleanedTitle != "京东下载页" && 
                cleanedTitle.isNotEmpty()) {
                shopNameFromWeb = cleanedTitle
            }
        }

        // 6. 最终店名决策
        val finalName = if (nameFromText != "未知商家") {
            nameFromText
        } else {
            shopNameFromWeb ?: nameFromText
        }

        // 7. 识别平台
        val platform = identifyPlatform(realUrl, finalName)

        // 8. 构造 ShopInfo (用于后续 assemble)
        val extra = mutableMapOf<String, String>()
        if (jdScheme != null) extra["jumpScheme"] = jdScheme
        if (taobaoShopId != null) extra["shopId"] = taobaoShopId
        if (taobaoItemId != null) extra["itemId"] = taobaoItemId

        val info = ShopInfo(
            platform = platform,
            shopName = finalName,
            originalUrl = rawUrl,
            realUrl = realUrl,
            extra = extra
        )

        return assembleResult(info)
    }

    private fun extractUrl(text: String): String? {
        // Use strict regex to match client behavior: ((http|https)://[a-zA-Z0-9./?=_-]+)
        val matcher = Pattern.compile("((http|https)://[a-zA-Z0-9./?=_-]+)").matcher(text)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun extractNameFromText(text: String): String {
        // 策略0: 优先匹配「店名」格式
        val bracketPattern = Pattern.compile("「(.*?)」")
        val bracketMatcher = bracketPattern.matcher(text)
        if (bracketMatcher.find()) {
            var name = bracketMatcher.group(1) ?: "未知商家"
            if (name.contains("京东外卖 | ")) {
                name = name.substringAfter("京东外卖 | ")
            }
            return name.trim()
        }

        // 策略1: 美团外卖关键词
        if (text.contains("美团外卖")) {
            val afterMeituan = text.substringAfter("美团外卖")
            val candidate = afterMeituan.substringBefore("http").trim()
            if (candidate.isNotEmpty()) return candidate
        }

        // 策略3: 淘宝/饿了么等通用格式 (取最后一个空格后的内容)
        val lastPart = text.substringAfterLast(" ").trim()
        if (lastPart.isNotEmpty() && !lastPart.startsWith("http") && lastPart.length > 2) {
            return lastPart
        }

        // 策略4: 第一段
        val firstPart = text.split("http", " ", "，", ",").firstOrNull { it.isNotBlank() }
        return firstPart ?: "未知商家"
    }

    private fun resolveNetworkInfo(urlStr: String): NetworkInfo {
        var currentUrl = urlStr
        var title = ""
        val visited = mutableSetOf<String>()

        for (i in 0 until 10) {
            if (!currentUrl.startsWith("http")) break
            if (!visited.add(currentUrl)) break

            try {
                val url = URL(currentUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = false
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                // 模拟 Android 客户端 User-Agent
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.181 Mobile Safari/537.36")

                val responseCode = connection.responseCode
                if (responseCode in 300..399) {
                    var location = connection.getHeaderField("Location")
                    if (!location.isNullOrBlank()) {
                         if (!location.startsWith("http") && !location.contains("://")) {
                            location = URL(url, location).toExternalForm()
                        }
                        currentUrl = location
                    } else {
                        break
                    }
                } else if (responseCode == 200) {
                    // 读取 body 以获取 title 或 JS 重定向 (淘宝 m.tb.cn)
                    val body = readBody(connection)
                    
                    // 淘宝短链 JS 重定向检查
                    if (currentUrl.contains("m.tb.cn")) {
                        val jsUrlMatcher = Pattern.compile("var url = '(.*?)'").matcher(body)
                        if (jsUrlMatcher.find()) {
                            val extractedUrl = jsUrlMatcher.group(1)
                            if (!extractedUrl.isNullOrEmpty()) {
                                currentUrl = extractedUrl
                                // 发现 JS 重定向，继续循环
                                continue
                            }
                        }
                    }

                    // 提取 Title
                    val titleMatcher = Pattern.compile("<title>(.*?)</title>").matcher(body)
                    if (titleMatcher.find()) {
                        title = titleMatcher.group(1)
                    }
                    break
                } else {
                    break
                }
            } catch (e: Exception) {
                logger.warn("Resolve error: ${e.message}")
                break
            }
        }
        return NetworkInfo(currentUrl, title)
    }

    private fun readBody(connection: HttpURLConnection): String {
        return try {
            val reader = BufferedReader(InputStreamReader(connection.inputStream, StandardCharsets.UTF_8))
            reader.use { it.readText() }
        } catch (e: Exception) {
            ""
        }
    }

    private fun cleanTitle(title: String): String {
        var name = title
        val suffixes = listOf(" - 大众点评", "-大众点评", " 电话", "地址", "营业时间", "_大众点评网", "-美团外卖")
        for (suffix in suffixes) {
            if (name.contains(suffix)) {
                name = name.substringBefore(suffix)
            }
        }
        return name.trim()
    }

    private fun extractJdScheme(url: String): String? {
        if (url.contains("yinliuhuanqi=")) {
            try {
                val decodedUrl = URLDecoder.decode(url, "UTF-8")
                val matcher = Pattern.compile("(openapp\\.jdmobile://[^&]+)").matcher(decodedUrl)
                if (matcher.find()) {
                    var scheme = matcher.group(1)
                    // 再次解码
                    scheme = URLDecoder.decode(scheme, "UTF-8")
                    return scheme
                }
            } catch (e: Exception) {
                logger.error("Error extracting JD scheme", e)
            }
        }
        return null
    }

    private fun extractTaobaoShopId(url: String): String? {
        val matcher = Pattern.compile("[?&]shop_id=(\\d+)").matcher(url)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun extractTaobaoItemId(url: String): String? {
        val matcher = Pattern.compile("[?&]id=(\\d+)").matcher(url)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun identifyPlatform(url: String, shopName: String): Platform {
        return when {
            url.contains("meituan") -> Platform.MEITUAN
            url.contains("jd.com") || url.contains("3.cn") || shopName.contains("京东") -> Platform.JINGDONG
            url.contains("taobao") || url.contains("tmall") || url.contains("tb.cn") -> Platform.TAOBAO
            url.contains("dianping") -> Platform.UNKNOWN // Codebase uses UNKNOWN for Dianping or add enum? Current enum doesn't have DIANPING.
            url.contains("ele.me") -> Platform.ELEME
            else -> Platform.UNKNOWN
        }
    }

    private fun assembleResult(info: ShopInfo): ParseResult {
        val actions = mutableListOf<JumpAction>()

        when (info.platform) {
            Platform.MEITUAN -> {
                // 美团降级策略
                actions.add(
                    JumpAction(
                        type = ActionType.SCHEME,
                        packageName = "com.sankuai.meituan",
                        uri = "imeituan://www.meituan.com/search?q=${info.shopName}",
                        intentFlags = listOf("NEW_TASK")
                    )
                )
            }
            Platform.JINGDONG -> {
                // 京东策略
                val jumpScheme = info.extra["jumpScheme"]
                if (!jumpScheme.isNullOrEmpty()) {
                    actions.add(
                        JumpAction(
                            type = ActionType.SCHEME,
                            packageName = "com.jingdong.app.mall",
                            uri = jumpScheme,
                            intentFlags = listOf("NEW_TASK")
                        )
                    )
                }
            }
            Platform.TAOBAO -> {
                // 淘宝策略
                val shopId = info.extra["shopId"]
                val itemId = info.extra["itemId"]
                val realUrl = info.realUrl
                val targetUrl = if (realUrl.isNotEmpty()) realUrl else info.originalUrl

                if (shopId != null) {
                    actions.add(
                        JumpAction(
                            type = ActionType.SCHEME,
                            packageName = "com.taobao.taobao",
                            uri = "taobao://shop.m.taobao.com/shop/shop_index.htm?shop_id=$shopId",
                            intentFlags = listOf("NEW_TASK", "CLEAR_TASK")
                        )
                    )
                } else if (itemId != null) {
                    actions.add(
                        JumpAction(
                            type = ActionType.SCHEME,
                            packageName = "com.taobao.taobao",
                            uri = "taobao://item.taobao.com/item.htm?id=$itemId",
                            intentFlags = listOf("NEW_TASK", "CLEAR_TASK")
                        )
                    )
                } else {
                     // App Link (HTTPS)
                     if (targetUrl.contains("taobao.com") || targetUrl.contains("tmall.com") || targetUrl.contains("m.tb.cn")) {
                         actions.add(
                            JumpAction(
                                type = ActionType.SCHEME,
                                packageName = "com.taobao.taobao",
                                uri = targetUrl,
                                intentFlags = listOf("NEW_TASK", "CLEAR_TASK")
                            )
                         )
                     } else {
                         // Fallback replace
                         val schemeUrl = targetUrl.replace("https://", "taobao://").replace("http://", "taobao://")
                         actions.add(
                            JumpAction(
                                type = ActionType.SCHEME,
                                packageName = "com.taobao.taobao",
                                uri = schemeUrl,
                                intentFlags = listOf("NEW_TASK", "CLEAR_TASK")
                            )
                         )
                     }
                }
            }
            Platform.UNKNOWN -> { // Assuming Dianping maps to UNKNOWN here for now, or check url
                 if (info.realUrl.contains("dianping")) {
                     actions.add(
                        JumpAction(
                            type = ActionType.SCHEME,
                            packageName = "com.dianping.v1",
                            uri = "dianping://searchshoplist?keyword=${info.shopName}",
                            intentFlags = listOf("NEW_TASK", "CLEAR_TOP")
                        )
                    )
                 } else {
                     // Generic Web Fallback
                     actions.add(
                         JumpAction(
                             type = ActionType.WEB,
                             uri = info.realUrl
                         )
                     )
                 }
            }
            else -> {
                // Generic Web Fallback
                 actions.add(
                     JumpAction(
                         type = ActionType.WEB,
                         uri = info.realUrl
                     )
                 )
            }
        }

        val platformName = when (info.platform) {
            Platform.MEITUAN -> "美团外卖" // Client uses "美团外卖"
            Platform.JINGDONG -> "京东"
            Platform.TAOBAO -> "淘宝"
            Platform.ELEME -> "饿了么"
            else -> if (info.realUrl.contains("dianping")) "大众点评" else "未知平台"
        }

        val meta = MetaInfo(
            title = "解析成功",
            desc = "检测到【$platformName】店铺：${info.shopName}\n建议跳转。",
            icon = null,
            platform = if (info.realUrl.contains("dianping")) "DIANPING" else info.platform.key
        )

        return ParseResult(meta, actions)
    }
}
