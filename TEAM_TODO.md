# SVFRMS refactor - team task list

Run the skeleton now: `mvn clean package && java -jar target/svfrms-1.0.0.jar`
It prints every loaded crop rule. Check them against legacy `processSensor` (lines 141-205).

## Done in the skeleton
- Package structure, interfaces, records.
- Crop model: `ThresholdRule`, `CropProfile`, `CropRegistry`, `CropProfileLoader`, `crops.csv`
  (all legacy thresholds and costs transcribed), `chambers.csv`.
- Thread-safe `ChamberState`, `ChamberSummary`, `ChamberRegistry`.
- `SensorProcessor`, `ChamberTask`, `ControlWorker`, `ConsoleEventLogger`, `ChamberTracker`,
  `CoolingFan` (example device), `RandomSensorSource` (seedable).

## Person A - crops & devices (OCP, Strategy, new-crop demo)
- [ ] Add `WaterPump`, `NutrientInjector`, `Humidifier`, `Dehumidifier`, `GrowLight`
      following `CoolingFan` (legacy lines 99-128; copy the exact messages).
- [ ] Verify every line of `crops.csv` against legacy lines 141-205.
- [ ] Demo: uncomment Kale in `crops.csv`, add `CH5,Kale` to `chambers.csv`, rerun. No Java edits.
- [ ] Unit tests for `CropProfileLoader` (good file, bad line number in error message).

## Person B - concurrency
- [ ] `ControlQueue` -> `ArrayBlockingQueue`.
- [ ] `ResourceUsageTracker` -> `AtomicInteger`.
- [ ] `AlertRepository` -> single lock, snapshots, no nested locks.
- [ ] Wiring in `App` with `ExecutorService`, poison-pill shutdown.
- [ ] Stress demo: legacy copy with 5000 rounds + printlns removed shows lost alerts
      (archived < generated). New version must always match.

## Person C - reports, wiring, docs
- [ ] `DailyReport`, file report writer (reuse DailyReport lines), archive step.
- [ ] Fix report/archive ordering (legacy race R5) and document the chosen order.
- [ ] Compare new output vs legacy output with a fixed seed (characterization check).
- [ ] UML: component diagram + sequence diagram (reading -> alert -> request -> device).
- [ ] README.pdf (3-6 pages + cover) and slides.

## Before submitting (Nov 1, 11:59 PM)
- [ ] `mvn clean package` on JDK 25, copy the jar to `run.jar`, run it from a clean folder.
- [ ] Zip: `src/`, `pom.xml`, `run.jar`, `README.pdf`.
