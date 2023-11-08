package br.ufsm.csi.pilacoin.controllers;

import br.ufsm.csi.pilacoin.model.PilaCoinJson;
import br.ufsm.csi.pilacoin.util.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;

@RestController
@RequestMapping("/teste")
public class TesteController {
    @GetMapping("/pila")
    public String getPila() throws JsonProcessingException, NoSuchAlgorithmException {
        boolean loop = true;
        PilaCoinJson pj = PilaCoinJson.builder().dataCriacao(new Date()).
                chaveCriador(Constants.PUBLIC_KEY.toString().getBytes()).
                nomeCriador(Constants.USERNAME).build();
        ObjectMapper om = new ObjectMapper();
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        BigInteger hash;
        while (loop){
            Random rnd = new Random();
            byte[] bytes = new byte[256/8];
            rnd.nextBytes(bytes);
            String nonce = new BigInteger(bytes).abs().toString();
            pj.setNonce(nonce);
            hash = new BigInteger(md.digest(om.writeValueAsString(pj).getBytes(StandardCharsets.UTF_8))).abs();
            if (hash.compareTo(Constants.DIFFICULTY) < 0){
                loop = false;
            }
        }
        return  om.writeValueAsString(pj);
    }
}
