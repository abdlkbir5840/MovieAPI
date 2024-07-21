package com.movieflex.dto;

import java.util.List;

public record MoviePageResponse(List<MovieDto> movieDtos,
                                Integer pageNumber,
                                Integer pageSize,
                                Long totalElements,
                                int totalPages,
                                boolean isLast) {
}
