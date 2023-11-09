package br.ufsm.csi.pilacoin.service;

import br.ufsm.csi.pilacoin.model.BlocoJson;
import br.ufsm.csi.pilacoin.model.MsgsJson;
import br.ufsm.csi.pilacoin.model.ValidaBlocoJson;
import br.ufsm.csi.pilacoin.util.Constants;
import br.ufsm.csi.pilacoin.util.PilaUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BlocoService {
    public String mineraBloco(String blocoJson) throws JsonProcessingException, NoSuchAlgorithmException {
        String blocoMinerado = null;
        System.out.println("Descobriu um bloco!");
        ObjectMapper om = new ObjectMapper();
        BlocoJson bloco = om.readValue(blocoJson, BlocoJson.class);
        BigInteger hash;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        bloco.setNomeUsuarioMinerador(Constants.USERNAME);
        bloco.setChaveUsuarioMinerador(Constants.PUBLIC_KEY.toString().getBytes());
        boolean loop = true;
        while(loop){
            bloco.setNonce(new PilaUtil().geraNonce());
            hash = new BigInteger(md.digest(om.writeValueAsString(bloco).getBytes(StandardCharsets.UTF_8))).abs();
            if (hash.compareTo(Constants.DIFFICULTY) < 0){
                blocoMinerado = om.writeValueAsString(bloco);
                loop = false;
            }
        }
        MsgsJson msg = MsgsJson.builder().msg("Bloco descoberto e minerado!").
                lida(false).nomeUsuario(Constants.USERNAME).queue("Decobre bloco").build();
        RabbitManager.mensagens.add(msg);
        return blocoMinerado;
    }

    public void validaBloco(String blocoJson) throws NoSuchAlgorithmException {
        System.out.println("XXXXXXXXXX".repeat(4));
        System.out.println("Validando bloco!");
        ObjectMapper om = new ObjectMapper();
        BlocoJson bloco;
        try {
            bloco = om.readValue(blocoJson, BlocoJson.class);
        } catch (JsonProcessingException e) {
            new RabbitManager().blocoMinerado(blocoJson);
            System.out.println("Erro conversão");
            return;
        }

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        BigInteger hash = new BigInteger(md.digest(blocoJson.getBytes(StandardCharsets.UTF_8))).abs();
        System.out.println(hash);
        System.out.println(Constants.DIFFICULTY);
        if(hash.compareTo(Constants.DIFFICULTY) < 0){
            ValidaBlocoJson vbj = ValidaBlocoJson.builder().
                    assinaturaBloco(new PilaUtil().getAssinatura(blocoJson)).bloco(bloco).
                    chavePublicaValidador(Constants.PUBLIC_KEY.toString().getBytes()).
                    nomeValidador(Constants.USERNAME).build();
            try {
                new RabbitManager().blocoValidado(om.writeValueAsString(bloco));
                System.out.println("Valido! :)");
            } catch (JsonProcessingException e) {
                new RabbitManager().blocoMinerado(blocoJson);
                System.out.println("Erro conversão");
                return;
            }
        } else {
            new RabbitManager().blocoMinerado(blocoJson);
            System.out.println("Não validou :(");
        }
        System.out.println("XXXXXXXXXX".repeat(4));
    }
}
