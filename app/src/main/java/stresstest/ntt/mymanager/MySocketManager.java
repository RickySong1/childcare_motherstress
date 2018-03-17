package stresstest.ntt.mymanager;

import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by SuperSong on 2017-03-04.
 */

public class MySocketManager {

    public static final int PORT = 22222;
    public static final String IP_ADDRESS = "143.248.134.121";
    public static final String SET_DATA = "SET_DATA";
    public static final String SET_REQUEST = "SET_REQUEST";

    public static final int SET_REQEUST_MY_ASKING = 0;
    public static final int SET_REQEUST_ASKING_TEACHER = 1;
    public static final int SET_DATA_MY_RESULT = 2;
    public static final int SET_PICTURE = 3;
    public static final int SET_TEACHER_DATA = 4;
    //public static final int TARGET_ASKING_SPOUSE = 2;

    public static final int GET_MY_DATA = 0;
    public static final int GET_SPOUSE_DATA = 1;
    public static final int GET_TEACHER_DATA = 2;
    public static final int GET_MY_REQUEST = 3;
    public static final int GET_SPOUSE_REQUEST = 4;
    public static final int GET_TEACHER_REQUEST = 5;
    public static final int GET_PICTURE = 6;
    public static final int GET_PIC_NAME = 7;
    public static final int GET_LOG = 8;
    public static final int GET_TRACK = 9;
    public static final int GET_COMMENT_ON = 10;
    public static final int GET_NOTI = 11;
    public static final int GET_RECENT_PICTURE = 12;
    public static final int GET_OPEN_CHAT = 13;

    BufferedReader br;
    PrintWriter out;
    Socket socket;
    int [][] checkAnswers = null;

    String userType;
    String pre_message;

    public enum SOCKET_MSG {
        GET_PAGE_COUNT ,  GET_FATHERCOMMENT , GET_MOTHERACTIVITY, GET_MOTHEREMOTION, GET_BABYACTIVITY_UP, GET_BABYACTIVITY_DOWN,
        SET_OPENAPP , SET_FATHERCOMMENT , SET_MOTHERACTIVITY, SET_MOTHEREMOTION, SET_BABYACTIVITY_UP, SET_BABYACTIVITY_DOWN
    }

    public MySocketManager(String _userType) {
        out = null;
        socket = null;
        this.userType = _userType;

        pre_message = "##"+ userType+"##";
    }

