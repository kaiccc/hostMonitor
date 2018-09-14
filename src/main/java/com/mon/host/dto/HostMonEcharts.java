package com.mon.host.dto;

import java.util.List;

public class HostMonEcharts {


    /**
     * name
     * data
     */

    private String name;
    private List<List<String>> data;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<List<String>> getData() {
        return data;
    }

    public void setData(List<List<String>> data) {
        this.data = data;
    }


    public HostMonEcharts(String name, List<List<String>> data) {
        this.name = name;
        this.data = data;
    }
}
