package com.app.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class CorsController {

    @GetMapping("/cors")
    fun cors(): String {
        return "cors"
    }
}
