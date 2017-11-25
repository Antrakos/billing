package com.antrakos.billing.service.impl

import com.antrakos.billing.models.Customer
import com.antrakos.billing.repository.CustomerRepository
import com.antrakos.billing.service.AbstractRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet

/**
 * @author Taras Zubrei
 */
@Repository
open class CustomerRepositoryImpl(jdbcTemplate: JdbcTemplate) : AbstractRepository<Customer>(jdbcTemplate, "customers"), CustomerRepository {
    override fun fromResultSet(resultSet: ResultSet): Customer {
        return Customer(
                id = resultSet.getInt("id"),
                balance = resultSet.getDouble("balance")
        )
    }

    override fun toFields(entity: Customer) = mapOf<String, Any>(
            "balance" to entity.balance
    )
}