package com.librax.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Single-Page-Application (SPA) client routing forwarder.
 * Forwards non-API and non-static asset routes to /index.html,
 * allowing HTML5 browser history navigation and direct URL refreshes.
 */
@Controller
public class SpaController {

    @GetMapping(value = {
            "/{path:[^\\.]*}",
            "/*/{path:[^\\.]*}"
    })
    public String forwardSpaRoutes() {
        return "forward:/index.html";
    }
}
