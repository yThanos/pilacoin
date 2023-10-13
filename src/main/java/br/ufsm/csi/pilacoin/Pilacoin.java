package br.ufsm.csi.pilacoin;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Random;

import br.ufsm.csi.pilacoin.model.Difficulty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder(alphabetic = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Pilacoin implements Serializable {
    private byte[] chavePublica;
    private String nomeMinerador;
    private Date dataHoraCriacao;
    private byte[] magicNumber;
    public static BigInteger dificuldade;
    public enum StatusPila {AG_VALIDACAO, AG_BLOCO, BLOCO_EM_VALIDACAO, VALIDO, INVALIDO}

    @SneakyThrows
    @RabbitListener(queues = "dificuldade")
    public void getDificuldade(@Payload String sla){
        ObjectMapper objectMapper = new ObjectMapper();
        Difficulty diff = objectMapper.readValue(sla, Difficulty.class);
        dificuldade = new BigInteger(diff.getDificuldade(), 16).abs();
    }

    @RabbitListener(queues = "pila-validado")
    public void getValidos(@Payload String valido){
        System.out.println(valido);
    }

    @RabbitListener(queues = "clients-errors")
    public void getErrors(@Payload String valido){
        System.out.println("clients-errors: "+valido);
    }

    @RabbitListener(queues = "clients-msgs")
    public void getMsgs(@Payload String valido){
        System.out.println("clients-msgs: "+valido);
    }
}
