package org.example.blog_spring.aop;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * All application services.
     */
    @Pointcut("within(org.example.blog_spring.service..*)")
    public void serviceLayer() {}

    /**
     * All REST and GraphQL controllers.
     */
    @Pointcut("within(org.example.blog_spring.web..*)")
    public void controllerLayer() {}

    /**
     * Services and controllers combined.
     */
    @Pointcut("serviceLayer() || controllerLayer()")
    public void serviceOrControllerLayer() {}

    @Before("serviceOrControllerLayer()")
    public void logEntry(JoinPoint joinPoint) {
        if (!log.isInfoEnabled()) {
            return;
        }
        log.info("Entering {} with args={}", joinPoint.getSignature().toShortString(),
                Arrays.toString(joinPoint.getArgs()));
    }

    @AfterReturning(pointcut = "serviceOrControllerLayer()", returning = "result")
    public void logExit(JoinPoint joinPoint, Object result) {
        if (!log.isInfoEnabled()) {
            return;
        }
        log.info("Exiting {} with result={}", joinPoint.getSignature().toShortString(),
                summarizeResult(result));
    }

    @AfterThrowing(pointcut = "serviceOrControllerLayer()", throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        log.error("Exception in {} with args={} message={}",
                joinPoint.getSignature().toShortString(), Arrays.toString(joinPoint.getArgs()),
                ex.getMessage(), ex);
    }

    private Object summarizeResult(Object result) {
        if (result == null) {
            return null;
        }
        var type = result.getClass().getSimpleName();

        // Avoid dumping huge payloads in logs.
        if (result instanceof org.springframework.http.ResponseEntity<?> resp) {
            return "ResponseEntity(status=%d)".formatted(resp.getStatusCode().value());
        }

        if (result instanceof org.springframework.data.domain.Page<?> page) {
            return "Page<%s>{page=%d,size=%d,totalElements=%d}".formatted(
                    page.getContent().isEmpty() ? "?"
                            : page.getContent().get(0).getClass().getSimpleName(),
                    page.getNumber(), page.getSize(), page.getTotalElements());
        }

        if (result instanceof java.util.Collection<?> collection) {
            return "%s(size=%d)".formatted(type, collection.size());
        }

        return type;
    }
}

