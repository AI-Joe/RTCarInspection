package com.lieb.j.traininspection;

import org.json.JSONObject;

/**
 * Restructuring for offline capability
 */

public class CarObject {
    private String carID;
    private String location;
    private String Rpmk;
    private String type;
    private Boolean S_W;
    private String WW;
    private String Comments;

    public CarObject(String cid, String loc, String reportingmark, String t, boolean sw){
        carID = cid;
        location = loc;
        Rpmk = reportingmark;
        type = t;
        S_W = sw;
        WW = "";
        Comments = "";

    }


}
