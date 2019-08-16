package uk.gov.ons.census.casesvc.logging;

import static uk.gov.ons.census.casesvc.utility.EventHelper.createEventDTO;
import static uk.gov.ons.census.casesvc.utility.JsonHelper.convertObjectToJson;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.ons.census.casesvc.model.dto.EventDTO;
import uk.gov.ons.census.casesvc.model.dto.FulfilmentRequestDTO;
import uk.gov.ons.census.casesvc.model.dto.PayloadDTO;
import uk.gov.ons.census.casesvc.model.dto.RefusalDTO;
import uk.gov.ons.census.casesvc.model.entity.Case;
import uk.gov.ons.census.casesvc.model.entity.Event;
import uk.gov.ons.census.casesvc.model.entity.EventType;
import uk.gov.ons.census.casesvc.model.entity.UacQidLink;
import uk.gov.ons.census.casesvc.model.repository.EventRepository;

@Component
public class EventLogger {
  private final EventRepository eventRepository;

  public EventLogger(EventRepository eventRepository) {
    this.eventRepository = eventRepository;
  }

  public void logEvent(
      UacQidLink uacQidLink, String eventDescription, EventType eventType, String payloadJson) {
    logEvent(uacQidLink, eventDescription, eventType, payloadJson, createEventDTO(eventType));
  }

  public void logRefusalEvent(
      Case caze, String eventDescription, EventType eventType, RefusalDTO payload, EventDTO event) {

    Event loggedEvent = new Event();

    loggedEvent.setId(UUID.randomUUID());
    loggedEvent.setCaze(caze);
    loggedEvent.setCaseId(UUID.fromString(payload.getCollectionCase().getId()));
    loggedEvent.setEventDate(event.getDateTime());
    loggedEvent.setRmEventProcessed(OffsetDateTime.now());
    loggedEvent.setEventDescription(eventDescription);
    loggedEvent.setEventType(eventType);
    loggedEvent.setEventChannel(event.getChannel());
    loggedEvent.setEventSource(event.getSource());

    if (StringUtils.isEmpty(event.getTransactionId())) {
      loggedEvent.setEventTransactionId(UUID.randomUUID());
    } else {
      loggedEvent.setEventTransactionId(event.getTransactionId());
    }

    loggedEvent.setEventPayload(convertObjectToJson(payload));

    eventRepository.save(loggedEvent);
  }

  public void logFulfilmentRequestedEvent(
      Case caze,
      UUID caseId,
      OffsetDateTime eventMetaDataDateTime,
      String eventDescription,
      EventType eventType,
      FulfilmentRequestDTO payload,
      EventDTO event) {

    Event loggedEvent = new Event();
    loggedEvent.setCaze(caze);
    loggedEvent.setId(UUID.randomUUID());
    loggedEvent.setCaseId(caseId);
    loggedEvent.setEventDate(eventMetaDataDateTime);
    loggedEvent.setEventDescription(eventDescription);
    loggedEvent.setEventType(eventType);
    loggedEvent.setEventPayload(convertObjectToJson(payload));
    loggedEvent.setEventChannel(event.getChannel());
    loggedEvent.setEventSource(event.getSource());

    if (StringUtils.isEmpty(event.getTransactionId())) {
      loggedEvent.setEventTransactionId(UUID.randomUUID());
    } else {
      loggedEvent.setEventTransactionId(event.getTransactionId());
    }

    loggedEvent.setRmEventProcessed(OffsetDateTime.now());

    eventRepository.save(loggedEvent);
  }

  public void logEvent(
      UacQidLink uacQidLink,
      String eventDescription,
      EventType eventType,
      String jsonPayload,
      EventDTO event) {

    Event loggedEvent = new Event();
    loggedEvent.setId(UUID.randomUUID());

    loggedEvent.setEventDate(event.getDateTime());
    loggedEvent.setRmEventProcessed(OffsetDateTime.now());
    loggedEvent.setEventDescription(eventDescription);
    loggedEvent.setUacQidLink(uacQidLink);
    loggedEvent.setEventType(eventType);

    // Only set Case Id if Addressed
    if (uacQidLink.getCaze() != null) {
      loggedEvent.setCaseId(uacQidLink.getCaze().getCaseId());
    }

    loggedEvent.setEventChannel(event.getChannel());
    loggedEvent.setEventSource(event.getSource());

    if (StringUtils.isEmpty(event.getTransactionId())) {
      loggedEvent.setEventTransactionId(UUID.randomUUID());
    } else {
      loggedEvent.setEventTransactionId(event.getTransactionId());
    }

    loggedEvent.setEventPayload(jsonPayload);

    eventRepository.save(loggedEvent);
  }

  public void logEvent(
      UacQidLink uacQidLink, String eventDescription, PayloadDTO payload, EventDTO event) {
    logEvent(uacQidLink, eventDescription, event.getType(), convertObjectToJson(payload), event);
  }
}