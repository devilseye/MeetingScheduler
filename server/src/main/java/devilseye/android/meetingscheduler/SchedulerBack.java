package devilseye.android.meetingscheduler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.*;
import javax.ejb.Stateless;



@Stateless
@Path("/")
public class SchedulerBack {
    private static final String TAG = "SchedulerBack";
    @Context
    private ServletContext context;
    @Context
    private HttpServletRequest request;
    @Context
    private HttpServletResponse response;

    public static ArrayList<Meeting> meetings = new ArrayList<>();

    private static final String LOGIN = "login";
    private static final String PASSWORD = "password";
    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String RESULT_OK = "[{\"result\":\"OK\"}]";
    private static final String RESULT_ERROR = "[{\"result\":\"Error\"}]";
    private static final String EMPTY_LIST = "[]";

    @GET
    @Produces(MediaType.TEXT_HTML + "; charset=UTF-8")
    public String getWelcomePage() {
        return "<html>" +
                "<head>\n" +
                "<script type=\"text/javascript\" language=\"JavaScript\">" +
                "if (top.location != self.location) {" +
                "top.location.href = self.location.href;} </script>" +
                " <meta http-equiv=\"CONTENT-TYPE\" content=\"text/html; charset=UTF-8\"/>\n" +
                " <title>Meetings Server</title>\n" +
                "</head>\n" +
                "<style type=\"text/css\">\n" +
                "   .header {\n" +
                "    background-color: rgba(236, 236, 236, 0.7);\n" +
                "    margin:0px 200px;\n" +
                "    padding: 10px 10px 10px 10px;\n" +
                "    color: #000;\n" +
                "    border-radius:6px; \n" +
                "      -webkit-border-radius:6px; \n" +
                "      -moz-border-radius:5px; \n" +
                "      -khtml-border-radius:10px;\n" +
                "   }\n" +
                "   .bodytext {\n" +
                "    background-color: rgba(255, 255, 255, 1);\n" +
                "    margin:0px 200px;\n" +
                "    padding: 10px 10px 10px 10px;\n" +
                "    color: #000;\n" +
                "    border-radius:6px; \n" +
                "      -webkit-border-radius:6px; \n" +
                "      -moz-border-radius:5px; \n" +
                "      -khtml-border-radius:10px;\n" +
                "   }\n" +
                "   .navandfoot {\n" +
                "   background-color: rgba(236, 236, 236, 0.7);\n" +
                "   margin:0px 200px;\n" +
                "   padding: 10px 10px 10px 10px;\n" +
                "   text-decoration: none;\n" +
                "   border-radius:6px; \n" +
                "   color: #000;\n" +
                "   }\n" +
                "</style>\n"+
                "<body bgcolor=\"#696969\">" +
                "<form action=\"createMeeting\" method=\"GET\">" +
                "<div class=\"header\"><h1 align=\"center\">Meeting Server</h1></div><br>\n" +
                "<div class=\"header\"><h2 align=\"center\">New meeting:</h2></div><br>\n"+
                "<div class=\"bodytext\"><p>"+
                "<table>\n" +
                "    <tr>\n" +
                "      <td>Name</td>\n" +
                "      <td><input type=\"text\" id=\"name\" required placeholder=\"Input name\" text=\"Example meeting\" name=\"name\" size=\"80\"/></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td>Description</td>\n" +
                "      <td><input type=\"text\" id=\"description\" required placeholder=\"Input description\" name=\"description\"size=\"80\"/></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td class=\"insert\">Start date</td>\n" +
                "      <td><input type=\"text\" id=\"startDate\" name=\"startDate\" size=\"80\" required placeholder=\"Input start date (yyyy-mm-dd HH:mm)\" pattern=\"^(19|20)\\d\\d-((0((1-(0[1-9]|[12][0-9]|3[01]) ([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$)|(2-(0[1-9]|1[0-9]|2[0-8]) ([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$)|([3-9]-(0[1-9]|[12][0-9]|3[01]) ([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$)))|(1[012]-(0[1-9]|[12][0-9]|3[01]) ([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$))$\" /></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td class=\"insert\">Finish date</td>\n" +
                "      <td><input type=\"text\" id=\"endDate\" name=\"endDate\" size=\"80\" required placeholder=\"Input finish date (yyyy-mm-dd HH:mm)\" pattern=\"^(19|20)\\d\\d-((0((1-(0[1-9]|[12][0-9]|3[01]) ([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$)|(2-(0[1-9]|1[0-9]|2[0-8]) ([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$)|([3-9]-(0[1-9]|[12][0-9]|3[01]) ([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$)))|(1[012]-(0[1-9]|[12][0-9]|3[01]) ([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$))$\" /></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td class=\"insert\">Priority</td>\n" +
                "      <td><input type=\"text\" id=\"priority\" name=\"priority\" size=\"80\" required placeholder=\"Input priority (URGENT, PLANNED, OPTIONAL)\"></td>\n" +
                "   </tr>\n" +
                "  </table>\n" +
                "<input type=\"reset\" value=\"CLEAR\" name=\"clear\"/>" +
                "&nbsp;&nbsp;\n" +
                "<input type=\"submit\" value=\"SAVE\" name=\"submit\"/>" +
                "&nbsp;&nbsp;</form>" +
                "<form action=\"createTestSet\" method=\"GET\">" +
                "<input type=\"submit\" value=\"TEST SET\" name=\"submit\"/>" +
                "&nbsp;&nbsp;</form>" +
                "<form action=\"getMeetingsInfo\" method=\"GET\">" +
                "<input type=\"submit\" value=\"GET MEETINGS INFO\" name=\"submit\"/>" +
                "&nbsp;&nbsp;</form>"+
                "</p></div>"+
                "</body></html>";
    }

