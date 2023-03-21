package com.griddynamics.reactive.utils;

import java.util.function.Consumer;
import org.slf4j.MDC;
import reactor.core.publisher.Signal;

public final class LoggerUtils {

  public static final String LOG_CONTEXT = "REQUEST_ID";

  private LoggerUtils() {}

  public static <T> void configureTraceId(Signal<T> signal) {
    signal
        .getContextView()
        .getOrEmpty(LOG_CONTEXT)
        .map(Object::toString)
        .ifPresent(requestId -> MDC.put("traceId", requestId));
  }

  public static <T> Consumer<Signal<T>> logOnNext(Consumer<T> statement) {
    return signal -> {
      if (signal.isOnNext()) {
        configureTraceId(signal);
        statement.accept(signal.get());
      }
    };
  }

  public static Consumer<Signal<?>> logOnError(Consumer<Throwable> statement) {
    return signal -> {
      if (signal.isOnError()) {
        configureTraceId(signal);
        statement.accept(signal.getThrowable());
      }
    };
  }
}
