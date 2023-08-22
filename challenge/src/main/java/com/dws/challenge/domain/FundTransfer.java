package com.dws.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class FundTransfer {

  @NotNull
  @NotEmpty
  private final String accountFrom;
  
  @NotNull
  @NotEmpty
  private final String accountTo;

  @NotNull
  @Min(value = 0, message = "Transfer Amount balance must be positive.")
  private BigDecimal amount;

  @JsonCreator
  public FundTransfer(@JsonProperty("accountFrom") String accountFrom, @JsonProperty("accountTo") String accountTo,
    @JsonProperty("amount") BigDecimal amount) {
	
	this.accountFrom = accountFrom;
	this.accountTo = accountTo;
    this.amount = amount;
  }
  
}
