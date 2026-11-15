package com.bigcoders.aqi.controller;

import com.bigcoders.aqi.dto.AqiResult;
import com.bigcoders.aqi.dto.ReadingSubmission;
import com.bigcoders.aqi.model.City;
import com.bigcoders.aqi.model.Pollutant;
import com.bigcoders.aqi.model.Reading;
import com.bigcoders.aqi.model.Station;
import com.bigcoders.aqi.repository.CityRepository;
import com.bigcoders.aqi.repository.PollutantRepository;
import com.bigcoders.aqi.repository.ReadingRepository;
import com.bigcoders.aqi.repository.StationRepository;
import com.bigcoders.aqi.service.DashboardService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/data-entry")
public class DataEntryController {

    private final CityRepository cityRepository;
    private final StationRepository stationRepository;
    private final PollutantRepository pollutantRepository;
    private final ReadingRepository readingRepository;
    private final DashboardService dashboardService;

    public DataEntryController(CityRepository cityRepository,
                               StationRepository stationRepository,
                               PollutantRepository pollutantRepository,
                               ReadingRepository readingRepository,
                               DashboardService dashboardService) {
        this.cityRepository = cityRepository;
        this.stationRepository = stationRepository;
        this.pollutantRepository = pollutantRepository;
        this.readingRepository = readingRepository;
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public String dataEntryPage(Model model) {
        populateFormAttributes(model);
        if (!model.containsAttribute("submission")) {
            model.addAttribute("submission", new ReadingSubmission());
        }
        return "data-entry";
    }

    @GetMapping("/stations-by-city")
    @ResponseBody
    public ResponseEntity<List<Station>> getStationsByCity(@RequestParam("cityId") int cityId) {
        List<Station> stations = stationRepository.findByCityId(cityId);
        return ResponseEntity.ok(stations);
    }

    @PostMapping({"", "/submit"})
    public String submitReading(@Valid @ModelAttribute("submission") ReadingSubmission submission,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateFormAttributes(model);
            return "data-entry";
        }

        try {
            AqiResult aqiResult = dashboardService.processReadingSubmission(submission);
            redirectAttributes.addFlashAttribute("successMessage", "Reading recorded successfully!");
            redirectAttributes.addFlashAttribute("aqiResult", aqiResult);
            return "redirect:/data-entry";

        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Submission failed: " + ex.getMessage());
            populateFormAttributes(model);
            return "data-entry";
        }
    }

    private void populateFormAttributes(Model model) {
        List<City> cities = cityRepository.findAll();
        List<Station> stations = stationRepository.findActive();
        List<Pollutant> pollutants = pollutantRepository.findAll();
        List<Reading> recentReadings = readingRepository.findRecent(10);

        model.addAttribute("cities", cities);
        model.addAttribute("stations", stations);
        model.addAttribute("pollutants", pollutants);
        model.addAttribute("recentReadings", recentReadings);
    }
}

