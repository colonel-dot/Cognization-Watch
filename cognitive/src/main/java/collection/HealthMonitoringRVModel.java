package collection;

public class HealthMonitoringRVModel {
    private String function;
    private double data;
    private double target;
    private String unit;

    public HealthMonitoringRVModel(String function, double data, double target, String unit) {
        this.function = function;
        this.data = data;
        this.target = target;
        this.unit = unit;
    }

    public String getFunction() {
        return function;
    }

    public double getData() {
        return data;
    }

    public double getTarget() {
        return target;
    }

    public String getUnit() {
        return unit;
    }
}
