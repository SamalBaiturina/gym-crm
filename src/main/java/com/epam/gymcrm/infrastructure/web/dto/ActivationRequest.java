package com.epam.gymcrm.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/** Body of PATCH .../active: the value the profile should have afterwards. */
public record ActivationRequest(@NotNull @JsonProperty("isActive") Boolean isActive) {
}
