package br.com.cotiinformatica.controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.cotiinformatica.components.JwtComponent;
import br.com.cotiinformatica.dtos.HistoricoGetDto;
import br.com.cotiinformatica.entities.Historico;
import br.com.cotiinformatica.entities.Usuario;
import br.com.cotiinformatica.repositories.HistoricoRepository;
import br.com.cotiinformatica.repositories.UsuarioRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "Consulta de histórico de usuário")
@RestController
public class HistoricoController {

	@Autowired
	UsuarioRepository usuarioRepository;
	
	@Autowired
	HistoricoRepository historicoRepository;
	
	@Autowired
	JwtComponent jwtComponent;
	
	@ApiOperation("Serviço autenticado para consulta de histórico de operações de usuário.")
	@GetMapping("api/historico")
	public ResponseEntity<List<HistoricoGetDto>> get(HttpServletRequest request) {
		
		try {
			
			//[Authorization] Bearer <<TOKEN>>
			String accessToken = request.getHeader("Authorization").replace("Bearer", "").trim();
			String emailUsuario = jwtComponent.getUserFromToken(accessToken);
			
			//consultar o usuário no banco de dados através do email
			Usuario usuario = usuarioRepository.findByEmail(emailUsuario);
			
			//consultar o histórico do usuário
			List<Historico> historicos = historicoRepository.findAllByUsuario(usuario.getIdUsuario());
			
			//retornar uma lista de HistoricoGetDto
			List<HistoricoGetDto> lista = new ArrayList<HistoricoGetDto>();
			
			for(Historico historico : historicos) {
				
				HistoricoGetDto dto = new HistoricoGetDto();
				dto.setDataHora(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(historico.getDataHora()));
				dto.setOperacao(historico.getOperacao());
				
				lista.add(dto);
			}
			
			return ResponseEntity.status(HttpStatus.OK)
					.body(lista);
		}
		catch(Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(null);
		}
	}

}