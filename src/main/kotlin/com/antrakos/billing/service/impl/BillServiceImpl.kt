package com.antrakos.billing.service.impl

import com.antrakos.billing.models.Bill
import com.antrakos.billing.models.Customer
import com.antrakos.billing.models.Service
import com.antrakos.billing.repository.BillRepository
import com.antrakos.billing.service.BillService
import com.antrakos.billing.service.UsageService

/**
 * @author Taras Zubrei
 */
@org.springframework.stereotype.Service
open class BillServiceImpl(private val repository: BillRepository, private val usageService: UsageService) : BillService {
    override fun lastBillDate(customer: Customer, service: Service) =
            repository.findLast(service.id!!, customer.id!!)?.date

    override fun createBill(customer: Customer, service: Service): List<Bill> {
        val (lastPaid, usages) = usageService.getUsageReport(customer, service, lastBillDate(customer, service))
        var monthlyUsages = usages.groupBy { it.date.withDayOfMonth(1) }.toMutableMap()
        val keys = monthlyUsages.keys.toList()

        keys.forEachIndexed { index, localDate ->
            if (index + 1 >= keys.size) return@forEachIndexed
            monthlyUsages.put(localDate, monthlyUsages[localDate]!!.plus(monthlyUsages[keys[index+1]]!!.first()))
        }
        if (lastPaid != null && monthlyUsages.isNotEmpty()) {
            val firstEntry = monthlyUsages.entries.first()
            monthlyUsages = monthlyUsages.toMutableMap().apply { put(firstEntry.key, mutableListOf(lastPaid).plus(firstEntry.value)) }
        }
        return monthlyUsages
                .asSequence()
                .map { it.value.last().date to (it.value.last().value - it.value.first().value) }
                .filterNot { it.second == 0.0 }
                .map {
                    Bill(
                            date = it.first,
                            amount = it.second * service.price,
                            customer = customer,
                            service = service,
                            paid = false
                    )
                }
                .onEach { repository.save(it) }
                .toList()
    }
}