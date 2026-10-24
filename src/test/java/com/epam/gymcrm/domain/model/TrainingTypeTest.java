package com.epam.gymcrm.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TrainingTypeTest {

    @Test
    void typeCreatedFromNameHasNoId() {
        assertNull(new TrainingType("YOGA").id());
    }

    @Test
    void typesAreEqualByNameIgnoringId() {
        assertEquals(new TrainingType("YOGA"), new TrainingType(2L, "YOGA"));
        assertEquals(new TrainingType("YOGA").hashCode(), new TrainingType(2L, "YOGA").hashCode());
    }

    @Test
    void typesWithDifferentNamesAreNotEqual() {
        assertNotEquals(new TrainingType(2L, "YOGA"), new TrainingType(2L, "ZUMBA"));
    }
}
