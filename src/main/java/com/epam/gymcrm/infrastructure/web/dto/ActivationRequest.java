package com.epam.gymcrm.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/** Body of PATCH .../active: required by the contract; the endpoint switches the flag to the opposite value. */
public record ActivationRequest(@NotNull @JsonProperty("isActive") Boolean isActive) {
}
