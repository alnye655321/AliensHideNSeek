package info.androidhive.AliensHideNSeek;

public class Environment extends Game {

    private String type;
    //we will need a rotating cartesian plane that encompasses these boundaries
    private static double maxDenverLat = 39.791087;
    private static double minDenverLat = 39.653177;
    private static double maxDenverLon = -104.825191;
    private static double minDenverLon = -105.053244;


    // environment constructor
    Environment(String type, int timeLimit, int players) {
        super(timeLimit, players);
        this.type = type; // Default, Human, Alien
    }

    public String getType() {
        return this.type;
    }

    //get distance between stationary human and alien(s), should be
    //running frequently in a game event loop. !!! In km !!!
    public double getDistance(double humanLat, double humanLon, double alienLat, double alienLon) {

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
        if(getDistance(humanLat, humanLon, alienLat, alienLon) < .015 ){
            winner = alienPlayerId;
            return winner;
        }
        else{
            return -1;
        }
    }

}
