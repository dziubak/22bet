package com.parse.twotwobet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parse.twotwobet.entity.Sport;
import lombok.extern.log4j.Log4j2;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.util.HttpConstants;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.asynchttpclient.Dsl.asyncHttpClient;

@Log4j2
@Service
public class GeneralInfoParseService {

    private static final String GET_MAIN_MENU_JSON_URL = "https://nodejs08.tglab.io/cache/20/en/ua/Europe%2FUzhgorod/prematch-menu.json";
    private final List<String> sportsNameForParse = Arrays.asList("football", "tennis", "ice_hockey", "basketball", "baseball", "rugby");

    private ObjectMapper objectMapper = new ObjectMapper();

    private List<Sport> sports = new ArrayList<>();


    public JsonNode getMenuJson(){
        AsyncHttpClient asyncHttpClient = asyncHttpClient();

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
}
