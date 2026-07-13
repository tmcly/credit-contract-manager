package br.com.creditcontract.application.query;

import java.util.List;
import java.util.Objects;

/** Stable application pagination result independent of Spring Data JSON. */
public record PageResult<T>(
		List<T> content,
		int page,
		int size,
		long totalElements,
		int totalPages) {

	public PageResult {
		content = List.copyOf(Objects.requireNonNull(content, "page content is required"));
		if (page < 0 || size < 1 || totalElements < 0 || totalPages < 0) {
			throw new IllegalArgumentException("invalid page metadata");
		}
	}

	public boolean first() {
		return page == 0;
	}

	public boolean last() {
		return totalPages == 0 || page >= totalPages - 1;
	}
}
