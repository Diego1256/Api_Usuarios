package br.com.cotiinformatica.controllers;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.cotiinformatica.components.EmailComponent;
import br.com.cotiinformatica.components.EmailComponentModel;
import br.com.cotiinformatica.components.MD5Component;
import br.com.cotiinformatica.dtos.CriarContaPostDto;
import br.com.cotiinformatica.entities.Historico;
import br.com.cotiinformatica.entities.Usuario;
import br.com.cotiinformatica.repositories.HistoricoRepository;
import br.com.cotiinformatica.repositories.UsuarioRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "Criação de conta de usuários")
@RestController
public class CriarContaController {

	@Autowired //inicialização automática (injeção de dependência)
	UsuarioRepository usuarioRepository; //atributo
	
	@Autowired //inicialização automática (injeção de dependência)
	HistoricoRepository historicoRepository; //atributo
	
	@Autowired //inicialização automática (injeção de dependência)
	MD5Component md5Component; //atributo
	
	@Autowired //inicialização automática (injeção de dependência)
	EmailComponent emailComponent; //atributo
	
	@ApiOperation("Serviço para cadastro e criação de conta de usuários")
	@PostMapping("api/criar-conta")
	public ResponseEntity<String> post(@RequestBody CriarContaPostDto dto) {

		try {
			
			//verificar se o email informado já está cadastrado no banco de dados
			if(usuarioRepository.findByEmail(dto.getEmail()) != null) {
				//HTTP 400 - BAD REQUEST (CLIENT ERROR)
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body("O email informado já está cadastrado no sistema, tente outro.");
			}
			
			//capturando os dados do usuário
			Usuario usuario = new Usuario();
			
			usuario.setNome(dto.getNome());
			usuario.setEmail(dto.getEmail());
			usuario.setSenha(md5Component.encrypt(dto.getSenha()));
			
			//gravando no banco de dados
			usuarioRepository.save(usuario);
			
			//criando o histórico do usuário
			Historico historico = new Historico();
			
			historico.setDataHora(new Date());
			historico.setOperacao("Cadastro de conta de usuário");
			historico.setUsuario(usuario);
			
			//gravando no banco de dados
			historicoRepository.save(historico);
			
			//enviando email para o usuário
			enviarMensagemDeBoasVindas(usuario);
			
			//HTTP 201 - CREATED
			return ResponseEntity.status(HttpStatus.CREATED)
					.body("Usuário " + usuario.getNome() + ", cadastrado com sucesso!");
		}
		catch(Exception e) {
			//HTTP 500 - INTERNAL SERVER ERROR
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(e.getMessage());
		}		
	}
	
	//método privado para fazer o envio do email
	private void enviarMensagemDeBoasVindas(Usuario usuario) throws Exception {
			
		EmailComponentModel model = new EmailComponentModel();
		model.setEmailTo(usuario.getEmail());
		
		//assunto e corpo da mensagem
		model.setSubject("Parabéns, sua conta de usuário foi criada com sucesso! COTI Informática");
		String texto = "<div style='border: 2px solid #ccc; padding: 40px; margin: 40px;'>"
					 + "<center>"
					 + "<img src='https://www.cotiinformatica.com.br/imagens/logo-coti-informatica.png'/>"
					 + "<h2>Parabéns, <strong>" + usuario.getNome() + "</strong></h2>"
					 + "<p>Sua conta de usuário foi cadastrada com sucesso em nosso sistema, seguem os dados:</p>"					
					 + "<p>Nome: " + usuario.getNome() + "</p>"
					 + "<p>Email: " + usuario.getEmail() + "</p>"
					 + "<p>Att,</p>"
					 + "<p>Equipe COTI Informática</p>"
					 + "</center>"
					 + "</div>";
			
		model.setBody(texto);
		model.setBodyHtml(true);
			
		emailComponent.sendMessage(model);
	}
}