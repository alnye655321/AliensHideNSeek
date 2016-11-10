package info.androidhive.AliensHideNSeek;

public class Alien extends Player {
    private String type;
    private double lat;
    private double lon;

    // alien constructor
    Alien(String type, String name, String tagline, double playerStartLat, double playerStartLon, double lat, double lon) {
        super(name, tagline, playerStartLat, playerStartLon, lat, lon);
        this.type = type;
    }
}