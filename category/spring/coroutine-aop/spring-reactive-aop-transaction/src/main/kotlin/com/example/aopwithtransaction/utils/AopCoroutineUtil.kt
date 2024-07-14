package com.example.aopwithtransaction.utils

import org.aspectj.lang.ProceedingJoinPoint
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.startCoroutineUninterceptedOrReturn
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

fun ProceedingJoinPoint.runCoroutine(
    block: suspend () -> Any?,
): Any? = block.startCoroutineUninterceptedOrReturn(this.coroutineContinuation())

@Suppress("UNCHECKED_CAST")
fun ProceedingJoinPoint.coroutineContinuation(): Continuation<Any?> {
    return this.args.last() as Continuation<Any?>
}

fun ProceedingJoinPoint.coroutineArgs(): Array<Any?> {
    return this.args.sliceArray(0 until this.args.size - 1)
}

suspend fun ProceedingJoinPoint.proceedCoroutine(
    args: Array<Any?> = this.coroutineArgs(),
): Any? = suspendCoroutineUninterceptedOrReturn { continuation ->
    this.proceed(args + continuation)
}
