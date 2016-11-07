package info.androidhive.AliensHideNSeek;

public class Human extends Player {

    private String type;
    private double lat;
    private double lon;

    // human constructor
    Human(String type, int id, String name, String tagline, double playerStartLat, double playerStartLon) {
        super(id, name, tagline, playerStartLat, playerStartLon);
        this.type = type;
    }

}