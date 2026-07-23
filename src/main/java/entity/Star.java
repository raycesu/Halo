package entity;

public class Star {
    private String name;
    private Double ra;
    private Double dec;
    private String brightness;

    public Star(String name, Double ra, Double dec, String brightness) {
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
    public Double getRa() {
        return ra;
    }
    public void setRa(Double ra) {
        this.ra = ra;
    }
    public Double getDec() {
        return dec;
    }
    public void setDec(Double dec) {
        this.dec = dec;
    }
    public String getBrightness() {
        return brightness;
    }
    public void setBrightness(String brightness) {
        this.brightness = brightness;
    }

}
