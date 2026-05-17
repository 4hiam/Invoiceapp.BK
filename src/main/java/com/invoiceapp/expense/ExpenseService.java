package com.invoiceapp.expense;

import com.invoiceapp.company.Company;
import com.invoiceapp.company.CompanyService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CompanyService companyService;

    public Page<ExpenseDTO.Response> findAll(UUID companyId, UUID userId, String category, Pageable pageable) {
        companyService.getCompanyOrThrow(companyId, userId);
        if (category != null && !category.isBlank()) {
            return expenseRepository.findByCompanyIdAndCategory(companyId, category, pageable)
                    .map(ExpenseDTO::toResponse);
        }
        return expenseRepository.findByCompanyId(companyId, pageable).map(ExpenseDTO::toResponse);
    }

    public ExpenseDTO.Response findById(UUID companyId, UUID expenseId, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        return ExpenseDTO.toResponse(getExpenseOrThrow(expenseId, companyId));
    }

    @Transactional
    public ExpenseDTO.Response create(UUID companyId, ExpenseDTO.CreateRequest req, UUID userId) {
        Company company = companyService.getCompanyOrThrow(companyId, userId);
        Expense expense = Expense.builder()
                .company(company).category(req.getCategory()).description(req.getDescription())
                .amount(req.getAmount())
                .taxAmount(req.getTaxAmount() != null ? req.getTaxAmount() : BigDecimal.ZERO)
                .expenseDate(req.getExpenseDate()).receiptUrl(req.getReceiptUrl())
                .build();
        return ExpenseDTO.toResponse(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseDTO.Response update(UUID companyId, UUID expenseId, ExpenseDTO.UpdateRequest req, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        Expense expense = getExpenseOrThrow(expenseId, companyId);
        expense.setCategory(req.getCategory());
        expense.setDescription(req.getDescription());
        expense.setAmount(req.getAmount());
        expense.setTaxAmount(req.getTaxAmount() != null ? req.getTaxAmount() : BigDecimal.ZERO);
        expense.setExpenseDate(req.getExpenseDate());
        expense.setReceiptUrl(req.getReceiptUrl());
        return ExpenseDTO.toResponse(expenseRepository.save(expense));
    }

    @Transactional
    public void delete(UUID companyId, UUID expenseId, UUID userId) {
        companyService.getCompanyOrThrow(companyId, userId);
        expenseRepository.delete(getExpenseOrThrow(expenseId, companyId));
    }

    private Expense getExpenseOrThrow(UUID expenseId, UUID companyId) {
        return expenseRepository.findByIdAndCompanyId(expenseId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Gasto no encontrado"));
    }
}
