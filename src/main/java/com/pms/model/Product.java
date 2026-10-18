package com.pms.model;

import java.math.BigDecimal;

/** Represents one row of the products table. */
public class Product {

    private int pid;
    private String pname;
    private BigDecimal pprice;   // BigDecimal is the right type for money (matches DECIMAL)
    private int pqty;

    public Product() {
    }

    public Product(int pid, String pname, BigDecimal pprice, int pqty) {
        this.pid = pid;
        this.pname = pname;
        this.pprice = pprice;
        this.pqty = pqty;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public BigDecimal getPprice() {
        return pprice;
    }

    public void setPprice(BigDecimal pprice) {
        this.pprice = pprice;
    }

    public int getPqty() {
        return pqty;
    }

    public void setPqty(int pqty) {
        this.pqty = pqty;
    }
}
