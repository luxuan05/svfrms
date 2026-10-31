package org.example.svfrms.config;

import org.example.svfrms.crop.CropRegistry;
import org.example.svfrms.crop.ThresholdRule;
import org.example.svfrms.domain.SensorType;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CropProfileLoaderTest {

    private static CropRegistry load(String csv) throws IOException {
        return new CropProfileLoader().load(new BufferedReader(new StringReader(csv)));
    }

    @Test
    void loadsCropAndFacilityRulesSkippingCommentsAndBlankLines() throws IOException {
        CropRegistry crops = load("""
                # comment
                Tomato,TEMPERATURE,ABOVE,28,High Temp,Cooling Fan,0,10

                Tomato,TEMPERATURE,BELOW,22,Low Temp,,0,15
                *,LIGHT,BELOW,500,Low Light Level,Grow Light,0,0
                """);

        assertEquals(Set.of("Tomato"), crops.cropNames());
        List<ThresholdRule> temp = crops.rulesFor("Tomato", SensorType.TEMPERATURE);
        assertEquals(2, temp.size());
        assertEquals("High Temp", temp.get(0).label());   // file order kept: High before Low
        assertEquals(Optional.of("Cooling Fan"), temp.get(0).action());
        assertEquals(10, temp.get(0).energyCost());
        assertEquals(Optional.empty(), temp.get(1).action());   // empty device column = alert only
        assertEquals(1, crops.facilityRules().size());
    }

    @Test
    void facilityRulesApplyToCropsWithNoProfile() throws IOException {
        CropRegistry crops = load("*,NUTRIENT,BELOW,40,Low Nutrient Level,Nutrient Injector,0,0\n");

        List<ThresholdRule> rules = crops.rulesFor("UnknownCrop", SensorType.NUTRIENT);
        assertEquals(1, rules.size());
        assertEquals("CH9 Low Nutrient Level", rules.get(0).alertMessage("CH9", "UnknownCrop"));
        assertTrue(crops.rulesFor("UnknownCrop", SensorType.TEMPERATURE).isEmpty());
    }

    @Test
    void newCropNeedsOnlyNewCsvLines() throws IOException {
        CropRegistry crops = load("""
                Tomato,TEMPERATURE,ABOVE,28,High Temp,Cooling Fan,0,10
                Kale,TEMPERATURE,ABOVE,24,High Temp,Cooling Fan,0,0
                Kale,TEMPERATURE,BELOW,7,Low Temp,,0,0
                """);

        assertTrue(crops.find("Kale").isPresent());
        ThresholdRule high = crops.rulesFor("Kale", SensorType.TEMPERATURE).get(0);
        assertTrue(high.isBreachedBy(25));
        assertEquals("CH5 Kale High Temp", high.alertMessage("CH5", "Kale"));
    }

    @Test
    void cropCanBeRemoved() throws IOException {
        CropRegistry crops = load("Lettuce,TEMPERATURE,ABOVE,21,High Temp,Cooling Fan,0,0\n");

        assertTrue(crops.remove("Lettuce"));
        assertFalse(crops.find("Lettuce").isPresent());
        assertTrue(crops.rulesFor("Lettuce", SensorType.TEMPERATURE).isEmpty());
    }

    @Test
    void wrongFieldCountReportsLineNumber() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> load("""
                # header
                Tomato,TEMPERATURE,ABOVE,28,High Temp,Cooling Fan,0,10
                Tomato,TEMPERATURE,BELOW,22
                """));
        assertTrue(e.getMessage().startsWith("crops.csv line 3:"), e.getMessage());
    }

    @Test
    void unknownSensorReportsLineNumber() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> load("Tomato,PRESSURE,ABOVE,28,High,Cooling Fan,0,0\n"));
        assertTrue(e.getMessage().startsWith("crops.csv line 1:"), e.getMessage());
    }

    @Test
    void badNumberReportsLineNumber() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> load("""

                Tomato,TEMPERATURE,ABOVE,hot,High Temp,Cooling Fan,0,10
                """));
        assertTrue(e.getMessage().startsWith("crops.csv line 2:"), e.getMessage());
    }

    @Test
    void negativeCostIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> load("Tomato,TEMPERATURE,ABOVE,28,High Temp,Cooling Fan,0,-5\n"));
        assertTrue(e.getMessage().startsWith("crops.csv line 1:"), e.getMessage());
    }
}
