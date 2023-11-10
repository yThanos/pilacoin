package br.ufsm.csi.pilacoin.service;

import br.ufsm.csi.pilacoin.model.PilaCoinJson;
import br.ufsm.csi.pilacoin.model.Usuario;
import br.ufsm.csi.pilacoin.model.ValidacaoPilaJson;
import br.ufsm.csi.pilacoin.util.Constants;
import br.ufsm.csi.pilacoin.util.PilaUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;

@Service
public class PilaService {
    @PostConstruct
    void mineService() {
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
                        ObjectMapper ow = new ObjectMapper();
                        PilaCoinJson pj = PilaCoinJson.builder().chaveCriador(Constants.PUBLIC_KEY.toString().getBytes()).
                                nomeCriador("Vitor Fraporti").
                                dataCriacao(new Date()).nonce(new PilaUtil().geraNonce()).build();
                        hash = new BigInteger(md.digest(ow.writeValueAsString(pj).getBytes(StandardCharsets.UTF_8))).abs();
                        if (hash.compareTo(Constants.DIFFICULTY) < 0){
                            System.out.println("-=+=-=+=-=+=".repeat(4));
                            System.out.println(tentativa+" tentativas on "+Thread.currentThread().getName());
                            System.out.println("-=+=-=+=-=+=".repeat(4));
                            new RabbitManager().pilaMinerado(ow.writeValueAsString(pj));
                            tentativa = 0;
                        }
                    }
                }
            });
            t.setName("Thread_"+i);
            //t.start();
        }
    }

    public void validaPila(String pilaStr) throws NoSuchAlgorithmException {
        System.out.println("-=+=-=+=-=+=".repeat(4));
        ObjectMapper ob = new ObjectMapper();
        PilaCoinJson pilaJson;
        try {
            pilaJson = ob.readValue(pilaStr, PilaCoinJson.class);
        } catch (JsonProcessingException e) {
            new RabbitManager().pilaMinerado(pilaStr);
            return;
        }
        if(pilaJson.getNomeCriador().equals("Vitor Fraporti")){
            new RabbitManager().pilaMinerado(pilaStr);
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
                    new RabbitManager().pilaValido(ob.writeValueAsString(validacaoPilaJson));
                    System.out.println("Valido! :)");
                } catch (JsonProcessingException e) {
                    new RabbitManager().pilaMinerado(pilaStr);
                    return;
                }
            } else {
                System.out.println("Não Validou! :(");
                new RabbitManager().pilaMinerado(pilaStr);
            }
        }
        System.out.println("-=+=-=+=-=+=".repeat(4));
    }

    public ArrayList<PilaCoinJson> mineraXpilas(int quantidade) throws NoSuchAlgorithmException, JsonProcessingException {
        int i = 0;
        ArrayList<PilaCoinJson> pilas = new ArrayList<>(quantidade);
        while (i < quantidade){
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
            pilas.add(pj);
            i++;
        }
        return pilas;
    }
}
