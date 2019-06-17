package demo.leonardogcsoares.webrtc;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.squareup.okhttp.internal.Util;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;

import java.net.URISyntaxException;
import java.util.ArrayList;

import static org.webrtc.SessionDescription.Type.ANSWER;
import static org.webrtc.SessionDescription.Type.OFFER;

public class Connection extends AppCompatActivity {
    private boolean isConnected = false;
    private static Connection instance = null;
    private User user;
    private Socket mSocket;
    private String id;
    private String TAG = "Debug";
    private String URI = "http://192.168.1.8:7000";


    // Events that server sends
    private String SET_ID = "set_id";

    private Connection(){
    }

    public static Connection getInstance() {
        if(instance == null) {
            instance = new Connection();
        }
        return instance;
    }

    public void connect(){
        try {
            if (!isConnected){
                isConnected = true;
                mSocket = IO.socket(URI);
                mSocket.connect();
                mSocket.on(SET_ID, msg -> {
                    id = mSocket.id();
                    User user = Buffer.getUserBuffer();
                    user.setSocketId(id);
                    Buffer.setUserBuffer(user);
                    JSONObject message = new JSONObject();
                    try{
                        message.put("username",user.getName());
                        message.put("id",user.getSocketId());
                        mSocket.emit("FLAG", message);
                    }catch(JSONException ex){
                        Log.d(TAG, "connect: " + ex.getMessage());
                    }

                });

            }
        }catch (URISyntaxException ex){
            Log.d(TAG, ex.getMessage());
        }
    }

    public void sendMessageToServer(String event, JSONObject message){
        if(isConnected){
            mSocket.emit(event,message);
        }else{
            Log.e(TAG, "Não existe uma conexão com o servidor");
        }
    }


    public String getId(){return id;}
    public User getUserConnection(){return user;}

    public Socket getmSocket() {
        return mSocket;
    }
}
