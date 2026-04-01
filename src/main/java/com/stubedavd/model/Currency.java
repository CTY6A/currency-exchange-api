package com.stubedavd.model;

public class Currency {

    private final Integer id;
    private final String name;
    private final String code;
    private final String sign;

    public Currency(Integer id, String name, String code, String sign) {

        this.id = id;
        this.name = name;
        this.code = code;
        this.sign = sign;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getSign() {
        return sign;
    }

    @Override
    public String toString() {

        return "Currency{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }
}
