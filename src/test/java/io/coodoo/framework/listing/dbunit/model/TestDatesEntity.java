package io.coodoo.framework.listing.dbunit.model;

import java.time.LocalDateTime;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "TEST_DATES_ENTITY")
public class TestDatesEntity {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "LOCAL_DATE_TIME1")
    private LocalDateTime localDateTime1;

    @Column(name = "LOCAL_DATE_TIME2")
    private LocalDateTime localDateTime2;

    @Column(name = "DATE1")
    private Date date1;

    @Column(name = "DATE2")
    private Date date2;

    @Override
    public String toString() {
        return "TestDatesEntity [id=" + id + ", localDateTime1=" + localDateTime1 + ", localDateTime2=" + localDateTime2 + ", date1=" + date1 + ", date2="
                        + date2 + "]";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getLocalDateTime1() {
        return localDateTime1;
    }

    public void setLocalDateTime1(LocalDateTime localDateTime1) {
        this.localDateTime1 = localDateTime1;
    }

    public LocalDateTime getLocalDateTime2() {
        return localDateTime2;
    }

    public void setLocalDateTime2(LocalDateTime localDateTime2) {
        this.localDateTime2 = localDateTime2;
    }

    public Date getDate1() {
        return date1;
    }

    public void setDate1(Date date1) {
        this.date1 = date1;
    }

    public Date getDate2() {
        return date2;
    }

    public void setDate2(Date date2) {
        this.date2 = date2;
    }

}
