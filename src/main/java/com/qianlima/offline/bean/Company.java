package com.qianlima.offline.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Company implements Serializable {

    private Integer id;

    private String classify;

    private String name;

    private String regStatus;

    private String legalPersonName;

    private String phoneNumber;

    private String actualCapital;

    private Date estiblishTime;

    private String regLocation;

    private String base;

}
