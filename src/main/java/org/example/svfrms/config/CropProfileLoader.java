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
        Map<String, List<ThresholdRule>> rulesByCrop = new LinkedHashMap<>();
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
                boolean facilityWide = crop.equals(FACILITY_WIDE);
                ThresholdRule rule = new ThresholdRule(
                        SensorType.valueOf(f[1].strip()),
                        Bound.valueOf(f[2].strip()),
                        Double.parseDouble(f[3].strip()),
                        f[4].strip(),
                        f[5].isBlank() ? null : f[5].strip(),
                        Integer.parseInt(f[6].strip()),
                        Integer.parseInt(f[7].strip()),
                        facilityWide);
                if (facilityWide) {
                    facilityRules.add(rule);
                } else {
                    rulesByCrop.computeIfAbsent(crop, k -> new ArrayList<>()).add(rule);
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("crops.csv line " + lineNo + ": " + e.getMessage(), e);
            }
        }

        CropRegistry registry = new CropRegistry(facilityRules);
        rulesByCrop.forEach((name, rules) -> registry.register(new CropProfile(name, rules)));
        return registry;
    }
}
