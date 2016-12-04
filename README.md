# ivol-parser
IVolatility chart parser

## Examples

Auto axis parsing

```java
IVolChart iVolChart = new IVolChart(new FileInputStream(CHART_FILE));
iVolChart.parseAxises();
LinkedHashMap<LocalDate, Double> values = iVolChart.parseData(IVolLineType.IMPLIED_VOLATILITY);
```

Or manually providing values

```java
IVolChart iVolChart = new IVolChart(new FileInputStream(CHART_FILE));
		
LinkedHashMap<Integer, LocalDate> knownPoints = new LinkedHashMap<>();
knownPoints.put(1, LocalDate.of(2015, 12, 2));
knownPoints.put(479, LocalDate.of(2016, 12, 2));
iVolChart.setKnownPoints(knownPoints);

iVolChart.setHighestValueY(50);
iVolChart.setLowestValueY(25);
		
LinkedHashMap<LocalDate, Double> values = iVolChart.parseData(IVolLineType.IMPLIED_VOLATILITY);
```
