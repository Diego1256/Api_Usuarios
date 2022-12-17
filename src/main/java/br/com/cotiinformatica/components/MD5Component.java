package br.com.cotiinformatica.components;

import java.math.BigInteger;
import java.security.MessageDigest;

import org.springframework.stereotype.Component;

@Component
public class MD5Component {

	// método para fazer criotografia no padrão MD5
	public String encrypt(String value) throws Exception {

		MessageDigest md = MessageDigest.getInstance("MD5");

		BigInteger hash = new BigInteger(1, md.digest(value.getBytes()));
		return hash.toString(16);
	}
}