    @GET
    @Path("/createMeeting")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public String createMeeting(@QueryParam(Meeting.NAME) String name,
                                @QueryParam(Meeting.DESCRIPTION) String description,
                                @QueryParam(Meeting.STARTDATE) String startdate,
                                @QueryParam(Meeting.ENDDATE) String enddate,
                                @QueryParam(Meeting.PRIORITY) String priority
    ) {
        addMeetingToList(name, description, startdate, enddate, priority);
        return meetings.toString();
    }

    @GET
    @Path("/createTestSet")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public String createTestSet() {
        final Meeting meeting1 = new Meeting("Test meeting",
                "Test meeting 1",
                "2016-01-16 22:00",
                "2016-01-16 23:00",
                "URGENT");
        final Meeting meeting2 = new Meeting("Studying",
                "Develop this lab!",
                "2016-01-16 21:00",
                "2016-01-16 22:00",
                "PLANNED");
        final Meeting meeting3 = new Meeting("Test meeting",
                "Test meeting 2",
                "2016-01-16 19:00",
                "2016-01-16 20:00",
                "OPTIONAL");
        meetings.add(meeting1);
        meetings.add(meeting2);
        meetings.add(meeting3);
        return meetings.toString();
    }

    @GET
    @Path("/getMeetingsInfo")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public String getMeetingsInfo() {
        return meetings.toString();
    }

    /***
     * Add meeting to the list of meetings. Params are characteristics of new meeting
     */

