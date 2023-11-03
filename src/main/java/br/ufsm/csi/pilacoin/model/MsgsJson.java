package br.ufsm.csi.pilacoin.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MsgsJson {
    private String msg;
    private String nomeUsuario;
    private String nonce;
    private String queue;
}
