package com.epam.gymcrm.infrastructure.web.error;

import java.util.List;

/**
 * Body of every error response: the kind of error and a list of human-readable details.
 * It never contains a stack trace.
 */
public record ApiError(String type, List<String> details) {
}
