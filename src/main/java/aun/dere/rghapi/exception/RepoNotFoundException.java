package aun.dere.rghapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Repo not found")
public class RepoNotFoundException extends RuntimeException { }
