package com.doht.wallet_ledger.common.exception;

import com.doht.wallet_ledger.auth.domain.exception.AccountLockedException;
import com.doht.wallet_ledger.auth.domain.exception.EmailAlreadyExistsException;
import com.doht.wallet_ledger.auth.domain.exception.InvalidCredentialsException;
import com.doht.wallet_ledger.auth.domain.exception.InvalidRefreshTokenException;
import com.doht.wallet_ledger.auth.domain.exception.UserNotFoundException;
import com.doht.wallet_ledger.common.i18n.MessageKeys;
import com.doht.wallet_ledger.common.i18n.MessageService;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageService messageService;

    @ExceptionHandler(InvalidSortFieldException.class)
    public ProblemDetail handleInvalidSortField(InvalidSortFieldException ex) {
        return buildDomainProblem(HttpStatus.BAD_REQUEST, MessageKeys.COMMON_ERROR_INVALID_REQUEST_TITLE, ex);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return buildDomainProblem(HttpStatus.CONFLICT, MessageKeys.COMMON_ERROR_CONFLICT_TITLE, ex);
    }

    @ExceptionHandler({InvalidCredentialsException.class, InvalidRefreshTokenException.class})
    public ProblemDetail handleAuthenticationFailure(DomainException ex) {
        return buildDomainProblem(HttpStatus.UNAUTHORIZED, MessageKeys.COMMON_ERROR_UNAUTHORIZED_TITLE, ex);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ProblemDetail handleAccountLocked(AccountLockedException ex) {
        return buildDomainProblem(HttpStatus.FORBIDDEN, MessageKeys.AUTH_ERROR_ACCOUNT_LOCKED_TITLE, ex);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        return buildDomainProblem(HttpStatus.NOT_FOUND, MessageKeys.COMMON_ERROR_NOT_FOUND_TITLE, ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String fallback = messageService.get(MessageKeys.COMMON_ERROR_INVALID_VALUE);
        Map<String, List<String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        LinkedHashMap::new,
                        Collectors.mapping(
                                err -> err.getDefaultMessage() != null ? err.getDefaultMessage() : fallback,
                                Collectors.toList()
                        )
                ));

        ProblemDetail problem = buildProblem(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_FAILED",
                MessageKeys.COMMON_ERROR_VALIDATION_FAILED_TITLE,
                MessageKeys.COMMON_ERROR_VALIDATION_FAILED_DETAIL
        );
        problem.setProperty("errors", fieldErrors);
        return problem;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, List<String>> violations = ex.getConstraintViolations().stream()
                .collect(Collectors.groupingBy(
                        v -> v.getPropertyPath().toString(),
                        LinkedHashMap::new,
                        Collectors.mapping(ConstraintViolation::getMessage, Collectors.toList())
                ));

        ProblemDetail problem = buildProblem(
                HttpStatus.BAD_REQUEST,
                "CONSTRAINT_VIOLATION",
                MessageKeys.COMMON_ERROR_VALIDATION_FAILED_TITLE,
                MessageKeys.COMMON_ERROR_CONSTRAINT_VIOLATION_DETAIL
        );
        problem.setProperty("errors", violations);
        return problem;
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockException.class})
    public ProblemDetail handleOptimisticLock(Exception ex) {
        log.warn("Optimistic locking conflict detected: {}", ex.getMessage());
        return buildProblem(
                HttpStatus.CONFLICT,
                "CONCURRENT_MODIFICATION",
                MessageKeys.COMMON_ERROR_CONCURRENT_MODIFICATION_TITLE,
                MessageKeys.COMMON_ERROR_CONCURRENT_MODIFICATION_DETAIL
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return buildProblem(
                HttpStatus.CONFLICT,
                "DATA_INTEGRITY_VIOLATION",
                MessageKeys.COMMON_ERROR_DATA_CONFLICT_TITLE,
                MessageKeys.COMMON_ERROR_DATA_CONFLICT_DETAIL
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return buildProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                MessageKeys.COMMON_ERROR_INTERNAL_SERVER_TITLE,
                MessageKeys.COMMON_ERROR_INTERNAL_SERVER_DETAIL
        );
    }

    private ProblemDetail buildDomainProblem(HttpStatus status, String titleKey, DomainException ex) {
        return buildProblem(status, ex.getErrorCode(), titleKey, ex.getMessageKey(), ex.getMessageArgs());
    }

    private ProblemDetail buildProblem(
            HttpStatus status,
            String errorCode,
            String titleKey,
            String detailKey,
            Object... detailArgs
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, messageService.get(detailKey, detailArgs));
        problem.setType(URI.create("urn:wallet-ledger:error:" + errorCode.toLowerCase(Locale.ROOT).replace('_', '-')));
        problem.setTitle(messageService.get(titleKey));
        problem.setProperty("errorCode", errorCode);
        problem.setProperty("messageKey", detailKey);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
