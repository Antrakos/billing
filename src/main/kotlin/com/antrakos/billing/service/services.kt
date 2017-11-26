package com.antrakos.billing.service

import com.antrakos.billing.models.*
import java.time.LocalDate

/**
 * @author Taras Zubrei
 */
interface BillService {
    fun lastBillDate(customer: Customer, service: Service): LocalDate?
    fun createBill(customer: Customer, service: Service): List<Bill>
}

interface UsageService {
    fun getUsageReport(customer: Customer, service: Service, lastPaid: LocalDate?): UsageReport
    fun create(usage: Usage): Usage
}

interface ServiceService {
    fun create(service: Service): Service
    fun find(id: Int): Service
    fun findAllEnabled(): List<Service>
    fun disable(service: Service)
}

interface CustomerService {
    fun create(customer: Customer): Customer
    fun find(id: Int): Customer
    fun addService(customer: Customer, service: Service)
    fun stopService(customer: Customer, service: Service, lastUsage: Usage)
}
