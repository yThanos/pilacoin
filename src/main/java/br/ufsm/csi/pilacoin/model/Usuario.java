package br.ufsm.csi.pilacoin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_usuario")
    private Long id;

    @Column(name = "chave_publica")
    private byte[] chavePublciaUsuario;

    @Column(name = "nome")
    private String nomeUsuario;

    @JsonIgnore
    @OneToMany
    @JoinColumn(name = "id_pila")
    private Set<Pilacoin> pilas;

}
