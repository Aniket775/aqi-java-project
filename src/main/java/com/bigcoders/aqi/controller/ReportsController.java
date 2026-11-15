package com.bigcoders.aqi.controller;

import com.bigcoders.aqi.dto.CityReportRow;
import com.bigcoders.aqi.model.AqiHistory;
import com.bigcoders.aqi.model.City;
import com.bigcoders.aqi.model.HealthAdvisory;
import com.bigcoders.aqi.model.ReportLog;
import com.bigcoders.aqi.repository.CityRepository;
import com.bigcoders.aqi.repository.UserRepository;
import com.bigcoders.aqi.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/reports")
public class ReportsController {

    private final ReportService reportService;
    private final CityRepository cityRepository;
    private final UserRepository userRepository;

    public ReportsController(ReportService reportService,
                             CityRepository cityRepository,
                             UserRepository userRepository) {
        this.reportService = reportService;
        this.cityRepository = cityRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String reportsPage(@RequestParam(value = "cityId", required = false) Integer cityId,
                              @RequestParam(value = "stationId", required = false) Integer stationId,
                              @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                              @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                              @RequestParam(value = "tab", defaultValue = "history") String activeTab,
                              Model model) {

        List<City> cities = cityRepository.findAll();
        List<AqiHistory> history = reportService.getAqiHistory(cityId, stationId, fromDate, toDate);
        List<HealthAdvisory> advisories = reportService.getAllAdvisories();
        List<ReportLog> logs = reportService.getRecentReportLogs(20);

        model.addAttribute("cities", cities);
        model.addAttribute("history", history);
        model.addAttribute("advisories", advisories);
        model.addAttribute("logs", logs);
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("selectedCityId", cityId);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);

        return "reports";
    }

    @PostMapping("/generate")
    public String generateCityReport(@RequestParam("cityId") int cityId,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        int userId = 1; // Default Admin user
        if (authentication != null) {
            userRepository.findByUsername(authentication.getName())
                    .ifPresent(u -> { /* resolve userId */ });
        }

        try {
            List<CityReportRow> reportRows = reportService.generateCityReport(cityId, userId);
            redirectAttributes.addFlashAttribute("generatedReport", reportRows);
            redirectAttributes.addFlashAttribute("generatedCityId", cityId);
            redirectAttributes.addFlashAttribute("successMessage", "City report generated successfully using MySQL Stored Procedure Cursor!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Report generation failed: " + ex.getMessage());
        }

        return "redirect:/reports?tab=generate&cityId=" + cityId;
    }

    @GetMapping({"/export-csv", "/export"})
    public ResponseEntity<byte[]> exportCsv(@RequestParam(value = "cityId", required = false) Integer cityId,
                                            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<AqiHistory> list = reportService.getAqiHistory(cityId, null, fromDate, toDate);
        byte[] csvData = reportService.exportAqiHistoryCsv(list);

        String filename = "aqi_report_" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }
}

