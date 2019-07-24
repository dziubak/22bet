package com.parse.twotwobet.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @ToString
@EqualsAndHashCode
public class Event {
    private int id;
    private int sportId;
    private String teamHome;
    private String teamAway;
    private LocalDateTime dateStart;

    private List<Outcome> outcomes;
}
