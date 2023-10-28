package br.ufsm.csi.pilacoin.service;

import br.ufsm.csi.pilacoin.model.Difficulty;
import br.ufsm.csi.pilacoin.model.Pilacoin;
import br.ufsm.csi.pilacoin.model.json.PilaCoinJson;
import br.ufsm.csi.pilacoin.model.json.ValidacaoPilaJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.ArrayList;

@Service
public class RabbitManager {
    @Autowired
    public RabbitManager(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    private final RabbitTemplate rabbitTemplate;
    public static PrivateKey privateKey;

    private static ArrayList<String> pilaIgnroe = new ArrayList<>();

    @SneakyThrows
    @RabbitListener(queues = "dificuldade")
    public void getDificuldade(@Payload String sla){
        ObjectMapper objectMapper = new ObjectMapper();
        Difficulty diff = objectMapper.readValue(sla, Difficulty.class);
        Pilacoin.dificuldade = new BigInteger(diff.getDificuldade(), 16).abs();
    }

    @RabbitListener(queues = "pila-validado")
    public void getValidos(@Payload String valido){
        System.out.println("Pila validado: "+valido);
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
            System.out.println("Pila minerado: "+pilaStr);
            ObjectMapper ob = new ObjectMapper();
            PilaCoinJson pilaJson = ob.readValue(pilaStr, PilaCoinJson.class);
            if(pilaJson.getNomeCriador().equals("Vitor Fraporti")){
                System.out.println("Ignora é meu");
                rabbitTemplate.convertAndSend("pila-minerado",pilaStr);//devolve pq n é meu
            } else {
                System.out.println("Não é meu");
                System.out.println("Validando pila do(a): "+pilaJson.getNomeCriador());
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                BigInteger hash = new BigInteger(md.digest(pilaStr.getBytes(StandardCharsets.UTF_8))).abs();
                System.out.println("Gerou o hash");
                while(Pilacoin.dificuldade == null){}//garatnir q n vai tentar comparar antes de receber a dificuldade
                if(hash.compareTo(Pilacoin.dificuldade) < 0){
                    System.out.println("Passou na dificuldade");
                    md.reset();//reseta o MessageDigest para usar dnv
                    Cipher cipher = Cipher.getInstance("RSA");
                    cipher.init(Cipher.ENCRYPT_MODE, privateKey);
                    String hashStr = ob.writeValueAsString(hash);
                    System.out.println("Passou do chipher");
                    byte[] assinatura = md.digest(hashStr.getBytes(StandardCharsets.UTF_8));//assinatura
                    ValidacaoPilaJson validacaoPilaJson = ValidacaoPilaJson.builder().pilaCoinJson(pilaJson).
                            assinaturaPilaCoin(cipher.doFinal(assinatura)).//cipher do final pra criptografar
                                    nomeValidador("Vitor Fraporti").
                            chavePublicaValidador(Pilacoin.chavePublica).build();
                    rabbitTemplate.convertAndSend("pila-validado", ob.writeValueAsString(validacaoPilaJson));
                    System.out.println("Valido!");
                } else {
                    System.out.println("Não Validou! :(");
                    rabbitTemplate.convertAndSend("pila-minerado", pilaStr);
                }
            }
        }
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
