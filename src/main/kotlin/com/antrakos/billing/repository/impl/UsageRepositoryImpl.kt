package com.antrakos.billing.repository.impl

import com.antrakos.billing.models.CustomerToServiceMapping
import com.antrakos.billing.models.ResourceNotFoundException
import com.antrakos.billing.models.Usage
import com.antrakos.billing.repository.*
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.query
import org.springframework.stereotype.Repository
import java.sql.Date
import java.sql.ResultSet
import java.time.LocalDate

/**
 * @author Taras Zubrei
 */
@Repository
open class UsageRepositoryImpl(jdbcTemplate: JdbcTemplate, private val customerRepository: CustomerRepository, private val serviceRepository: ServiceRepository, private val customerToServiceMappingRepository: CustomerToServiceMappingRepository):
        AbstractRepository<Usage>(jdbcTemplate, "service_usages"), UsageRepository {
    override fun findLastPaid(serviceId: Int, customerId: Int, date: LocalDate): Usage? {
        val id = customerToServiceMappingRepository.exists(serviceId, customerId) ?: throw ResourceNotFoundException("No recorded usages for customer[id=$customerId] and service[id=$serviceId]")
        return try {
            jdbcTemplate.queryForObject("SELECT * FROM $tableName WHERE customer_service_id=? AND usage_date <= ? ORDER BY id DESC LIMIT 1;", RowMapper { rs, _ -> fromResultSet(rs, serviceId, customerId) }, id, Date.valueOf(date))
        } catch (ex: EmptyResultDataAccessException) {
            null
        }
    }

    override fun find(serviceId: Int, customerId: Int, after: LocalDate): List<Usage> {
        val id = customerToServiceMappingRepository.exists(serviceId, customerId) ?: throw ResourceNotFoundException("No recorded usages for customer[id=$customerId] and service[id=$serviceId]")
        return jdbcTemplate.query("SELECT * FROM $tableName WHERE customer_service_id=? AND usage_date > ?;", id, Date.valueOf(after)) { rs, _ -> fromResultSet(rs, serviceId, customerId) }
    }

    override fun find(serviceId: Int, customerId: Int): List<Usage> {
        val id = customerToServiceMappingRepository.exists(serviceId, customerId) ?: throw ResourceNotFoundException("No recorded usages for customer[id=$customerId] and service[id=$serviceId]")
        return jdbcTemplate.query("SELECT * FROM $tableName WHERE customer_service_id=?;", id) { rs, _ -> fromResultSet(rs, serviceId, customerId) }
    }

    override fun fromResultSet(resultSet: ResultSet): Usage {
        val (_, customerId, serviceId) = customerToServiceMappingRepository.findById(resultSet.getInt("customer_service_id"))!!
        return fromResultSet(resultSet, serviceId, customerId)
    }

    private fun fromResultSet(resultSet: ResultSet, serviceId: Int, customerId: Int): Usage {
        return Usage(
                id = resultSet.getInt("id"),
                date = resultSet.getDate("usage_date").toLocalDate(),
                value = resultSet.getDouble("usage_value"),
                customer = customerRepository.findById(customerId)!!,
                service = serviceRepository.findById(serviceId)!!
        )
    }

    override fun toFields(entity: Usage): Map<String, Any> {
        val id = customerToServiceMappingRepository.exists(entity.service.id!!, entity.customer.id!!) ?: customerToServiceMappingRepository.save(CustomerToServiceMapping(customerId = entity.customer.id, serviceId = entity.service.id)).id!!
        return mapOf(
                "usage_date" to Date.valueOf(entity.date),
                "usage_value" to entity.value,
                "customer_service_id" to id
        )
    }
}