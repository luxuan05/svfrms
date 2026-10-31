package org.example.svfrms.crop;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.example.svfrms.domain.SensorType.LIGHT;
import static org.example.svfrms.domain.SensorType.TEMPERATURE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CropRegistry is read on every sensor reading by every chamber thread, and written when a crop is
 * added or removed. Readers must always see either the whole crop or no crop, never an error.
 */
@Timeout(30)
class CropRegistryTest {

    private static final CropProfile KALE = CropProfile.builder("Kale")
            .rule(ThresholdRule.builder(TEMPERATURE).above(24).label("High Temp").device("Cooling Fan"))
            .rule(ThresholdRule.builder(TEMPERATURE).below(7).label("Low Temp"))
            .build();

    @RepeatedTest(3)
    void readersAndAWriterRunConcurrentlyWithoutErrors() throws Exception {
        CropRegistry crops = new CropRegistry(List.of(
                ThresholdRule.builder(LIGHT).below(500).label("Low Light Level").device("Grow Light").facilityWide().build()));
        ExecutorService pool = Executors.newFixedThreadPool(5);
        try {
            List<Callable<Integer>> jobs = new ArrayList<>();
            jobs.add(() -> {   // writer: add and remove Kale repeatedly
                for (int i = 0; i < 5_000; i++) {
                    crops.register(KALE);
                    crops.remove("Kale");
                }
                crops.register(KALE);
                return 0;
            });
            for (int r = 0; r < 4; r++) {
                jobs.add(() -> {   // readers: like chamber threads looking up rules
                    int lookups = 0;
                    for (int i = 0; i < 20_000; i++) {
                        int temperatureRules = crops.rulesFor("Kale", TEMPERATURE).size();
                        assertTrue(temperatureRules == 0 || temperatureRules == 2, "saw a half-registered crop");
                        assertEquals(1, crops.rulesFor("Kale", LIGHT).size());   // facility rule always there
                        lookups++;
                    }
                    return lookups;
                });
            }
            for (Future<Integer> f : pool.invokeAll(jobs)) {
                f.get();   // rethrows any assertion failure from a reader
            }
        } finally {
            pool.shutdownNow();
        }

        assertEquals(2, crops.rulesFor("Kale", TEMPERATURE).size());
        assertTrue(crops.cropNames().contains("Kale"));
    }
}
