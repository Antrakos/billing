package com.antrakos.billing.repository.impl

import com.antrakos.billing.models.Role
import com.antrakos.billing.models.User
import com.antrakos.billing.repository.AbstractRepository
import com.antrakos.billing.repository.UserRepository
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Service
import java.sql.ResultSet

/**
 * @author Taras Zubrei
 */
@Service
open class UserRepositoryImpl(jdbcTemplate: JdbcTemplate) : AbstractRepository<User>(jdbcTemplate, "users"), UserRepository {
    override fun findByUsername(username: String) = try {
        jdbcTemplate.queryForObject("SELECT * FROM $tableName WHERE username=?;", username) { rs, _ -> fromResultSet(rs) }
    } catch (ex: EmptyResultDataAccessException) {
        null
    }

    override fun fromResultSet(resultSet: ResultSet) = User(
            id = resultSet.getInt("id"),
            username = resultSet.getString("username"),
            password = resultSet.getString("password"),
            role = Role.valueOf(resultSet.getString("access_role")),
            enabled = resultSet.getBoolean("enabled"),
            customerId = resultSet.getObject("customer_id", Integer::class.java)?.toInt()
    )

    override fun toFields(entity: User) = mutableMapOf(
            "username" to entity.username,
            "password" to entity.password,
            "access_role" to entity.role.name,
            "enabled" to entity.enabled
    ).apply { if (entity.customerId != null) put("customer_id", entity.customerId) }
}