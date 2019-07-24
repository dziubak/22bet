package com.parse.twotwobet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parse.twotwobet.entity.Event;
import com.parse.twotwobet.entity.Filter;
import com.parse.twotwobet.entity.Outcome;
import com.parse.twotwobet.entity.Sport;
import com.parse.twotwobet.entity.Tournament;
import lombok.extern.log4j.Log4j2;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.util.HttpConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.asynchttpclient.Dsl.asyncHttpClient;

@Log4j2
@Service
public class ParseService {

    private static final String GET_MAIN_MENU_JSON_URL = "https://nodejs08.tglab.io/cache/20/en/ua/Europe%2FUzhgorod/prematch-menu.json";

    private List<String> sportsName = Arrays.asList("football", "tennis", "ice_hockey", "basketball", "baseball", "rugby");
    private List<Sport> sportList = new ArrayList<>();
    private List<Tournament> tournamentList = new ArrayList<>();

    private List<Filter> filters = new ArrayList<>();

    private DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private GeneralInfoParseService generalInfoParseService;

    @PostConstruct
    public void init() throws ExecutionException, InterruptedException, IOException {
        AsyncHttpClient asyncHttpClient = asyncHttpClient();

        Request getRequest = new RequestBuilder(HttpConstants.Methods.GET)
                .setUrl(GET_MAIN_MENU_JSON_URL)
                .build();

        String response = asyncHttpClient.executeRequest(getRequest).get().getResponseBody();

        JsonNode jsonNode = objectMapper.readTree(response);

        JsonNode sportsJson = jsonNode.get("data").get("sports");

        sportsJson.forEach(sport -> {
            Sport sportForList = new Sport();
            int id = sport.get("id").asInt();
            String name = sport.get("name").asText();
            sportForList.setId(id);

            sportForList.setName(name);

            if(sportsName.contains(name)){
                sportList.add(sportForList);
            }
        });

        JsonNode tournamentsJson = jsonNode.get("data").get("tournaments");

        tournamentsJson.forEach(tournament -> {
            Tournament tournamentForList = new Tournament();
            tournamentForList.setId(tournament.get("id").asInt());
            tournamentForList.setName(tournament.get("name").asText());
            int sportId = tournament.get("sport_id").asInt();
            tournamentForList.setSportId(sportId);

            if(sportsName.get(sportId) != null){
                tournamentList.add(tournamentForList);
            }
        });

        sportList.forEach(sport -> {
            sport.setTournaments(tournamentList.stream().filter(tournament ->
                    tournament.getSportId() == sport.getId()).collect(Collectors.toList()));

            //0,1 after sport_id mean live or prematch
            String url = "https://nodejs.tglab.io/cache/" + sport.getId() + "/0/en/odd-filters.json";

            Request getFilters = new RequestBuilder(HttpConstants.Methods.GET)
                    .setUrl(url)
                    .build();

            try {
                String responseFilters = asyncHttpClient.executeRequest(getFilters).get().getResponseBody();

                List<Filter> filtersForSpecificSport = Arrays.asList(objectMapper.readValue(responseFilters, Filter[].class));
                filters.addAll(filtersForSpecificSport);

            }catch (Exception ex){
                log.error(ex);
            }
        });

        tournamentList.forEach(tournament -> {
            String url = "https://nodejs08.tglab.io/cache/20/en/ua/" + tournament.getId() + "/prematch-by-tournaments.json";

            Request getEventsForTournament = new RequestBuilder(HttpConstants.Methods.GET)
                    .setUrl(url)
                    .build();

            try {
                String responseBody = asyncHttpClient.executeRequest(getEventsForTournament).get().getResponseBody();

                JsonNode jsonNodeEvents = objectMapper.readTree(responseBody);
                JsonNode eventsJson = jsonNodeEvents.get("events");

                List<Event> eventListForTournament = new ArrayList<>();
                eventsJson.forEach(event -> {
                    Event eventForList = new Event();
                    eventForList.setId(event.get("id").asInt());
                    eventForList.setSportId(event.get("sport_id").asInt());
                    eventForList.setTeamHome(event.get("teams").get("home").asText());
                    eventForList.setTeamAway(event.get("teams").get("away").asText());
                    eventForList.setDateStart(LocalDateTime.parse(event.get("date_start").asText(), inputFormatter));

                    String urlGetOutcomesByEvent = "https://nodejs08.tglab.io/cache/20/en/ua/" + eventForList.getId() + "/single-pre-event.json";
                    Request getOutcomesByEvent = new RequestBuilder(HttpConstants.Methods.GET)
                            .setUrl(urlGetOutcomesByEvent)
                            .build();
                    try {
                        String responseOutcomesByEvent = asyncHttpClient.executeRequest(getOutcomesByEvent).get().getResponseBody();

                        JsonNode jsonNodeOutcomesByEvent = objectMapper.readTree(responseOutcomesByEvent);
                        JsonNode jsonOutcomes = jsonNodeOutcomesByEvent.get("odds");

                        List<Outcome> outcomeList = new ArrayList<>();
                        jsonOutcomes.forEach(outcome -> {
                            Outcome outcomeForList = new Outcome();
                            outcomeForList.setId(outcome.get("id").asInt());
                            outcomeForList.setFilterId(outcome.get("filter_id").asInt());
                            outcomeForList.setName(outcome.get("team_name").get("en").asText());
                            outcomeForList.setCoefficient(outcome.get("odd_value").asDouble());
                            outcomeForList.setAdditionalValue(outcome.get("additional_value").asText());

                            System.out.println(outcomeForList);
                            outcomeList.add(outcomeForList);
                        });

                        eventForList.setOutcomes(outcomeList);
                        eventListForTournament.add(eventForList);

                    }catch (Exception ex){
                        log.error(ex);
                    }
                });

                tournament.setEvents(eventListForTournament);
            }catch (Exception ex){
                log.error(ex);
            }
        });

        filters.forEach(System.out::println);
    }

}
