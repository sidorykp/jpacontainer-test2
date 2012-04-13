package com.sidorykp.sandbox.vaadin.domain;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pawel
 * Date: 4/13/12
 * Time: 9:04 PM
 * To change this template use File | Settings | File Templates.
 */
@Embeddable
public class Address {
    private String street;
    private String city;
    private String zipCode;

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

}
