package org.example.svfrms.device;

import org.example.svfrms.domain.ChamberState;
import org.example.svfrms.domain.SensorType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Each device must match legacy activateControl (lines 93-128): effect, clamp and message text. */
class DeviceTest {

    private static ChamberState stateWith(SensorType type, double value) {
        ChamberState state = new ChamberState();
        state.set(type, value);
        return state;
    }

    @Test
    void coolingFanLowersTemperatureButNotBelow18() {
        ChamberState state = stateWith(SensorType.TEMPERATURE, 30);
        assertEquals("Temperature reduced to 25.0C", new CoolingFan().apply(state));

        state.set(SensorType.TEMPERATURE, 20);
        assertEquals("Temperature reduced to 18.0C", new CoolingFan().apply(state));
    }

    @Test
    void waterPumpRaisesSoilMoistureButNotAbove100() {
        ChamberState state = stateWith(SensorType.SOIL_MOISTURE, 40);
        assertEquals("Soil moisture increased to 60.0%", new WaterPump().apply(state));

        state.set(SensorType.SOIL_MOISTURE, 95);
        assertEquals("Soil moisture increased to 100.0%", new WaterPump().apply(state));
    }

    @Test
    void nutrientInjectorRaisesNutrientButNotAbove100() {
        ChamberState state = stateWith(SensorType.NUTRIENT, 30);
        assertEquals("Nutrient level increased to 45.0%", new NutrientInjector().apply(state));

        state.set(SensorType.NUTRIENT, 90);
        assertEquals("Nutrient level increased to 100.0%", new NutrientInjector().apply(state));
    }

    @Test
    void humidifierRaisesHumidityButNotAbove100() {
        ChamberState state = stateWith(SensorType.HUMIDITY, 55);
        assertEquals("Humidity increased to 65.0%", new Humidifier().apply(state));

        state.set(SensorType.HUMIDITY, 95);
        assertEquals("Humidity increased to 100.0%", new Humidifier().apply(state));
    }

    @Test
    void dehumidifierLowersHumidityButNotBelow30() {
        ChamberState state = stateWith(SensorType.HUMIDITY, 90);
        assertEquals("Humidity reduced to 80.0%", new Dehumidifier().apply(state));

        state.set(SensorType.HUMIDITY, 35);
        assertEquals("Humidity reduced to 30.0%", new Dehumidifier().apply(state));
    }

    @Test
    void growLightRaisesLightButNotAbove1000() {
        ChamberState state = stateWith(SensorType.LIGHT, 400);
        assertEquals("Light level increased to 550.0 lux", new GrowLight().apply(state));

        state.set(SensorType.LIGHT, 900);
        assertEquals("Light level increased to 1000.0 lux", new GrowLight().apply(state));
    }

    @Test
    void deviceChangesOnlyItsOwnSensor() {
        ChamberState state = new ChamberState();
        new WaterPump().apply(state);
        assertEquals(SensorType.TEMPERATURE.initialValue(), state.get(SensorType.TEMPERATURE));
        assertEquals(SensorType.HUMIDITY.initialValue(), state.get(SensorType.HUMIDITY));
    }

    @Test
    void factoryCreatesEveryLegacyDeviceByCsvName() {
        DeviceFactory devices = DeviceFactory.withStandardDevices();

        for (String name : new String[] {"Cooling Fan", "Water Pump", "Nutrient Injector",
                "Humidifier", "Dehumidifier", "Grow Light"}) {
            assertEquals(name, devices.create(name).orElseThrow().name());
            assertTrue(devices.supports(name));
        }
        assertEquals(Optional.empty(), devices.create("Heating Unit"));
        assertFalse(devices.supports("Heating Unit"));
    }

    @Test
    void newDeviceNeedsOnlyARegistration() {
        // e.g. a future Heating Unit: one new class + one register call, no existing code edited
        Device heater = new Device() {
            @Override
            public String name() {
                return "Heating Unit";
            }

            @Override
            public String apply(ChamberState state) {
                return "Temperature raised to " + state.adjust(SensorType.TEMPERATURE, v -> v + 5) + "C";
            }
        };
        DeviceFactory devices = DeviceFactory.withStandardDevices().register(() -> heater);

        assertEquals("Temperature raised to 15.0C",
                devices.create("Heating Unit").orElseThrow().apply(stateWith(SensorType.TEMPERATURE, 10)));
    }
}
