package com.antrakos.billing.web

import java.time.LocalDate

/**
 * @author Taras Zubrei
 */
data class UsageRequest(
        val date: LocalDate = LocalDate.now(),
        val value: Double,
        val customerId: Int,
        val serviceId: Int
)