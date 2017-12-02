package com.antrakos.billing.models

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.time.LocalDate

/**
 * @author Taras Zubrei
 */
interface BaseEntity {
    val id: Int?
}

enum class Role {
    WORKER, CUSTOMER
}

data class User(
        override val id: Int? = null,
        val username: String,
        val password: String,
        val role: Role,
        val enabled: Boolean,
        val customerId: Int? = null
) : BaseEntity

data class Customer(
        override val id: Int? = null,
        val balance: Double = 0.0,
        val name: String,
        val address: String
) : BaseEntity

data class Service(
        override val id: Int? = null,
        val enabled: Boolean = true,
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
        val amount: Double,
        val customer: Customer,
        val service: Service,
        val paid: Boolean = false
) : BaseEntity


data class CustomerToServiceMapping(
        override val id: Int? = null,
        val customerId: Int,
        val serviceId: Int,
        val active: Boolean = true
) : BaseEntity


data class UsageReport(val lastPaid: Usage? = null, val indexes: List<Usage>)

@ResponseStatus(HttpStatus.NOT_FOUND)
class ResourceNotFoundException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.BAD_REQUEST)
class BusinessLogicException(message: String) : RuntimeException(message)