package demo.leonardogcsoares.webrtc;

public class Buffer {
    private static String stringBuffer;
    private static User userBuffer;
    public static void setStringBuffer(String string){
        stringBuffer = string;
    }
    public static String getStringBuffer(){ return stringBuffer; }

    public static User getUserBuffer() {
        return userBuffer;
    }

    public static void setUserBuffer(User userBuffer) {
        Buffer.userBuffer = userBuffer;
    }
}
