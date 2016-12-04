package dk.anamuser.ivolparser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

/**
 * IVolatility chart
 */
public class IVolChart {

	private double lowestValueY;
	private double highestValueY;
	private LinkedHashMap<Integer, LocalDate> knownPoints;
	private BufferedImage chartImage;

	public IVolChart(InputStream chartFile) {
		try {
			this.chartImage = ImageIO.read(chartFile);
		} catch (IOException e) {
			throw new RuntimeException("Could not read chart", e);
		}
	}

	public void setLowestValueY(double lowestValueY) {
		this.lowestValueY = lowestValueY;
	}

	public void setHighestValueY(double highestValueY) {
		this.highestValueY = highestValueY;
	}

	public void setKnownPoints(LinkedHashMap<Integer, LocalDate> knownPoints) {
		this.knownPoints = knownPoints;
	}

	public void parseAxises() {
		IVolChartAxisParser iVolChartAxisParser = new IVolChartAxisParser(chartImage);

		try {
			this.knownPoints = iVolChartAxisParser.parseXAxis();
		} catch (Exception e) {
			throw new RuntimeException("Could not parse x axis", e);
		}

		try {
			LinkedHashMap<Integer, Double> valueMap = iVolChartAxisParser.parseYAxis();
			List<Double> values = new ArrayList<>(valueMap.values());
			this.highestValueY = values.get(0);
			this.lowestValueY = values.get(values.size() - 1);
		} catch (Exception e) {
			throw new RuntimeException("Could not parse y axis", e);
		}
	}

	public LinkedHashMap<LocalDate, Double> parseData(IVolLineType lineType) {
		LinkedHashMap<LocalDate, Double> result = new LinkedHashMap<>();

		LinkedHashMap<LocalDate, Integer> dateXPositions = getDatePositions();

		Set<Map.Entry<LocalDate, Integer>> dateXPositionEntries = dateXPositions.entrySet();
		for (Map.Entry<LocalDate, Integer> dateXPositionEntry : dateXPositionEntries) {
			Integer xPosition = dateXPositionEntry.getValue();

			int rgbColor;
			switch (lineType) {
				case IMPLIED_VOLATILITY:
					rgbColor = IVolConstants.IVOL_RGB_COLOR;
					break;
				case HISTORIC_VOLATILITY:
					rgbColor = IVolConstants.HIST_RGB_COLOR;
					break;
				default:
					throw new IllegalArgumentException("Line type not supported " + lineType);
			}

			double valueFromColor = findValueFromColor(xPosition, rgbColor);
			result.put(dateXPositionEntry.getKey(), valueFromColor);
		}

		return result;
	}

	private double findValueFromColor(int x, int color) {
		List<Integer> foundYs = new ArrayList<>(5);
		for (int y = IVolConstants.START_PX_Y; y < IVolConstants.END_PX_Y; y++) {
			int rgb = chartImage.getRGB(x, y);
			if (color == rgb) {
				foundYs.add(y);
			}
		}

		double averageValuePosition = foundYs.stream().mapToInt(y -> y).average().getAsDouble();
		double relativeValue = 1 - ((averageValuePosition - IVolConstants.START_PX_Y) / (IVolConstants.END_PX_Y - IVolConstants.START_PX_Y));

		return (highestValueY - lowestValueY) * relativeValue + lowestValueY;
	}

	private LinkedHashMap<LocalDate, Integer> getDatePositions() {
		LinkedHashMap<LocalDate, Integer> dateXPositions = new LinkedHashMap<>();

		Set<Map.Entry<Integer, LocalDate>> entries = knownPoints.entrySet();
		Iterator<Map.Entry<Integer, LocalDate>> iterator = entries.iterator();

		Map.Entry<Integer, LocalDate> currentLocalDateEntry;
		Map.Entry<Integer, LocalDate> nextLocalDateEntry = iterator.next();
		while (iterator.hasNext()) {
			currentLocalDateEntry = nextLocalDateEntry;
			nextLocalDateEntry = iterator.next();

			Integer currentDateX = currentLocalDateEntry.getKey();
			Integer nextDateX = nextLocalDateEntry.getKey();
			Integer periodLengthX = nextDateX - currentDateX;

			LocalDate currentLocaleDate = currentLocalDateEntry.getValue();
			LocalDate nextLocaleDate = nextLocalDateEntry.getValue();
			int countTradingDays = TradingDays.countTradingDaysInBetween(currentLocaleDate, nextLocaleDate);

			double avgLengthOfOneTradingDay = (double) periodLengthX / (double) countTradingDays;

			LocalDate date = currentLocaleDate;
			double xPositionOfDate = currentDateX;
			while (date.isBefore(nextLocaleDate)) {
				if (TradingDays.isTradingDay(date)) {
					if (xPositionOfDate > nextDateX) {
						xPositionOfDate = nextDateX;
					}
					dateXPositions.put(date, (int) xPositionOfDate);


					xPositionOfDate = xPositionOfDate + avgLengthOfOneTradingDay;
				}
				date = date.plusDays(1);
			}
		}

		return dateXPositions;
	}


}
