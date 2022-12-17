package br.com.cotiinformatica.components;

import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import br.com.cotiinformatica.JwtSecurity;
import br.com.cotiinformatica.entities.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtComponent {

	// método para gerar e retornar o TOKEN do usuário
	public String generateToken(Usuario usuario) throws Exception {

		String secretKey = JwtSecurity.SECRET; //chave antifalsificação do TOKEN

		List<GrantedAuthority> grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER");

		String token = Jwts.builder().setId("usuariosapi").setSubject(usuario.getEmail())
				.claim("authorities",
						grantedAuthorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 6000000))
				.signWith(SignatureAlgorithm.HS512, secretKey.getBytes()).compact();

		return token;
	}
	
	// método para ler o conteudo de um TOKEN recebido pela API
	public String getUserFromToken(String token) {
		return getContentFromToken(token, Claims::getSubject);
	}
	
	private <T> T getContentFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = Jwts.parser().setSigningKey(JwtSecurity.SECRET.getBytes())
				.parseClaimsJws(token).getBody();
		return claimsResolver.apply(claims);
	}
	
}