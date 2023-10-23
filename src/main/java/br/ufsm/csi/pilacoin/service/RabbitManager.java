package br.ufsm.csi.pilacoin.service;

import br.ufsm.csi.pilacoin.Pilacoin;
import br.ufsm.csi.pilacoin.model.Difficulty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class RabbitManager {
    @SneakyThrows
    @RabbitListener(queues = "dificuldade")
    public void getDificuldade(@Payload String sla){
        ObjectMapper objectMapper = new ObjectMapper();
        Difficulty diff = objectMapper.readValue(sla, Difficulty.class);
        Pilacoin.dificuldade = new BigInteger(diff.getDificuldade(), 16).abs();
    }

    @RabbitListener(queues = "pila-validado")
    public void getValidos(@Payload String valido){
        System.out.println("Pila valido: "+valido);
    }

    @RabbitListener(queues = "clients-errors")
    public void getErrors(@Payload String valido){
        System.out.println("clients-errors: "+valido);
    }

    /*@RabbitListener(queues = "clients-msgs")
    public void getMsgs(@Payload String valido){
        System.out.println("clients-msgs: "+valido);
    }*/
}
