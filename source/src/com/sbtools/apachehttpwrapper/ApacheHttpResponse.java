package com.sbtools.apachehttpwrapper;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;

public class ApacheHttpResponse {
    private int mStatusCode;
    private String mReasonPhrase;
    private Header[] mHeaders;
    
    public ApacheHttpResponse() {
        
    }
    
    public void parseResponse(HttpResponse response) {
        StatusLine statusLine = response.getStatusLine();
        
        mStatusCode = statusLine.getStatusCode();
        mReasonPhrase = statusLine.getReasonPhrase();
        
        mHeaders = response.getAllHeaders();
    }
    
    public int getStatusCode() {
        return mStatusCode;
    }
    
    public String getReasonPhrase() {
        return mReasonPhrase;
    }
    
    public String getHeaderValue(String key) {
        String value = null;
        
        if (mHeaders != null) {
            for (int i = 0; i < mHeaders.length; i++) {
                if (mHeaders[i].getName().equals(key)) {
                    value = mHeaders[i].getValue();
                    break;
                }
            }
        }
        
        return value;
    }

    public Object getContent() {
        return null;
    }
}