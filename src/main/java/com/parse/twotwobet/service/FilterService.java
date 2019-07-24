package com.parse.twotwobet.service;

import com.parse.twotwobet.entity.Filter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
public class FilterService {

    public String getFilterNameByFilterId(List<Filter> filterList, int filterId){
        return filterList.stream().filter(filter -> filter.getId() == filterId)
                .collect(Collectors.toList()).get(0).getName();
    }
}
