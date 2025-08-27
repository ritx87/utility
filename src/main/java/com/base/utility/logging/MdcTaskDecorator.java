package com.base.utility.logging;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

public class MdcTaskDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
        // Capture MDC context from the original thread
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        return () -> {
            try {
                // Restore MDC context in the new thread
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                runnable.run();
            } finally {
                // Always clean up MDC after execution
                MDC.clear();
            }
        };
    }
}
