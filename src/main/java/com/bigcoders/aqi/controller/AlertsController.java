package com.bigcoders.aqi.controller;

import com.bigcoders.aqi.model.AlertLog;
import com.bigcoders.aqi.service.AlertService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/alerts")
public class AlertsController {

    private final AlertService alertService;

    public AlertsController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public String alertsPage(@RequestParam(value = "severity", required = false) String severity,
                             @RequestParam(value = "resolved", required = false) Boolean resolved,
                             Model model) {
        List<AlertLog> alerts = alertService.getAllAlerts(severity, resolved, 50);
        model.addAttribute("alerts", alerts);
        model.addAttribute("severity", severity);
        model.addAttribute("resolved", resolved);
        model.addAttribute("unresolvedCount", alertService.getUnresolvedCount());
        return "alerts";
    }

    @PostMapping("/{id}/resolve")
    public String resolveAlert(@PathVariable("id") int alertId,
                               RedirectAttributes redirectAttributes) {
        try {
            alertService.resolveAlert(alertId);
            redirectAttributes.addFlashAttribute("successMessage", "Alert #" + alertId + " marked as resolved.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to resolve alert: " + ex.getMessage());
        }
        return "redirect:/alerts";
    }
}

