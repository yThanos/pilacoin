package br.ufsm.csi.pilacoin.service;

import br.ufsm.csi.pilacoin.model.PilaCoinJson;
import br.ufsm.csi.pilacoin.util.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;

@Service
public class MineService {
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public MineService(RabbitTemplate rbt){
        this.rabbitTemplate = rbt;
    }

    @PostConstruct
    void mineService() throws NoSuchAlgorithmException {
        KeyPair kp;
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("keypair.ser"))) {
            kp = (KeyPair) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Erro carregando as chaves");
            return;
        }
        Constants.PUBLIC_KEY = kp.getPublic();
        Constants.PRIVATE_KEY = kp.getPrivate();
        for(int i = 0; i<Runtime.getRuntime().availableProcessors(); i++){
            Thread t = new Thread(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    BigInteger hash;
                    MessageDigest md;
                    md = MessageDigest.getInstance("SHA-256");
                    int tentativa = 0;
                    while (true){
                        tentativa++;
                        Random rnd = new Random();
                        byte[] bytes = new byte[256/8];
                        rnd.nextBytes(bytes);
                        ObjectMapper ow = new ObjectMapper();
                        String nonce = new BigInteger(bytes).abs().toString();
                        PilaCoinJson pj = PilaCoinJson.builder().chaveCriador(Constants.PUBLIC_KEY.toString().getBytes()).
                                nomeCriador("Vitor Fraporti").
                                dataCriacao(new Date()).nonce(nonce).build();
                        hash = new BigInteger(md.digest(ow.writeValueAsString(pj).getBytes(StandardCharsets.UTF_8))).abs();
                        if (hash.compareTo(Constants.DIFFICULTY) < 0){
                            System.out.println("-=+=-=+=-=+=".repeat(4));
                            System.out.println(tentativa+" tentativas on "+Thread.currentThread().getName());
                            System.out.println("-=+=-=+=-=+=".repeat(4));
                            rabbitTemplate.convertAndSend("pila-minerado", ow.writeValueAsString(pj));
                            tentativa = 0;
                        }
                    }
                }
            });
            t.setName("Thread_"+i);
            t.start();
        }
    }
}
