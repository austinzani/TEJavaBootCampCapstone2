package com.techelevator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

import com.techelevator.model.Campground;
import com.techelevator.model.Park;
import com.techelevator.model.Reservation;
import com.techelevator.model.Site;
import com.techelevator.model.jdbc.JDBCCampgroundDAO;
import com.techelevator.model.jdbc.JDBCParkDAO;
import com.techelevator.model.jdbc.JDBCReservationDAO;
import com.techelevator.model.jdbc.JDBCSiteDAO;
import com.techelevator.projects.view.Menu;

public class CampgroundCLI {

	private static final String SELECT_PARK = "Select a park for further details";
	private static final String SELECT_A_COMMAND = "Select a command";
	private static final String VIEW_CAMPGROUNDS = "View campgrounds";
	private static final String RETURN_TO_PREVIOUS_SCREEN = "Return to previous screen";
	private static final String SEARCH_FOR_AVAILABLE_RESERVATIONS = "Search for available reservation";
	private static final String WHICH_CAMPGROUND = "Which campground ?(enter 0 to cancel)";
	private static final String WHAT_IS_THE_ARRIVAL_DATE = "What is the arrival date?(YYYY-MM-DD)";
	private static final String WHAT_IS_THE_DEPARTURE_DATE = "What is the departure date?(YYYY-MM-DD)";

	private static final String[] PARK_SUB_MENU = new String[] { VIEW_CAMPGROUNDS, RETURN_TO_PREVIOUS_SCREEN };
	private static final String[] CAMPGROUND_SUB_MENU = new String[] { SEARCH_FOR_AVAILABLE_RESERVATIONS,
			RETURN_TO_PREVIOUS_SCREEN };
	private static final String[] NUMS_TO_MONTHS = new String[] { "January", "February", "March", "April", "May",
			"June", "July", "August", "Septmember", "October", "November", "December" };

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/YYYY");

	private JDBCSiteDAO site;
	private JDBCParkDAO park;
	private JDBCReservationDAO reservation;
	private JDBCCampgroundDAO campground;
	private Menu menu;
	private Scanner input;

	public static void main(String[] args) {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setUrl("jdbc:postgresql://localhost:5432/campgrounds");
		dataSource.setUsername(System.getenv("DB_USERNAME"));
		dataSource.setPassword(System.getenv("DB_PASSWORD"));
		CampgroundCLI application = new CampgroundCLI(dataSource);
		application.run();
	}

	public CampgroundCLI(DataSource datasource) {
		site = new JDBCSiteDAO(datasource);
		park = new JDBCParkDAO(datasource);
		campground = new JDBCCampgroundDAO(datasource);
		reservation = new JDBCReservationDAO(datasource);
		menu = new Menu(System.in, System.out);
		input = new Scanner(System.in);
	}

	public void run() {
		handleDisplayInitialMenu();
	}

	private void handleDisplayInitialMenu() {
		boolean runApp = true;
		while (runApp) {
			List<Park> allParks = park.getAllParks();
			Object choice = handlePrintOptionsForParks(allParks.toArray());
			if (choice.equals("Q")) {
				runApp = false;
			} else {
				handleParkSubMenu((Park) choice);
			}
		}
	}

	private void printHeading(String headingText) {
		System.out.println("\n" + headingText);
		for (int i = 0; i < headingText.length(); i++) {
			System.out.print("-");
		}
		System.out.println();
	}

	private Object handlePrintOptionsForParks(Object[] object) {
		Object choice = null;
		while (choice == null) {
			printHeading(SELECT_PARK);
			for (int i = 0; i < object.length; i++) {
				System.out.format("%d) %s\n", i + 1, object[i]);

			}
			System.out.println("Q) quit");
			String userInput = input.nextLine();
			try {
				if (userInput.toUpperCase().equals("Q")) {
					choice = userInput.toUpperCase();
				} else if (Integer.parseInt(userInput) > 0 && Integer.parseInt(userInput) <= object.length) {
					choice = object[Integer.parseInt(userInput) - 1];

				}
			} catch (NumberFormatException e) {
				// eat the exception, an error message will be displayed below since the choice
				// will be null.
			}
			if (choice == null) {
				System.out.println("\n*** " + userInput + " is not a valid option ***\n");
			}

		}
		return choice;

	}

