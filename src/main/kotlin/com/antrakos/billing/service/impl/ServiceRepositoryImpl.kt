package com.antrakos.billing.service.impl

import com.antrakos.billing.models.Service
import com.antrakos.billing.repository.ServiceRepository
import com.antrakos.billing.service.AbstractRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet

/**
 * @author Taras Zubrei
 */
@Repository
open class ServiceRepositoryImpl(jdbcTemplate: JdbcTemplate) : AbstractRepository<Service>(jdbcTemplate, "services"), ServiceRepository {
    override fun fromResultSet(resultSet: ResultSet): Service {
        return Service(
                id = resultSet.getInt("id"),
                price = resultSet.getDouble("price")
        )
    }

    override fun toFields(entity: Service) = mapOf<String, Any>(
            "price" to entity.price
    )
}