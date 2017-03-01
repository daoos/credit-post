package bean;

/**
 * Created by hpre on 16-12-16.
 */
public class Frequence {
    private String entity;
    private int frequence = 0;

    public Frequence(String entity, int frequence) {
        this.entity = entity;
        this.frequence = frequence;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public int getFrequence() {
        return frequence;
    }

    public void setFrequence(int frequence) {
        this.frequence = frequence;
    }
}
