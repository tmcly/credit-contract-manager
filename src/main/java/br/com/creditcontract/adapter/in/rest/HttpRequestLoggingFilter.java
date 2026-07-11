package br.com.creditcontract.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** Adds request correlation and emits one structured completion log per API call. */
@Component
public class HttpRequestLoggingFilter extends OncePerRequestFilter {

	public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
	public static final String CORRELATION_ID_ATTRIBUTE = "effectiveCorrelationId";

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestLoggingFilter.class);
	private final ObjectMapper objectMapper;

	public HttpRequestLoggingFilter(ObjectMapper objectMapper) {
		this.objectMapper = Objects.requireNonNull(objectMapper);
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		long startedAt = System.nanoTime();
		UUID correlationId;
		try {
			correlationId = correlationIdFrom(request);
		} catch (IllegalArgumentException exception) {
			correlationId = UUID.randomUUID();
			response.setHeader(CORRELATION_ID_HEADER, correlationId.toString());
			MDC.put("correlationId", correlationId.toString());
			try {
				writeInvalidCorrelationId(response);
			} finally {
				logCompletion(request, response, startedAt);
				MDC.remove("correlationId");
			}
			return;
		}

		request.setAttribute(CORRELATION_ID_ATTRIBUTE, correlationId);
		response.setHeader(CORRELATION_ID_HEADER, correlationId.toString());
		MDC.put("correlationId", correlationId.toString());
		try {
			filterChain.doFilter(request, response);
		} finally {
			logCompletion(request, response, startedAt);
			MDC.remove("correlationId");
		}
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path.equals("/health") || path.startsWith("/actuator");
	}

	private UUID correlationIdFrom(HttpServletRequest request) {
		String requested = request.getHeader(CORRELATION_ID_HEADER);
		if (requested == null || requested.isBlank()) {
			return UUID.randomUUID();
		}
		return UUID.fromString(requested);
	}

	private void writeInvalidCorrelationId(HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), Map.of(
					"type", "/errors/invalid-correlation-id",
					"title", "Invalid request",
					"status", HttpServletResponse.SC_BAD_REQUEST,
					"detail", CORRELATION_ID_HEADER + " must be a valid UUID"));
	}

	private void logCompletion(
			HttpServletRequest request, HttpServletResponse response, long startedAt) {
		long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
		Object route = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
		LOGGER.atInfo()
				.addKeyValue("event", "http_request_completed")
				.addKeyValue("httpMethod", request.getMethod())
				.addKeyValue("httpPath", route == null ? request.getRequestURI() : route)
				.addKeyValue("httpStatus", response.getStatus())
				.addKeyValue("durationMs", durationMs)
				.log("HTTP request completed");
	}
}
