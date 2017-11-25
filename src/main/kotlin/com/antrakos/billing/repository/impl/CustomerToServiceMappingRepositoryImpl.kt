package com.antrakos.billing.repository.impl

import com.antrakos.billing.models.CustomerToServiceMapping
import com.antrakos.billing.repository.AbstractRepository
import com.antrakos.billing.repository.CustomerToServiceMappingRepository
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet

/**
 * @author Taras Zubrei
 */
@Repository
open class CustomerToServiceMappingRepositoryImpl(jdbcTemplate: JdbcTemplate) : AbstractRepository<CustomerToServiceMapping>(jdbcTemplate, "customers_services_mapping"), CustomerToServiceMappingRepository {
    override fun exists(serviceId: Int, customerId: Int) = try {
        jdbcTemplate.queryForObject("SELECT id FROM $tableName WHERE service_id=? AND customer_id=?", Int::class.java, serviceId, customerId)
    } catch (ex: EmptyResultDataAccessException) {
        null
    }

    override fun fromResultSet(resultSet: ResultSet) = CustomerToServiceMapping(
            id = resultSet.getInt("id"),
            serviceId = resultSet.getInt("service_id"),
            customerId = resultSet.getInt("customer_id")
    )

    override fun toFields(entity: CustomerToServiceMapping) = mapOf<String, Any>(
            "customer_id" to entity.customerId,
            "service_id" to entity.serviceId
    )
}