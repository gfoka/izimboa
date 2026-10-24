package com.izimboa.common.web;

import java.net.URI;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Deux personnes modifient la même chose au même moment (ex. un livreur accepte pendant qu'un opérateur attribue) :
 * on répond 409 « réessayez » plutôt qu'une erreur 500.
 */
@RestControllerAdvice
class ConcurrencyProblemHandler {

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ProblemDetail conflict() {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Cette information vient d'être modifiée par quelqu'un d'autre. Rechargez et réessayez.");
        p.setType(URI.create("https://api.izimboa.com/problems/concurrent-update"));
        p.setProperty("code", "concurrent-update");
        return p;
    }
}
