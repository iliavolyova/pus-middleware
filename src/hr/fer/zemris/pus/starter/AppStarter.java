package hr.fer.zemris.pus.starter;

import hr.fer.zemris.pus.provider.ServiceProvider;
import hr.fer.zemris.pus.register.CentralRegister;

public class AppStarter {

	public static void main(String[] args) throws InterruptedException {
		
		new Thread(new CentralRegister(8000)).start();
		
		Thread.sleep(2000);
		new Thread(new ServiceProvider(8080, "charlie")).start();
		new Thread(new ServiceProvider(8081, "tango")).start();
		new Thread(new ServiceProvider(8082, "foxtrot")).start();
		new Thread(new ServiceProvider(8083, "bravo")).start();
	}
	
}
