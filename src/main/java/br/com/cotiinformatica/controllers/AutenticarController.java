package br.com.cotiinformatica.controllers;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.cotiinformatica.components.JwtComponent;
import br.com.cotiinformatica.components.MD5Component;
import br.com.cotiinformatica.dtos.AutenticarPostDto;
import br.com.cotiinformatica.entities.Historico;
import br.com.cotiinformatica.entities.Usuario;
import br.com.cotiinformatica.repositories.HistoricoRepository;
import br.com.cotiinformatica.repositories.UsuarioRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "Autenticação de usuários")
@RestController
public class AutenticarController {

	@Autowired
	UsuarioRepository usuarioRepository;

	@Autowired
	HistoricoRepository historicoRepository;

	@Autowired
	MD5Component md5Component;
	
	@Autowired
	JwtComponent jwtComponent;

	@ApiOperation("Serviço para autenticação de usuários e geração de TOKEN JWT.")
	@PostMapping("api/autenticar")
	public ResponseEntity<String> post(@RequestBody AutenticarPostDto dto) {

		try {
			
			//consultando o usuário no banco de dados através do email e senha
			Usuario usuario = usuarioRepository.findByEmailAndSenha(dto.getEmail(), md5Component.encrypt(dto.getSenha()));
			
			//verificar se o usuário foi encontrado
			if(usuario != null) {
				
				String accessToken = jwtComponent.generateToken(usuario);
				
				Historico historico = new Historico();
				
				historico.setDataHora(new Date());
				historico.setOperacao("Autenticação de usuário");
				historico.setUsuario(usuario);
				
				historicoRepository.save(historico);
				
				return ResponseEntity.status(HttpStatus.OK)
						.body(accessToken);
			}
			else {
				
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body("Acesso negado. Usuário não encontrado.");
			}
		}
		catch(Exception e) {
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Erro ao autenticar usuário: " + e.getMessage());
		}		
	}
}



