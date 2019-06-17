package demo.leonardogcsoares.webrtc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.github.nkzawa.emitter.Emitter;
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
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.webrtc.SessionDescription.Type.ANSWER;
import static org.webrtc.SessionDescription.Type.OFFER;

public class LocalPeerConnectionActivity extends AppCompatActivity {

    //My vars
    private final String _ICE = "_ice";
    private final String _OFFER = "_offer";
    private final String _ANSWER    = "_answer";
    private Connection connection = Connection.getInstance();
    private boolean connectionEstablished = false;

    // Activity elements
    private EditText mCalleEditText;
    private Button mSendButton;
    private TextView mStatusTextView;
    private EditText mMessageEditText;



    private PeerConnection peerConnection;
    private PeerConnectionFactory peerConnectionFactory;


    //----------------------------------------------------


    //Previously vars
    private static final String TAG = "Debug";


    private DataChannel dataChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "init MainActivity onCreate");

        Log.d(TAG, PeerConnectionFactory.initializeAndroidGlobals(getApplicationContext(), true, true, true)
                ? "Success initAndroidGlobals" : "Failed initAndroidGlobals");


        peerConnectionFactory = new PeerConnectionFactory();
        Log.d(TAG, "has yet to create local and remote peerConnection");

        //My code
        initializePeerConnectionFactory();
        initializePeerConnections();

        //Initialize the activity elements
        mCalleEditText = (EditText) findViewById(R.id.calleEditText);
        mStatusTextView= (TextView) findViewById(R.id.statusTextView);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton      = (Button) findViewById(R.id.messageButton);


        // Initializes the send button listener
        initializeSendButtonListener();

        // This handle with the signaling
        handleWithSignalingServer();
    }


    // My code
    //-------------------------------------------------------------------------------------------------------------

    private void handleWithSignalingServer(){

        connection.getmSocket().on(_OFFER, args -> {
            try {
                updateStatus("Recebi uma oferta");
                JSONObject message = (JSONObject) args[0];
                User caller = new User();
                peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(
                        OFFER,
                        message.getString("sdp")
                ));
                caller.setName(message.getString("callerusername"));
                doAnswer(caller.getName());
                updateStatus("Enviei uma resposta");
            }catch (JSONException ex){
                Log.d(TAG, "handleWithSignalingServer: " + ex.getMessage());
            }
        }).on(_ANSWER, args -> {
            try{
                JSONObject message = (JSONObject) args[0];
                // Eu pego o SDP recebido pelo servidor e coloco no meu remote description
                peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(
                        ANSWER,
                        message.getString("sdp")));

                connectionEstablished = true;
            }catch (JSONException ex){
                Log.d(TAG, "handleWithSignalingServer: " + ex.getMessage());
            }
        }).on(_ICE, args ->{
            try{
                JSONObject message = (JSONObject) args[0];
                Log.d(TAG, "connectToSignallingServer: receiving candidates");
                IceCandidate candidate = new IceCandidate(message.getString("id"), message.getInt("label"), message.getString("candidate"));
                peerConnection.addIceCandidate(candidate);
            }catch (JSONException ex){
                Log.d(TAG, "handleWithSignalingServer: " + ex.getMessage());
            }
        });

    }



    private void updateStatus(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatusTextView.setText(message);
            }
        });
    }

    private void initializeSendButtonListener(){
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ByteBuffer buffer = ByteBuffer.wrap(mMessageEditText.getText().toString().getBytes());
//                sendChannel.send(new DataChannel.Buffer(buffer,false));

                if(!Utils.editTextIsEmpty(mCalleEditText) && !connectionEstablished){
                    doCall(Utils.getTextFromEditText(mCalleEditText));
                }

                if(connectionEstablished){
                    ByteBuffer data = ByteBuffer.wrap(mMessageEditText.getText().toString().getBytes());
                    dataChannel.send(new DataChannel.Buffer(data,false));
                }
            }
        });
    }

    //-------------------------------------------------------------------------------------------------------------

    private void initializePeerConnectionFactory() {
        PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true);
        peerConnectionFactory = new PeerConnectionFactory();
    }
    private void initializePeerConnections() {
        peerConnection = createPeerConnection(peerConnectionFactory);
        dataChannel = peerConnection.createDataChannel("sendDataChannel", new DataChannel.Init());
        dataChannel.registerObserver(new DataChannel.Observer() {
            @Override
            public void onBufferedAmountChange(long l) {

            }

            @Override
            public void onStateChange() {
                if(dataChannel.state() == DataChannel.State.OPEN){
                    updateStatus("Deu bom");
                }else{
                    updateStatus("Deu ruim");
                }
            }

            @Override
            public void onMessage(DataChannel.Buffer buffer) {

            }
        });
    }

    private PeerConnection createPeerConnection(PeerConnectionFactory factory) {
        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        MediaConstraints pcConstraints = new MediaConstraints();

        PeerConnection.Observer pcObserver = new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                Log.d(TAG, "onSignalingChange: ");
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                Log.d(TAG, "onIceConnectionChange: ");
            }

            @Override
            public void onIceConnectionReceivingChange(boolean b) {
                Log.d(TAG, "onIceConnectionReceivingChange: ");
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                Log.d(TAG, "onIceGatheringChange: ");
            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                Log.d(TAG, "onIceCandidate: ");
                JSONObject message = new JSONObject();
                // Toda vez que eu obtenho um ICE candidate eu envio para o servidor
                try {
                    message.put("type", "candidate");
                    message.put("label", iceCandidate.sdpMLineIndex);
                    message.put("id", iceCandidate.sdpMid);
                    message.put("candidate", iceCandidate.sdp);

                    Log.d(TAG, "onIceCandidate: sending candidate " + message);
                    connection.sendMessageToServer(_ICE,message);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onAddStream(MediaStream mediaStream) {
            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {
                Log.d(TAG, "onRemoveStream: ");
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                Log.d(TAG, "onDataChannel: ");
                dataChannel.registerObserver(new DataChannel.Observer() {
                    @Override
                    public void onBufferedAmountChange(long l) {

                    }

                    @Override
                    public void onStateChange() {

                    }

                    @Override
                    public void onMessage(DataChannel.Buffer buffer) {
                        updateStatus(buffer.toString());
                    }
                });
            }

            @Override
            public void onRenegotiationNeeded() {
                Log.d(TAG, "onRenegotiationNeeded: ");
            }
        };

        return factory.createPeerConnection(rtcConfig, pcConstraints, pcObserver);
    }

    private void doCall(String to) {
        MediaConstraints sdpMediaConstraints = new MediaConstraints();

        // Cria uma oferta e manda para o servidor
        peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                JSONObject message = new JSONObject();
                try {
                    message.put("to", to);
                    message.put("type", "offer");
                    message.put("sdp", sessionDescription.description);

                    connection.sendMessageToServer(_OFFER,message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, sdpMediaConstraints);
    }

    private void doAnswer(String to) {
        // Cria a resposta
        peerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                JSONObject message = new JSONObject();
                try {
                    message.put("to", to);
                    message.put("type", "answer");
                    message.put("sdp", sessionDescription.description);
                    // Envia para o servidor
                    connection.sendMessageToServer(_ANSWER,message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new MediaConstraints());
    }
}
