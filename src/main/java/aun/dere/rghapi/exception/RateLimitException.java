package aun.dere.rghapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.TOO_MANY_REQUESTS, reason = "GitHub API Rate limit exceeded")
public class RateLimitException extends RuntimeException { }
