package io.github.egd.prodigal.dynamic.rabbit.sample;

import java.io.Serializable;

public class SampleMessageBean implements Serializable {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "SampleMessageBean{" +
                "id='" + id + '\'' +
                '}';
    }
}
