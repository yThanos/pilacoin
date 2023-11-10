package br.ufsm.csi.pilacoin.controllers;

import br.ufsm.csi.pilacoin.model.Pilacoin;
import br.ufsm.csi.pilacoin.model.Usuario;
import br.ufsm.csi.pilacoin.service.JpaServices;
import br.ufsm.csi.pilacoin.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/")
public class JpaController {
    @Autowired
    JpaServices jpaServices;

    @GetMapping("getUsers")
    public List<Usuario> getUsers(){
        return jpaServices.getUsers();
    }

    @GetMapping("getPilas")
    public List<Pilacoin> getPilas(){
        return jpaServices.getPilas();
    }

    @GetMapping("teste")
    public void teste(){
        jpaServices.saveUser(Usuario.builder().nomeUsuario(Constants.USERNAME).chavePublciaUsuario(Constants.PUBLIC_KEY.toString().getBytes()).build());
    }
}