    public void setDataFromServer(SOCKET_MSG _msg, final String _target_time , final int viewid , final String save_string) {

       final SOCKET_MSG msg = _msg;
       final String target_time = _target_time;

        new Thread(new Runnable() {
            @Override public void run() {
                String send_message = null;

                switch(msg){
                    case SET_OPENAPP:
                        send_message = pre_message+"LOGIN##"+_target_time+"##";
                        break;
                    case SET_MOTHEREMOTION:
                        send_message = pre_message+"SET_MOTHEREMOTION##"+_target_time+"##"+Integer.toString(viewid)+"##"+save_string+"##";
                        break;
                    case SET_MOTHERACTIVITY:
                        send_message = pre_message+"SET_MOTHERACTIVITY##"+_target_time+"##"+Integer.toString(viewid)+"##"+save_string+"##";
                        break;
                    case SET_FATHERCOMMENT:
                        send_message = pre_message+"SET_FATHERCOMMENT##"+_target_time+"##"+Integer.toString(viewid)+"##"+save_string+"##";
                        break;
                    case SET_BABYACTIVITY_UP:
                         //언제 아기 위에 스케쥴을 고칠지,
                        send_message = pre_message+"SET_BABYACTIVITY_UP##"+_target_time+"##"+Integer.toString(viewid)+"##"+save_string+"##";
                        break;
                    case SET_BABYACTIVITY_DOWN:
                         //언제 아기 밑에 스케쥴을 고칠지 확인해야하고,
                         //그리고,  아이콘을 지우는것도 서버에 반영 할 수 있도록 하자.
                        send_message = pre_message+"SET_BABYACTIVITY_DOWN##"+_target_time+"##"+Integer.toString(viewid)+"##"+save_string+"##";
                        break;
                }

                try {
                    socket = new Socket(IP_ADDRESS , PORT);
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter( socket.getOutputStream())), true);
                    out.println(send_message);
                    out.flush();
                }
                catch(Exception e) {
                    Log.e("So_excp",e.toString());
                } finally {
                    try {
                        if(socket!=null)
                            socket.close();
                        if(out !=null)
                            out.close();
                    } catch(Exception er) {
                        Log.e("Soc_excp",er.toString());
                    }
                }
            } }).start();

        return ;
    }

    public String getDataFromServer(SOCKET_MSG msg, String date) {
        String send_message = null;
        String get_message = null;

                switch(msg){
                    case GET_PAGE_COUNT:
                        send_message = pre_message+"GET_PAGE_COUNT##";
                        break;
                    case GET_MOTHEREMOTION:
                        send_message = pre_message+"GET_MOTHEREMOTION##"+date+"##";
                        break;
                    case GET_MOTHERACTIVITY:
                        send_message = pre_message+"GET_MOTHERACTIVITY##"+date+"##";
                        break;
                    case GET_FATHERCOMMENT:
                        send_message = pre_message+"GET_FATHERCOMMENT##"+date+"##";
                        break;
                    case GET_BABYACTIVITY_UP:
                        send_message = pre_message+"GET_BABYACTIVITY_UP##"+date+"##";
                        break;
                    case GET_BABYACTIVITY_DOWN:
                        send_message = pre_message+"GET_BABYACTIVITY_DOWN##"+date+"##";
                        break;
                }

                try {
                    socket = new Socket(IP_ADDRESS , PORT);
                    br = new BufferedReader(new InputStreamReader( socket.getInputStream()));
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter( socket.getOutputStream())), true);

                    out.println(send_message);
                    out.flush();

                    String _temp;

                    get_message = br.readLine();
                    while( (_temp = br.readLine()) != null) {
                        get_message = get_message.concat(_temp);
                    }

                }
                catch(Exception e) {
                    Log.e("So_excp",e.toString());
                } finally {
                    try {
                        if(socket!=null)
                            socket.close();
                        if(out !=null)
                            out.close();
                        if (br !=null)
                            br.close();
                    } catch(Exception er) {
                        Log.e("Soc_excp",er.toString());
                    }
                }

        return get_message;
    }






    public String getTcpIpResult(String userType, String childID , int get_target_data){
        String send_message = null;

        switch(get_target_data){
            case GET_MY_DATA:
                send_message = pre_message+"##GET_DATA##"+childID+"##"+userType+"##";
                break;
            case GET_SPOUSE_DATA:  // TEACHER -> GET : MOTHER
                if(userType.contains("MOTHER"))
                    send_message = pre_message+"##GET_DATA##"+childID+"##"+"FATHER"+"##";
                else if(userType.contains("FATHER"))
                    send_message = pre_message+"##GET_DATA##"+childID+"##"+"MOTHER"+"##";
                else
                    send_message = pre_message+"##GET_DATA##"+childID+"##"+"MOTHER"+"##";
                break;

            case GET_TEACHER_DATA: // TEACHER -> GET : FATHER
                if(userType.contains("TEACHER")){
                    send_message = pre_message+"##GET_DATA##"+childID+"##"+"FATHER"+"##";
                }else
                    send_message = pre_message+"##GET_DATA##"+childID+"##"+"TEACHER"+"##";
                break;

            case GET_MY_REQUEST:
                send_message = pre_message+"##GET_REQUEST##"+childID+"##"+userType+"##";
                break;
            case GET_SPOUSE_REQUEST:
                if(userType.contains("MOTHER"))
                    send_message = pre_message+"##GET_REQUEST##"+childID+"##"+"FATHER"+"##";
                else
                    send_message = pre_message+"##GET_REQUEST##"+childID+"##"+"MOTHER"+"##";
                break;
            case GET_TEACHER_REQUEST: // TEACHER -> GET : QUESTIONS ASKED ME
                send_message = pre_message+"##GET_REQUEST##"+childID+"##"+"TEACHER"+"##";
                break;
            case GET_PICTURE:
                send_message = pre_message+"##GET_PICTURE##"+childID+"##"+"NONE"+"##";
                break;
            case GET_PIC_NAME:
                send_message = pre_message+"##GET_PIC_NAME##"+childID+"##"+"NONE"+"##";
                break;
            case GET_LOG:
                send_message = pre_message+"##GET_LOG##"+childID+"##"+"NONE"+"##";
                break;
            case GET_COMMENT_ON:
                send_message = pre_message+"##GET_COMMNET_ON##"+childID+"##"+"NONE"+"##";
                break;
            case GET_NOTI:
                send_message = pre_message+"##GET_NOTI##"+childID+"##"+userType+"##";
                break;
            case GET_RECENT_PICTURE:
                send_message = pre_message+"##GET_RECENT_PICTURE##"+childID+"##"+userType+"##";
                break;
            case GET_OPEN_CHAT:
                send_message = pre_message+"##GET_OPEN_CHAT##"+childID+"##"+userType+"##";
                break;
        }


        String get_message = "Cannot Get The Data From Server";
        int [][] result = null;

        try {
            socket = new Socket(IP_ADDRESS , PORT);
            br = new BufferedReader(new InputStreamReader( socket.getInputStream(), "UTF-8" ));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter( socket.getOutputStream())), true);

            out.println(send_message);
            out.flush();

            String _temp;
            get_message = br.readLine();

            while( (_temp = br.readLine()) != null){ // && get_message.length()<116) {
                get_message = get_message.concat(_temp);
            }
            //get_meesage have the results

        } catch(Exception e) {

        } finally {
            try {
                if(socket!=null)
                    socket.close();
                if(out !=null)
                    out.close();
                if (br !=null)
                    br.close();
            } catch(Exception er) {

            }
        }

        return get_message;
    }

    public void setLog(String userType, String childID , String log ){
        try {
            socket = new Socket(IP_ADDRESS , PORT);
            br = new BufferedReader(new InputStreamReader( socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter( socket.getOutputStream())), true);

            String send_message = "%%" + userType +"%%" + "##" + "SET_CODE" + "##" + childID + "##" + log +"##";
            out.println(send_message);
        } catch(Exception e) {
            Log.e("Socket",e.toString());
        } finally {
            try {
                if(socket!=null)
                    socket.close();
                if(out !=null)
                    out.close();
                if (br !=null)
                    br.close();
            } catch(Exception er) {
                Log.e("Socket",er.toString());
            }
        }
    }



    public void setMyCode(String childID , String userType, String code){
        try {
            socket = new Socket(IP_ADDRESS , PORT);
            br = new BufferedReader(new InputStreamReader( socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter( socket.getOutputStream())), true);

            String send_message = "%%" + userType +"%%" + "##" + "SET_MYCODE" + "##" + childID + "##" +userType  +"##" + code +"##";
            out.println(send_message);

        } catch(Exception e) {
            Log.e("Socket",e.toString());
        } finally {
            try {
                if(socket!=null)
                    socket.close();
                if(out !=null)
                    out.close();
                if (br !=null)
                    br.close();
            } catch(Exception er) {
                Log.e("Socket",er.toString());
            }
        }
    }

    public void openChat(String childID, int position){
        try {
            socket = new Socket(IP_ADDRESS , PORT);
            br = new BufferedReader(new InputStreamReader( socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter( socket.getOutputStream())), true);

            String send_message = "%%" + userType +"%%" + "##" + "SET_OPEN_CHAT" + "##" + childID + "##"+ Integer.toString(position) +"##";
            out.println(send_message);

        } catch(Exception e) {
            Log.e("Socket",e.toString());
        } finally {
            try {
                if(socket!=null)
                    socket.close();
                if(out !=null)
                    out.close();
                if (br !=null)
                    br.close();
            } catch(Exception er) {
                Log.e("Socket",er.toString());
            }
        }
    }

    public void sendWidgetUpdate(String childID, String _usertype){
        try {
            socket = new Socket(IP_ADDRESS , PORT);
            br = new BufferedReader(new InputStreamReader( socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter( socket.getOutputStream())), true);

            String send_message = "%%" + _usertype +"%%" + "##" + "WIDGET_UPDATE" + "##" + childID + "##" ;

            out.println(send_message);

        } catch(Exception e) {
            Log.e("Socket",e.toString());
        } finally {
            try {
                if(socket!=null)
                    socket.close();
                if(out !=null)
                    out.close();
                if (br !=null)
                    br.close();
            } catch(Exception er) {
                Log.e("Socket",er.toString());
            }
        }
    }


}