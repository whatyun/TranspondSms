package com.jds.wk.api.annotation;

import java.util.ArrayList;
import java.util.List;

class Msg {
    String from;
    String content;

    public Msg(String from, String content) {
        this.from = from;
        this.content = content;
    }

    public static void main(String[] args) {
        List<String> MiddleList=new ArrayList<>(4);
        System.out.println(MiddleList.size());

    }

    @Override
    public String toString() {
        return "Msg{" +
                "from='" + from + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}