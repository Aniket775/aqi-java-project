package com.bigcoders.aqi.service;

import com.bigcoders.aqi.dto.PredictionResult;
import com.bigcoders.aqi.model.AqiHistory;
import com.bigcoders.aqi.model.Station;
import com.bigcoders.aqi.repository.AqiHistoryRepository;
import com.bigcoders.aqi.repository.StationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AQI Prediction Engine Unit Tests")
class AqiPredictionServiceTest {

    private FakeAqiHistoryRepository aqiHistoryRepository;
    private FakeStationRepository stationRepository;
    private AqiCalculationService aqiCalculationService;
    private AqiPredictionService predictionService;

    // Lightweight in-memory test doubles avoiding Java 26 bytecode instrumentation issues
    static class FakeAqiHistoryRepository extends AqiHistoryRepository {
        private List<AqiHistory> points = new ArrayList<>();

        public FakeAqiHistoryRepository() {
            super(null);
        }

        public void setPoints(List<AqiHistory> points) {
            this.points = points;
        }

        @Override
        public List<AqiHistory> findHistoricalPointsAsc(int stationId, int days) {
            return points;
        }
    }

    static class FakeStationRepository extends StationRepository {
        public FakeStationRepository() {
            super(null);
        }

        @Override
        public Optional<Station> findById(int id) {
            Station s = new Station(id, 1, "ITO Delhi", "Central Delhi", "Continuous", LocalDate.now(), true);
            s.setCityName("Delhi");
            return Optional.of(s);
        }
    }

    @BeforeEach
    void setUp() {
        aqiHistoryRepository = new FakeAqiHistoryRepository();
        stationRepository = new FakeStationRepository();
        aqiCalculationService = new AqiCalculationService();
        predictionService = new AqiPredictionService(aqiHistoryRepository, stationRepository, aqiCalculationService);
    }

    @Test
    @DisplayName("Fewer than 7 historical days fails sufficiency check")
    void testInsufficientDataRequirement() {
        List<AqiHistory> shortHistory = new ArrayList<>();
        LocalDate start = LocalDate.now().minusDays(5);
        for (int i = 0; i < 5; i++) {
            shortHistory.add(new AqiHistory(i, 1, BigDecimal.valueOf(150 + i * 5), "Moderate", start.plusDays(i), "PM2.5", "Sample"));
        }
        aqiHistoryRepository.setPoints(shortHistory);

        PredictionResult res = predictionService.forecast(1, 3);
        assertFalse(res.isSufficientData(), "Must reject forecast when fewer than 7 days exist");
        assertEquals(5, res.getHistoricalDataPoints());
        assertTrue(res.getPredictions().isEmpty());
    }

    @Test
    @DisplayName("Increasing trend forecasted correctly for next 3 days")
    void testIncreasingTrend() {
        List<AqiHistory> history = new ArrayList<>();
        LocalDate start = LocalDate.now().minusDays(8);
        // Clear increasing daily sequence: 100, 110, 120, 130, 140, 150, 160, 170
        for (int i = 0; i < 8; i++) {
            history.add(new AqiHistory(i, 1, BigDecimal.valueOf(100.0 + i * 10.0), "Moderate", start.plusDays(i), "PM2.5", "Sample"));
        }
        aqiHistoryRepository.setPoints(history);

        PredictionResult res = predictionService.forecast(1, 3);
        assertTrue(res.isSufficientData(), "Must accept forecast with 8 days");
        assertTrue(res.getTrendDirection().contains("Increasing"));
        assertEquals(3, res.getPredictions().size());

        // Day +1 should predict ~ 180
        assertEquals(180.0, res.getPredictions().get(0).getPredictedAqi().doubleValue(), 1.0);
    }

    @Test
    @DisplayName("Decreasing trend identified correctly")
    void testDecreasingTrend() {
        List<AqiHistory> history = new ArrayList<>();
        LocalDate start = LocalDate.now().minusDays(8);
        for (int i = 0; i < 8; i++) {
            history.add(new AqiHistory(i, 1, BigDecimal.valueOf(250.0 - i * 15.0), "Poor", start.plusDays(i), "PM2.5", "Sample"));
        }
        aqiHistoryRepository.setPoints(history);

        PredictionResult res = predictionService.forecast(1, 2);
        assertTrue(res.isSufficientData());
        assertTrue(res.getTrendDirection().contains("Decreasing"));
        assertEquals(2, res.getPredictions().size());
    }
}
