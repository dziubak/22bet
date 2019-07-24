package com.parse.twotwobet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.parse.twotwobet.entity.Event;
import com.parse.twotwobet.entity.Filter;
import com.parse.twotwobet.entity.Outcome;
import com.parse.twotwobet.entity.Sport;
import com.parse.twotwobet.entity.Tournament;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class ParseService {

    @Autowired
    private GeneralInfoParseService generalInfoParseService;

    @Autowired
    private EventParseService eventParseService;

    @Autowired
    private FilterService filterService;

    @PostConstruct
    public void init(){
        JsonNode jsonNode = generalInfoParseService.getMenuJson();

        List<Sport> sports = generalInfoParseService.parseSports(jsonNode);

        List<Tournament> tournaments = generalInfoParseService.parseTournaments(jsonNode);
        generalInfoParseService.setTournamentsForSport(sports);

        List<Filter> filters = generalInfoParseService.parseFiltersForNeededSports(sports);

        sports.forEach(sport -> {
            System.out.println(sport.getName());

            sport.getTournaments().forEach(tournament -> {
                    System.out.println(tournament.getName());

                    List<Event> events = eventParseService.parseEventsByTournament(tournament);
                    events.forEach(event -> {
                        System.out.println(String.format("%s X %s, %s, %d", event.getTeamHome(), event.getTeamAway(),
                                EventParseService.OUTPUT_FORMATTER.format(event.getDateStart()), event.getId()));

                        List<Outcome> outcomes = eventParseService.parseOutcomesByEvent(event);

                        HashSet<Integer> setOfFiltersInOutcomesByEvent = new HashSet<>();
                        outcomes.forEach(outcome -> setOfFiltersInOutcomesByEvent.add(outcome.getFilterId()));

                        HashMap<Integer, List<Outcome>> filterIdWithOutcomes = new HashMap<>();
                        setOfFiltersInOutcomesByEvent.forEach(filterId -> {
                            List<Outcome> outcomeList = new ArrayList<>();
                            outcomes.forEach(outcome -> {
                                if(filterId.equals(outcome.getFilterId())){
                                    outcomeList.add(outcome);
                                }
                            });
                            filterIdWithOutcomes.put(filterId, outcomeList);
                        });

                        for(Map.Entry<Integer, List<Outcome>> entry: filterIdWithOutcomes.entrySet()) {
                            System.out.println(filterService.getFilterNameByFilterId(filters, entry.getKey()));

                            entry.getValue().forEach(outcome -> System.out.println(String.format("       %s %s, %f, %d",
                                    outcome.getName(), outcome.getAdditionalValue(),
                                    outcome.getCoefficient(), outcome.getId())));
                        }
                    });
            });
        });
    }

}
