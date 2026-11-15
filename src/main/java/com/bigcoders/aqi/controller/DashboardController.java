package com.bigcoders.aqi.controller;

import com.bigcoders.aqi.dto.DashboardData;
import com.bigcoders.aqi.model.City;
import com.bigcoders.aqi.repository.CityRepository;
import com.bigcoders.aqi.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final CityRepository cityRepository;

    public DashboardController(DashboardService dashboardService, CityRepository cityRepository) {
        this.dashboardService = dashboardService;
        this.cityRepository = cityRepository;
    }

    @GetMapping("/")
    public String dashboard(@RequestParam(value = "cityId", required = false) Integer cityId,
                            Model model,
                            Authentication authentication) {
        DashboardData data = dashboardService.getDashboardData();
        List<City> cities = cityRepository.findAll();

        model.addAttribute("data", data);
        model.addAttribute("cities", cities);
        model.addAttribute("selectedCityId", cityId);

        if (authentication != null) {
            model.addAttribute("currentUsername", authentication.getName());
            model.addAttribute("userRole", authentication.getAuthorities().toString());
        }

        return "dashboard";
    }
}

