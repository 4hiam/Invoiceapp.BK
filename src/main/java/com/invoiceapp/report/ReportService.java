package com.invoiceapp.report;

import com.invoiceapp.company.CompanyService;
import com.invoiceapp.expense.ExpenseRepository;
import com.invoiceapp.invoice.InvoiceRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final InvoiceRepository invoiceRepository;
    private final ExpenseRepository expenseRepository;
    private final CompanyService companyService;

    public DashboardReport getDashboard(UUID companyId, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);

        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate startOfYear = now.withDayOfYear(1);

        BigDecimal monthlyRevenue = invoiceRepository.sumPaidByCompanyIdAndDateRange(
                companyId, startOfMonth, now);
        BigDecimal yearlyRevenue = invoiceRepository.sumPaidByCompanyIdAndDateRange(
                companyId, startOfYear, now);
        BigDecimal outstanding = invoiceRepository.sumOutstandingByCompanyId(companyId);
        BigDecimal monthlyExpenses = expenseRepository.sumByCompanyIdAndDateRange(
                companyId, startOfMonth, now);

        long draftCount = invoiceRepository.countByCompanyIdAndStatus(companyId, "draft");
        long sentCount = invoiceRepository.countByCompanyIdAndStatus(companyId, "sent");
        long overdueCount = invoiceRepository.countByCompanyIdAndStatus(companyId, "overdue");
        long paidCount = invoiceRepository.countByCompanyIdAndStatus(companyId, "paid");

        return DashboardReport.builder()
                .monthlyRevenue(monthlyRevenue)
                .yearlyRevenue(yearlyRevenue)
                .outstanding(outstanding)
                .monthlyExpenses(monthlyExpenses)
                .netMonthly(monthlyRevenue.subtract(monthlyExpenses))
                .invoiceCountDraft(draftCount)
                .invoiceCountSent(sentCount)
                .invoiceCountOverdue(overdueCount)
                .invoiceCountPaid(paidCount)
                .build();
    }

    public PeriodReport getPeriodReport(UUID companyId, UUID userId, LocalDate from, LocalDate to) {
        companyService.getCompanyOrThrow(companyId, userId);

        BigDecimal revenue = invoiceRepository.sumPaidByCompanyIdAndDateRange(companyId, from, to);
        BigDecimal expenses = expenseRepository.sumByCompanyIdAndDateRange(companyId, from, to);

        return PeriodReport.builder()
                .from(from).to(to)
                .totalRevenue(revenue)
                .totalExpenses(expenses)
                .netProfit(revenue.subtract(expenses))
                .build();
    }

    @Data @Builder @AllArgsConstructor
    public static class DashboardReport {
        private BigDecimal monthlyRevenue;
        private BigDecimal yearlyRevenue;
        private BigDecimal outstanding;
        private BigDecimal monthlyExpenses;
        private BigDecimal netMonthly;
        private long invoiceCountDraft;
        private long invoiceCountSent;
        private long invoiceCountOverdue;
        private long invoiceCountPaid;
    }

    @Data @Builder @AllArgsConstructor
    public static class PeriodReport {
        private LocalDate from;
        private LocalDate to;
        private BigDecimal totalRevenue;
        private BigDecimal totalExpenses;
        private BigDecimal netProfit;
    }
}
