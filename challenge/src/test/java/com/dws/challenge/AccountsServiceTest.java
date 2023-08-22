package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.service.AccountsService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

	@Autowired
	private AccountsService accountsService;

	private MockMvc mockMvc;

	@Test
	void addAccount() {
		Account account = new Account("Id-123");
		account.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account);

		assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
	}

	@Test
	void addAccount_failsOnDuplicateId() {
		String uniqueId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueId);
		this.accountsService.createAccount(account);

		try {
			this.accountsService.createAccount(account);
			fail("Should have failed when adding duplicate account");
		} catch (DuplicateAccountIdException ex) {
			assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
		}
	}

	@Test
	void transferFund() throws Exception {
		Account accountFrm = new Account("Id-123");
		accountFrm.setBalance(new BigDecimal(1000));    
		this.accountsService.createAccount(accountFrm);

		Account accountTo = new Account("Id-133");
		accountTo.setBalance(new BigDecimal(500));    
		this.accountsService.createAccount(accountTo);

		this.mockMvc.perform(post("/v1/accounts/amountTransfer").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountFrom\":\"Id-123\",\"accountTo\":\"Id-133\",\"amount\":300}")).andExpect(status().isCreated());

		assertThat(accountFrm.getBalance()).isEqualByComparingTo("700");
	}

	@Test
	void transferFund_failed() {
		Account accountFrm = new Account("Id-211");
		accountFrm.setBalance(new BigDecimal(1000));    
		this.accountsService.createAccount(accountFrm);

		Account accountTo = new Account("Id-212");
		accountTo.setBalance(new BigDecimal(500));    
		this.accountsService.createAccount(accountTo);

		try {
			this.mockMvc.perform(post("/v1/accounts/amountTransfer").contentType(MediaType.APPLICATION_JSON)
					.content("{\"accountFrom\":\"Id-123\",\"accountTo\":\"Id-133\",\"amount\":2000}")).andExpect(status().isCreated());

			fail("The Fund Transfer should have failed when Source account not having sufficient balance.");
		} catch (Exception ex) {
			assertThat(ex.getMessage()).isEqualTo("Source Account does not have sufficient amount.");
		}
	}

}
