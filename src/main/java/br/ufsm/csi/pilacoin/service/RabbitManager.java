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
import java.util.Random;

@Service
public class RabbitManager {
    @Autowired
    public RabbitManager(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    private final RabbitTemplate rabbitTemplate;
    private static final ArrayList<String> pilaIgnroe = new ArrayList<>();

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

    @RabbitListener(queues = "vitor_fraporti")
    public void getMsgs(@Payload String valido){
        System.out.println("Olha a mensagem ae: "+valido);
    }


    @RabbitListener(queues = "descobre-bloco")
    public void descobreBloco(@Payload String blocoJson) throws JsonProcessingException, NoSuchAlgorithmException {
        System.out.println("Descobriu um bloco!");
        System.out.println(blocoJson);
        ObjectMapper om = new ObjectMapper();
        BlocoJson bloco = om.readValue(blocoJson, BlocoJson.class);
        String nonceAnterior = (bloco.getNonce() != null)?bloco.getNonce():"nulo";
        System.out.println("Nonce anterior: "+ nonceAnterior);
        BigInteger hash;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        bloco.setChaveUsuarioMinerador(Constants.PUBLIC_KEY.toString().getBytes());
        boolean loop = true;
        while(loop){
            Random rnd = new Random();
            byte[] bytes = new byte[256/8];
            rnd.nextBytes(bytes);
            String nonce = new BigInteger(bytes).abs().toString();
            bloco.setNonce(nonce);
            hash = new BigInteger(md.digest(om.writeValueAsString(bloco).getBytes(StandardCharsets.UTF_8))).abs();
            if (hash.compareTo(Constants.DIFFICULTY) < 0){
                rabbitTemplate.convertAndSend("bloco-minerado", om.writeValueAsString(bloco));
                System.out.println(hash);
                loop = false;
            }
        }
        System.out.println("bloco minerado!");
        System.out.println(om.writeValueAsString(bloco));

    }

    @RabbitListener(queues = "bloco-minerado")
    public void validaBloco(@Payload String blocoJson) throws NoSuchAlgorithmException {
        System.out.println("/////////////".repeat(4));
        System.out.println("Validando bloco!");
        ObjectMapper om = new ObjectMapper();
        BlocoJson bloco;
        try {
            bloco = om.readValue(blocoJson, BlocoJson.class);
        } catch (JsonProcessingException e) {
            rabbitTemplate.convertAndSend("bloco-minerado", blocoJson);
            System.out.println("Erro conversão");
            return;
        }
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        BigInteger hash = new BigInteger(md.digest(blocoJson.getBytes(StandardCharsets.UTF_8))).abs();
        System.out.println(hash);
        if(hash.compareTo(Constants.DIFFICULTY) < 0){
            ValidaBlocoJson vbj = ValidaBlocoJson.builder().
                    assinaturaBloco(new PilaUtil().getAssinatura(blocoJson)).bloco(bloco).
                    chavePublicaValidador(Constants.PUBLIC_KEY.toString().getBytes()).
                    nomeValidador(Constants.USERNAME).build();
            try {
                rabbitTemplate.convertAndSend("bloco-validado", om.writeValueAsString(vbj));
                System.out.println("Valido! :)");
            } catch (JsonProcessingException e) {
                rabbitTemplate.convertAndSend("bloco-minerado", blocoJson);
                System.out.println("Erro conversão");
                return;
            }
        } else {
            rabbitTemplate.convertAndSend("bloco-minerado", blocoJson);
            System.out.println("Não validou :(");
        }
        System.out.println("//////////////".repeat(4));
    }
}
