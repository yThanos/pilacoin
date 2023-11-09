package br.ufsm.csi.pilacoin.service;

import br.ufsm.csi.pilacoin.model.TransferirPila;
import br.ufsm.csi.pilacoin.model.Usuario;
import br.ufsm.csi.pilacoin.util.Constants;

import java.util.Date;

public class TransferenciaService {
    public void tranferirPila(Usuario destino){//ToDo: receber String e procurar usuario no banco por nome

        //ToDo: pegar noce do banco, tipo top(1) e ja mover de Pilas para historico

        String nonce = "";
        TransferirPila tp = TransferirPila.builder().chaveUsuarioDestino(destino.getChavePublciaUsuario()).
                nomeUsuarioDestino(destino.getNomeUsuario()).nomeUsuarioOrigem(Constants.USERNAME).
                chaveUsuarioOrigem(Constants.PUBLIC_KEY.toString().getBytes()).
                noncePila(nonce).dataTransacao(new Date()).build();
        new RabbitManager().tranferirPila(tp);
    }
}
