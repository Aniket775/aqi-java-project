package com.bigcoders.aqi.controller;

import com.bigcoders.aqi.model.AppUser;
import com.bigcoders.aqi.model.City;
import com.bigcoders.aqi.model.Pollutant;
import com.bigcoders.aqi.model.Station;
import com.bigcoders.aqi.repository.CityRepository;
import com.bigcoders.aqi.repository.PollutantRepository;
import com.bigcoders.aqi.repository.StationRepository;
import com.bigcoders.aqi.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CityRepository cityRepository;
    private final StationRepository stationRepository;
    private final PollutantRepository pollutantRepository;
    private final UserRepository userRepository;

    public AdminController(CityRepository cityRepository,
                           StationRepository stationRepository,
                           PollutantRepository pollutantRepository,
                           UserRepository userRepository) {
        this.cityRepository = cityRepository;
        this.stationRepository = stationRepository;
        this.pollutantRepository = pollutantRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String adminPage(@RequestParam(value = "tab", defaultValue = "cities") String tab, Model model) {
        List<City> cities = cityRepository.findAll();
        List<Station> stations = stationRepository.findAll();
        List<Pollutant> pollutants = pollutantRepository.findAll();
        List<AppUser> users = userRepository.findAll();

        model.addAttribute("cities", cities);
        model.addAttribute("stations", stations);
        model.addAttribute("pollutants", pollutants);
        model.addAttribute("users", users);
        model.addAttribute("activeTab", tab);

        return "admin";
    }

    @PostMapping("/cities/add")
    public String addCity(@ModelAttribute City city, RedirectAttributes redirectAttributes) {
        try {
            cityRepository.save(city);
            redirectAttributes.addFlashAttribute("successMessage", "City '" + city.getCityName() + "' added successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding city: " + ex.getMessage());
        }
        return "redirect:/admin?tab=cities";
    }

    @PostMapping("/stations/add")
    public String addStation(@ModelAttribute Station station, RedirectAttributes redirectAttributes) {
        try {
            if (station.getInstallationDate() == null) {
                station.setInstallationDate(LocalDate.now());
            }
            station.setActive(true);
            stationRepository.save(station);
            redirectAttributes.addFlashAttribute("successMessage", "Station '" + station.getStationName() + "' added successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding station: " + ex.getMessage());
        }
        return "redirect:/admin?tab=stations";
    }

    @PostMapping("/stations/{id}/toggle")
    public String toggleStation(@PathVariable("id") int stationId,
                                @RequestParam("active") boolean active,
                                RedirectAttributes redirectAttributes) {
        try {
            stationRepository.toggleActive(stationId, active);
            redirectAttributes.addFlashAttribute("successMessage", "Station status updated.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating station: " + ex.getMessage());
        }
        return "redirect:/admin?tab=stations";
    }

    @PostMapping("/pollutants/add")
    public String addPollutant(@ModelAttribute Pollutant pollutant, RedirectAttributes redirectAttributes) {
        try {
            pollutantRepository.save(pollutant);
            redirectAttributes.addFlashAttribute("successMessage", "Pollutant '" + pollutant.getChemicalFormula() + "' added successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding pollutant: " + ex.getMessage());
        }
        return "redirect:/admin?tab=pollutants";
    }

    @PostMapping("/users/{id}/role")
    public String updateUserRole(@PathVariable("id") int userId,
                                 @RequestParam("role") String role,
                                 RedirectAttributes redirectAttributes) {
        try {
            userRepository.updateRole(userId, role);
            redirectAttributes.addFlashAttribute("successMessage", "User role updated to " + role);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating role: " + ex.getMessage());
        }
        return "redirect:/admin?tab=users";
    }
}

