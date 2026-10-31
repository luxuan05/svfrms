package org.example.svfrms.crop;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.example.svfrms.domain.SensorType.HUMIDITY;
import static org.example.svfrms.domain.SensorType.LIGHT;
import static org.example.svfrms.domain.SensorType.TEMPERATURE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CropProfileBuilderTest {

    @Test
    void buildsAProfileInCodeWithoutCsv() {
        CropProfile kale = CropProfile.builder("Kale")
                .rule(ThresholdRule.builder(TEMPERATURE).above(24).label("High Temp").device("Cooling Fan").energyCost(10))
                .rule(ThresholdRule.builder(TEMPERATURE).below(7).label("Low Temp").energyCost(15))
                .rule(ThresholdRule.builder(HUMIDITY).below(60).label("Low Humidity").device("Humidifier").waterCost(20))
                .build();

        assertEquals("Kale", kale.name());
        assertEquals(2, kale.rulesFor(TEMPERATURE).size());
        ThresholdRule high = kale.rulesFor(TEMPERATURE).get(0);
        assertEquals(Bound.ABOVE, high.bound());
        assertEquals(24, high.limit());
        assertEquals(Optional.of("Cooling Fan"), high.action());
        assertEquals(10, high.energyCost());
        assertEquals(0, high.waterCost());
        assertEquals(Optional.empty(), kale.rulesFor(TEMPERATURE).get(1).action());
        assertEquals(20, kale.rulesFor(HUMIDITY).get(0).waterCost());
    }

    @Test
    void comparisonsAreStrictLikeLegacy() {
        ThresholdRule high = ThresholdRule.builder(TEMPERATURE).above(28).label("High Temp").build();
        ThresholdRule low = ThresholdRule.builder(TEMPERATURE).below(22).label("Low Temp").build();

        assertTrue(high.isBreachedBy(28.5));
        assertFalse(high.isBreachedBy(28));
        assertTrue(low.isBreachedBy(21.9));
        assertFalse(low.isBreachedBy(22));
    }

    @Test
    void facilityWideRuleMessageHasNoCropName() {
        ThresholdRule light = ThresholdRule.builder(LIGHT).below(500).label("Low Light Level")
                .device("Grow Light").facilityWide().build();

        assertEquals("CH2 Low Light Level", light.alertMessage("CH2", "Strawberry"));
    }

    @Test
    void missingLabelOrBoundIsRejected() {
        assertThrows(NullPointerException.class, () -> ThresholdRule.builder(TEMPERATURE).above(28).build());
        assertThrows(NullPointerException.class, () -> ThresholdRule.builder(TEMPERATURE).label("High").build());
    }

    @Test
    void builtProfileIsImmutable() {
        CropProfile p = CropProfile.builder("Lettuce")
                .rule(ThresholdRule.builder(TEMPERATURE).above(21).label("High Temp"))
                .build();

        assertThrows(UnsupportedOperationException.class, () -> p.rules().clear());
    }
}
