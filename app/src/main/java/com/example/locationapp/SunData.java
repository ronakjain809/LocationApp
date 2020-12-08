package com.example.locationapp;

import java.time.ZonedDateTime;
import java.util.Observable;

public class SunData extends Observable {
    private ZonedDateTime sunrise;
    private ZonedDateTime noon;
    private ZonedDateTime sunset;

    public ZonedDateTime getSunrise() {
        return sunrise;
    }

    public void setSunrise(ZonedDateTime sunrise) {
        this.sunrise = sunrise;
    }

    public ZonedDateTime getNoon() {
        return noon;
    }

    public void setNoon(ZonedDateTime noon) {
        this.noon = noon;
    }

    public ZonedDateTime getSunset() {
        return sunset;
    }

    public void setSunset(ZonedDateTime sunset) {
        this.sunset = sunset;
    }

    public void alertObservers() {
        setChanged();
        notifyObservers();
        clearChanged();
    }
}
