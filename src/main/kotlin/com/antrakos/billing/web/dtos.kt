package com.antrakos.billing.web

import com.antrakos.billing.models.Service
import io.swagger.annotations.ApiModel
import java.time.LocalDate

/**
 * @author Taras Zubrei
 */
@ApiModel("customer", description = "Represents details about existing customer", subTypes = [Service::class, BillDTO::class])
data class CustomerDTO(
        val id: Int,
        val name: String,
        val balance: Double = 0.0,
        val address: String,
        val services: List<Service>,
        val unpaidBills: List<BillDTO>
)

@ApiModel("new customer", description = "Describes new customer that will be added to system")
data class CustomerRequest(
        val username: String,
        val password: String,
        val name: String,
        val address: String
)

@ApiModel("usage", description = "Contains details about customer service usage")
data class UsageDTO(
        val id: Int,
        val date: LocalDate = LocalDate.now(),
        val value: Double
)

@ApiModel("bill", description = "Describes bill details", subTypes = [Service::class])
data class BillDTO(
        val id: Int,
        val date: LocalDate,
        val amount: Double,
        val service: Service? = null,
        val paid: Boolean = false
)
