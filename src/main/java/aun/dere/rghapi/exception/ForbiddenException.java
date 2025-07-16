package aun.dere.rghapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.TOO_MANY_REQUESTS, reason = "Forbidden access to the resource. Check your API key or permissions.")
public class ForbiddenException extends RuntimeException { }
