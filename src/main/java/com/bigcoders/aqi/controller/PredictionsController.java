package com.bigcoders.aqi.controller;

import com.bigcoders.aqi.dto.PredictionResult;
import com.bigcoders.aqi.model.Station;
import com.bigcoders.aqi.repository.StationRepository;
import com.bigcoders.aqi.service.AqiPredictionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/predictions")
public class PredictionsController {

    private final AqiPredictionService predictionService;
    private final StationRepository stationRepository;

    public PredictionsController(AqiPredictionService predictionService,
                                 StationRepository stationRepository) {
        this.predictionService = predictionService;
        this.stationRepository = stationRepository;
    }

    @GetMapping
    public String predictionsPage(@RequestParam(value = "stationId", required = false) Integer stationId,
                                  @RequestParam(value = "days", defaultValue = "3") int days,
                                  Model model) {
        List<Station> stations = stationRepository.findActive();
        model.addAttribute("stations", stations);

        // Default to first active station (e.g. ITO Delhi with rich 10-day history)
        int targetStationId = (stationId != null) ? stationId : (!stations.isEmpty() ? stations.get(0).getStationId() : 1);
        model.addAttribute("selectedStationId", targetStationId);

        PredictionResult result = predictionService.forecast(targetStationId, days);
        model.addAttribute("result", result);

        return "predictions";
    }
}

