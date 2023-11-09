package br.ufsm.csi.pilacoin.controllers;

import br.ufsm.csi.pilacoin.model.MsgsJson;
import br.ufsm.csi.pilacoin.model.PilaCoinJson;
import br.ufsm.csi.pilacoin.service.RabbitManager;
import br.ufsm.csi.pilacoin.util.Constants;
import br.ufsm.csi.pilacoin.util.PilaUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;

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
            pj.setNonce(new PilaUtil().geraNonce());
            hash = new BigInteger(md.digest(om.writeValueAsString(pj).getBytes(StandardCharsets.UTF_8))).abs();
            if (hash.compareTo(Constants.DIFFICULTY) < 0){
                loop = false;
            }
        }
        return  om.writeValueAsString(pj);
    }

    @GetMapping("/msgs")
    public ArrayList<MsgsJson> getMsgs(){
        return RabbitManager.mensagens;
    }
}
