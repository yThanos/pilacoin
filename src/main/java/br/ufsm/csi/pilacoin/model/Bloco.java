package br.ufsm.csi.pilacoin.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;

@Data
@Builder
public class Bloco {
    private int numeroBloco;
    private String nonceBlocoAnterior;
    private String nonce;
    private byte[] chaveUsuarioMinerador;
    private ArrayList<Transacoes> transacoes;
}
