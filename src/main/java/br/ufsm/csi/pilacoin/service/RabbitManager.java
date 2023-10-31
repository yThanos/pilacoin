package br.ufsm.csi.pilacoin.service;

import br.ufsm.csi.pilacoin.model.Difficulty;
import br.ufsm.csi.pilacoin.model.json.PilaCoinJson;
import br.ufsm.csi.pilacoin.model.json.ValidacaoPilaJson;
import br.ufsm.csi.pilacoin.util.PilaUtil;
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
import java.util.ArrayList;

@Service
public class RabbitManager {
    @Autowired
    public RabbitManager(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    private final RabbitTemplate rabbitTemplate;
    private static ArrayList<String> pilaIgnroe = new ArrayList<>();

    @SneakyThrows
    @RabbitListener(queues = "dificuldade")
    public void getDificuldade(@Payload String sla){
        ObjectMapper objectMapper = new ObjectMapper();
        Difficulty diff = objectMapper.readValue(sla, Difficulty.class);
        PilaUtil.difficulty = new BigInteger(diff.getDificuldade(), 16).abs();
    }


    @SneakyThrows
    @RabbitListener(queues = "pila-minerado")
    public void getMinerados(@Payload String pilaStr) {
        boolean fim = true;
        for(String pila: pilaIgnroe){
            if (pila.equals(pilaStr)){
                fim = false;
                break;
            }
        }
        pilaIgnroe.add(pilaStr);
        if (fim){
            System.out.println("-=+=-=+=-=+=".repeat(4));
            ObjectMapper ob = new ObjectMapper();
            PilaCoinJson pilaJson = ob.readValue(pilaStr, PilaCoinJson.class);
            if(pilaJson.getNomeCriador().equals("Vitor Fraporti")){
                rabbitTemplate.convertAndSend("pila-minerado",pilaStr);//devolve pq n é meu
            } else {
                System.out.println("Validando pila do(a): "+pilaJson.getNomeCriador());
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                BigInteger hash = new BigInteger(md.digest(pilaStr.getBytes(StandardCharsets.UTF_8))).abs();
                if(hash.compareTo(PilaUtil.difficulty) < 0){
                    ValidacaoPilaJson validacaoPilaJson = ValidacaoPilaJson.builder().
                            pilaCoinJson(pilaJson).
                            assinaturaPilaCoin(new PilaUtil().getAssinatura(pilaStr)).
                            nomeValidador("Vitor Fraporti").
                            chavePublicaValidador(PilaUtil.publicKey.toString().getBytes()).build();
                    rabbitTemplate.convertAndSend("pila-validado", ob.writeValueAsString(validacaoPilaJson));
                    System.out.println("Valido!");
                } else {
                    System.out.println("Não Validou! :(");
                    rabbitTemplate.convertAndSend("pila-minerado", pilaStr);
                }
            }
            System.out.println("-=+=-=+=-=+=".repeat(4));
        }
    }

    /*@RabbitListener(queues = "clients-errors")
    public void getErrors(@Payload String valido){
        System.out.println("clients-errors: "+valido);
    }*/

    @RabbitListener(queues = "vitor_fraporti")
    public void getMsgs(@Payload String valido){
        System.out.println("Olha a mensagem ae: "+valido);
    }
}
