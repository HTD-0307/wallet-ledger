package com.doht.wallet_ledger;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class WalletLedgerApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(WalletLedgerApplication.class)
				.headless(false)
				.run(args);
	}

}
