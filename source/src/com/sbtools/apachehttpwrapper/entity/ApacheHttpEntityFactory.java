package com.sbtools.apachehttpwrapper.entity;

import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import com.sbtools.apachehttpwrapper.log.ApacheHttpWrapperLog;

public final class ApacheHttpEntityFactory {
    
    public static HttpEntity getEntityFromString(String str) {
        HttpEntity entity = null;
        
        try {
            entity = new StringEntity(str, "UTF8");
        } catch (UnsupportedEncodingException e) {
            ApacheHttpWrapperLog.e("Error while creating string entity: " + e.toString());
        }
        
        return entity;
    }
    
    public static HttpEntity getEntityFromJSON(JSONObject json) {
        HttpEntity entity = null;
        
        try {
            entity = new ApacheHttpJSONEntity(json);
        } catch (UnsupportedEncodingException e) {
            ApacheHttpWrapperLog.e("Error while creating json entity: " + e.toString());
        }
        
        return entity;
    }
}