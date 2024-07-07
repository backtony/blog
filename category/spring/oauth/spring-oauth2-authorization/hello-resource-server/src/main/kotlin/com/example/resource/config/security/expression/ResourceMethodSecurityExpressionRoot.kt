package com.example.resource.config.security.expression

import com.example.resource.config.security.oauth.User
import org.springframework.security.access.expression.SecurityExpressionRoot
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.security.core.Authentication
import java.util.function.Supplier

class ResourceMethodSecurityExpressionRoot(
    authentication: Supplier<Authentication>,
    private val operations: MethodSecurityExpressionOperations,
) : SecurityExpressionRoot(authentication), MethodSecurityExpressionOperations {

    fun hasUserType(userType: String): Boolean {
        return hasAnyUserType(userType)
    }

    fun hasAnyUserType(vararg userTypes: String): Boolean {
        if (this.principal !is User) {
            return false
        }

        val user = principal as User

        for (userType in userTypes) {
            if (userType == user.type) {
                return true
            }
        }
        return false
    }

    fun hasEveryRole(vararg roleTypes: String): Boolean {
        for (roleType in roleTypes) {
            if (!hasRole(roleType)) {
                return false
            }
        }
        return true
    }

    override fun setFilterObject(filterObject: Any?) {
        operations.filterObject = filterObject
    }

    override fun getFilterObject(): Any? {
        return operations.filterObject
    }

    override fun setReturnObject(returnObject: Any?) {
        operations.returnObject = returnObject
    }

    override fun getReturnObject(): Any? {
        return operations.returnObject
    }

    override fun getThis(): Any? {
        return operations.`this`
    }
}
