package br.ufsm.csi.pilacoin.service;

import br.ufsm.csi.pilacoin.model.*;
import br.ufsm.csi.pilacoin.util.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

@Service
public class RabbitManager {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    private static final ArrayList<String> pilaIgnroe = new ArrayList<>();
    public static ArrayList<MsgsJson> mensagens = new ArrayList<>();

    @SneakyThrows
    @RabbitListener(queues = "dificuldade")
    public void getDificuldade(@Payload String sla){
        ObjectMapper objectMapper = new ObjectMapper();
        Difficulty diff = objectMapper.readValue(sla, Difficulty.class);
        Constants.DIFFICULTY = new BigInteger(diff.getDificuldade(), 16).abs();
    }

    @RabbitListener(queues = "pila-minerado")
    public void getMinerados(@Payload String pilaStr) throws NoSuchAlgorithmException {
        boolean fim = true;
        for(String pila: pilaIgnroe){
            if (pila.equals(pilaStr)){
                fim = false;
                break;
            }
        }
        pilaIgnroe.add(pilaStr);
        if (fim){
            new PilaService().validaPila(pilaStr);
        }
    }

    @SneakyThrows
    @RabbitListener(queues = "vitor_fraporti")
    public void getMsgs(@Payload String valido){
        ObjectMapper om = new ObjectMapper();
        MsgsJson msg = om.readValue(valido, MsgsJson.class);
        msg.setLida(false);
        mensagens.add(msg);
        System.out.println("Olha a mensagem ae: "+valido);
    }


    @RabbitListener(queues = "descobre-bloco")
    public void descobreBloco(@Payload String blocoJson) throws JsonProcessingException, NoSuchAlgorithmException {
        blocoMinerado(new BlocoService().mineraBloco(blocoJson));
    }

    @RabbitListener(queues = "bloco-minerado")
    public void validaBloco(@Payload String blocoJson) throws NoSuchAlgorithmException {
        new BlocoService().validaBloco(blocoJson);
    }

    public void blocoMinerado(String bloco){
        rabbitTemplate.convertAndSend("bloco-minerado", bloco);
    }

    public void blocoValidado(String blocoJson){
        rabbitTemplate.convertAndSend("bloco-validado", blocoJson);
    }

    public void tranferirPila(TransferirPila tp){
        rabbitTemplate.convertAndSend("tranferir-pila", tp);
    }

    public void pilaMinerado(String pj){
        rabbitTemplate.convertAndSend("pila-minerado",pj);
    }

    public void pilaValido(String pila){
        rabbitTemplate.convertAndSend("pila-validado", pila);
    }
}
