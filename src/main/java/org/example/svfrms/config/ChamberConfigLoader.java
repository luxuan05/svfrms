package org.example.svfrms.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Reads chambers.csv: one "chamberId,crop" per line (replaces hard-coded legacy main, lines 351-354). */
public final class ChamberConfigLoader {

    public List<ChamberAssignment> load(BufferedReader in) throws IOException {
        List<ChamberAssignment> result = new ArrayList<>();
        String line;
        int lineNo = 0;
        while ((line = in.readLine()) != null) {
            lineNo++;
            line = line.strip();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] f = line.split(",", -1);
            if (f.length != 2 || f[0].isBlank() || f[1].isBlank()) {
                throw new IllegalArgumentException("chambers.csv line " + lineNo + ": expected chamberId,crop");
            }
            result.add(new ChamberAssignment(f[0].strip(), f[1].strip()));
        }
        return result;
    }
}
