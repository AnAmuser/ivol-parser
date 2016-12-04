package dk.anamuser.ivolparser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

/**
 * Tries to parse the axis' automatically
 */
public class IVolChartAxisParser {

	private static final int LABEL_HEIGHT = 10;
	private static final String[] MONTH_NAMES = new String[]{"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};
	private static BufferedImage commaImage;

	private BufferedImage chartImage;

	public IVolChartAxisParser(BufferedImage chartImage) {
		this.chartImage = chartImage;
	}

	private static BufferedImage getCommaImage() throws IOException {
		if (commaImage == null) {
			commaImage = ImageIO.read(IVolChartAxisParser.class.getResourceAsStream("/ivolatility/comma.gif"));
		}
		return commaImage;
	}

	public LinkedHashMap<Integer, LocalDate> parseXAxis() throws Exception {
		List<Integer> xs = new ArrayList<>();
		for (int x = IVolConstants.START_PX_X; x <= IVolConstants.END_PX_X; x++) {
			int rgb = chartImage.getRGB(x, IVolConstants.END_PX_Y - 1);
			if (IVolConstants.AXIS_MARK_COLOR == rgb) {
				xs.add(x);
			}
		}

		LinkedHashMap<Integer, LocalDate> result = new LinkedHashMap<>();

		Iterator<Integer> xIterator = xs.iterator();
		Integer x = xIterator.next();
		while (xIterator.hasNext()) {
			Integer nextX = xIterator.next();

			LocalDate localDate = parseXLabel(chartImage, x, nextX);
			result.put(x, localDate);

			x = nextX;
		}
		LocalDate localDate = parseXLabel(chartImage, x, chartImage.getWidth() - 1);
		result.put(x, localDate);

		result.put(IVolConstants.END_PX_X, LocalDate.now());

		return result;
	}

	private static LocalDate parseXLabel(BufferedImage chartImage, Integer x1, Integer x2) throws IOException {
		BufferedImage label = chartImage.getSubimage(
				x1,
				IVolConstants.LABEL_START_Y,
				x2 - x1,
				LABEL_HEIGHT);
		label = ImgUtils.trim(label);

		// Trim things after comma away
		int[] commaCoord = ImgUtils.findSubimage(label, getCommaImage());
		if (commaCoord != null) {
			int commaX = commaCoord[0];
			label = label.getSubimage(
					0,
					0,
					commaX,
					label.getHeight());
			label = ImgUtils.trim(label);
		}

		for (int i = 0; i < MONTH_NAMES.length; i++) {
			String monthName = MONTH_NAMES[i];
			if (isMonth(label, monthName)) {
				LocalDate proposal = LocalDate.now().withMonth(i + 1).withDayOfMonth(1);
				if (proposal.isAfter(LocalDate.now())) {
					proposal = proposal.minusYears(1);
				}
				return proposal;
			}
		}

		throw new RuntimeException("Could not parse x axis from " + x1 + ", " + x2);
	}

	private static boolean isMonth(BufferedImage label, String month) throws IOException {
		InputStream inputStream = IVolChartAxisParser.class.getResourceAsStream("/ivolatility/label_x_" + month + ".gif");
		return isApproximatelyEqual(label, inputStream);
	}

	public LinkedHashMap<Integer, Double> parseYAxis() throws Exception {
		List<Integer> ys = new ArrayList<>();
		for (int y = IVolConstants.START_PX_Y; y <= IVolConstants.END_PX_Y; y++) {
			int rgb = chartImage.getRGB(IVolConstants.END_PX_X, y);
			if (IVolConstants.AXIS_MARK_COLOR == rgb) {
				ys.add(y);
			}
		}

		LinkedHashMap<Integer, Double> temp = new LinkedHashMap<>();

		for (Integer nextY : ys) {
			double value = parseYLabel(chartImage, nextY);
			if (value >= 0) {
				temp.put(nextY, value);
			}
		}

		// The first never has a value. Calculate from diff

		Collection<Double> values = temp.values();
		Iterator<Double> iterator = values.iterator();
		Double diff = 0d;
		Double value = iterator.next();
		while (iterator.hasNext()) {
			Double nextValue = iterator.next();
			double newDiff = value - nextValue;
			if (diff > 0 && newDiff - diff > 0.01) {
				throw new RuntimeException("Inconsistent diff on y axis");
			}
			diff = newDiff;
			value = nextValue;
		}

		Optional<Double> first = temp.values().stream().findFirst();
		temp.put(1, first.get() + diff);

		LinkedHashMap<Integer, Double> result = new LinkedHashMap<>();

		temp.entrySet().stream()
				.sorted((o1, o2) -> o1.getKey() - o2.getKey())
				.forEach(integerDoubleEntry -> result.put(integerDoubleEntry.getKey(), integerDoubleEntry.getValue()));

		return result;
	}

	private double parseYLabel(BufferedImage chartImage, Integer y1) throws IOException {
		if (y1 - LABEL_HEIGHT > 0) {
			BufferedImage label = chartImage.getSubimage(
					IVolConstants.LABEL_START_X,
					y1 - LABEL_HEIGHT,
					chartImage.getWidth() - 1 - IVolConstants.LABEL_START_X,
					LABEL_HEIGHT);

			label = ImgUtils.trim(label);

			String numberString = "";

			List<BufferedImage> chars = splitLabel(label);
			// last char is percent
			for (int i = 0; i < chars.size() - 1; i++) {
				BufferedImage aChar = chars.get(i);
				for (int c = 0; c < 10; c++) {
					if (isNumber(aChar, c)) {
						numberString += String.valueOf(c);
						break;
					} else if (isDot(aChar)) {
						numberString += ".";
						break;
					}
				}
			}

			try {
				return Double.parseDouble(numberString);
			} catch (NumberFormatException e) {
				throw new RuntimeException("Could not parse " + numberString, e);
			}
		}
		return -1d;
	}

	private static boolean isNumber(BufferedImage label, int number) throws IOException {
		InputStream inputStream = IVolChartAxisParser.class.getResourceAsStream("/ivolatility/label_y_" + number + ".gif");
		return isApproximatelyEqual(label, inputStream);
	}

	private static boolean isDot(BufferedImage label) throws IOException {
		InputStream inputStream = IVolChartAxisParser.class.getResourceAsStream("/ivolatility/label_y_dot.gif");
		return isApproximatelyEqual(label, inputStream);
	}

	private static boolean isApproximatelyEqual(BufferedImage label, InputStream inputStream) throws IOException {
		BufferedImage image = ImageIO.read(inputStream);
		return ImgUtils.imagesEqual(label, image);
	}

	private static List<BufferedImage> splitLabel(BufferedImage label) {
		List<BufferedImage> result = new ArrayList<>();

		int x = 0;
		int lastWhite = -1;
		while (x < label.getWidth()) {
			BufferedImage maybeASplit = label.getSubimage(x, 0, 1, label.getHeight());
			if (ImgUtils.isAllWhite(maybeASplit) || x - lastWhite > 5) {
				if (x - lastWhite > 1) {
					int x0 = lastWhite + 1;
					result.add(label.getSubimage(x0, 0, x - x0, label.getHeight()));
				}
				lastWhite = x;
			}
			x++;
		}

		return result;
	}


}
