package devilseye.android.meetingscheduler;

public class Participant {
    /***
     * FIO of the meeting participant
     */
    public static final String FIO = "fio";
    private String fio;
    /***
     * Position of the meeting participant
     */
    public static final String POSITION = "position";
    private String position;
    public static final String LOGIN = "login";
    private String login;
    public static final String PASSWORD = "password";
    private String password;

    public Participant(String fio, String position, String login) {
        setFio(fio);
        setPosition(position);
    }

    public Participant(String fio, String position) {
        setFio(fio);

        setPosition(position);
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Override
    public String toString() {
        return "{" +
                "\"fio\":" + "\""+fio + "\"" +
                ", \"position\":" + "\"" + position +"\""+
                "}";
    }

}
