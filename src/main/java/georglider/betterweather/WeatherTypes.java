package georglider.betterweather;

public enum WeatherTypes {

    CLEAR("CLEAR"),
    RAIN("RAINY"),
    THUNDER("THUNDER"),
    SNOW("SNOWY"); // Experimental

    private final String weather;
    WeatherTypes(String weather){
        this.weather = weather;
    }
    public String getName(){ return weather; }

}
