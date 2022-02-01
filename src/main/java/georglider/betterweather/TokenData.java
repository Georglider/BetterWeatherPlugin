package georglider.betterweather;

import java.util.UUID;

public class TokenData {

    private UUID id;
    private int cityId;
    private String cityName;
    private String country;
    private String state;
    private WeatherTypes weather;


    public TokenData(UUID id, int cityId, String cityName, String country, String state) {
        this.id = id;
        this.cityId = cityId;
        this.cityName = cityName;
        this.country = country;
        this.state = state;
    }

    public String getCityName() {
        return cityName;
    }

    public String getCountry() {
        return country;
    }

    public String getState() {
        return state;
    }

    public WeatherTypes getWeather() {
        return weather;
    }

    public void setWeather(WeatherTypes weather) {
        this.weather = weather;
    }
}
