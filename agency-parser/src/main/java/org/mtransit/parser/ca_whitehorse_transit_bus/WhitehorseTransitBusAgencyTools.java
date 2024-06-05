package org.mtransit.parser.ca_whitehorse_transit_bus;

import static org.mtransit.commons.StringUtils.EMPTY;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.mt.data.MAgency;

import java.util.regex.Pattern;

// https://data.whitehorse.ca/
public class WhitehorseTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new WhitehorseTransitBusAgencyTools().start(args);
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "Whitehorse Transit";
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public boolean defaultRouteIdEnabled() {
		return true;
	}

	@Override
	public boolean useRouteShortNameForRouteId() {
		return true;
	}

	private static final Pattern STARTS_WITH_ROUTE_ = Pattern.compile("(^route )", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String provideMissingRouteShortName(@NotNull GRoute gRoute) {
		String routeShortName = gRoute.getRouteLongNameOrDefault();
		if ("CGC Express".equalsIgnoreCase(routeShortName)) {
			return "CGC E";
		} else if ("Riverdale Loop".equalsIgnoreCase(routeShortName)) {
			return "Rvrdl Lp";
		}
		routeShortName = STARTS_WITH_ROUTE_.matcher(routeShortName).replaceAll(EMPTY);
		routeShortName = CleanUtils.cleanBounds(routeShortName);
		return cleanRouteShortName(routeShortName);
	}

	@NotNull
	@Override
	public String getRouteShortName(@NotNull GRoute gRoute) {
		switch (gRoute.getRouteLongNameOrDefault()) { // override default route ID from route short name to avoid route merge
		case "Route 5 Southbound":
			return "5 SB";
		case "Route 5 Northbound":
			return "5 NB";
		}
		return super.getRouteShortName(gRoute);
	}

	@Nullable
	@Override
	public Long convertRouteIdFromShortNameNotSupported(@NotNull String routeShortName) {
		switch (routeShortName) {
		case "2 (July 23)":
			return 2_23_07L;
		case "5 SB":
			return 1_005L;
		case "5 SB (Detour)":
			return 3_005L;
		case "5 NB":
			return 2_005L;
		case "401 (July 23)":
			return 401_23_07L;
		case "CGC E":
			return 100_000L;
		case "Rvrdl Lp":
			return 101_000L;
		}
		return null;
	}

	private static final Pattern STARTS_WITH_R_ = Pattern.compile("(^r )", Pattern.CASE_INSENSITIVE);

	// private static final Pattern ENDS_WITH_MONTH_YEAR_ = Pattern.compile("(_[a-z]+\\d+$)", Pattern.CASE_INSENSITIVE);
	private static final Pattern ENDS_WITH_MONTH_YEAR_ = Pattern.compile("(_july2023$)", Pattern.CASE_INSENSITIVE);

	// private static final String ENDS_WITH_MONTH_YEAR_REPLACEMENT = EMPTY;
	private static final String ENDS_WITH_MONTH_YEAR_REPLACEMENT = " (July 23)";

	private static final String EXPRESS_SHORT = CleanUtils.cleanWordsReplacement("E");

	@NotNull
	@Override
	public String cleanRouteShortName(@NotNull String routeShortName) {
		routeShortName = STARTS_WITH_R_.matcher(routeShortName).replaceAll(EMPTY);
		routeShortName = ENDS_WITH_MONTH_YEAR_.matcher(routeShortName).replaceAll(ENDS_WITH_MONTH_YEAR_REPLACEMENT);
		routeShortName = EXPRESS_.matcher(routeShortName).replaceAll(EXPRESS_SHORT);
		return super.cleanRouteShortName(routeShortName);
	}

	@Override
	public @NotNull String cleanRouteLongName(@NotNull String routeLongName) {
		routeLongName = ENDS_WITH_MONTH_YEAR_.matcher(routeLongName).replaceAll(ENDS_WITH_MONTH_YEAR_REPLACEMENT);
		return super.cleanRouteLongName(routeLongName);
	}

	@Override
	public boolean defaultRouteLongNameEnabled() {
		return true;
	}

	@Override
	public boolean defaultAgencyColorEnabled() {
		return true;
	}

	private static final String AGENCY_COLOR_BLUE = "006666"; // BLUE (from Twitter color)

	private static final String AGENCY_COLOR = AGENCY_COLOR_BLUE;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Nullable
	@Override
	public String provideMissingRouteColor(@NotNull GRoute gRoute) {
		final int rid = (int) getRouteId(gRoute);
		switch (rid) {
		// @formatter:off
		case 1: return "73A3CE";
		case 2: return "79B242";
		case 3: return "D42027";
		case 4: return "80407E";
		case 5: return "EA9025";
		case 6: return "14A79D";
		// @formatter:on
		}
		throw new MTLog.Fatal("Unexpected route color for %s!", gRoute);
	}

	private static final Pattern STARTS_WITH_ROUTE_AND_ = Pattern.compile("(^route .*)$", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanDirectionHeadsign(int directionId, boolean fromStopName, @NotNull String directionHeadSign) {
		directionHeadSign = STARTS_WITH_ROUTE_AND_.matcher(directionHeadSign).replaceAll(EMPTY);
		return super.cleanDirectionHeadsign(directionId, fromStopName, directionHeadSign);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	private static final Pattern EXPRESS_ = CleanUtils.cleanWord("express");

	private static final Pattern COPPER_RIDGE_ = CleanUtils.cleanWord("CopperRidge");
	private static final String COPPER_RIDGE_REPLACEMENT = CleanUtils.cleanWordsReplacement("Copper Ridge");

	private static final Pattern _DASH_ = Pattern.compile(" - ");
	private static final String _DASH_REPLACEMENT = "<>";

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = EXPRESS_.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = COPPER_RIDGE_.matcher(tripHeadsign).replaceAll(COPPER_RIDGE_REPLACEMENT);
		tripHeadsign = _DASH_.matcher(tripHeadsign).replaceAll(_DASH_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanBounds(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = COPPER_RIDGE_.matcher(gStopName).replaceAll(COPPER_RIDGE_REPLACEMENT);
		gStopName = CleanUtils.CLEAN_AND.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		gStopName = CleanUtils.cleanSlashes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}
}
