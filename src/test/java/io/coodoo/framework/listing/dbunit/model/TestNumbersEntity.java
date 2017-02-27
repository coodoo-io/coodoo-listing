package io.coodoo.framework.listing.dbunit.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.coodoo.framework.listing.boundary.annotation.ListingLikeOnNumber;

@Entity
@Table(name = "TEST_NUMBERS_ENTITY")
public class TestNumbersEntity {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "LONG_CLASS")
    private Long longClass;

    @Column(name = "LONG_PRIMITIVE")
    private long longPrimitive;

    @ListingLikeOnNumber
    @Column(name = "LONG_LIKE")
    private Long longLike;

    @Column(name = "INT_CLASS")
    private Integer intClass;

    @Column(name = "INT_PRIMITIVE")
    private int intPrimitive;

    @ListingLikeOnNumber
    @Column(name = "INT_LIKE")
    private Integer intLike;

    @Column(name = "SHORT_CLASS")
    private Short shortClass;

    @Column(name = "SHORT_PRIMITIVE")
    private short shortPrimitive;

    @ListingLikeOnNumber
    @Column(name = "SHORT_LIKE")
    private Short shortLike;

    @Override
    public String toString() {
        return "TestNumbersEntity [id=" + id + ", longClass=" + longClass + ", longPrimitive=" + longPrimitive + ", longLike=" + longLike + ", intClass="
                        + intClass + ", intPrimitive=" + intPrimitive + ", intLike=" + intLike + ", shortClass=" + shortClass + ", shortPrimitive="
                        + shortPrimitive + ", shortLike=" + shortLike + "]";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLongClass() {
        return longClass;
    }

    public void setLongClass(Long longClass) {
        this.longClass = longClass;
    }

    public long getLongPrimitive() {
        return longPrimitive;
    }

    public void setLongPrimitive(long longPrimitive) {
        this.longPrimitive = longPrimitive;
    }

    public Long getLongLike() {
        return longLike;
    }

    public void setLongLike(Long longLike) {
        this.longLike = longLike;
    }

    public Integer getIntClass() {
        return intClass;
    }

    public void setIntClass(Integer intClass) {
        this.intClass = intClass;
    }

    public int getIntPrimitive() {
        return intPrimitive;
    }

    public void setIntPrimitive(int intPrimitive) {
        this.intPrimitive = intPrimitive;
    }

    public Integer getIntLike() {
        return intLike;
    }

    public void setIntLike(Integer intLike) {
        this.intLike = intLike;
    }

    public Short getShortClass() {
        return shortClass;
    }

    public void setShortClass(Short shortClass) {
        this.shortClass = shortClass;
    }

    public short getShortPrimitive() {
        return shortPrimitive;
    }

    public void setShortPrimitive(short shortPrimitive) {
        this.shortPrimitive = shortPrimitive;
    }

    public Short getShortLike() {
        return shortLike;
    }

    public void setShortLike(Short shortLike) {
        this.shortLike = shortLike;
    }

}
