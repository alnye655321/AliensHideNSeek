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

    //get distance between stationary human and alien(s), should be
    //running frequently in a game event loop. !!! In km !!!
    public double calcDistance(double humanLat, double humanLon, double alienLat, double alienLon) {

        double d2r = Math.PI / 180;
        double distance = 0;


        double dlong = (alienLon - humanLon) * d2r;
        double dlat = (alienLat - humanLat) * d2r;
        double a =
                Math.pow(Math.sin(dlat / 2.0), 2)
                        + Math.cos(humanLat * d2r)
                        * Math.cos(alienLat * d2r)
                        * Math.pow(Math.sin(dlong / 2.0), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = 6367 * c;
        System.out.println(d);
        return d;
    }

    //game over check if alien gets close enough (on top of) human --> game over
    //should change this to meters, along with above distance func. !!! Currently .01 km !!!
    public int gameWinnerCheck(int humanPlayerId, int alienPlayerId, double humanLat, double humanLon, double alienLat, double alienLon) {
        int winner;
        if(calcDistance(humanLat, humanLon, alienLat, alienLon) < .015 ){
            winner = alienPlayerId;
            return winner;
        }
        else{
            return -1;
        }
    }

}