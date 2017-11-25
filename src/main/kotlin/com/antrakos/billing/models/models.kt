package com.antrakos.billing.models

import java.time.LocalDate

/**
 * @author Taras Zubrei
 */
interface BaseEntity {
    val id: Int?
}

data class Customer(
        override val id: Int? = null,
        val balance: Double = 0.0
) : BaseEntity

data class Service(
        override val id: Int? = null,
        val price: Double
) : BaseEntity


data class Usage(
        override val id: Int? = null,
        val date: LocalDate = LocalDate.now(),
        val value: Double,
        val customer: Customer,
        val service: Service
) : BaseEntity


data class Bill(
        override val id: Int? = null,
        val date: LocalDate,
        val customer: Customer,
        val service: Service
) : BaseEntity


data class UsageReport(val lastPaid: Usage, val indexes: List<Usage>)