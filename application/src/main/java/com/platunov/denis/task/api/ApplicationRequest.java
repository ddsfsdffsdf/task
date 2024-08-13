package com.platunov.denis.task.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Value
@Jacksonized
@Builder
public class ApplicationRequest {
    @Pattern(regexp = "\\+[0-9]{11,15}")
    String phone;

    @Email
    String email;

    @Min(0)
    BigDecimal monthlyIncome;

    @Min(0)
    BigDecimal monthlyExpenses;

    @Min(0)
    Integer dependents;

    @NotNull
    MaritalStatus maritalStatus;

    boolean agreeToBeScored;

    @Min(0)
    BigDecimal amount;
}
