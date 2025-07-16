package aun.dere.rghapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class AppException {

    @ResponseStatus(value = HttpStatus.TOO_MANY_REQUESTS, reason = "Forbidden access to the resource. Check your API key or permissions.")
    public static class ForbiddenException extends RuntimeException { }

    @ResponseStatus(value = HttpStatus.TOO_MANY_REQUESTS, reason = "GitHub API Rate limit exceeded")
    public static class RateLimitException extends RuntimeException { }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Repo not found")
    public static class RepoNotFoundException extends RuntimeException { }

    @ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Unauthorized access. Check your credentials.")
    public static class UnauthorizedException extends RuntimeException { }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "User not found")
    public static class UserNotFoundException extends RuntimeException { }
}
