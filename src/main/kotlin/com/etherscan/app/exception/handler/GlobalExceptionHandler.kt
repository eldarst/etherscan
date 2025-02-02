package com.etherscan.app.exception.handler

import com.etherscan.app.exception.exception.ErrorResponse
import com.etherscan.app.exception.exception.NoRecordsFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(NoRecordsFoundException::class)
    fun handleMyCustomException(ex: NoRecordsFoundException): ResponseEntity<ErrorResponse> {
        val body =
            ErrorResponse(
                error = "Resource wasn't found",
                message = ex.message!!,
            )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body)
    }

    // You can handle other exceptions, e.g. NullPointerException or generic:
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        val body =
            ErrorResponse(
                error = "GENERIC_ERROR",
                message = ex.message ?: "An unexpected error occurred.",
            )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }
}
