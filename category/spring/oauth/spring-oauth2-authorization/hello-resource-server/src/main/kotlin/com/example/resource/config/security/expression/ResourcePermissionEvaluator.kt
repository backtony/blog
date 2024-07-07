package com.example.resource.config.security.expression

import com.example.resource.config.security.oauth.User
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import java.io.Serializable

// Resource별 권한 검증
class ResourcePermissionEvaluator : PermissionEvaluator {
    override fun hasPermission(authentication: Authentication?, targetDomainObject: Any?, permission: Any?): Boolean {
        throw UnsupportedOperationException("not support")
    }

    override fun hasPermission(authentication: Authentication?, targetId: Serializable, targetType: String, permission: Any): Boolean {
        if (authentication?.principal !is User) {
            return false
        }

        val user = authentication.principal as User
        if (user.type == "OPERATOR") {
            return true
        }
        return false
    }
}
