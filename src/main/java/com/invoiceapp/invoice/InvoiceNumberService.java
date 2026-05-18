package com.invoiceapp.invoice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceNumberService {

    private final InvoiceRepository invoiceRepository;

    public String next(UUID companyId) {
        String prefix = "INV-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMM"));
        int max = invoiceRepository.findMaxSequenceForPrefix(companyId, prefix);
        return prefix + String.format("%04d", max + 1);
    }
}
