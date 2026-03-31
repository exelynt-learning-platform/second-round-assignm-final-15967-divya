package com.example.demo.common;

import java.util.function.Function;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtil {

	public static Pageable createPageable(int page, int size, String sortBy, String sortDir, int defaultPage,
			int defaultSize, Function<String, String> sortValidator) {

		int finalPage = Math.max(page, defaultPage);
		int finalSize = Math.max(size, defaultSize);

		String validSortBy = sortValidator.apply(sortBy);

		Sort.Direction direction;
		try {
			direction = Sort.Direction.fromString(sortDir);
		} catch (Exception e) {
			direction = Sort.Direction.DESC;
		}

		return PageRequest.of(finalPage, finalSize, Sort.by(direction, validSortBy));
	}
}