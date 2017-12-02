package com.antrakos.billing.repository.impl

import com.antrakos.billing.models.Customer
import com.antrakos.billing.repository.AbstractRepository
import com.antrakos.billing.repository.CustomerRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet

/**
 * @author Taras Zubrei
 */
@Repository
open class CustomerRepositoryImpl(jdbcTemplate: JdbcTemplate) : AbstractRepository<Customer>(jdbcTemplate, "customers"), CustomerRepository {
    override fun fromResultSet(resultSet: ResultSet) = Customer(
            id = resultSet.getInt("id"),
            name = resultSet.getString("name"),
            address = resultSet.getString("address"),
            balance = resultSet.getDouble("balance")
    )

    override fun toFields(entity: Customer) = mapOf(
            "balance" to entity.balance,
            "name" to entity.name,
            "address" to entity.address
    )
}