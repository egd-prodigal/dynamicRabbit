package io.github.egd.prodigal.dynamic.rabbit.router.in;

public enum DynamicRabbitRouterInResultEnum {

    SUCCESS((byte) 1),

    FAIL((byte) 2),

    TIMEOUT((byte) 3);

    private final byte code;

    DynamicRabbitRouterInResultEnum(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

}
