package com.dyna.studenthub

import com.dyna.studenthub.domain.ProfileValidation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileValidationTest {
    @Test fun validAmountIsConvertedToCents() {
        assertEquals(1250L, ProfileValidation.parseCents("12,50"))
    }

    @Test fun emptyAmountIsRejected() {
        assertNull(ProfileValidation.parseCents(""))
    }

    @Test fun negativeAmountIsRejected() {
        assertNull(ProfileValidation.parseCents("-1"))
    }
}
