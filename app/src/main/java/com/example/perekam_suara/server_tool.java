package com.example.perekam_suara;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class server_tool implements Serializable {
    public transient String status;
    public transient int[] response;
    public transient String Reson_fail;

    public server_tool(){

    }

    public void setReson_fail(String status,String reson_fail) {
        this.status=status;
        this.Reson_fail = reson_fail;
    }

    public void setResponse(String status,int[] response) {
        this.status=status;
        this.response = response;
    }

    public Map<String,Object> getResonponse() {
        Map<String,Object> list = new HashMap<>();
        if(this.response.equals("200"))
        {
            list.put("status", this.status);
            list.put("response", this.response);

        }else{
            list.put("status", this.status);
            list.put("response", this.Reson_fail);
        }

        return list;
    }


}