	private void handleDisplayParkInfo(Park park) {
		System.out.println("\n" + park.getName());
		System.out.format("%-20s%-20s\n", "Location:", park.getLocation());
		System.out.format("%-20s%-20s\n", "Established:", DATE_FORMATTER.format(park.getEstablishDate()));
		System.out.format("%-20s%-20s\n", "Area:", park.getArea() + " sq km");
		System.out.format("%-20s%-20d\n", "Annual Visitors:", park.getVisitors());
		System.out.println("\n" + park.getDescription());
	}

	private void handleParkSubMenu(Park park) {
		boolean runMenu = true;
		while (runMenu) {
			handleDisplayParkInfo(park);
			System.out.print("\n" + SELECT_A_COMMAND);
			String choice = (String) menu.getChoiceFromOptions(PARK_SUB_MENU);
			if (choice.equals(VIEW_CAMPGROUNDS)) {
				handleDisplayCampgroundInfo(park);
				handleCampgroundSubMenu(park);
			} else if (choice.equals(RETURN_TO_PREVIOUS_SCREEN)) {
				runMenu = false;
			}
		}
	}

	private void handleDisplayCampgroundInfo(Park newPark) {
		int i = 1;
		List<Campground> parkCampgrounds = campground.getAllCampgroundsByParkId(newPark);
		System.out.format("%10s %-35s %-15s %-15s %-15s \n", "", "Name", "Open", "Close", "Daily Fee");
		for (Campground c : parkCampgrounds) {
			System.out.format("%-10s %-35s %-15s %-15s %-15s \n", "#" + i, c.getName(),
					NUMS_TO_MONTHS[Integer.parseInt(c.getOpenFromMM()) - 1],
					NUMS_TO_MONTHS[Integer.parseInt(c.getOpenToMM()) - 1], "$" + c.getDailyFee());
			i++;

		}
	}

	private void handleCampgroundSubMenu(Park park) {
		boolean runMenu = true;
		while (runMenu) {
			System.out.print("\n" + SELECT_A_COMMAND);
			String choice = (String) menu.getChoiceFromOptions(CAMPGROUND_SUB_MENU);
			if (choice.equals(SEARCH_FOR_AVAILABLE_RESERVATIONS)) {
				handleDisplayCampgroundInfo(park);
				handleWhichCampground(park);
			} else if (choice.equals(RETURN_TO_PREVIOUS_SCREEN)) {
				runMenu = false;
			}
		}
	}

	private void handleWhichCampground(Park park) {
		boolean moveOn = false;
		Campground choice = null;
		List<Campground> parkCampgrounds = campground.getAllCampgroundsByParkId(park);
		while (moveOn == false) {
			System.out.print(WHICH_CAMPGROUND);
			String userInput = input.nextLine();
			try {
				int selectedOption = Integer.valueOf(userInput);
				if (selectedOption == 0) {
					moveOn = true;
					// allows you to cancel out and go back to the previous menu.
				} else if (selectedOption > 0 && selectedOption <= parkCampgrounds.size()) {
					choice = parkCampgrounds.get(selectedOption - 1);
					moveOn = true;
					handleInAndOut(choice);
				}
			} catch (NumberFormatException n) {
				// exception will be handled in the line below ..
			}
			if (moveOn == false) {
				System.out.println("\n*** " + userInput + " is not a valid option ***\n");
			}

		}
	}

	private void handleInAndOut(Campground campground) {
		Reservation newReservation = new Reservation();

			boolean canParse = false;
			LocalDate arrival = null;
			LocalDate departure = null;
			while (canParse == false) {
				System.out.print(WHAT_IS_THE_ARRIVAL_DATE);
				String userInput = input.nextLine();
				canParse = canInputParseDate(userInput);
				// Makes sure String can be parsed as a date and then tries to set date
				if (canParse == true) {
					arrival = LocalDate.parse(userInput);
					if (LocalDate.now().isBefore(arrival)) {
						// Makes sure this reservation is not set for a past date
						newReservation.setStartOfRes(arrival);
					} else {
						System.out.println("Cannot set reservation for past date");
						canParse = false;
						// Set to false so we can continue the loop and ask for another date
					}
				}

			}
			canParse = false;
			while (canParse == false) {
				System.out.print(WHAT_IS_THE_DEPARTURE_DATE);
				String userInput = input.nextLine();
				canParse = canInputParseDate(userInput);
				if (canParse == true) {
					departure = LocalDate.parse(userInput);
					if (arrival.isBefore(departure)) {
						// Makes sure that departure is a date after the arrival date
						newReservation.setEndDate(departure);
					} else {
						System.out.println("Cannot set end date before start date");
						canParse = false;
						// Set false to make sure we go back to ask for departure date if it is invalid
					}
				}

			}
			handleDisplaySiteAvailability(newReservation, campground);
		}
	

