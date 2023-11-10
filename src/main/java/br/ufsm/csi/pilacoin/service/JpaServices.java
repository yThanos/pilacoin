package br.ufsm.csi.pilacoin.service;

import br.ufsm.csi.pilacoin.model.Pilacoin;
import br.ufsm.csi.pilacoin.model.Usuario;
import br.ufsm.csi.pilacoin.repositories.PilaRepository;
import br.ufsm.csi.pilacoin.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JpaServices {
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    PilaRepository pilaRepository;

    public JpaServices(){}

    public List<Usuario> getUsers(){
        return usuarioRepository.findAll();
    }

    public void saveUser(Usuario user){
        usuarioRepository.save(user);
    }

    public Optional<Usuario> getUserById(Long id){
        return usuarioRepository.findById(id);
    }

    public List<Pilacoin> getPilas(){
        return pilaRepository.findAll();
    }

    public void savePila(Pilacoin pila){
        pilaRepository.save(pila);
    }

    public Optional<Pilacoin> getPilaById(Long id){
        return pilaRepository.findById(id);
    }
}
