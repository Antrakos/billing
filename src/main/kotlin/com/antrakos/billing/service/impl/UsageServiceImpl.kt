package com.antrakos.billing.service.impl

import com.antrakos.billing.models.*
import com.antrakos.billing.repository.CustomerToServiceMappingRepository
import com.antrakos.billing.repository.UsageRepository
import com.antrakos.billing.service.UsageService
import java.time.LocalDate

/**
 * @author Taras Zubrei
 */
@org.springframework.stereotype.Service
open class UsageServiceImpl(private val repository: UsageRepository, private val customerToServiceMappingRepository: CustomerToServiceMappingRepository) : UsageService {
    override fun find(customer: Customer, service: Service) = repository.find(service.id!!, customer.id!!)

    override fun getUsageReport(customer: Customer, service: Service, lastPaid: LocalDate?) = if (lastPaid != null) UsageReport(
            lastPaid = repository.findLastPaid(service.id!!, customer.id!!, lastPaid),
            indexes = repository.find(service.id, customer.id, lastPaid)
    ) else UsageReport(indexes = repository.find(service.id!!, customer.id!!))

    override fun create(usage: Usage): Usage {
        val mapping = customerToServiceMappingRepository.find(usage.service.id!!, usage.customer.id!!)
        if (!mapping.active)
            throw BusinessLogicException("Can't add usage for stopped service")
        return repository.save(usage)
    }
}