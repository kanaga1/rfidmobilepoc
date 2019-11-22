package com.example.rfidapp;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("EPCID")
    private String EPCID;
    @SerializedName("P_Id")
    private String P_ID;
    @SerializedName("P_name")
    private String P_name;
    @SerializedName("P_type")
    private String P_type;
    @SerializedName("unit")
    private String unit;

    public User(String EPCID, String p_ID, String p_name, String p_type, String unit) {
        this.EPCID = EPCID;
        P_ID = p_ID;
        P_name = p_name;
        P_type = p_type;
        this.unit = unit;
    }

    public String getEPCID() {
        return EPCID;
    }

    public void setEPCID(String EPCID) {
        this.EPCID = EPCID;
    }

    public String getP_ID() {
        return P_ID;
    }

    public void setP_ID(String p_ID) {
        P_ID = p_ID;
    }

    public String getP_name() {
        return P_name;
    }

    public void setP_name(String p_name) {
        P_name = p_name;
    }

    public String getP_type() {
        return P_type;
    }

    public void setP_type(String p_type) {
        P_type = p_type;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
