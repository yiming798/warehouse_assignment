package com.warehouse.models;

public abstract class Item implements Comparable<Item> {
    protected String name;
    protected double weight;

    public Item(String name, double weight){
        this.name = name;
        this.weight = weight;
    }

    public String getName() { return name;};
    public double getWeight() { return weight;}

    @Override
    public int compareTo(Item other){
        return Double.compare(this.weight, other.weight);
    }

    @Override
    public String toString(){
        return "Item{name = '" + name + ", weight = " + weight + "}";
    }

}
