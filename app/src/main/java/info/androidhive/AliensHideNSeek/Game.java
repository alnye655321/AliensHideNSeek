package info.androidhive.AliensHideNSeek;

//import java.time.Instant;
//import java.time.temporal.Temporal;
//import java.time.temporal.ChronoUnit;

public class Game {

    //Instant startTimeFlag;
    //Instant currentTimeFlag;
    public int id;
    public Boolean active = true;
    public String name;
    public int players;
    public int timeLimit;


    // game state constructor
    Game(int id, int timeLimit, String name, int players) {
        this.id = id;
        this.timeLimit = timeLimit;
        this.name = name;
        this.players = players;
        //this.startTimeFlag = Instant.now();
    }

    //game duration in ms --> should check this and compare with a game time limit
//    public long gameDuration() {
//        currentTimeFlag = Instant.now();
//        return ChronoUnit.MILLIS.between(this.startTimeFlag,currentTimeFlag);
//    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

}