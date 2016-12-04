package dk.anamuser.ivolparser;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

/**
 * Trading days api
 */
public class TradingDays {

	private static final List<LocalDate> TRADING_HOLIDAYS = new LinkedList<LocalDate>() {{
		add(LocalDate.of(2015, 1, 1)); // New year
		add(LocalDate.of(2015, 1, 19)); // Martin Luther
		add(LocalDate.of(2015, 2, 16)); // Presidents day
		add(LocalDate.of(2015, 4, 3)); // Good Friday
		add(LocalDate.of(2015, 5, 25)); // Memorial Day
		add(LocalDate.of(2015, 7, 3)); // Friday
		add(LocalDate.of(2015, 7, 4)); // Independence Day
		add(LocalDate.of(2015, 9, 7)); // Labor Day
		add(LocalDate.of(2015, 11, 26));  // Thanksgiving Day
		add(LocalDate.of(2015, 12, 25));  // Christmas Day
		add(LocalDate.of(2016, 1, 1)); // New year
		add(LocalDate.of(2016, 1, 18)); // Martin Luther
		add(LocalDate.of(2016, 2, 15)); // Presidents day
		add(LocalDate.of(2016, 3, 25)); // Good Friday
		add(LocalDate.of(2016, 5, 30)); // Memorial Day
		add(LocalDate.of(2016, 7, 4)); // Independence Day
		add(LocalDate.of(2016, 9, 5)); // Labor Day
		add(LocalDate.of(2016, 11, 24)); // Thanksgiving Day
		add(LocalDate.of(2016, 12, 26)); // Christmas Day
		add(LocalDate.of(2017, 1, 1)); // New year
		add(LocalDate.of(2017, 1, 16)); // Martin Luther
		add(LocalDate.of(2017, 2, 20)); // Presidents day
		add(LocalDate.of(2017, 4, 14)); // Good Friday
		add(LocalDate.of(2017, 5, 29)); // Memorial Day
		add(LocalDate.of(2017, 7, 4)); // Independence Day
		add(LocalDate.of(2017, 9, 4)); // Labor Day
		add(LocalDate.of(2017, 11, 23)); // Thanksgiving Day
		add(LocalDate.of(2017, 12, 25)); // Christmas Day
	}};

	public static int countTradingDaysInBetween(LocalDate currentLocaleDate, LocalDate nextLocaleDate) {
		int count = 0;
		LocalDate date = currentLocaleDate;
		while (date.isBefore(nextLocaleDate)) {
			if (TradingDays.isTradingDay(date)) {
				count++;
			}
			date = date.plusDays(1);
		}
		return count;
	}

	public static boolean isTradingDay(LocalDate date) {
		return !TRADING_HOLIDAYS.contains(date) && !isWeekend(date);
	}

	private static boolean isWeekend(LocalDate date) {
		return date.getDayOfWeek().equals(DayOfWeek.SATURDAY) || date.getDayOfWeek().equals(DayOfWeek.SUNDAY);
	}

	public static boolean isThirdFridayInMonth(LocalDate localDate) {
		int dayOfMonth = localDate.getDayOfMonth();
		boolean isFriday = isFriday(localDate);
		return isFriday && dayOfMonth >= 15 && dayOfMonth <= 21;
	}

	public static LocalDate nextFriday(LocalDate localDate) {
		LocalDate current = LocalDate.now().plusDays(1);
		while (!isFriday(current)) {
			current = current.plusDays(1);
		}
		return current;
	}

	private static boolean isFriday(LocalDate localDate) {
		return localDate.getDayOfWeek().equals(DayOfWeek.FRIDAY);
	}
}
