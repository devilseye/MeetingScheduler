package devilseye.android.meetingscheduler;

import java.util.ArrayList;

public class Meeting {
    private static int autoID = 0;
    public static final String ID = "id";

    public enum Priority {
        URGENT,
        PLANNED,
        OPTIONAL
    }

    private int id;
    public static final String NAME = "name";
    private String name;
    public static final String DESCRIPTION = "description";
    private String description;
    public static final String STARTDATE = "startDate";
    private String startDate;
    public static final String ENDDATE = "endDate";
    private String endDate;
    public static final String PRIORITY = "priority";
    private Priority priority = Priority.PLANNED;
    public static final String PARTICIPANTS = "participants";
    private ArrayList<Participant> participantsList = new ArrayList<>();



    public Meeting(String name, String startDate, String endDate) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.id = ++autoID;
    }


    public Meeting(String name, String description, String startDate, String endDate, String priority){
        this(name, startDate, endDate);
        this.description = description;
        setPriority(priority);
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        Priority p = this.getPriority();
        switch (priority) {
            case "URGENT": {
                p = Meeting.Priority.URGENT;
            }
            break;
            case "OPTIONAL": {
                p = Meeting.Priority.OPTIONAL;
            }
            break;
            case "PLANNED": {
                p = Meeting.Priority.PLANNED;
            }
            break;
        }
        this.priority = p;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public ArrayList<Participant> getParticipantsList() {
        return participantsList;
    }

    public void setParticipantsList(ArrayList<Participant> participantsList) {
        this.participantsList = participantsList;
    }

    public void addParticipant(Participant participant) {
        participantsList.add(participant);
    }

    /***
     * Remove participant from the set
     *
     * @param participant Participant to remove
     * @return true if the participant was in the list and removed
     * false - otherwise
     */
    public boolean deleteParticipant(Participant participant) {
        return participantsList.remove(participant);
    }

    public int getID() {
        return id;
    }

    @Override
    public String toString() {
        return "{" + "\"id\":\"" + id + "\", " +
                "\"name\":" + "\"" + name + "\"" +
                ", \"startDate\":" + "\"" + startDate + "\"" +
                ", \"endDate\":" + "\"" + endDate + "\"" +
                ", \"priority\":\"" + priority.toString() + "\"}\n\n";
    }


    public String getDetails() {
        StringBuilder details = new StringBuilder("[{" + "\""+DESCRIPTION+"\":\"" + description + "\"");
        System.out.println("Get details + id:" + id);
        System.out.println("ParticipantList:" + participantsList.toString());
        System.out.println("ParticipantList size:" + participantsList.size());
        if (participantsList.size() > 0) {
            System.out.println("> 0:" + participantsList.size());
            details.append(", \""+PARTICIPANTS+"\": [");
            for (Participant participant : participantsList) {
                details.append(participant.toString() + ",");
                System.out.println("participant.toString():" + participant.toString());
            }
            details.deleteCharAt(details.length() - 1);
            details.append("]");
        }
        details.append("}]");
        return details.toString();
    }
}
