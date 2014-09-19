package com.sbtools.apachehttpwrapper.response;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;

import com.sbtools.apachehttpwrapper.ApacheHttpResponse;
import com.sbtools.apachehttpwrapper.log.ApacheHttpWrapperLog;

public class ApacheHttpStringResponse extends ApacheHttpResponse {
    
    private String mContent;
    
    public ApacheHttpStringResponse() {
        super();
    }
    
    @Override
    public void parseResponse(HttpResponse response) {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            try {
                mContent = EntityUtils.toString(entity, "UTF8");
            } catch (ParseException e) {
                ApacheHttpWrapperLog.e("Error while converting http response to string: " + e.toString());
            } catch (IOException e) {
                ApacheHttpWrapperLog.e("Error while converting http response to string: " + e.toString());
            }
        }
        
        super.parseResponse(response);
    }
    
    @Override
    public Object getContent() {
        return mContent;
    }
}