	private boolean canInputParseDate(String userInput) {
		boolean result = false;
		try {
			LocalDate.parse(userInput);
			result = true;
		} catch (DateTimeParseException e) {
			// exception handled below
		}
		if (result == false) {
			System.out.println("\n*** " + userInput + " is not a valid option ***\n");
		}
		return result;
	}

	private void handleDisplaySiteAvailability(Reservation newReservation, Campground campground) {

		Boolean foundSites = false;
		while (foundSites == false) {
			// Code below finds duration of stay so we can multiply the daily fee for total
			// cost of stay
			int days = (int) ChronoUnit.DAYS.between(newReservation.getStartOfRes(), newReservation.getEndDate());
			BigDecimal duration = new BigDecimal(days);
			List<Site> topSites = site.listTopFiveAvailableByCampgroundId(campground.getId(),
					newReservation.getStartOfRes(), newReservation.getEndDate());

			// Print out the header for displaying available campsites
			System.out.format("%-15s %-15s %-15s %-15s %-15s %-15s \n", "Site No.", "Max Occup.", "Accessible?",
					"Max RV Length", "Utility", "Cost");
			for (Site site : topSites) {
				System.out.format("%-15d %-15d %-15s %-15s %-15s %-15s \n", site.getSiteNumber(),
						site.getMaxOccupancy(), convertBoolToString(site.isAccessible()),
						convertRvLengthToString(site.getMaxRvLength()), convertBoolToString(site.isUtilities()),
						"$" + campground.getDailyFee().multiply(duration));

			}

			// If no available sites are found it will ask if you want to search another
			// range
			if (topSites.size() == 0) {
				System.out.print("Sorry , there are no sites available.\n"
						+ "Would you like to search another date range?(y/n)");
				String userInput = input.nextLine();
				boolean correctlyAnswered = false;
				while (correctlyAnswered == false) {
					// move question and user input here to avoid duplicating asking below
					if (userInput.toLowerCase().equals("y")) {
						// Wrap dates part in while loop so it falls back to it if chosen
						handleInAndOut(campground);
						correctlyAnswered = true;
					} else if (userInput.toLowerCase().equals("n")) {
						correctlyAnswered = true;
						// Go back to main menu;
						handleDisplayInitialMenu();
					} else {
						System.out
								.println("User Input Invalid.\n" + "Would you like to search another date range?(y/n)");
						userInput = input.nextLine();
					}
				}

			} else {
				foundSites = true;
				handleCreateReservation(newReservation, topSites);
			}
		}
	}

	// Allows to convert some booleans from Database for display campsite info
	private String convertBoolToString(boolean bool) {
		String result = "Yes";
		if (bool == false) {
			result = "No";
		}
		return result;
	}

	// Allows us to print N/A when the max length is 0
	private String convertRvLengthToString(int rvLength) {
		String result = "" + rvLength;
		if (rvLength == 0) {
			result = "N/A";

		}
		return result;
	}

	private void handleCreateReservation(Reservation siteReservation, List<Site> sites) {

		boolean haveNoValidAnswer = true;
		Map<Integer, Integer> siteIds = new HashMap<>();
		for (Site s : sites) {
			siteIds.put(s.getSiteNumber(), s.getId());
		}
		while (haveNoValidAnswer) {
			System.out.print("Which site should be reserved? (Enter 0 to cancel)");

			String userInput = input.nextLine();
			try {
				int selectedOption = Integer.valueOf(userInput);
				if (selectedOption == 0) {
					haveNoValidAnswer = false;
				} else if (siteIds.containsKey(selectedOption)) {
					siteReservation.setSiteId(siteIds.get(selectedOption));
					haveNoValidAnswer = false;
					System.out.println("What name would you like the reservation placed under?");
					String reservationName = input.nextLine();
					siteReservation.setName(reservationName);

					Reservation finalReservation = reservation.createReservation(siteReservation);
					System.out.println(
							"The reservation has been made and the confirmation id is " + finalReservation.getId());

				}
			} catch (NumberFormatException n) {
				// exception will be handled in the line below ..
			}
			if (haveNoValidAnswer) {
				System.out.println("\n*** " + userInput + " is not a valid option ***\n");
			}

		}
	}
}