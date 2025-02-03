package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import lombok.Data;

@Data
public class PostPaymentRequest implements Serializable {

  @JsonProperty("card_number")
  @NotNull(message = "Card number is required")
  @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
  private String cardNumber;

  @NotNull(message = "Expiry month is required")
  @JsonProperty("expiry_month")
  @Min(value = 1, message = "Expiration month must be between 1 and 12")
  @Max(value = 12, message = "Expiration month must be between 1 and 12")
  private int expiryMonth;

  @NotNull(message = "Expiry year is required")
  @Min(value = 2025, message = "Expiration year must be at least 2025")
  @JsonProperty("expiry_year")
  private int expiryYear;

  @NotNull(message = "Currency is required")
  @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code")
  private String currency;

  @NotNull(message = "Amount is required")
  @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
  private BigDecimal amount;

  @NotNull(message = "CVV is required")
  @Pattern(regexp = "\\d{3}", message = "CVV must be 3 digits")
  private String cvv;

  public String getExpiryDate() {
    return String.format("%02d/%d", expiryMonth, expiryYear);
  }

  @JsonIgnore
  public boolean isExpiryDateValid() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
    YearMonth cardExpiry = YearMonth.parse(getExpiryDate(), formatter);
    return cardExpiry.isAfter(YearMonth.now());
  }

  @Override
  public String toString() {
    return "PostPaymentRequest{" +
        "cardNumber=" + cardNumber +
        ", expiryMonth=" + expiryMonth +
        ", expiryYear=" + expiryYear +
        ", currency='" + currency + '\'' +
        ", amount=" + amount +
        ", cvv=" + cvv +
        '}';
  }
}
