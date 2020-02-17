package uk.gov.ons.census.casesvc.service;

import static uk.gov.ons.census.casesvc.utility.QuestionnaireTypeHelper.iscontinuationQuestionnaireTypes;

import org.springframework.stereotype.Component;
import uk.gov.ons.census.casesvc.model.entity.Case;
import uk.gov.ons.census.casesvc.model.entity.UacQidLink;

@Component
public class CaseReceiptService {
  private final CaseService caseService;

  public CaseReceiptService(CaseService caseService) {
    this.caseService = caseService;
  }

  public void receiptCase(UacQidLink uacQidLink) {
    Case caze = uacQidLink.getCaze();

    if (caze.isReceiptReceived()) return;

    if (!iscontinuationQuestionnaireTypes(uacQidLink.getQid())) {
      caze.setReceiptReceived(true);
      caseService.saveAndEmitCaseUpdatedEvent(caze);
    }
  }
}