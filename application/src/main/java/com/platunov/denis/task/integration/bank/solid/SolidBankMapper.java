package com.platunov.denis.task.integration.bank.solid;

import com.platunov.denis.task.api.ApplicationRequest;
import com.platunov.denis.task.api.OfferDto;
import com.platunov.denis.task.integration.bank.solid.client.model.Offer;
import org.mapstruct.Mapper;

@Mapper
public interface SolidBankMapper {

    com.platunov.denis.task.integration.bank.solid.client.model.ApplicationRequest toBankApplicationRequest(ApplicationRequest applicationRequest);

    OfferDto toOfferDto(Offer offer);
}
