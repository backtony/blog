package com.example.aopwithtransaction.aop

import java.lang.annotation.Inherited
import kotlin.annotation.AnnotationTarget.*

@Target(CLASS, FUNCTION, ANNOTATION_CLASS, TYPE)
@Inherited
@Retention(AnnotationRetention.RUNTIME)
annotation class Logging()
