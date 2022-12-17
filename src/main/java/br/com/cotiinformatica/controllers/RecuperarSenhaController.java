package br.com.cotiinformatica.controllers;

import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.javafaker.Faker;

import br.com.cotiinformatica.components.EmailComponent;
import br.com.cotiinformatica.components.EmailComponentModel;
import br.com.cotiinformatica.components.MD5Component;
import br.com.cotiinformatica.dtos.RecuperarSenhaPostDto;
import br.com.cotiinformatica.entities.Historico;
import br.com.cotiinformatica.entities.Usuario;
import br.com.cotiinformatica.repositories.HistoricoRepository;
import br.com.cotiinformatica.repositories.UsuarioRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "Recuperação de senha de usuários")
@RestController
public class RecuperarSenhaController {
	
	@Autowired
	UsuarioRepository usuarioRepository;
	
	@Autowired
	HistoricoRepository historicoRepository;
	
	@Autowired
	EmailComponent emailComponent;
	
	@Autowired
	MD5Component md5Component;

	@ApiOperation("Serviço para recuperação de senha por email do usuário")
	@PostMapping("api/recuperar-senha")
	public ResponseEntity<String> post(@RequestBody RecuperarSenhaPostDto dto) {

		try {
			
			//buscar o usuário no banco de dados através do email
			Usuario usuario = usuarioRepository.findByEmail(dto.getEmail());
			
			//verificar se o usuário não foi encontrado
			if(usuario == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body("Usuário não encontrado, verifique o email informado.");
			}
			
			//gerando uma nova senha para o usuário
			Faker faker = new Faker(new Locale("pt-BR"));
			String novaSenha = faker.internet().password(8, 10);
			
			//enviando a nova senha por email
			enviarMensagemDeRecuperacaoDeSenha(usuario, novaSenha);
			
			//atualizando a senha do usuário no banco de dados
			usuario.setSenha(md5Component.encrypt(novaSenha));
			usuarioRepository.save(usuario);
			
			//criando o histórico do usuário
			Historico historico = new Historico();
			
			historico.setDataHora(new Date());
			historico.setOperacao("Recuperação de Senha do usuário");
			historico.setUsuario(usuario);
			
			//gravando no banco de dados
			historicoRepository.save(historico);
			
			//Sucesso HTTO 200 (OK)
			return ResponseEntity.status(HttpStatus.OK)
					.body("Recuperação de senha realizada com sucesso.");
		}
		catch(Exception e) {
			//Erro HTTP 500 (Internal Server Error)
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(e.getMessage());
		}
	}
	
	//método privado para fazer o envio do email
	private void enviarMensagemDeRecuperacaoDeSenha(Usuario usuario, String novaSenha) throws Exception {
				
		EmailComponentModel model = new EmailComponentModel();
		model.setEmailTo(usuario.getEmail());
			
		//assunto e corpo da mensagem
		model.setSubject("Recuperação de senha realizado com sucesso! COTI Informática");
		String texto = "<div style='border: 2px solid #ccc; padding: 40px; margin: 40px;'>"
					 + "<center>"
					 + "<img src='https://www.cotiinformatica.com.br/imagens/logo-coti-informatica.png'/>"
					 + "<h2>Olá, <strong>" + usuario.getNome() + "</strong></h2>"
					 + "<p>Uma nova senha foi gerada com sucesso!</p>"					
					 + "<p>Acesse o sistema com a senha: " + novaSenha + "</p>"
					 + "<p>Att,</p>"
					 + "<p>Equipe COTI Informática</p>"
					 + "</center>"
					 + "</div>";
				
		model.setBody(texto);
		model.setBodyHtml(true);
				
		emailComponent.sendMessage(model);
	}
}