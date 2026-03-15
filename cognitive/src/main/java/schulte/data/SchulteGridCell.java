package schulte.data;

public class SchulteGridCell {

    private int num;
    private boolean selected = false;

    public SchulteGridCell(int num) {
        this.num = num;
    }

    public int getNum() {
        return num;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}