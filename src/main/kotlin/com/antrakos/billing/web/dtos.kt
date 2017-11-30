package com.antrakos.billing.web

import com.antrakos.billing.models.Service
import java.time.LocalDate

/**
 * @author Taras Zubrei
 */
data class CustomerDTO(
        val id: Int,
        val balance: Double = 0.0,
        val services: List<Service>,
        val unpaidBills: List<BillDTO>
)

data class CustomerRequest(
        val username: String,
        val password: String
)


data class UsageDTO(
        val id: Int,
        val date: LocalDate = LocalDate.now(),
        val value: Double
)


data class BillDTO(
        val id: Int,
        val date: LocalDate,
        val amount: Double,
        val service: Service,
        val paid: Boolean = false
)
