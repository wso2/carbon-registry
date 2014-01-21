/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.registry.indexing.solr;

import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;

import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.registry.core.session.UserRegistry;

/**
 * TODO this class will be added to carbon core, so we will remove this class
 * @author Dimuthu Leelarathne <dimuthul@wso2.com>, Srinath Perera (srinath@wso2.com) 
 *
 */

public class SignatureUtil {
	
//	public static void init(){
//		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
//	}
//
//    private static final String THUMB_DIGEST_ALGORITHM = "SHA-1";
//
//    private static String signatureAlgorithm = "SHA1withRSA";
//    private static String provider = "BC";
//
//    /**
//     * Retrieves the thumbprint for alias.
//     * @param alias The alias
//     * @return Thumbprint is returned.
//     * @throws Exception
//     */
//    public static byte[] getThumbPrintForAlias(String alias) throws Exception {
//        MessageDigest sha = MessageDigest.getInstance(THUMB_DIGEST_ALGORITHM);
//        sha.reset();
//        Certificate cert = getCertificate(alias);
//        sha.update(cert.getEncoded());
//        byte[] thumb = sha.digest();
//        return thumb;
//    }
//
//    /**
//     * Validates the signature with the given thumbprint
//     * @param thumb Thumbprint of the certificate
//     * @param data Data on which the signature is performed
//     * @param signature The signature.
//     * @return
//     * @throws Exception
//     */
//    public static boolean validateSignature(byte[] thumb, String data, byte[] signature) throws Exception {
//        Signature signer = Signature.getInstance(signatureAlgorithm, provider);
//        signer.initVerify(getPublicKey(thumb));
//        signer.update(data.getBytes());
//        boolean isVerified = signer.verify(signature);
//        return isVerified;
//    }
//    
//    /**
//     * Validate the signature with the default thumbprint.
//     * @param data The data which is used to perfrom the signature.
//     * @param signature The signature to be validated.
//     * @return True is returned if singature is valid.
//     * @throws Exception
//     */
//    public static boolean validateSignature(String data, byte[] signature) throws Exception {
//        Signature signer = Signature.getInstance(signatureAlgorithm, provider);
//        signer.initVerify(getDefaultPublicKey());
//        signer.update(data.getBytes());
//        boolean isVerified = signer.verify(signature);
//        return isVerified;
//    }
//
//    /**
//     * Performs the signature with the default private key in the system.
//     * @param data Data to be signed.
//     * @return The signature is returned.
//     * @throws Exception
//     */
//    public static byte[] doSignature(String data) throws Exception {       
//        Signature signer = Signature.getInstance(signatureAlgorithm, provider);
//        signer.initSign(getDefaultPrivateKey());
//        signer.update(data.getBytes());
//        byte[] signature = signer.sign();
//        return signature;
//    }
//    
//    private static PrivateKey getDefaultPrivateKey(UserRegistry userRegistry) throws Exception {
//        KeyStoreManager keyStoreMan = KeyStoreManager.getInstance(userRegistry);
//        KeyStore keyStore = keyStoreMan.getPrimaryKeyStore();
//        ServerConfiguration config = ServerConfiguration.getInstance();
//        String password = config
//                .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_PASSWORD);
//        String alias = config.getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_KEY_ALIAS);
//        PrivateKey privateKey = (PrivateKey)keyStore.getKey(alias, password.toCharArray());
//        return privateKey;
//    }
//    
//    private static PublicKey getDefaultPublicKey(UserRegistry userRegistry) throws Exception {
//        KeyStoreManager keyStoreMan = KeyStoreManager.getInstance(userRegistry);
//        KeyStore keyStore = keyStoreMan.getPrimaryKeyStore();
//        ServerConfiguration config = ServerConfiguration.getInstance();
//        String alias = config
//                .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_KEY_ALIAS);
//        PublicKey publicKey = (PublicKey) keyStore.getCertificate(alias).getPublicKey();
//        return publicKey;
//
//    }
//
//    private static PublicKey getPublicKey(byte[] thumb, UserRegistry userRegistry) throws Exception {
//        KeyStoreManager keyStoreMan = KeyStoreManager.getInstance(userRegistry);
//        KeyStore keyStore = keyStoreMan.getPrimaryKeyStore();
//        PublicKey pubKey = null;
//        Certificate cert = null;
//        MessageDigest sha = MessageDigest.getInstance(THUMB_DIGEST_ALGORITHM);
//        sha.reset();
//        for (Enumeration<String> e = keyStore.aliases(); e.hasMoreElements();) {
//            String alias = e.nextElement();
//            cert = getCertificate(alias);
//            sha.update(cert.getEncoded());
//            byte[] data = sha.digest();
//
//            if (Arrays.equals(data, thumb)) {
//                pubKey = cert.getPublicKey();
//                break;
//            }
//        }
//        return pubKey;
//    }
//
//    private static Certificate getCertificate(String alias, UserRegistry userRegistry) throws Exception {
//        KeyStoreManager keyStoreMan = KeyStoreManager.getInstance(userRegistry);
//        KeyStore keyStore = keyStoreMan.getPrimaryKeyStore();
//        Certificate cert = null;
//        Certificate[] certs = keyStore.getCertificateChain(alias);
//        if (certs == null || certs.length == 0) {
//            cert = keyStore.getCertificate(alias);
//        } else {
//            cert = certs[0];
//        }
//        if (cert == null || !(cert instanceof X509Certificate)) {
//            throw new Exception("Please check alias. Cannot retrieve valid certificate");
//        }
//        return cert;
//    }
    
    
//    public static PrivateKey getDefaultPrivateKey() throws Exception {
//        ServerConfiguration config = ServerConfiguration.getInstance();
//        String password = config
//                .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_PASSWORD);
//        String alias = config
//                .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_KEY_ALIAS);
//        PrivateKey privateKey = (PrivateKey) primaryKeyStore.getKey(alias, password.toCharArray());
//        return privateKey;
//    }
//
//    public static PublicKey getDefaultPublicKey() throws Exception {
//        ServerConfiguration config = ServerConfiguration.getInstance();
//        String alias = config
//                .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_KEY_ALIAS);
//        PublicKey publicKey = (PublicKey) primaryKeyStore.getCertificate(alias).getPublicKey();
//        return publicKey;
//    }


//String loggeduser = (String)request.getSession().getAttribute("logged-user");
//    protected byte[] sign(PrivateKey _macKey, byte[] data) throws GeneralSecurityException{
//        String algorithm = _macKey.getAlgorithm();
//        Mac mac = Mac.getInstance(algorithm);
//
//        mac.init(_macKey);
//
//        return mac.doFinal(data);
//}
//
//public String sign(PrivateKey _macKey, String text) throws GeneralSecurityException, UnsupportedEncodingException{
//        String signature = new String(Base64.encodeBase64(sign(_macKey,text.getBytes("utf-8"))), "utf-8");
//        return signature;
//}
//
//public boolean verifySignature(PrivateKey _macKey,String text, String signature) throws AssociationException
//{
//    return signature.equals(sign(_macKey,text));
//}
}