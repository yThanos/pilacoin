package br.ufsm.csi.pilacoin.service;

import br.ufsm.csi.pilacoin.model.*;
import br.ufsm.csi.pilacoin.util.Constants;
import br.ufsm.csi.pilacoin.util.PilaUtil;
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
            System.out.println("-=+=-=+=-=+=".repeat(4));
            ObjectMapper ob = new ObjectMapper();
            PilaCoinJson pilaJson = null;
            try {
                pilaJson = ob.readValue(pilaStr, PilaCoinJson.class);
            } catch (JsonProcessingException e) {
                rabbitTemplate.convertAndSend("pila-minerado", pilaStr);
                return;
            }
            if(pilaJson.getNomeCriador().equals("Vitor Fraporti")){
                rabbitTemplate.convertAndSend("pila-minerado",pilaStr);//devolve pq n é meu
                System.out.println("Ignora é meu!");
            } else {
                System.out.println("Validando pila do(a): "+pilaJson.getNomeCriador());
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                BigInteger hash = new BigInteger(md.digest(pilaStr.getBytes(StandardCharsets.UTF_8))).abs();
                if(hash.compareTo(Constants.DIFFICULTY) < 0){
                    ValidacaoPilaJson validacaoPilaJson = ValidacaoPilaJson.builder().
                            pilaCoinJson(pilaJson).
                            assinaturaPilaCoin(new PilaUtil().getAssinatura(pilaStr)).
                            nomeValidador("Vitor Fraporti").
                            chavePublicaValidador(Constants.PUBLIC_KEY.toString().getBytes()).build();
                    try {
                        rabbitTemplate.convertAndSend("pila-validado", ob.writeValueAsString(validacaoPilaJson));
                        System.out.println("Valido! :)");
                    } catch (JsonProcessingException e) {
                        rabbitTemplate.convertAndSend("pila-minerado", pilaStr);
                        return;
                    }
                } else {
                    System.out.println("Não Validou! :(");
                    rabbitTemplate.convertAndSend("pila-minerado", pilaStr);
                }
            }
            System.out.println("-=+=-=+=-=+=".repeat(4));
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

    public void blocoMinerado(String bloco){
        rabbitTemplate.convertAndSend("bloco-minerado", bloco);
    }

    @RabbitListener(queues = "bloco-minerado")
    public void validaBloco(@Payload String blocoJson) throws NoSuchAlgorithmException {
        new BlocoService().validaBloco(blocoJson);
    }

    public void blocoValidado(String blocoJson){
        rabbitTemplate.convertAndSend("bloco-validado", blocoJson);
    }

    public void tranferirPila(TransferirPila tp){
        rabbitTemplate.convertAndSend("tranferir-pila", tp);
    }

    public void pilaMinerado(PilaCoinJson pj){
        rabbitTemplate.convertAndSend("pila-minerado",pj);
    }
}
