package br.ufsm.csi.pilacoin.service;

import br.ufsm.csi.pilacoin.model.Pilacoin;
import br.ufsm.csi.pilacoin.model.json.PilaCoinJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Random;

@Service
public class MineService {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @PostConstruct
    void mineService() {
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                kpg.initialize(1040);
                KeyPair kp = kpg.genKeyPair();
                Pilacoin.chavePublica = kp.getPublic().getEncoded();
                BigInteger hash;
                MessageDigest md;
                md = MessageDigest.getInstance("SHA-256");
                while (Pilacoin.dificuldade == null){}
                int tentativa = 0;
                do {
                    tentativa++;
                    Random rnd = new Random();
                    byte[] bytes = new byte[256/8];
                    rnd.nextBytes(bytes);
                    ObjectMapper ow = new ObjectMapper();
                    String nonce = new BigInteger(bytes).abs().toString();
                    PilaCoinJson pj = PilaCoinJson.builder().chaveCriador(Pilacoin.chavePublica).nomeCriador("Vitor Fraporti").
                            dataCriacao(new Date()).nonce(nonce).build();
                    hash = new BigInteger(md.digest(ow.writeValueAsString(pj).getBytes(StandardCharsets.UTF_8))).abs();
                    if (hash.compareTo(Pilacoin.dificuldade) < 0){
                        System.out.println(tentativa+" tentativas");
                        System.out.println("Hash: "+hash);
                        System.out.println("Diff: "+Pilacoin.dificuldade);
                        System.out.println(ow.writeValueAsString(pj));
                        rabbitTemplate.convertAndSend("pila-minerado", ow.writeValueAsString(pj));
                        tentativa = 0;
                    }
                } while (true);
            }
        }).start();
    }
}
