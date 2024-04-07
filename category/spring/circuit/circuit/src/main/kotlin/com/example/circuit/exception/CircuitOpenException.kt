package com.example.circuit.exception

class CircuitOpenException(message: String = "Circuit breaker is open") : RuntimeException(message)
