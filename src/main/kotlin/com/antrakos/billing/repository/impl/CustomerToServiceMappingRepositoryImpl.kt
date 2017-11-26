package com.antrakos.billing.repository.impl

import com.antrakos.billing.models.CustomerToServiceMapping
import com.antrakos.billing.repository.AbstractRepository
import com.antrakos.billing.repository.CustomerRepository
import com.antrakos.billing.repository.CustomerToServiceMappingRepository
import com.antrakos.billing.repository.ServiceRepository
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Repository
import java.sql.ResultSet

/**
 * @author Taras Zubrei
 */
@Repository
open class CustomerToServiceMappingRepositoryImpl(jdbcTemplate: JdbcTemplate, private val serviceRepository: ServiceRepository, private val customerRepository: CustomerRepository) :
        AbstractRepository<CustomerToServiceMapping>(jdbcTemplate, "customers_services_mapping"), CustomerToServiceMappingRepository {
    override fun find(serviceId: Int, customerId: Int) = try {
        jdbcTemplate.queryForObject("SELECT * FROM $tableName WHERE service_id=? AND customer_id=?", serviceId, customerId) { rs, _ -> fromResultSet(rs) }!!
    } catch (ex: EmptyResultDataAccessException) {
        throw IllegalStateException("Customer[id=$customerId] doesn't have service[id=$serviceId]")
    }

    override fun findServices(customerId: Int) =
            jdbcTemplate.queryForList("SELECT id FROM $tableName WHERE customer_id=?", Int::class.java, customerId)
                    .map { serviceRepository.findById(it)!! }

    override fun findCustomers(serviceId: Int) =
            jdbcTemplate.queryForList("SELECT id FROM $tableName WHERE service_id=?", Int::class.java, serviceId)
                    .map { customerRepository.findById(it)!! }

    override fun exists(serviceId: Int, customerId: Int) = try {
        jdbcTemplate.queryForObject("SELECT id FROM $tableName WHERE service_id=? AND customer_id=?", Int::class.java, serviceId, customerId)
    } catch (ex: EmptyResultDataAccessException) {
        null
    }

    override fun fromResultSet(resultSet: ResultSet) = CustomerToServiceMapping(
            id = resultSet.getInt("id"),
            serviceId = resultSet.getInt("service_id"),
            customerId = resultSet.getInt("customer_id"),
            active = resultSet.getBoolean("active")
    )

    override fun toFields(entity: CustomerToServiceMapping) = mapOf(
            "customer_id" to entity.customerId,
            "service_id" to entity.serviceId,
            "active" to entity.active
    )
}