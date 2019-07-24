package com.parse.twotwobet.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
@EqualsAndHashCode
public class Outcome {
    private int id;
    private Integer filterId;
    private String name;
    private double coefficient;
    private String additionalValue;
}
