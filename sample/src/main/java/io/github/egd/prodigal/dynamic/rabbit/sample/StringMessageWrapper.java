package io.github.egd.prodigal.dynamic.rabbit.sample;

import java.util.List;

public class StringMessageWrapper {

    private byte[] bytes;

    private List<String> list;

    public StringMessageWrapper() {
    }

    public StringMessageWrapper(byte[] bytes, List<String> list) {
        this.bytes = bytes;
        this.list = list;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }
}
