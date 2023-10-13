package br.ufsm.csi.pilacoin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Difficulty {
    private String dificuldade;
    private long inicio;
    private long validadeFinal;
}
