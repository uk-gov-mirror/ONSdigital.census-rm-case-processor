package uk.gov.ons.census.casesvc.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.ons.census.casesvc.type.RefusalType;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class RefusalDTO {
  private RefusalType type;
  private String report;
  private String agentId;
  private CollectionCase collectionCase;
}