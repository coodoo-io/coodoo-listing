package io.coodoo.framework.listing.boundary;

public class Stats {

    private Long count;
    private Number min;
    private Number max;
    private Double avg;
    private Number sum;

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Number getMin() {
        return min;
    }

    public void setMin(Number min) {
        this.min = min;
    }

    public Number getMax() {
        return max;
    }

    public void setMax(Number max) {
        this.max = max;
    }

    public Double getAvg() {
        return avg;
    }

    public void setAvg(Double avg) {
        this.avg = avg;
    }

    public Number getSum() {
        return sum;
    }

    public void setSum(Number sum) {
        this.sum = sum;
    }

    @Override
    public String toString() {
        return "Stats [count=" + count + ", min=" + min + ", max=" + max + ", avg=" + avg + ", sum=" + sum + "]";
    }

}