    private void addMeetingToList(String name,
                                  String description,
                                  String startdate,
                                  String enddate,
                                  String priority) {
        try {
            name = URLDecoder.decode(name, "UTF-8");
            description = URLDecoder.decode(description, "UTF-8");
            startdate = URLDecoder.decode(startdate, "UTF-8");
            enddate = URLDecoder.decode(enddate, "UTF-8");
            priority = URLDecoder.decode(priority, "UTF-8");
            if (priority.endsWith("\r\n")) {
                priority = priority.substring(0, priority.length() - 2);
            }
            Meeting meeting = new Meeting(name, description, startdate, enddate, priority);
            meetings.add(meeting);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void addMeetingToList(ArrayList<Meeting> meetings) {
        for (Meeting m : meetings) {
            this.meetings.add(m);
        }
    }

    //1. Get today meetings
    @GET
    @Path("/getTodayMeetings")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public String getTodayMeetings(@QueryParam(Participant.LOGIN) String login,
                                   @QueryParam(Participant.PASSWORD) String password) {
        if (LOGIN.equals(login) && PASSWORD.equals(password)) {
            ArrayList<Meeting> todayMeetings = getTodayMeetings();
            if (todayMeetings != null)
                return todayMeetings.toString();
            else
                return EMPTY_LIST;
        } else
            return RESULT_ERROR;
    }

    private ArrayList<Meeting> getTodayMeetings() {
        final int TIME_LENGTH = 6; //" HH:mm"
        Date currentDateTime = new Date();
        SimpleDateFormat formatDate = new SimpleDateFormat(DATE_FORMAT);
        SimpleDateFormat formatDateTime = new SimpleDateFormat(DATETIME_FORMAT);
        try {
            String todayDate = formatDate.format(currentDateTime);
            ArrayList<Meeting> todayMeetings = new ArrayList<>();
            for (Meeting m : meetings) {
                String startDate = m.getStartDate();
                startDate = startDate.substring(0, startDate.length() - TIME_LENGTH);
                if (startDate.equals(todayDate)) {
                    Date endDate = formatDateTime.parse(m.getEndDate());
                    int compare = currentDateTime.compareTo(endDate);
                    System.out.println(compare);
                    if (compare == -1 || compare == 0) {
                        todayMeetings.add(m);
                    }
                }
            }
            return todayMeetings;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    //2. Add participant to meeting
    @PUT
    @Path("/addParticipant/{"
            + Participant.LOGIN + "}/{"
            + Participant.PASSWORD + "}/{"
            + Meeting.ID + "}/{"
            + Participant.FIO + "}/{"
            + Participant.POSITION + "}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED + ";charset=UTF-8")
    public String addParticipant(@PathParam(Participant.LOGIN) String login,
                                 @PathParam(Participant.PASSWORD) String password,
                                 @PathParam(Meeting.ID) String id,
                                 @PathParam(Participant.FIO) String fio,
                                 @PathParam(Participant.POSITION) String position) {
        System.out.println("Add participant");
        if (LOGIN.equals(login) && PASSWORD.equals(password)) {
            try {
                fio = URLDecoder.decode(fio, "UTF-8");
                System.out.println("fio" + fio);
                position = URLDecoder.decode(position, "UTF-8");
                System.out.println("position" + position);
                Participant participant = new Participant(fio, position);
                System.out.println("participant" + participant.toString());
                Meeting m = findMeetingById(Integer.parseInt(id));
                if (m != null) {
                    meetings.remove(m);
                    System.out.println("meeting id" + m.getID());
                    System.out.println("m.getParticipantList" + m.getParticipantsList().toString());
                    ArrayList<Participant> participants = m.getParticipantsList();
                    System.out.println("add " + m.getParticipantsList().toString());
                    boolean res = participants.add(participant);
                    System.out.println("added? " + res);
                    m.setParticipantsList(participants);
                    System.out.println("setList " + m.getParticipantsList().toString());
                    meetings.add(m);
                    System.out.println("meetingsToString " + meetings.toString());
                }
                return RESULT_OK;
            } catch (UnsupportedEncodingException uee) {
                uee.printStackTrace();
                return RESULT_ERROR;
            }
        } else
            return RESULT_ERROR;

    }

    private Meeting findMeetingById(int id) {
        Meeting meeting = null;
        for (Meeting m : meetings) {
            if (m.getID() == id) {
                meeting = m;
            }
        }
        return meeting;
    }

    //3. Request detailed meeting info
    @GET
    @Path("/getDescription")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public String getDescription(@QueryParam(Participant.LOGIN) String login,
                                 @QueryParam(Participant.PASSWORD) String password,
                                 @QueryParam(Meeting.ID) String id) {
        String description;
        System.out.println("Get description");
        if (LOGIN.equals(login) && PASSWORD.equals(password)) {
            try {
                System.out.println("Find id");
                Meeting meeting = findMeetingById(Integer.parseInt(id));
                if (meeting != null) {
                    System.out.println("Found");
                    description = meeting.getDetails();
                } else {
                    description = EMPTY_LIST;
                    System.out.println("Not Found");
                }
                return description;
            } catch (Exception e) {
                e.printStackTrace();
                return RESULT_ERROR;
            }
        } else {
            return RESULT_ERROR;
        }
    }

    //4. Cancel meeting
    @DELETE
    @Path("/cancelMeeting")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public String cancelMeeting() {
        int id = Integer.parseInt((request.getHeader(Meeting.ID)));
        String result = RESULT_ERROR;
        try {
            if (LOGIN.equals(request.getHeader(Participant.LOGIN)) && PASSWORD.equals(request.getHeader(Participant.PASSWORD))) {
                Meeting m = findMeetingById(id);
                if (m != null) {
                    meetings.remove(m);
                    result = RESULT_OK;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    //5.Create meeting: client option
    @POST
    @Path("/androidCreateMeeting/{"
            + Participant.LOGIN + "}/{"
            + Participant.PASSWORD + "}/{"
            + Meeting.NAME + "}/{"
            + Meeting.DESCRIPTION + "}/{"
            + Meeting.STARTDATE + "}/{"
            + Meeting.ENDDATE + "}/{"
            + Meeting.PRIORITY + "}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public String androidCreateMeeting(@PathParam(Meeting.NAME) String name,
                                       @PathParam(Meeting.DESCRIPTION) String description,
                                       @PathParam(Meeting.STARTDATE) String startdate,
                                       @PathParam(Meeting.ENDDATE) String enddate,
                                       @PathParam(Meeting.PRIORITY) String priority,
                                       @PathParam(Participant.LOGIN) String login,
                                       @PathParam(Participant.PASSWORD) String password) {
        if (LOGIN.equals(login) && PASSWORD.equals(password)) {
            addMeetingToList(name, description, startdate, enddate, priority);
            return RESULT_OK;
        } else {
            return RESULT_ERROR;
        }
    }

    //6.Find meeting by description, including meetings happened
    @GET
    @Path("/getMeetingByDescription")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public String findMeetingOnDescription(@QueryParam(Participant.LOGIN) String username,
                                           @QueryParam(Participant.PASSWORD) String password,
                                           @QueryParam(Meeting.DESCRIPTION) String description) {
        if (LOGIN.equals(username) && PASSWORD.equals(password)) {
            try {
                description = URLDecoder.decode(description, "UTF-8");
                ArrayList<Meeting> meetingFound = new ArrayList<>();
                for (Meeting m : meetings) {
                    if (m.getDescription().toLowerCase().contains(description.toLowerCase()))
                        meetingFound.add(m);
                }
                if (meetingFound.isEmpty())
                    return EMPTY_LIST;
                else
                    return meetingFound.toString();

            } catch (UnsupportedEncodingException e) {
                System.out.println(e.getMessage());
                return EMPTY_LIST;
            }
        } else {
            return RESULT_ERROR;
        }
    }
}
