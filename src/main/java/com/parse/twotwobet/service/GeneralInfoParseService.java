package com.parse.twotwobet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parse.twotwobet.entity.Filter;
import com.parse.twotwobet.entity.Sport;
import com.parse.twotwobet.entity.Tournament;
import lombok.extern.log4j.Log4j2;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.util.HttpConstants;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.asynchttpclient.Dsl.asyncHttpClient;

@Log4j2
@Service
public class GeneralInfoParseService {

    public static final String REPLACE_PART_URL = "{REPLACE_PART_URL}";

    private static final String GET_MAIN_MENU_JSON_URL = "https://nodejs08.tglab.io/cache/20/en/ua/Europe%2FUzhgorod/prematch-menu.json";
    private static final String GET_FILTERS_JSON_URL = "https://nodejs.tglab.io/cache/" + REPLACE_PART_URL + "/0/en/odd-filters.json";
    private final List<String> sportsNameForParse = Arrays.asList("football", "tennis", "ice_hockey", "basketball", "baseball", "rugby");

    private List<Sport> sports = new ArrayList<>();
    private List<Tournament> tournaments = new ArrayList<>();
    private List<Filter> filters = new ArrayList<>();

    private ObjectMapper objectMapper = new ObjectMapper();
    private AsyncHttpClient asyncHttpClient = asyncHttpClient();

    public JsonNode getMenuJson(){
        try {
            Request getRequest = new RequestBuilder(HttpConstants.Methods.GET)
                    .setUrl(GET_MAIN_MENU_JSON_URL)
                    .build();

            String response = asyncHttpClient.executeRequest(getRequest).get().getResponseBody();

            return objectMapper.readTree(response);
        }catch (Exception ex){
            log.error(ex);
            return null;
        }
    }

    public List<Sport> parseSports(JsonNode jsonNode){
        JsonNode jsonNodeSports = jsonNode.get("data").get("sports");

        jsonNodeSports.forEach(sport -> {
            Sport sportForList = new Sport();
            int id = sport.get("id").asInt();
            String name = sport.get("name").asText();

            sportForList.setId(id);
            sportForList.setName(name);

            if(sportsNameForParse.contains(name)){
                sports.add(sportForList);
            }
        });
        return sports;
    }

    public List<Tournament> parseTournaments(JsonNode jsonNode){
        JsonNode tournamentsJson = jsonNode.get("data").get("tournaments");

        tournamentsJson.forEach(tournament -> {
            Tournament tournamentForList = new Tournament();
            tournamentForList.setId(tournament.get("id").asInt());
            tournamentForList.setName(tournament.get("name").asText());
            int sportId = tournament.get("sport_id").asInt();
            tournamentForList.setSportId(sportId);

            if(!sports.stream().filter(sport -> sport.getId() == sportId).collect(Collectors.toList()).isEmpty()){
                tournaments.add(tournamentForList);
            }
        });
        return tournaments;
    }

    public void setTournamentsForSport(List<Sport> sportList){
        sportList.forEach(sport -> sport.setTournaments(tournaments.stream().filter(tournament ->
                    tournament.getSportId() == sport.getId()).collect(Collectors.toList())));
    }

    public List<Filter> parseFiltersForNeededSports(List<Sport> sportList){
        sportList.forEach(sport -> {
            //0,1 after sport_id mean live or prematch || Need replace to sport.getId()
            String urlGetFilters = GET_FILTERS_JSON_URL.replace(REPLACE_PART_URL, String.valueOf(sport.getId()));

            Request getFilters = new RequestBuilder(HttpConstants.Methods.GET)
                    .setUrl(urlGetFilters)
                    .build();

            try {
                String responseFilters = asyncHttpClient.executeRequest(getFilters).get().getResponseBody();

                List<Filter> filtersForSpecificSport = Arrays.asList(objectMapper.readValue(responseFilters, Filter[].class));
                filters.addAll(filtersForSpecificSport);

            }catch (Exception ex){
                log.error(ex);
            }
        });

        return filters;
    }
}
