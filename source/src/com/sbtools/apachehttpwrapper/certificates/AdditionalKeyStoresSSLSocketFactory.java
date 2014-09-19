package com.sbtools.apachehttpwrapper.certificates;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;

public class AdditionalKeyStoresSSLSocketFactory extends SSLSocketFactory {
    private SSLContext mSSLContext = SSLContext.getInstance("TLS");
    private AdditionalKeyStoresTrustManager mTrustManager = new AdditionalKeyStoresTrustManager();

    public AdditionalKeyStoresSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(null, null, null, null, null, null);
        mSSLContext.init(null, new TrustManager[] { mTrustManager }, null);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return mSSLContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return mSSLContext.getSocketFactory().createSocket();
    }
    
    public void addKeyStore(KeyStore keyStore) {
        mTrustManager.addKeyStore(keyStore);
    }
}
