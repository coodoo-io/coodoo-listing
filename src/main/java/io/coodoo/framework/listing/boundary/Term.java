package io.coodoo.framework.listing.boundary;

public class Term {

    private long count;
    private Object value;
    private String valueAsString;
    private Number valueAsNumber;

    public Term() {}

    public Term(Object value, long count) {
        this.count = count;
        this.value = value;
        if (value == null) {
            valueAsString = "null";
        } else {
            valueAsString = value.toString();
            if (value instanceof Number) {
                valueAsNumber = (Number) value;
            }
        }
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getValueAsString() {
        return valueAsString;
    }

    public void setValueAsString(String valueAsString) {
        this.valueAsString = valueAsString;
    }

    public Number getValueAsNumber() {
        return valueAsNumber;
    }

    public void setValueAsNumber(Number valueAsNumber) {
        this.valueAsNumber = valueAsNumber;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Term [count=");
        builder.append(count);
        builder.append(", value=");
        builder.append(value);
        builder.append("]");
        return builder.toString();
    }

}
