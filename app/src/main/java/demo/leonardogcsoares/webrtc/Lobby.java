package demo.leonardogcsoares.webrtc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;

import java.util.ArrayList;

import static org.webrtc.SessionDescription.Type.ANSWER;
import static org.webrtc.SessionDescription.Type.OFFER;

public class Lobby extends AppCompatActivity {


    // Vars to connection
    private Connection connection = Connection.getInstance();

    // Activity elements
    public static EditText userNameEditText;
    private Button   connectButton;
    private String TAG = "Debug";
    private Context context = this;

    private boolean isInitiator;
    private boolean isChannelReady;
    private boolean isStarted;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        // Initializes the elements
        userNameEditText = (EditText) findViewById(R.id.usernameEditText);
        connectButton    = (Button)   findViewById(R.id.connectButton);

        initializeConnectButtonListener();

    }


    //Listeners
    private void initializeConnectButtonListener(){
        connectButton.setOnClickListener(click -> {
            if(!Utils.editTextIsEmpty(userNameEditText)){
                Buffer.setUserBuffer(new User(Utils.getTextFromEditText(userNameEditText)));
                connection.connect();

                Intent intent = new Intent(context, LocalPeerConnectionActivity.class);
                startActivity(intent);
            }else{
                Toast.makeText(this,"Digite um nome de usuário", Toast.LENGTH_LONG).show();
            }
        });
    }


    public void initializeActivity(Class activity){
        Intent intent = new Intent(this,activity);
        startActivity(intent);
    }



}
