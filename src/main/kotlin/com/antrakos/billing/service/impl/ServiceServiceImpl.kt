package com.antrakos.billing.service.impl

import com.antrakos.billing.models.Service
import com.antrakos.billing.repository.ServiceRepository
import com.antrakos.billing.service.ServiceService

/**
 * @author Taras Zubrei
 */
@org.springframework.stereotype.Service
open class ServiceServiceImpl(private val repository: ServiceRepository) : ServiceService {
    override fun find(id: Int) = repository.findById(id) ?: throw IllegalStateException("No service found for id=$id")

    override fun create(service: Service) = repository.save(service)

    override fun findAllEnabled() = repository.findAllEnabled()

    override fun disable(service: Service) {
        repository.save(service.copy(enabled = false))
    }
}