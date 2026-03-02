package pl.jobscraper.core.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FiltersResponseDto(
        @JsonProperty("seniorities")
        List<String> seniorities,
        @JsonProperty("employmentTypes")
        List<String> employmentTypes,
        @JsonProperty("locations")
        List<String> locations,
        @JsonProperty("sources")
        List<String> sources
) {
}
