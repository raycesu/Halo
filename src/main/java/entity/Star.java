package entity;

public class Star {
    private String name;
    private String ra;
    private String dec;
    private String brightness;

    public Star(String name, String ra, String dec, String brightness) {
        this.name = name;
        this.ra = ra;
        this.dec = dec;
        this.brightness = brightness;
    }


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getRa() {
        return ra;
    }
    public void setRa(String ra) {
        this.ra = ra;
    }
    public String getDec() {
        return dec;
    }
    public void setDec(String dec) {
        this.dec = dec;
    }

}
