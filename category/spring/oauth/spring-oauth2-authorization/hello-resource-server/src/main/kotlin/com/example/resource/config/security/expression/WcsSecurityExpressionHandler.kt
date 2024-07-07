package com.webtoons.wcs.security.permission

import com.example.resource.config.security.expression.ResourceMethodSecurityExpressionRoot
import org.aopalliance.intercept.MethodInvocation
import org.springframework.expression.EvaluationContext
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.security.authentication.AuthenticationTrustResolverImpl
import org.springframework.security.core.Authentication
import java.util.function.Supplier

/**
 * https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html#_use_a_custom_bean_instead_of_subclassing_defaultmethodsecurityexpressionhandler
 */
class WcsSecurityExpressionHandler : DefaultMethodSecurityExpressionHandler() {

    private val trustResolver = AuthenticationTrustResolverImpl()

    override fun createEvaluationContext(authentication: Supplier<Authentication>, mi: MethodInvocation): EvaluationContext {
        val context = super.createEvaluationContext(authentication, mi) as StandardEvaluationContext
        val delegate = context.rootObject.value as MethodSecurityExpressionOperations
        val root = ResourceMethodSecurityExpressionRoot(
            authentication = authentication,
            operations = delegate,
        ).apply {
            setPermissionEvaluator(permissionEvaluator)
            setTrustResolver(trustResolver)
            setRoleHierarchy(roleHierarchy)
            setDefaultRolePrefix("")
        }
        context.setRootObject(root)
        return context
    }
}
