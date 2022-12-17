package br.com.cotiinformatica.controllers;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.cotiinformatica.components.JwtComponent;
import br.com.cotiinformatica.components.MD5Component;
import br.com.cotiinformatica.dtos.AlterarSenhaPutDto;
import br.com.cotiinformatica.entities.Historico;
import br.com.cotiinformatica.entities.Usuario;
import br.com.cotiinformatica.repositories.HistoricoRepository;
import br.com.cotiinformatica.repositories.UsuarioRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "Alteração de senha do usuário")
@RestController
public class AlterarSenhaController {

	@Autowired
	UsuarioRepository usuarioRepository;
	
	@Autowired
	HistoricoRepository historicoRepository;
	
	@Autowired
	JwtComponent jwtComponent;
	
	@Autowired
	MD5Component md5Component;
	
	@ApiOperation("Serviço autenticado para alteração da senha do usuário")
	@PutMapping("/api/alterar-senha")
	public ResponseEntity<String> put(@RequestBody AlterarSenhaPutDto dto, HttpServletRequest request) {

		try {
		
			//capturar o token enviado
			String accessToken = request.getHeader("Authorization").replace("Bearer", "").trim();
			String emailUsuario = jwtComponent.getUserFromToken(accessToken);
			
			//criptografar as senhas enviadas..
			String senhaAtual = md5Component.encrypt(dto.getSenhaAtual());
			String novaSenha = md5Component.encrypt(dto.getNovaSenha());
			
			//consultar o usuário no banco de dados atraves do email e da senha
			Usuario usuario = usuarioRepository.findByEmailAndSenha(emailUsuario, senhaAtual);
			
			//verificando se o usuário não foi encontrado
			if(usuario == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body("Usuário não encontrado, verifique a senha atual informada.");
			}
			
			//atualizando a senha do usuário
			usuario.setSenha(novaSenha);
			usuarioRepository.save(usuario);
			
			//gerando um histórico
			Historico historico = new Historico();			
			historico.setDataHora(new Date());
			historico.setOperacao("Atualização da senha do usuário");
			historico.setUsuario(usuario);
			
			historicoRepository.save(historico);
			
			return ResponseEntity.status(HttpStatus.OK)
					.body("Senha de usuário atualizada com sucesso.");
		}
		catch(Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(e.getMessage());
		}		
	}
}



