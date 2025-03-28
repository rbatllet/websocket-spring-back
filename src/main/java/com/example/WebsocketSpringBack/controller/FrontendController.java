package com.example.WebsocketSpringBack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    /**
     * Forward the root path to Vue.js frontend
     * This ensures that routes managed by Vue Router will work correctly
     */
    @GetMapping(value = "/")
    public String index() {
        return "forward:/index.html";
    }
    
    /**
     * Forward any UI routes to the frontend
     * Explicitly excluding the WebSocket endpoint path
     */
    @GetMapping(value = {"/app/**", "/login", "/about", "/profile", "/settings"})
    public String routes() {
        return "forward:/index.html";
    }
}
