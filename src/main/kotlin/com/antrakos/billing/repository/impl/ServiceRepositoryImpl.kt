package com.antrakos.billing.repository.impl

import com.antrakos.billing.models.Service
import com.antrakos.billing.repository.AbstractRepository
import com.antrakos.billing.repository.ServiceRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.query
import org.springframework.stereotype.Repository
import java.sql.ResultSet

/**
 * @author Taras Zubrei
 */
@Repository
open class ServiceRepositoryImpl(jdbcTemplate: JdbcTemplate) : AbstractRepository<Service>(jdbcTemplate, "services"), ServiceRepository {
    override fun findAllEnabled() =
            jdbcTemplate.query("SELECT * FROM $tableName WHERE enabled=?;", true) { rs, _ -> fromResultSet(rs) }

    override fun fromResultSet(resultSet: ResultSet) = Service(
            id = resultSet.getInt("id"),
            enabled = resultSet.getBoolean("enabled"),
            price = resultSet.getDouble("price")
    )

    override fun toFields(entity: Service) = mapOf(
            "price" to entity.price,
            "enabled" to entity.enabled
    )
}