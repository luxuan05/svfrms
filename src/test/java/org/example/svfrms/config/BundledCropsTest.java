package org.example.svfrms.config;

import org.example.svfrms.crop.CropRegistry;
import org.example.svfrms.crop.ThresholdRule;
import org.example.svfrms.domain.SensorType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Characterization test: the bundled crops.csv must make the same decisions as the hard-coded
 * if-chain in legacy SVFRMS.processSensor (lines 145-204) - same alert text, same device request,
 * same water/energy cost - including the boundary values, where legacy uses strict > and <.
 */
class BundledCropsTest {

    private static CropRegistry crops;

    @BeforeAll
    static void loadBundledFile() throws IOException {
        InputStream bundled = BundledCropsTest.class.getClassLoader().getResourceAsStream("crops.csv");
        assertNotNull(bundled, "crops.csv missing from classpath");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(bundled, UTF_8))) {
            crops = new CropProfileLoader().load(in);
        }
    }

    @ParameterizedTest(name = "{0} {1} = {2} -> \"{3}\"")
    @CsvSource({
            // crop,     sensor,        value, alert,                          device,              water, energy
            "Tomato,     TEMPERATURE,   29,    'CH1 Tomato High Temp',         'Cooling Fan',       0,  10",
            "Tomato,     TEMPERATURE,   28,    '',                             '',                  0,  0",
            "Tomato,     TEMPERATURE,   22,    '',                             '',                  0,  0",
            "Tomato,     TEMPERATURE,   21,    'CH1 Tomato Low Temp',          '',                  0,  15",
            "Strawberry, TEMPERATURE,   25,    'CH1 Strawberry High Temp',     'Cooling Fan',       0,  10",
            "Strawberry, TEMPERATURE,   17,    'CH1 Strawberry Low Temp',      '',                  0,  15",
            "Lettuce,    TEMPERATURE,   22,    'CH1 Lettuce High Temp',        'Cooling Fan',       0,  0",
            "Lettuce,    TEMPERATURE,   14,    'CH1 Lettuce Low Temp',         '',                  0,  0",
            "Spinach,    TEMPERATURE,   21,    'CH1 Spinach High Temp',        'Cooling Fan',       0,  0",
            "Spinach,    TEMPERATURE,   9,     'CH1 Spinach Low Temp',         '',                  0,  0",
            "Chili,      TEMPERATURE,   33,    'CH1 Chili High Temp',          'Cooling Fan',       0,  0",
            "Chili,      TEMPERATURE,   23,    'CH1 Chili Low Temp',           '',                  0,  0",
            "Cucumber,   TEMPERATURE,   28,    'CH1 Cucumber High Temp',       'Cooling Fan',       0,  0",
            "Cucumber,   TEMPERATURE,   17,    'CH1 Cucumber Low Temp',        '',                  0,  0",
            "Tomato,     HUMIDITY,      81,    'CH1 Tomato High Humidity',     'Dehumidifier',      0,  0",
            "Tomato,     HUMIDITY,      59,    'CH1 Tomato Low Humidity',      'Humidifier',        20, 0",
            "Strawberry, HUMIDITY,      86,    'CH1 Strawberry High Humidity', 'Dehumidifier',      0,  0",
            "Strawberry, HUMIDITY,      69,    'CH1 Strawberry Low Humidity',  'Humidifier',        25, 0",
            "Lettuce,    HUMIDITY,      30,    '',                             '',                  0,  0",
            "Tomato,     SOIL_MOISTURE, 44,    'CH1 Tomato Low Soil Moisture', 'Water Pump',        50, 0",
            "Tomato,     SOIL_MOISTURE, 45,    '',                             '',                  0,  0",
            "Strawberry, SOIL_MOISTURE, 10,    '',                             '',                  0,  0",
            "Tomato,     LIGHT,         499,   'CH1 Low Light Level',          'Grow Light',        0,  0",
            "Tomato,     LIGHT,         500,   '',                             '',                  0,  0",
            "Chili,      LIGHT,         300,   'CH1 Low Light Level',          'Grow Light',        0,  0",
            "Tomato,     NUTRIENT,      39,    'CH1 Low Nutrient Level',       'Nutrient Injector', 0,  0",
            "Tomato,     NUTRIENT,      40,    '',                             '',                  0,  0",
            // A crop with no profile gets only the facility-wide checks, as in legacy
            "Kale,       TEMPERATURE,   50,    '',                             '',                  0,  0",
            "Kale,       NUTRIENT,      10,    'CH1 Low Nutrient Level',       'Nutrient Injector', 0,  0",
    })
    void matchesLegacyProcessSensor(String crop, SensorType sensor, double value, String alert,
                                    String device, int water, int energy) {
        List<ThresholdRule> breached = crops.rulesFor(crop, sensor).stream()
                .filter(r -> r.isBreachedBy(value))
                .toList();

        assertEquals(alert.isEmpty() ? List.of() : List.of(alert),
                breached.stream().map(r -> r.alertMessage("CH1", crop)).toList());
        assertEquals(device.isEmpty() ? List.of() : List.of(device),
                breached.stream().flatMap(r -> r.action().stream()).toList());
        assertEquals(water, breached.stream().mapToInt(ThresholdRule::waterCost).sum());
        assertEquals(energy, breached.stream().mapToInt(ThresholdRule::energyCost).sum());
    }
}
