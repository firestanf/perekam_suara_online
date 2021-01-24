package com.example.perekam_suara;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class send_to_server  extends AsyncTask<server_tool, Void, server_tool> {

    String asrJsonString="";
    private String TAG="send_to_server";
    private String ASR_URL="https://apiquerybyhumming.herokuapp.com/get_result";
    private PostTaskListener<server_tool> postTaskListener;
    private String file_location="";
    public send_to_server(String file_location,PostTaskListener<server_tool> postTaskListener)
    {
        this.file_location=file_location;
        this.postTaskListener = postTaskListener;
    }

    public interface PostTaskListener<K> {
        // K is the type of the result object of the async task
        void onPostTask(K result);
    }




    @Override
    protected server_tool doInBackground(server_tool... server_tools) {
        String result = "";
        server_tool st= new server_tool();
        try {
            Log.i(TAG,"**** UPLOADING .WAV to ASR...");
            URL obj = new URL(ASR_URL);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setRequestProperty("Content-Type", "audio/wav");
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            String wavpath= file_location; //audio.wav";
            File wavfile = new File(wavpath);
            boolean success = true;
            if (wavfile.exists()) {
                Log.i(TAG,"**** audio.wav DETECTED: "+wavfile);
            }
            else{
                Log.i(TAG,"**** audio.wav MISSING: " +wavfile);
            }

            String charset="UTF-8";
            String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
            String CRLF = "\r\n"; // Line separator required by multipart/form-data.

            OutputStream output=null;
            PrintWriter writer=null;
            try {
                output = conn.getOutputStream();
                writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
                byte [] music=new byte[(int) wavfile.length()];//size & length of the file
                InputStream is  = new FileInputStream(wavfile);
                BufferedInputStream bis = new BufferedInputStream(is, 16000);
                DataInputStream dis = new DataInputStream(bis);      //  Create a DataInputStream to read the audio data from the saved file
                int i = 0;
                copyStream(dis,output);
            }
            catch(Exception e){

            }

            conn.connect();

            int responseCode = conn.getResponseCode();
            Log.i(TAG,"POST Response Code : " + responseCode + " , MSG: " + conn.getResponseMessage());

            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                Log.i(TAG,"***ASR RESULT: " + response.toString());


                JSONArray jresponse=new JSONObject(response.toString()).getJSONArray("data");
                asrJsonString=jresponse.toString();

                for(int i = 0 ; i < jresponse.length(); i++){
                    JSONObject jsoni=jresponse.getJSONObject(i);
                    if(jsoni.has("value")){
                        String asrResult=jsoni.getString("value");
                        //ActionManager.getInstance().addDebugMessage("ASR Result: "+asrResult);
                        Log.i(TAG,"*** Result Text: "+asrResult);
                        result = asrResult;
                    }
                }
                Log.i(TAG,"***ASR RESULT: " + jresponse.toString());

            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                Log.i(TAG,"POST FAILED: " + response.toString());
                result = "";
            }
        } catch (Exception e) {
            Log.i(TAG,"HTTP Exception: " + e.getLocalizedMessage());
        }
        return st; //"Failed to fetch data!";
    }

    @Override
    protected void onPostExecute(server_tool result) {

        super.onPostExecute(result);
        if(!result.equals("")){
            Log.i(TAG,"onPostEXECUTE SUCCESS, consuming result");
            if (result != null && postTaskListener != null)
                postTaskListener.onPostTask(result);

        }else{
            Log.i(TAG,"onPostEXECUTE FAILED");
        }


        }

        public void copyStream( InputStream is, OutputStream os) {
            final int buffer_size = 4096;
            try {
                byte[] bytes = new byte[buffer_size];
                int k=-1;
                double prog=0;
                while ((k = is.read(bytes, 0, bytes.length)) > -1) {
                    if(k != -1) {
                        os.write(bytes, 0, k);
                        prog=prog+k;
                        double progress = ((long) prog)/1000;///size;
                        Log.i(TAG,"UPLOADING: "+progress+" kB");
                    }
                }
            os.flush();
            is.close();
            os.close();
        } catch (Exception ex) {
            Log.i(TAG,"File to Network Stream Copy error "+ex);
        }
    }
}
