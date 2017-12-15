package com.antrakos.billing.repository.impl

import com.antrakos.billing.models.Bill
import com.antrakos.billing.models.CustomerToServiceMapping
import com.antrakos.billing.models.ResourceNotFoundException
import com.antrakos.billing.repository.*
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.query
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Repository
import java.sql.Date
import java.sql.ResultSet

/**
 * @author Taras Zubrei
 */
@Repository
open class BillRepositoryImpl(jdbcTemplate: JdbcTemplate, private val customerRepository: CustomerRepository, private val serviceRepository: ServiceRepository, private val customerToServiceMappingRepository: CustomerToServiceMappingRepository) :
        AbstractRepository<Bill>(jdbcTemplate, "bills"), BillRepository {
    override fun find(serviceId: Int, customerId: Int): List<Bill> {
        val id = customerToServiceMappingRepository.exists(serviceId, customerId) ?: throw ResourceNotFoundException("No recorded usages for customer[id=$customerId] and service[id=$serviceId]")
        return jdbcTemplate.query("SELECT * FROM $tableName WHERE customer_service_id=?;", id) { rs, _ -> fromResultSet(rs, serviceId, customerId) }
    }

    override fun findLast(serviceId: Int, customerId: Int): Bill? {
        val id = customerToServiceMappingRepository.exists(serviceId, customerId) ?: throw ResourceNotFoundException("No recorded usages for customer[id=$customerId] and service[id=$serviceId]")
        return try {
            jdbcTemplate.queryForObject("SELECT * FROM $tableName WHERE customer_service_id=? ORDER BY id DESC LIMIT 1;", id) { rs, _ -> fromResultSet(rs, serviceId, customerId) }
        } catch (ex: EmptyResultDataAccessException) {
            null
        }
    }

    override fun fromResultSet(resultSet: ResultSet): Bill {
        val (_, customerId, serviceId) = customerToServiceMappingRepository.findById(resultSet.getInt("customer_service_id"))!!
        return fromResultSet(resultSet, serviceId, customerId)
    }

    private fun fromResultSet(resultSet: ResultSet, serviceId: Int, customerId: Int): Bill {
        return Bill(
                id = resultSet.getInt("id"),
                date = resultSet.getDate("billing_date").toLocalDate(),
                amount = resultSet.getDouble("amount"),
                paid = resultSet.getBoolean("paid"),
                customer = customerRepository.findById(customerId)!!,
                service = serviceRepository.findById(serviceId)!!
        )
    }

    override fun toFields(entity: Bill): Map<String, Any> {
        val id = customerToServiceMappingRepository.exists(entity.service.id!!, entity.customer.id!!) ?: customerToServiceMappingRepository.save(CustomerToServiceMapping(customerId = entity.customer.id, serviceId = entity.service.id)).id!!
        return mapOf(
                "billing_date" to Date.valueOf(entity.date),
                "amount" to entity.amount,
                "paid" to entity.paid,
                "customer_service_id" to id
        )
    }
}