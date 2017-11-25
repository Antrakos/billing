package com.antrakos.billing.service

import com.antrakos.billing.models.BaseEntity
import com.antrakos.billing.repository.CrudRepository
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import java.sql.ResultSet

/**
 * @author Taras Zubrei
 */
abstract class AbstractRepository<T : BaseEntity>(
        protected val jdbcTemplate: JdbcTemplate,
        private val tableName: String,
        private val insert: SimpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate).withTableName(tableName).usingGeneratedKeyColumns("id")) : CrudRepository<T, Int> {

    abstract fun fromResultSet(resultSet: ResultSet): T

    abstract fun toFields(entity: T): Map<String, Any>

    override fun save(entity: T): T {
        val fields = toFields(entity)
        val id = if (entity.id != null && findById(entity.id!!) != null) {
            val fieldsToUpdate = fields.map { it.key to it.value } // to keep sorted
            val sql = "UPDATE $tableName SET ${fieldsToUpdate.joinToString(separator = ",") { "${it.first} = ?" }} WHERE id=${entity.id}"
            jdbcTemplate.update(sql) { ps -> fieldsToUpdate.forEachIndexed { index, pair -> ps.setObject(index + 1, pair.second) } }
            entity.id!!
        } else {
            insert.executeAndReturnKey(fields)
        }
        return findById(id.toInt())!!
    }

    override fun findById(id: Int): T? = try {
        jdbcTemplate.queryForObject("SELECT * FROM $tableName WHERE id=?", id) { rs, _ -> fromResultSet(rs) }
    } catch (ex: EmptyResultDataAccessException) {
        null
    }

    override fun findAll(): List<T> {
        return jdbcTemplate.query("SELECT * FROM $tableName;") { rs, _ -> fromResultSet(rs) }
    }

    override fun delete(entity: T) {
        deleteById(entity.id!!)
    }

    override fun deleteById(id: Int) {
        jdbcTemplate.update("DELETE FROM $tableName WHERE id=?", id)
    }


}