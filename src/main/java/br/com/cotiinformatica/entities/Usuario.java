package br.com.cotiinformatica.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class Usuario {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="idusuario")
	private Integer idUsuario;
	
	@Column(name="nome", length = 150, nullable = false)
	private String nome;
	
	@Column(name="email", length = 100, nullable = false, unique = true)
	private String email;
	
	@Column(name="senha", length = 40, nullable = false)
	private String senha;
	
	@OneToMany(mappedBy = "usuario")
	private List<Historico> historicos;
}
