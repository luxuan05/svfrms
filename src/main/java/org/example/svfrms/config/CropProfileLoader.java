package org.example.svfrms.config;

import org.example.svfrms.crop.Bound;
import org.example.svfrms.crop.CropProfile;
import org.example.svfrms.crop.CropRegistry;
import org.example.svfrms.crop.ThresholdRule;
import org.example.svfrms.domain.SensorType;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds a {@link CropRegistry} from crops.csv.
 * Format per line: crop,sensor,bound,limit,label,device,waterCost,energyCost
 * Blank lines and lines starting with # are ignored. Crop "*" marks a facility-wide rule.
 */
public final class CropProfileLoader {
    public static final String FACILITY_WIDE = "*";
    private static final int FIELDS = 8;

    public CropRegistry load(BufferedReader in) throws IOException {
        Map<String, CropProfile.Builder> cropsByName = new LinkedHashMap<>();
        List<ThresholdRule> facilityRules = new ArrayList<>();

        String line;
        int lineNo = 0;
        while ((line = in.readLine()) != null) {
            lineNo++;
            line = line.strip();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] f = line.split(",", -1);
            if (f.length != FIELDS) {
                throw new IllegalArgumentException("crops.csv line " + lineNo
                        + ": expected " + FIELDS + " fields but found " + f.length);
            }
            try {
                String crop = f[0].strip();
                ThresholdRule.Builder rule = ThresholdRule.builder(SensorType.valueOf(f[1].strip()))
                        .bound(Bound.valueOf(f[2].strip()), Double.parseDouble(f[3].strip()))
                        .label(f[4].strip())
                        .waterCost(Integer.parseInt(f[6].strip()))
                        .energyCost(Integer.parseInt(f[7].strip()));
                if (!f[5].isBlank()) {
                    rule.device(f[5].strip());
                }
                if (crop.equals(FACILITY_WIDE)) {
                    facilityRules.add(rule.facilityWide().build());
                } else {
                    cropsByName.computeIfAbsent(crop, CropProfile::builder).rule(rule);
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("crops.csv line " + lineNo + ": " + e.getMessage(), e);
            }
        }

        CropRegistry registry = new CropRegistry(facilityRules);
        cropsByName.values().forEach(builder -> registry.register(builder.build()));
        return registry;
    }
}
