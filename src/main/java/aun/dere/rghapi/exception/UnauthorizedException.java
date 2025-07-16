package aun.dere.rghapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Unauthorized access. Check your credentials.")
public class UnauthorizedException extends RuntimeException { }
