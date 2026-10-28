package com.izimboa.common.contact;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

class ContactProblem extends ErrorResponseException {

    private ContactProblem(HttpStatus status, String code, String detail) {
        super(status, problem(status, code, detail), null);
    }

    private static ProblemDetail problem(HttpStatus status, String code, String detail) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detail);
        p.setType(URI.create("https://api.izimboa.com/problems/" + code));
        p.setProperty("code", code);
        return p;
    }

    static ContactProblem invalid(String detail) {
        return new ContactProblem(HttpStatus.UNPROCESSABLE_CONTENT, "invalid-request", detail);
    }

    static ContactProblem tooMany() {
        return new ContactProblem(HttpStatus.TOO_MANY_REQUESTS, "too-many-requests",
                "Trop de demandes envoyées. Réessayez plus tard ou écrivez-nous à contact@izimboa.com.");
    }
}
