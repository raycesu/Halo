package app;

import data_access.CsvLocationDataAccessObject;
import data_access.CsvStarCatalogDataAccessObject;
import data_access.CsvStaticConstellationDataAccessObject;
import data_access.InMemoryConstellationDataAccessObject;
import data_access.OpenMeteoWeatherDataAccessObject;
import data_access.UsnoCelestialBodyDataAccessObject;
import use_case.custom_constellation.ConstellationDataAccessInterface;
import use_case.lookup_location.LocationDataAccessInterface;
import use_case.view_sky.CelestialBodyDataAccessInterface;
import use_case.view_sky.StarCatalogDataAccessInterface;
import use_case.view_sky.StaticConstellationDataAccessInterface;
import use_case.weather.WeatherDataAccessInterface;

/**
 * Groups every concrete data-access implementation the app needs, constructed once here so
 * later wiring code depends on this one bundle instead of on each concrete DAO individually.
 */
final class DataAccessBundle {

    private final LocationDataAccessInterface locationDataAccess = new CsvLocationDataAccessObject();
    private final StarCatalogDataAccessInterface starCatalogDataAccess = new CsvStarCatalogDataAccessObject();
    private final StaticConstellationDataAccessInterface staticConstellationDataAccess =
            new CsvStaticConstellationDataAccessObject();
    private final CelestialBodyDataAccessInterface celestialBodyDataAccess =
            new UsnoCelestialBodyDataAccessObject();
    private final WeatherDataAccessInterface weatherDataAccess = new OpenMeteoWeatherDataAccessObject();
    private final ConstellationDataAccessInterface constellationDataAccess =
            new InMemoryConstellationDataAccessObject();

    LocationDataAccessInterface getLocationDataAccess() {
        return locationDataAccess;
    }

    StarCatalogDataAccessInterface getStarCatalogDataAccess() {
        return starCatalogDataAccess;
    }

    StaticConstellationDataAccessInterface getStaticConstellationDataAccess() {
        return staticConstellationDataAccess;
    }

    CelestialBodyDataAccessInterface getCelestialBodyDataAccess() {
        return celestialBodyDataAccess;
    }

    WeatherDataAccessInterface getWeatherDataAccess() {
        return weatherDataAccess;
    }

    ConstellationDataAccessInterface getConstellationDataAccess() {
        return constellationDataAccess;
    }
}
