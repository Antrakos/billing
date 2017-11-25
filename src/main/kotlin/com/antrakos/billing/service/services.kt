package com.antrakos.billing.service

import com.antrakos.billing.models.Bill
import com.antrakos.billing.models.Customer
import com.antrakos.billing.models.Service
import com.antrakos.billing.models.UsageReport
import java.time.LocalDate

/**
 * @author Taras Zubrei
 */
interface BillService {
    fun lastBillDate(customer: Customer): LocalDate
    fun createBill(customer: Customer, service: Service): List<Bill>
    //group by month
    //filter first and last
    //map: calculate value difference
    //map: multiply by service price
    //map: create bill
    //peek: save id db
    //return
}
interface UsageService {
    fun getUsageReport(customer: Customer, service: Service, lastPaid: LocalDate): UsageReport
}
