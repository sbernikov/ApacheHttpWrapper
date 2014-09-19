package com.sbtools.apachehttpwrapper.entity;

import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

class ApacheHttpJSONEntity extends ByteArrayEntity {
    
    public ApacheHttpJSONEntity(JSONObject json) throws UnsupportedEncodingException {
        super(json.toString().getBytes("UTF8"));
    }
    
    @Override
    public Header getContentType() {
        return new BasicHeader(HTTP.CONTENT_TYPE, "application/json; charset=UTF-8");
    }
}