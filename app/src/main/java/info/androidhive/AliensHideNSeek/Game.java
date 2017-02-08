package info.androidhive.AliensHideNSeek;

//import java.time.Instant;
//import java.time.temporal.Temporal;
//import java.time.temporal.ChronoUnit;

public class Game {

    //Instant startTimeFlag;
    //Instant currentTimeFlag;
    private int id;
    private Boolean active = true;
    private int players;
    private int timeLimit;


    // game state constructor
    Game(int timeLimit, int players) {
        this.timeLimit = timeLimit;
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

    public int getGameId() {
        return this.id;
    }

    public void setGameId(int newId) {
        this.id = newId;
    }

}