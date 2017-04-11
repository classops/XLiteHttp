package com.hanter.litehttp.utils;

import android.content.Context;
import android.util.Log;

import com.hanter.litehttp.http.CustomTrustManager;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * 类名：HttpsUtils <br/>
 * 描述：HTTPS相关实用方法 <br/>
 * 创建时间：2017/3/31 9:47
 *
 * @author wangmingshuo
 * @version 1.0
 */
public class HttpsUtils {


    public static SSLSocketFactory createSSLSocketFactory(Context context,
                                                          HashMap<String, Integer> keyCertificates,
                                                          HashMap<String, Integer> trustCertificates) {
        // 客户端的认证
        KeyManagerFactory kmf = null;

        /*
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            for (String keyName : keyCertificates.keySet()) {
                InputStream cais = null;
                try {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    cais = context.getResources().openRawResource(keyCertificates.get(keyName));
                    Certificate ca = cf.generateCertificate(cais);
                    keyStore.setCertificateEntry(keyName, ca);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (cais != null)
                        cais.close();
                }
            }
            keyStore.load(null, null);
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore);
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        // 客户端的认证
        TrustManagerFactory tmf = null;
        try {
            KeyStore clientKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            for (String keyName : trustCertificates.keySet()) {
                InputStream cais = null;
                try {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    cais = context.getResources().openRawResource(trustCertificates.get(keyName));
                    Certificate ca = cf.generateCertificate(cais);
                    clientKeyStore.setCertificateEntry(keyName, ca);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (cais != null)
                        cais.close();
                }
            }
            clientKeyStore.load(null, null);
            tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(clientKeyStore);
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");

            KeyManager[] kms = null;
            if (kmf != null)
                kms = kmf.getKeyManagers();

            TrustManager[] tms = null;
            if (tmf != null)
                tms = tmf.getTrustManagers();

            sslContext.init(kms, tms, new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return sslContext == null ? null : sslContext.getSocketFactory();
    }

    /**
     * 根据BKS创建SSLSocketFactory
     */
    public static SSLSocketFactory createSSLSocketFactory(Context context,
                                                          int keyStoreRes, String keyStorePwd) {
        return createSSLSocketFactory(context, keyStoreRes, keyStorePwd, keyStoreRes, keyStorePwd);
    }

    /**
     * 根据BKS创建SSLSocketFactory
     */
    public static SSLSocketFactory createSSLSocketFactory(Context context,
                                                          int keyStoreRes, String keyStorePwd,
                                                          int trustKeyStoreRes, String trustKeyStorePwd) {

        // 服务端认证
        TrustManagerFactory tmf = null;
        InputStream keyStoreInputStream = null;
        try {
            KeyStore keyStore = KeyStore.getInstance("BKS");
            keyStoreInputStream = context.getResources().openRawResource(trustKeyStoreRes);
            keyStore.load(keyStoreInputStream, trustKeyStorePwd.toCharArray());
            tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

//            while (keyStore.aliases().hasMoreElements()) {
//                String key = keyStore.aliases().nextElement();
//
//                X509Certificate ca = (X509Certificate) keyStore.getCertificate(key);
//
//                Log.d("HttpsUtils", "dn - " + ca.getSubjectDN());
//            }


        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } finally {
            if (keyStoreInputStream != null) {
                try {
                    keyStoreInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        CustomTrustManager tm = null;

        // 客户端的认证
        KeyManagerFactory kmf = null;
        InputStream clientInputStream = null;
        try {
            KeyStore clientKeyStore = KeyStore.getInstance("BKS");
            clientInputStream = context.getResources().openRawResource(keyStoreRes);
            clientKeyStore.load(clientInputStream, keyStorePwd.toCharArray());
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(clientKeyStore, keyStorePwd.toCharArray());

            tm = new CustomTrustManager(clientKeyStore);

        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } finally {
            if (clientInputStream != null) {
                try {
                    clientInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        /*
        TrustManager tm = new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {return null; }
            @Override
            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {


                Log.i("X509 ca", "authType-" + authType);

                int i = 0;

                for (X509Certificate ca : chain) {
                    i++;

                    Log.i("X509 ca" + i, "sdn = " + ca.getSubjectDN());
                    Log.i("X509 ca" + i, "idn = " + ca.getIssuerDN());
                    Log.i("X509 ca" + i, "an = " + ca.getIssuerAlternativeNames());

                    Log.i("X509 ca" + i, "ku = " + ca.getExtendedKeyUsage());
                }

            }
            @Override
            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {

                for (X509Certificate ca : chain) {
                    Log.d("X509", "dn = " + ca.getSubjectDN());
                }

                // 确认服务器端证书颁发者和代码中的证书主体一致
                if (!chain[0].getIssuerDN().equals(cert.getSubjectDN())) { }
                    // 确认服务器端证书被代码中证书公钥签名
                    chain[0].verify(cert.getPublicKey());
                    // 确认服务器端证书没有过期
                    chain[0].checkValidity();
            }
        };
        */

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLSv1.2");

            KeyManager[] kms = null;
            if (kmf != null)
                kms = kmf.getKeyManagers();

            TrustManager[] tms = null;
            if (tmf != null)
                tms = tmf.getTrustManagers();

//            sslContext.init(kms, tms, new SecureRandom());

            sslContext.init(kms, new TrustManager[] {tm}, null);

//            sslContext.init(kms, new TrustManager[] {tm}, new SecureRandom());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return sslContext == null ? null : sslContext.getSocketFactory();
    }
}
