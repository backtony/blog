package com.example.resource.controller

import com.example.resource.config.security.oauth.User
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class ResourceController {

//    @PreAuthorize("hasPermission(#id, 'RESOURCE', 'READ')")
    @PreAuthorize("hasRole('RAED_ALL_TITLE') || hasUserType('OPERATOR')")
    @GetMapping("/resource/{id}")
    fun getResource(@PathVariable id :Long, @AuthenticationPrincipal user: User): String {
        println(user)
        return "resource"
    }
}
