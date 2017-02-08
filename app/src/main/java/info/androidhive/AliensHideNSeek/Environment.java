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

    // figures out where to plot gps markers on the animated motion tracker bitmap
    public double[] plotGPSpoint(double humanLat, double humanLon, double alienLat, double alienLon, int bitmapRadiusX, int bitmapRadiusY){
        double[] xyPair = new double[2];

        double latDiff = Math.abs(humanLat - alienLat);
        double lonDiff = Math.abs(humanLon - alienLon);

        double lonPercent = lonDiff / 0.0009; // .0009 is about a 100m distance in degrees, will need to alter with a function if close to poles
        double latPercent = latDiff / 0.0009;

        double xVal = lonPercent * bitmapRadiusX;
        double yVal = latPercent * bitmapRadiusY;

        if (alienLon > humanLon){
            xyPair[0] = bitmapRadiusX + xVal;
        }
        else {
            xyPair[0] = bitmapRadiusX - xVal;
        }

        if (alienLat > humanLat){
            xyPair[1] = bitmapRadiusY + yVal;
        }
        else {
            xyPair[1] = bitmapRadiusY - yVal;
        }

        return xyPair; // format --> x[0], y[1]
    }

}
