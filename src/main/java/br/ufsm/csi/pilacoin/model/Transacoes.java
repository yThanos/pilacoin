package br.ufsm.csi.pilacoin.model;

import java.util.Date;

public class Transacoes {
    private byte[] chaveUsuarioOrigem;
    private byte[] chaveUsuarioDestino;
    private byte[] assinatura;
    private String noncePila;
    private Date dataTransacao;
    private Long id;
}
