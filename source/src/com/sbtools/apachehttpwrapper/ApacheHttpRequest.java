package com.sbtools.apachehttpwrapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.util.Base64;

import com.sbtools.apachehttpwrapper.certificates.AdditionalKeyStoresSSLSocketFactory;
import com.sbtools.apachehttpwrapper.log.ApacheHttpWrapperLog;

public class ApacheHttpRequest {
    
    private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;
    private static final int DEFAULT_READ_TIMEOUT = 25000;
    
    private static class HttpPatch extends HttpPost {
        @Override
        public String getMethod() {
            return "PATCH";
        }
    }
    
    private static DefaultHttpClient sHttpClient;
    private static AdditionalKeyStoresSSLSocketFactory sSSLFactory;
    
    static {
        sHttpClient = new DefaultHttpClient();
        HttpClientParams.setRedirecting(sHttpClient.getParams(), false);
        
        try {
            sSSLFactory = new AdditionalKeyStoresSSLSocketFactory();
            sHttpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", sSSLFactory, 443));
        } catch (Exception e) {
        }
    }
    
    private int mConnectionTimeout;
    private int mReadTimeout;
    private final ApacheHttpMethod mMethod;

    private HashMap<String, String> mHeaders;
    
    private ApacheHttpResponse mResponse;
    private HttpEntity mEntity;
    
    private ArrayList<String> mRedirectUrls;
    
    public ApacheHttpRequest(ApacheHttpMethod method, ApacheHttpResponse response) {
        mMethod = method;

        mConnectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
        mReadTimeout = DEFAULT_READ_TIMEOUT;

        mHeaders = new HashMap<String, String>();
        appendDefaultHeaders();
        
        mResponse = response;
        mEntity = null;
        
        mRedirectUrls = new ArrayList<String>();
    }
    
    public final void setTimeouts(int connectionTimeout, int readTimeout) {
        mConnectionTimeout = connectionTimeout;
        mReadTimeout = readTimeout;
    }
    
    public final void addHeader(String key, String value) {
        mHeaders.put(key, value);
    }
    
    public final void setEntity(HttpEntity entity) {
        if (mMethod != ApacheHttpMethod.GET) {
            mEntity = entity;
        }
    }
    
    public final void addBasicAuthorization(String login, String password) {
        mHeaders.put("Authorization", "Basic " + Base64.encodeToString((login + ":" + password).getBytes(), Base64.NO_WRAP));
    }

    private HttpResponse performRequest(HttpRequestBase request) {
        HttpResponse httpResponse = null;
        
        try {
            httpResponse = sHttpClient.execute(request);
            
            if (httpResponse != null) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                
                // check for redirect
                if (statusCode == HttpStatus.SC_MOVED_TEMPORARILY ||
                    statusCode == HttpStatus.SC_MOVED_PERMANENTLY ||
                    statusCode == HttpStatus.SC_SEE_OTHER ||
                    statusCode == HttpStatus.SC_TEMPORARY_REDIRECT) {
                    String redirectUrl = httpResponse.getFirstHeader("Location").getValue();
                    
                    if (redirectUrl != null && redirectUrl.length() > 0 && !mRedirectUrls.contains(redirectUrl)) {
                        mRedirectUrls.add(redirectUrl);
                        
                        request.setURI(new URI(redirectUrl));
                        httpResponse = performRequest(request);
                    } else {
                        httpResponse = null;
                    }
                }
            }
        } catch (ClientProtocolException e) {
            ApacheHttpWrapperLog.e("Error while processing http request: " + e.toString());
        } catch (IOException e) {
            ApacheHttpWrapperLog.e("Error while processing http request: " + e.toString());
        } catch (URISyntaxException e) {
            ApacheHttpWrapperLog.e("Error while processing http request: " + e.toString());
        }
        
        return httpResponse;
    }
    
    public final void execute(String url) {
        synchronized (sHttpClient) {
            try {
                HttpRequestBase request = getRequestByMethod(mMethod);

                prepareClient(sHttpClient);
                prepareParams(sHttpClient.getParams());
                prepareRequest(request);

                request.setURI(new URI(url));
                appendHeadersToRequest(request);
                
                HttpResponse httpResponse = performRequest(request);
                mRedirectUrls.clear();
                
                if (httpResponse != null) {
                    if (mResponse != null) {
                        mResponse.parseResponse(httpResponse);
                    }

                    HttpEntity entity = httpResponse.getEntity();
                    if (entity != null) {
                        try {
                            entity.consumeContent();
                        } catch (IOException e) {
                            ApacheHttpWrapperLog.e("Error while consuming entity content: " + e.toString());
                        }
                    }
                } else {
                    mResponse = null;
                }
            } catch (URISyntaxException e) {
                ApacheHttpWrapperLog.e("Error while processing http request: " + e.toString());
                mResponse = null;
            }
        }
    }
    
    public final ApacheHttpResponse getResponse() {
        return mResponse;
    }
    
    protected void prepareClient(HttpClient client) {
        // may be overriden by subclasses
    }
    
    protected void prepareParams(HttpParams params) {
        HttpConnectionParams.setConnectionTimeout(params, mConnectionTimeout);
        HttpConnectionParams.setSoTimeout(params, mReadTimeout);
    }
    
    protected void prepareRequest(HttpRequest request) {
        // for GET request entity always null
        if (mEntity != null) {
            HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest)request;
            entityRequest.setEntity(mEntity);
        }
    }
    
    protected void appendDefaultHeaders() {
     // may be overriden by subclasses
    }
    
    private void appendHeadersToRequest(HttpRequest request) {
        Iterator<Entry<String, String>> iterator = mHeaders.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, String> entry = iterator.next();
            request.addHeader(entry.getKey(), entry.getValue());
        }
    }
    
    private static HttpRequestBase getRequestByMethod(ApacheHttpMethod method) {
        HttpRequestBase request = null;
        
        switch (method) {
            case GET:
                request = new HttpGet();
                break;
            case PUT:
                request = new HttpPut();
                break;
            case POST:
                request = new HttpPost();
                break;
            case PATCH:
                request = new HttpPatch();
                break;
        }
        
        return request;
    }
    
    // Certificates
    public static void addCertificatesStore(KeyStore keyStore) {
        synchronized (sHttpClient) {
            sSSLFactory.addKeyStore(keyStore);
        }
    }
    
    public static void addCertificatesStore(InputStream is, String type, String password) {
        try {
            final KeyStore ks = KeyStore.getInstance(type);
            ks.load(is, password.toCharArray());
            
            addCertificatesStore(ks);
        } catch (Exception e) {
            ApacheHttpWrapperLog.e("Error while adding certificate: " + e.toString());
        }
    }
}