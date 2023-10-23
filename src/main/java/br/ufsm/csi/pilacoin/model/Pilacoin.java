package br.ufsm.csi.pilacoin.model;

import java.io.*;
import java.math.BigInteger;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import org.springframework.stereotype.Service;

@Service
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder(alphabetic = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Pilacoin implements Serializable {
    public static byte[] chavePublica;
    private String nomeMinerador;
    private Date dataHoraCriacao;
    private byte[] magicNumber;
    public static BigInteger dificuldade;
    public enum StatusPila {AG_VALIDACAO, AG_BLOCO, BLOCO_EM_VALIDACAO, VALIDO, INVALIDO}
}
