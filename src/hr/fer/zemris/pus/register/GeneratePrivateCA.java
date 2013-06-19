package hr.fer.zemris.pus.register;

import hr.fer.zemris.pus.middleware.Crypto;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignedObject;
import java.security.cert.X509Certificate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GeneratePrivateCA extends HttpServlet{
	
	private static final long serialVersionUID = -194647058213092438L;
	private KeyPair regPair;
	
	public GeneratePrivateCA(KeyPair pair){
		this.regPair = pair;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		KeyPair kp;
		X509Certificate spcert;
		SignedObject signedCert = null;
		try {
			kp = KeyPairGenerator.getInstance("RSA").generateKeyPair();
			spcert = Crypto.generateCertificate("CN=Test, L=London, C=GB", kp, 30, "RSA");
			Signature sig = Signature.getInstance(regPair.getPrivate().getAlgorithm());
			signedCert = new SignedObject(spcert,regPair.getPrivate(), sig);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
		
		ObjectOutputStream oos = new ObjectOutputStream(resp.getOutputStream());
		oos.writeObject(signedCert);
		oos.flush();
		oos.close();
		
	}
	
}
