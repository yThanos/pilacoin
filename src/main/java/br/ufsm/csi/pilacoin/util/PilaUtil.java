package br.ufsm.csi.pilacoin.util;

import br.ufsm.csi.pilacoin.service.RabbitManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;

public class PilaUtil {
    public static PrivateKey privateKey;
    public static PublicKey publicKey;
    public static BigInteger difficulty = new BigInteger("fffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16);

    @SneakyThrows
    public byte[] getAssinatura(Object object){
        ObjectMapper om = new ObjectMapper();
        String strObj = om.writeValueAsString(object);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] assinatura = md.digest(strObj.getBytes(StandardCharsets.UTF_8));
        return cipher.doFinal(assinatura);
    }
}
