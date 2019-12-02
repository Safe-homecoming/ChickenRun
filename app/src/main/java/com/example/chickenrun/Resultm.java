package com.example.chickenrun;

import java.util.List;

public class Resultm {

    // 기본으로 보여주는 json 결과값
    private  String result;
    private  String result_code;
    private  String description;
    private  int result_row;

    // 회원 로그인시
    private String memId;
    private String name;
    private int chicken_quantity;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getChicken_quantity() {
        return chicken_quantity;
    }

    public void setChicken_quantity(int chicken_quantity) {
        this.chicken_quantity = chicken_quantity;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResult_code() {
        return result_code;
    }

    public void setResult_code(String result_code) {
        this.result_code = result_code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getResult_row() {
        return result_row;
    }

    public void setResult_row(int result_row) {
        this.result_row = result_row;
    }

    public String getMemId() {
        return memId;
    }

    public void setMemId(String memId) {
        this.memId = memId;
    }

}
