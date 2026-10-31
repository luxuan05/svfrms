# Person A: crops and devices

Scope: crop profiles, threshold rules, automated devices, the crop configuration files and the
new-crop demo. Branch: `person-a-devices`.

Legacy line numbers refer to `src/main/java/org/example/SVFRMS.java` (the original 393-line file).
Lecture references: Wk2 (OO design patterns), Wk7 (code smells, SOLID), Wk9 (race conditions),
Wk10 (explicit locks).

## 1. What changed

| File | Change |
|---|---|
| `device/WaterPump`, `NutrientInjector`, `Humidifier`, `Dehumidifier`, `GrowLight` | **New.** One class per legacy device, same effect, limit and message as legacy. |
| `device/DeviceFactory` | **New.** Creates a device from its crops.csv name. Replaces `DeviceRegistry`. |
| `device/DeviceRegistry` | **Removed** (replaced by `DeviceFactory`). |
| `device/Device` | Comment only (points to `DeviceFactory`). |
| `crop/ThresholdRule` | Added a Builder. Breach check now delegates to `Bound`. |
| `crop/CropProfile` | Added a Builder. |
| `crop/Bound` | Each constant now carries its own comparison (`>` or `<`). |
| `crop/CropRegistry` | `ConcurrentHashMap` replaced by a `HashMap` guarded by a `ReentrantReadWriteLock`. |
| `config/CropProfileLoader` | Builds rules and profiles with the builders instead of an 8-argument constructor. |
| `resources/crops.csv` | Kale example (commented out) now has temperature, humidity and soil rules. |
| `control/ControlWorker`, `App` | Outside Person A, unavoidable: `DeviceRegistry` became `DeviceFactory` (4 lines + 1 comment, same behaviour). |
| `src/test/...` | 54 tests (section 6). |

The devices behave exactly like legacy `activateControl` (lines 93-128):

| Device | Effect | Limit | Message |
|---|---|---|---|
| Cooling Fan | temperature -5 | not below 18 | `Temperature reduced to 25.0C` |
| Water Pump | soil moisture +20 | not above 100 | `Soil moisture increased to 60.0%` |
| Nutrient Injector | nutrient level +15 | not above 100 | `Nutrient level increased to 45.0%` |
| Humidifier | humidity +10 | not above 100 | `Humidity increased to 65.0%` |
| Dehumidifier | humidity -10 | not below 30 | `Humidity reduced to 80.0%` |
| Grow Light | light level +150 | not above 1000 | `Light level increased to 550.0 lux` |

## 2. Legacy problems this part fixes

| Problem (lecture) | Legacy | Now |
|---|---|---|
| OCP violation (Wk7) | Crop if-chain in `processSensor` (145-204): a new crop means editing it | A crop is lines in `crops.csv`; no Java changes |
| Magic numbers | Thresholds and costs hard-coded in 19 `if`s (148-202) | Data in `crops.csv`, checked against legacy by `BundledCropsTest` |
| DRY (Wk7) | 19 near-identical alert blocks (148-202); 6 copy-pasted device branches (93-128) | One generic rule loop; one small class per device |
| Feature envy (Wk7) | `activateControl` reaches into `ChamberState`'s fields (94, 100, ...) | Each device asks the state to adjust itself: `state.adjust(TEMPERATURE, v -> Math.max(18, v - 5))` |
| Selector on a string | `if (control.equals("Cooling Fan")) ... else if ...` (93-128) | `DeviceFactory.create(name)` returns the right object; no if-chain |

## 3. Design patterns (Wk2)

| Pattern | Where | Problem it solves |
|---|---|---|
| **Strategy** (behavioural) | `Device` interface + 6 implementations | Each device's behaviour is its own class, chosen at run time. Adding a device never edits the others. Replaces the if/else chain in `activateControl`. |
| **Factory Method** (creational) | `DeviceFactory.create(name)` | Callers ask for "Cooling Fan" by its crops.csv name and get a `Device` without naming or `new`-ing a concrete class. Same idea as `LockFactory.getLock(name)` in Wk10: the client says *what* it needs, the factory decides *how* to create it. |
| **Builder** (creational) | `ThresholdRule.builder(...)`, `CropProfile.builder(...)` | A rule has 8 values. With a positional constructor, `waterCost` and `energyCost` (both `int`) are easy to swap without any error. The builder names every value and reads like spec 3.1. |

Builder example (a crop defined in code instead of CSV):

```java
CropProfile kale = CropProfile.builder("Kale")
        .rule(ThresholdRule.builder(TEMPERATURE).above(24).label("High Temp").device("Cooling Fan").energyCost(10))
        .rule(ThresholdRule.builder(TEMPERATURE).below(7).label("Low Temp").energyCost(15))
        .build();
```

Patterns considered and not used here:
- **Singleton** for `DeviceFactory` or `CropRegistry`. Wk2 warns about "Singleton abuse", and the legacy problem was already global state (`public static` fields). They are created once in `App` and passed in instead.
- **Abstract Factory**: there is only one family of devices.

