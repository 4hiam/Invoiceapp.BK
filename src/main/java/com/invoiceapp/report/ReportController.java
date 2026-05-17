package com.invoiceapp.report;

import com.invoiceapp.auth.User;
import com.invoiceapp.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<ReportService.DashboardReport>> dashboard(
            @PathVariable UUID companyId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getDashboard(companyId, user.getId())));
    }

    @GetMapping("/period")
    public ResponseEntity<ApiResponse<ReportService.PeriodReport>> period(
            @PathVariable UUID companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getPeriodReport(companyId, user.getId(), from, to)));
    }
}
