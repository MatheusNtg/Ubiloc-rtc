package demo.leonardogcsoares.webrtc;


import org.webrtc.SessionDescription;

public class User {
    private String name;
    private String socketId;
    private SessionDescription sessionDescription;
    private String to;

    public User(String name, String socketId, SessionDescription sessionDescription) {
        this.name = name;
        this.socketId = socketId;
        this.sessionDescription = sessionDescription;
    }

    public User(String name){
        this.name = name;
    }

    public User(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSocketId() {
        return socketId;
    }

    public void setSocketId(String socketId) {
        this.socketId = socketId;
    }

    public SessionDescription getSessionDescription() {
        return sessionDescription;
    }

    public void setSessionDescription(SessionDescription sessionDescription) {
        this.sessionDescription = sessionDescription;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
