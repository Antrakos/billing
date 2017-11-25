package com.antrakos.billing.repository.impl

import com.antrakos.billing.models.Bill
import com.antrakos.billing.models.CustomerToServiceMapping
import com.antrakos.billing.repository.*
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.Date
import java.sql.ResultSet

/**
 * @author Taras Zubrei
 */
@Repository
open class BillRepositoryImpl(jdbcTemplate: JdbcTemplate, private val customerRepository: CustomerRepository, private val serviceRepository: ServiceRepository, private val customerToServiceMappingRepository: CustomerToServiceMappingRepository):
        AbstractRepository<Bill>(jdbcTemplate, "bills"), BillRepository {
    override fun fromResultSet(resultSet: ResultSet): Bill {
        val (_, customerId, serviceId) = customerToServiceMappingRepository.findById(resultSet.getInt("customer_service_id"))!!
        return Bill(
                id = resultSet.getInt("id"),
                date = resultSet.getDate("date").toLocalDate(),
                amount = resultSet.getDouble("amount"),
                customer = customerRepository.findById(customerId)!!,
                service = serviceRepository.findById(serviceId)!!
        )
    }

    override fun toFields(entity: Bill): Map<String, Any> {
        val id = customerToServiceMappingRepository.exists(entity.service.id!!, entity.customer.id!!) ?: customerToServiceMappingRepository.save(CustomerToServiceMapping(customerId = entity.customer.id, serviceId = entity.service.id)).id!!
        return mapOf(
                "date" to Date.valueOf(entity.date),
                "amount" to entity.amount,
                "customer_service_id" to id
        )
    }
}