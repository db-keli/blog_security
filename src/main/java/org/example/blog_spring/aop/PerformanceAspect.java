package org.example.blog_spring.aop;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceAspect {

    private static final Logger log = LoggerFactory.getLogger(PerformanceAspect.class);

    private final MeterRegistry meterRegistry;

    public PerformanceAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * All application services.
     */
    @Pointcut("within(org.example.blog_spring.service..*)")
    public void serviceLayer() {
        // pointcut marker
    }

    @Around("serviceLayer()")
    public Object timeExecution(ProceedingJoinPoint pjp) throws Throwable {
        String method = pjp.getSignature().toShortString();
        long start = System.nanoTime();
        try {
            Object result = pjp.proceed();
            long durationNs = System.nanoTime() - start;
            recordMetric(method, durationNs, "success");
            if (log.isDebugEnabled()) {
                log.debug("Executed {} in {} ms", method, durationNs / 1_000_000);
            }
            return result;
        } catch (Throwable ex) {
            long durationNs = System.nanoTime() - start;
            recordMetric(method, durationNs, "error");
            log.warn("Exception in {} after {} ms: {}", method, durationNs / 1_000_000,
                    ex.getMessage());
            throw ex;
        }
    }

    private void recordMetric(String method, long durationNs, String outcome) {
        Timer.builder("blog.service.execution").tag("method", method).tag("outcome", outcome)
                .register(meterRegistry)
                .record(durationNs, java.util.concurrent.TimeUnit.NANOSECONDS);
    }
}

