package com.sbtools.apachehttpwrapper.certificates;

import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class AdditionalKeyStoresTrustManager implements X509TrustManager {

    private ArrayList<X509TrustManager> x509TrustManagers = new ArrayList<X509TrustManager>();

    protected AdditionalKeyStoresTrustManager() {
        try {
            // init default trust manager factory
            final TrustManagerFactory original = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            original.init((KeyStore) null);
            
            addTrustManagersFromFactory(original);
        } catch (Exception e) {
        }
    }

    private void addTrustManagersFromFactory(final TrustManagerFactory tmf) {
        for (TrustManager tm : tmf.getTrustManagers()) {
            if (tm instanceof X509TrustManager) {
                x509TrustManagers.add((X509TrustManager)tm);
            }
        }
    }
    
    public void addKeyStore(KeyStore keyStore) {
        try {
            TrustManagerFactory additionalCerts = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            additionalCerts.init(keyStore);
            
            addTrustManagersFromFactory(additionalCerts);
        } catch (Exception e) {
        }
    }
    
    /*
     * Delegate to the default trust manager.
     */
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        final X509TrustManager defaultX509TrustManager = x509TrustManagers.get(0);
        defaultX509TrustManager.checkClientTrusted(chain, authType);
    }

    /*
     * Loop over the trustmanagers until we find one that accepts our server
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        for( X509TrustManager tm : x509TrustManagers ) {
            try {
                tm.checkServerTrusted(chain,authType);
                return;
            } catch( CertificateException e ) {
                // ignore
            }
        }
        throw new CertificateException();
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        final ArrayList<X509Certificate> list = new ArrayList<X509Certificate>();
        
        for( X509TrustManager tm : x509TrustManagers ) {
            list.addAll(Arrays.asList(tm.getAcceptedIssuers()));
        }
        
        return list.toArray(new X509Certificate[list.size()]);
    }
}