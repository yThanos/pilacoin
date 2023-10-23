package br.ufsm.csi.pilacoin.service;

import br.ufsm.csi.pilacoin.model.Difficulty;
import br.ufsm.csi.pilacoin.model.Pilacoin;
import br.ufsm.csi.pilacoin.model.json.PilaCoinJson;
import br.ufsm.csi.pilacoin.model.json.ValidacaoPilaJson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class RabbitManager {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @SneakyThrows
    @RabbitListener(queues = "dificuldade")
    public void getDificuldade(@Payload String sla){
        ObjectMapper objectMapper = new ObjectMapper();
        Difficulty diff = objectMapper.readValue(sla, Difficulty.class);
        Pilacoin.dificuldade = new BigInteger(diff.getDificuldade(), 16).abs();
    }

    @SneakyThrows
    @RabbitListener(queues = "pila-validado")
    public void getValidos(@Payload String valido) {
        ObjectMapper ob = new ObjectMapper();
        ValidacaoPilaJson vpj = ob.readValue(valido, ValidacaoPilaJson.class);
        PilaCoinJson pilaJson = ob.readValue(vpj.getPilaCoinJson(), PilaCoinJson.class);
        if(pilaJson.getNomeCriador().equals("Vitor Fraporti")){
            rabbitTemplate.convertAndSend("pila-validado",valido);
        } else {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            BigInteger hash = new BigInteger(md.digest(ob.writeValueAsString(pilaJson).getBytes(StandardCharsets.UTF_8))).abs();
            if(hash.compareTo(Pilacoin.dificuldade) < 0){
                ValidacaoPilaJson validacaoPilaJson = ValidacaoPilaJson.builder().pilaCoinJson(ob.writeValueAsString(pilaJson)).
                        assinaturaPilaCoin(vpj.getAssinaturaPilaCoin()).nomeValidador("Vitor Fraporti").
                        chavePublicaValidador(Pilacoin.chavePublica).build();
                rabbitTemplate.convertAndSend("pila-validado", ob.writeValueAsString(validacaoPilaJson));
            }
        }
        System.out.println("Pila valido: "+valido);
    }

    @RabbitListener(queues = "clients-errors")
    public void getErrors(@Payload String valido){
        System.out.println("clients-errors: "+valido);
    }

    @RabbitListener(queues = "vitor_fraporti")
    public void getMsgs(@Payload String valido){
        System.out.println("Olha a mensagem ae: "+valido);
    }
}
