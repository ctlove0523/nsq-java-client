package io.github.ctlove0523.nsq.cmd;

public enum CommandName {
    IDENTIFY("IDENTIFY"),
    SUB("SUB"),
    PUB("PUB"),
    MultiplePub("MPUB"),
    DeferPub("DPUB"),
    RDY("RDY"),
    FIN("FIN"),
    REQ("REQ"),
    TOUCH("TOUCH"),
    CLS("CLS"),
    NOP("NOP"),
    AUTH("AUTH");

    private final String name;

    CommandName(String name) {
        this.name = name;
    }

    public String cmdName() {
        return this.name;
    }
}
