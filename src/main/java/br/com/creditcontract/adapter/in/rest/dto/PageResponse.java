package br.com.creditcontract.adapter.in.rest.dto;

import br.com.creditcontract.application.query.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.function.Function;

/** Stable pagination envelope owned by the API instead of Spring Data. */
@Schema(description = "Stable zero-based pagination envelope owned by the API.")
public record PageResponse<T>(
		@Schema(description = "Items in the current page.")
		List<T> content,
		@Schema(description = "Zero-based page number.", example = "0", minimum = "0")
		int page,
		@Schema(description = "Requested page size, from 1 through 100.", example = "20", minimum = "1", maximum = "100")
		int size,
		@Schema(example = "1")
		long totalElements,
		@Schema(example = "1")
		int totalPages,
		@Schema(example = "true")
		boolean first,
		@Schema(example = "true")
		boolean last) {

	public static <S, T> PageResponse<T> from(PageResult<S> result, Function<S, T> mapper) {
		return new PageResponse<>(
				result.content().stream().map(mapper).toList(),
				result.page(),
				result.size(),
				result.totalElements(),
				result.totalPages(),
				result.first(),
				result.last());
	}
}