## 4. SOLID (spec 4.2)

| Principle | How this part applies it |
|---|---|
| **SRP** | `CropProfileLoader` only parses, `CropRegistry` only stores and looks up, each device only applies its own effect. |
| **OCP** (spec 4.2.ii, 4.5) | New crop: CSV lines only. New device: a new class plus one `register(...)` line in `DeviceFactory.withStandardDevices()`. Existing classes are not edited. |
| **LSP** | Every `Device` can replace any other wherever a `Device` is expected; the worker never checks which one it has. |
| **ISP** | `SensorProcessor` sees crops only through `CropCatalog`, which has one method (`rulesFor`). It cannot register or remove crops. |
| **DIP** | `ControlWorker` depends on the `Device` interface, not on `CoolingFan` etc.; `SensorProcessor` depends on `CropCatalog`, not `CropRegistry`. |

## 5. Thread safety in this part

Rules are looked up on every sensor reading by all four chamber threads at once.

| Class | Approach (lecture) | Why |
|---|---|---|
| `CropRegistry` | `ReentrantReadWriteLock` (Wk10) | Read-mostly: every reading reads; crops are added or removed rarely. Many readers can hold the read lock together; `register`/`remove` take the write lock, so a reader never sees a half-registered crop. |
| `DeviceFactory` | `ConcurrentHashMap` (Wk9) | Looked up from several threads. |
| `ThresholdRule`, `CropProfile` | Immutable records | Nothing to lock: they never change after creation, so all threads can share them. |
| Devices | Call `ChamberState.adjust(...)` | The read-modify-write (e.g. temperature - 5) happens in one synchronized step, unlike legacy line 94. |

Note: the original `ConcurrentHashMap` in `CropRegistry` was already thread-safe. The read-write lock is
the Wk10 pattern for exactly this read-mostly case, and it also makes `cropNames()` an exact snapshot
(iterating a `ConcurrentHashMap` is only "weakly consistent").

## 6. Tests (54, all passing with `mvn clean package`)

| Test class | What it checks |
|---|---|
| `BundledCropsTest` (29) | Every rule in `crops.csv` makes the same decision as legacy `processSensor`: alert text, device, water and energy cost, including the exact threshold values (legacy uses strict `>` / `<`). |
| `CropProfileLoaderTest` (8) | Valid files load; comments and blank lines are skipped; errors give the right line number; a crop can be added or removed through CSV only. |
| `CropProfileBuilderTest` (5) | Builders produce the right rules; missing values are rejected; built profiles cannot be modified. |
| `CropRegistryTest` (3 runs) | 4 reader threads and 1 writer thread at once: readers never see a half-registered crop. |
| `DeviceTest` (9) | Each device's effect, limit and exact message; the factory creates all six; a new device needs only a registration. |

## 7. Extensibility

| Change | What to do | Existing Java modified |
|---|---|---|
| Add a crop (e.g. Kale, Bok Choy) | Add its lines to `crops.csv`, add `CH5,Kale` to `chambers.csv` | None |
| Remove a crop | Delete or comment out its lines in both files | None |
| Add a device (e.g. Heating Unit) | New `HeatingUnit implements Device`; use its name in `crops.csv` | One `register(HeatingUnit::new)` line |
| Change a threshold or cost | Edit the number in `crops.csv` | None |

## 8. New-crop demo (about 1 minute)

`ConfigReader` reads `crops.csv` / `chambers.csv` from the working folder before the copies inside the jar,
so no rebuild is needed:

1. Put `run.jar` in an empty folder and run `java -jar run.jar`: CH1-CH4 with the bundled crops.
2. Copy `crops.csv` and `chambers.csv` from `src/main/resources` into that folder.
3. Add Kale: uncomment the 5 Kale lines in `crops.csv`; append `CH5,Kale` to `chambers.csv`.
4. Remove Lettuce: comment out the 2 `Lettuce` lines and `CH3,Lettuce`.
5. Run again: Kale appears on CH5 with 5 rules and Lettuce is gone. No Java was changed.

## 9. Likely questions

- **Is `DeviceFactory` really Factory Method?** It is the parameterised form used in the course
  (`LockFactory.getLock(name)` in Wk10): the client passes a name and gets a product, without knowing the
  concrete class. The textbook GoF variant uses subclasses overriding a creation method; with one family of
  devices that would add classes without benefit.
- **Why CSV and not just the Builder for crops?** CSV lets a crop be added or removed without recompiling,
  which is what spec 4.5 asks for. The Builder is what the loader uses internally, and it lets code and
  tests define crops readably.
- **Why change `CropRegistry` if `ConcurrentHashMap` was already safe?** Both are correct. The read-write lock
  matches the read-mostly access pattern from Wk10 and gives exact snapshots; see section 5.
- **What happens with a misspelt device name in `crops.csv`?** `DeviceFactory.create` returns empty and the
  request is ignored, exactly like legacy `activateControl`, which silently ignored unknown names.
