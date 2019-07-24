package com.parse.twotwobet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parse.twotwobet.entity.Event;
import com.parse.twotwobet.entity.Outcome;
import com.parse.twotwobet.entity.Tournament;
import lombok.extern.log4j.Log4j2;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.util.HttpConstants;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.asynchttpclient.Dsl.asyncHttpClient;

@Log4j2
@Service
public class EventParseService {

    private static final String GET_EVENTS_BY_TOURNAMENT_JSON_URL = "https://nodejs08.tglab.io/cache/20/en/ua/" + GeneralInfoParseService.REPLACE_PART_URL + "/prematch-by-tournaments.json";
    private static final String GET_OUTCOMES_BY_EVENT_JSON_URL = "https://nodejs08.tglab.io/cache/20/en/ua/" + GeneralInfoParseService.REPLACE_PART_URL + "/single-pre-event.json";

    public static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);

    private ObjectMapper objectMapper = new ObjectMapper();
    private AsyncHttpClient asyncHttpClient = asyncHttpClient();

    public List<Event> parseEventsByTournament(Tournament tournament){
        String urlGetEventsForTournament = GET_EVENTS_BY_TOURNAMENT_JSON_URL.replace(GeneralInfoParseService.REPLACE_PART_URL, String.valueOf(tournament.getId()));

        Request getEventsForTournament = new RequestBuilder(HttpConstants.Methods.GET)
                .setUrl(urlGetEventsForTournament)
                .build();

        List<Event> eventListForTournament = new ArrayList<>();
        try {
            String responseBody = asyncHttpClient.executeRequest(getEventsForTournament).get().getResponseBody();

            JsonNode jsonNodeEvents = objectMapper.readTree(responseBody);
            JsonNode eventsJson = jsonNodeEvents.get("events");

            eventsJson.forEach(event -> {
                Event eventForList = new Event();
                eventForList.setId(event.get("id").asInt());
                eventForList.setSportId(event.get("sport_id").asInt());
                eventForList.setTeamHome(event.get("teams").get("home").asText());
                eventForList.setTeamAway(event.get("teams").get("away").asText());
                eventForList.setDateStart(LocalDateTime.parse(event.get("date_start").asText(), INPUT_FORMATTER));

                eventListForTournament.add(eventForList);
            });

            tournament.setEvents(eventListForTournament);
        }catch (Exception ex){
            log.error(ex);
        }
        return eventListForTournament;
    }

    public List<Outcome> parseOutcomesByEvent(Event event){
        String urlGetOutcomesByEvent = GET_OUTCOMES_BY_EVENT_JSON_URL.replace(GeneralInfoParseService.REPLACE_PART_URL, String.valueOf(event.getId()));

        Request getOutcomesByEvent = new RequestBuilder(HttpConstants.Methods.GET)
                .setUrl(urlGetOutcomesByEvent)
                .build();

        List<Outcome> outcomeList = new ArrayList<>();
        try {
            String responseOutcomesByEvent = asyncHttpClient.executeRequest(getOutcomesByEvent).get().getResponseBody();

            JsonNode jsonNodeOutcomesByEvent = objectMapper.readTree(responseOutcomesByEvent);
            JsonNode jsonOutcomes = jsonNodeOutcomesByEvent.get("odds");


            jsonOutcomes.forEach(outcome -> {
                Outcome outcomeForList = new Outcome();
                outcomeForList.setId(outcome.get("id").asInt());
                outcomeForList.setFilterId(outcome.get("filter_id").asInt());
                outcomeForList.setName(outcome.get("team_name").get("en").asText());
                outcomeForList.setCoefficient(outcome.get("odd_value").asDouble());
                outcomeForList.setAdditionalValue(outcome.get("additional_value").asText());

                outcomeList.add(outcomeForList);
            });

            event.setOutcomes(outcomeList);
        }catch (Exception ex){
            log.error(ex);
        }
        return outcomeList;
    }
}
