package info.androidhive.AliensHideNSeek;

public class Player {
    private int id;
    private String name;
    private String tagline;
    private double playerStartLat;
    private double playerStartLon;
    private double lat;
    private double lon;

    // player constructor
    Player(int id, String name, String tagline, double playerStartLat, double playerStartLon, double lat, double lon ) {
        this.id = id;
        this.name = name;
        this.tagline = tagline;
        this.playerStartLat = playerStartLat;
        this.playerStartLon = playerStartLon;
        this.lat = lat;
        this.lon = lon;
    }

    public String getName() {
        return this.name;
    }

    public int getId() {
        return this.id;
    }

    public String getTagline() {
        return this.tagline;
    }

    public double getLat() {
        return this.lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    //method to run an update on location -->  add to game event loop
    //should be grabbing android sensor values here
    //possibly a put request to api server as well

}