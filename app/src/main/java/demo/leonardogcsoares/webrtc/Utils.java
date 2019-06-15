package demo.leonardogcsoares.webrtc;

import android.widget.EditText;

public class Utils {
    public static String getTextFromEditText(EditText editText) {
        return editText.getText().toString();
    }

    public static boolean editTextIsEmpty(EditText editText){
        if(editText.getText().toString().equals("")){
            return true;
        }else{
            return false;
        }
    }
}
