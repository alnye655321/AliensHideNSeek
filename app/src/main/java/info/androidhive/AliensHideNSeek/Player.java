package info.androidhive.AliensHideNSeek;

public class Player {
    private int id;
    private String name;
    private String tagline;
    private double playerStartLat;
    private double playerStartLon;
    private double lat;
    private double lon;
    private String checkStart;
    private int gameId;

    // player constructor
    Player(String name, String tagline, double playerStartLat, double playerStartLon, double lat, double lon ) {
        this.name = name;
        this.tagline = tagline;
        this.playerStartLat = playerStartLat;
        this.playerStartLon = playerStartLon;
        this.lat = lat;
        this.lon = lon;
        this.checkStart = "true";
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

    public double getLon() {
        return this.lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setCheckStart(String val) {
        this.checkStart = val;
    }

    public String getCheckStart() {
        return this.checkStart;
    }

    public int getGameId() {
        return this.gameId;
    }

    public void setGameId(int id) {
        this.gameId = id;
    }

    public int getPlayerId() {
        return this.id;
    }

    public void setPlayerId(int id) {
        this.id = id;
    }

    //method to run an update on location -->  add to game event loop
    //should be grabbing android sensor values here
    //possibly a put request to api server as well

}