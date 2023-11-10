package br.ufsm.csi.pilacoin.repositories;

import br.ufsm.csi.pilacoin.model.Pilacoin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PilaRepository extends JpaRepository<Pilacoin, Long> {
}
