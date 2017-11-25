package com.antrakos.billing.repository.impl

import com.antrakos.billing.models.CustomerToServiceMapping
import com.antrakos.billing.models.Usage
import com.antrakos.billing.repository.*
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.Date
import java.sql.ResultSet

/**
 * @author Taras Zubrei
 */
@Repository
open class UsageRepositoryImpl(jdbcTemplate: JdbcTemplate, private val customerRepository: CustomerRepository, private val serviceRepository: ServiceRepository, private val customerToServiceMappingRepository: CustomerToServiceMappingRepository):
        AbstractRepository<Usage>(jdbcTemplate, "service_usages"), UsageRepository {
    override fun fromResultSet(resultSet: ResultSet): Usage {
        val (_, customerId, serviceId) = customerToServiceMappingRepository.findById(resultSet.getInt("customer_service_id"))!!
        return Usage(
                id = resultSet.getInt("id"),
                date = resultSet.getDate("date").toLocalDate(),
                value = resultSet.getDouble("value"),
                customer = customerRepository.findById(customerId)!!,
                service = serviceRepository.findById(serviceId)!!
        )
    }

    override fun toFields(entity: Usage): Map<String, Any> {
        val id = customerToServiceMappingRepository.exists(entity.service.id!!, entity.customer.id!!) ?: customerToServiceMappingRepository.save(CustomerToServiceMapping(customerId = entity.customer.id, serviceId = entity.service.id)).id!!
        return mapOf(
                "date" to Date.valueOf(entity.date),
                "value" to entity.value,
                "customer_service_id" to id
        )
    }
}