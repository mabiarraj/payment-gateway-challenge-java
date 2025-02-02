package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BankProcessorRequest {
  @JsonProperty("card_number")
  private String card_number;
  @JsonProperty("expiry_date")
  private String expiry_date;
  private String currency;
  private int amount;
  private int cvv;

}
