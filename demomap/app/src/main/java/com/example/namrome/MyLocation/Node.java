package com.example.namrome.MyLocation;

import java.io.Serializable;

/**
 * Created by Nam Rome on 2/26/2017.
 */

public class Node implements Serializable{

    final private int id;

    private String name;

    public Node(int id) {
        this.id = id;
    }

    public Node(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
