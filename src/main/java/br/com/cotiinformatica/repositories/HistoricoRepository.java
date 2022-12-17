package br.com.cotiinformatica.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.cotiinformatica.entities.Historico;

@Repository
public interface HistoricoRepository extends CrudRepository<Historico, Integer> {

	@Query("from Historico h join h.usuario u where u.idUsuario = :pIdUsuario order by h.dataHora desc")
	List<Historico> findAllByUsuario(@Param("pIdUsuario") Integer idUsuario) throws Exception;
	
}