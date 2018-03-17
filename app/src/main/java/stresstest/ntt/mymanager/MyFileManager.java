package stresstest.ntt.mymanager;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by Ricky Song on 2018-03-14.
 */

public class MyFileManager {

    public static String saveFolderLocation = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/MotherIsTried";
    private String userInfoFile = "userInformation.txt";

    public boolean initNewFile(String user_id){

        File folder = new File(saveFolderLocation);
        if ( !folder.exists() ) {
            if (!folder.mkdirs()) { Log.e("TravellerLog :: ", "Problem creating folder: "+saveFolderLocation); }
        }

        FileOutputStream fileWriter = null;
        BufferedWriter bw = null;

        String tempFileString = saveFolderLocation +"/"+userInfoFile;
        try {
            fileWriter = new FileOutputStream(tempFileString);
            bw = new BufferedWriter(new OutputStreamWriter(fileWriter));
            bw.write("##USER:"+user_id);
            bw.flush();
            fileWriter.close();
            bw.close();
            return true;
        }catch (Exception e ){
            Log.e("MSG",tempFileString +" writing error");
        }
        return false;
    }

    public boolean isUserInfo() {
        String tempFileString = saveFolderLocation +"/"+userInfoFile;
        File tempFile = new File(tempFileString);
        if (tempFile.exists()) {
            return true;
        }
        else return false;
    }

    public String getUserIdFromFile(){
        String tempFileString = saveFolderLocation +"/"+userInfoFile;
        FileInputStream fileReader=null;
        String type=null;

        File tempFile = new File(tempFileString);

        if (tempFile.exists()) {
            try {
                fileReader = new FileInputStream(tempFileString);
                BufferedReader br = new BufferedReader(new InputStreamReader(fileReader));
                type = br.readLine().split(":")[1];
                fileReader.close();
                br.close();
            } catch (Exception e) {
                Log.e("MSG", e.toString());
            }
        }

        return type;
    }


    public void deleteUserInfo(){
        File folder = new File(saveFolderLocation);
        if ( folder.exists() ) {
            deleteAllFiles(saveFolderLocation);
        }
    }
    public  void deleteAllFiles(String path){ File file = new File(path);  File[] tempFile = file.listFiles(); if(tempFile.length >0){ for (int i = 0; i < tempFile.length; i++) { if(tempFile[i].isFile()){ tempFile[i].delete(); }else{  deleteAllFiles(tempFile[i].getPath()); } tempFile[i].delete(); } file.delete(); } }

}
