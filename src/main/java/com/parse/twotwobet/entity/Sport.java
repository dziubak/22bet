package com.parse.twotwobet.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter @Setter @ToString
@EqualsAndHashCode
public class Sport {
    private int id;
    private String name;

    private List<Tournament> tournaments;
}
