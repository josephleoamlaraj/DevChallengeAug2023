package com.dws.challenge.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.FundTransfer;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.service.EmailNotificationService;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    
    @Autowired
    EmailNotificationService emailNotify;
    
    @Override
    public void createAccount(Account account) throws DuplicateAccountIdException {
        Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
        if (previousAccount != null) {
            throw new DuplicateAccountIdException(
                    "Account id " + account.getAccountId() + " already exists!");
        }
    }

    @Override
    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    @Override
    public void clearAccounts() {
        accounts.clear();
    }
    
    @Override
    public synchronized void transferFund(FundTransfer fundTransfer) throws Exception {
    	
    	// Validation for not same account id, invalid account id and not having sufficient amount
    	if (fundTransfer.getAccountFrom().equals(fundTransfer.getAccountTo())) {
    		throw new Exception("From and To accounts can not be same.");
    	}
    	
    	if (getAccount(fundTransfer.getAccountFrom()) == null) {
    		throw new Exception("Source Account does not exist.. Account Id: "+fundTransfer.getAccountFrom());
    	}
    	
    	if (getAccount(fundTransfer.getAccountTo()) == null) {
    		throw new Exception("Destination Account does not exist.. Account Id: "+fundTransfer.getAccountTo());
    	}
    	
    	Account frmAcct = getAccount(fundTransfer.getAccountFrom());
    	if ( frmAcct.getBalance().compareTo(fundTransfer.getAmount()) == -1) {
    		throw new Exception("Source Account does not have sufficient amount.");
    	}
    	
    	Account toAcct = getAccount(fundTransfer.getAccountTo());
    	
    	// Do transfer the amounts in both accounts
    	frmAcct.setBalance(frmAcct.getBalance().subtract(fundTransfer.getAmount()));
    	toAcct.setBalance(toAcct.getBalance().add(fundTransfer.getAmount()));
    	
    	// send notification
    	emailNotify.notifyAboutTransfer(frmAcct, "Transferred Amount : "+fundTransfer.getAmount()+" To Account No:"+toAcct.getAccountId());
    	emailNotify.notifyAboutTransfer(toAcct, "Amount : "+fundTransfer.getAmount()+" has been received from Account No:"+frmAcct.getAccountId());
    }
}
