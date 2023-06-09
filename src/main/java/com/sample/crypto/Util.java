package com.sample.crypto;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;
import java.util.Random;

public class Util {

    public static PublicKey getPublicKey(String publicKey)  throws Exception {

        byte[] keyBytes = publicKey.getBytes( );

        String keyString = new String(keyBytes, "US-ASCII");
        InputStream inputStream = new ByteArrayInputStream(keyString.getBytes(Charset.forName("UTF-8")));
        X509Certificate certificate = null;
        try {
            CertificateFactory f = CertificateFactory.getInstance("X.509");
            certificate = (X509Certificate) f.generateCertificate(inputStream);
        }  catch (Exception e) {
            e.printStackTrace();
        }
        return (Objects.nonNull(certificate) ? certificate.getPublicKey() : null);
    }

    public static RSAPrivateKey getPrivateKey( String privateKey ) throws Exception {

        String privateKeyPEM = privateKey
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");
        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }
    public static String decrypt(String jweEncryptedData, String clientOrServer  ) throws Exception {

        String pub, priv;

        if (clientOrServer.equalsIgnoreCase("server") ) {
            System.out.println("Server is decrypting using server's private key and verifying signature with client's public certificate");
            pub = Keys.clientPublicCertificate ;
            priv = Keys.serverPrivateKey;
        } else{
            System.out.println("Client is decrypting using client's private key and verifying signature with server's public certificate");

            pub = Keys.serverPublicCertificate ;
            priv = Keys.clientPrivateKey;
        }

        JWEObject jwe = JWEObject.parse(jweEncryptedData);
        jwe.decrypt(new RSADecrypter(getPrivateKey(priv)));
        String decryptedJweData = jwe.getPayload().toString();
        // Get Payload from JWS
        JWSObject jwsObject = JWSObject.parse(decryptedJweData);
        String verifiedData = null;
        JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey)getPublicKey( pub ));
        if(jwsObject.verify(verifier)) {
            verifiedData = jwsObject.getPayload().toString();
        } else {
            System.out.println("Failed to verify signed data");
            throw new Exception("Failed to verify signed data");
        }
        return verifiedData;
    }

    public static String encrypt(String plainData, String clientOrServer ) throws Exception {

        String pub, priv;

        if (clientOrServer.equalsIgnoreCase("server") ) {
            System.out.println("Server is Encrypting using client's public certificate and signing with server private key");
            pub = Keys.clientPublicCertificate ;
            priv = Keys.serverPrivateKey;
        } else{
            System.out.println("Client is Encrypting using server's public certificate and signing with client's private key");
            pub = Keys.serverPublicCertificate ;
            priv = Keys.clientPrivateKey;
        }
        // If the encryption is done by client, signing is done using client's private key
         // Generate the preset Content Encryption (CEK) key
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(EncryptionMethod.A128CBC_HS256.cekBitLength());
        SecretKey cek = keyGenerator.generateKey();

        String keyID = generateRandomString();
        RSASSASigner rsa = new RSASSASigner(getPrivateKey(priv));

        // Sign
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(keyID).build();
        JWSObject jws = new JWSObject(header, new Payload(plainData));

        jws.sign(rsa);
        String jwsData = jws.serialize();

        JWEObject jwe = new JWEObject(new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128CBC_HS256), new Payload(jwsData));

        jwe.encrypt(new RSAEncrypter((RSAPublicKey) getPublicKey(pub), cek));
        String jweString = jwe.serialize();

        return jweString;
    }

    public static String generateRandomString() {
        byte[] array = new byte[7]; // length is bounded by 7
        new Random().nextBytes(array);
        String generatedString = new String(array, Charset.forName("UTF-8"));
        return generatedString;
    }

    private static byte[] streamToBytes(InputStream is) throws IOException {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }
}
