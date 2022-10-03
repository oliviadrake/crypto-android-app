package com.example.cryptocoin;

/**
 * Crypto objects represent one cryptocurrency coin and contain all the information about it
 */
public class Crypto {
    public String coinSymbol;
    public String coinName;
    public String coinPrice;
    public String dailyPercentageChange;
    public String hourlyPercentageChange;

    public Crypto(String coinSymbol, String coinName, String coinPrice, String dailyPercentageChange, String hourlyPercentageChange){
        this.coinSymbol = coinSymbol;
        this.coinName = coinName;
        this.coinPrice = coinPrice;
        this.dailyPercentageChange = dailyPercentageChange;
        this.hourlyPercentageChange = hourlyPercentageChange;
    }
}
