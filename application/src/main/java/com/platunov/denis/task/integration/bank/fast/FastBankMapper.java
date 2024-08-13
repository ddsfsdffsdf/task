package com.platunov.denis.task.integration.bank.fast;

import com.platunov.denis.task.api.ApplicationRequest;
import com.platunov.denis.task.api.OfferDto;
import com.platunov.denis.task.integration.bank.fast.client.model.Offer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface FastBankMapper {
    @Mapping(source = "phone", target = "phoneNumber")
    @Mapping(source = "monthlyIncome", target = "monthlyIncomeAmount")
    @Mapping(source = "monthlyExpenses", target = "monthlyCreditLiabilities")
    @Mapping(source = "dependents", target = "dependents")
    @Mapping(source = "agreeToBeScored", target = "agreeToDataSharing")
    com.platunov.denis.task.integration.bank.fast.client.model.ApplicationRequest toBankApplicationRequest(ApplicationRequest applicationRequest);

    OfferDto toOfferDto(Offer offer);
}
