package com.parse.twotwobet.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter @Setter @ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class Filter {
    @JsonProperty("filter_id")
    private int id;
    @JsonProperty("sport_id")
    private int sportId;
    @JsonProperty("translation")
    private String name;
}
