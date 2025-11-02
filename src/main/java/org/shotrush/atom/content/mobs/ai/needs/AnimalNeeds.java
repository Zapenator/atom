package org.shotrush.atom.content.mobs.ai.needs;

public class AnimalNeeds {
    
    private double hunger;
    private double thirst;
    private double energy;
    private long lastUpdate;
    
    private static final double MAX_HUNGER = 100.0;
    private static final double MAX_THIRST = 100.0;
    private static final double MAX_ENERGY = 100.0;
    
    private static final double HUNGER_DRAIN_PER_SECOND = 0.02;
    private static final double THIRST_DRAIN_PER_SECOND = 0.03;
    private static final double ENERGY_DRAIN_PER_SECOND = 0.015;
    
    private static final double HUNGER_CRITICAL = 20.0;
    private static final double THIRST_CRITICAL = 15.0;
    private static final double ENERGY_CRITICAL = 10.0;
    
    public AnimalNeeds() {
        this.hunger = MAX_HUNGER;
        this.thirst = MAX_THIRST;
        this.energy = MAX_ENERGY;
        this.lastUpdate = System.currentTimeMillis();
    }
    
    public void update() {
        long now = System.currentTimeMillis();
        double deltaSeconds = (now - lastUpdate) / 1000.0;
        lastUpdate = now;
        
        hunger = Math.max(0, hunger - (HUNGER_DRAIN_PER_SECOND * deltaSeconds));
        thirst = Math.max(0, thirst - (THIRST_DRAIN_PER_SECOND * deltaSeconds));
        energy = Math.max(0, energy - (ENERGY_DRAIN_PER_SECOND * deltaSeconds));
    }
    
    public void drainFromActivity(double hungerCost, double thirstCost, double energyCost) {
        hunger = Math.max(0, hunger - hungerCost);
        thirst = Math.max(0, thirst - thirstCost);
        energy = Math.max(0, energy - energyCost);
    }
    
    public void eat(double amount) {
        hunger = Math.min(MAX_HUNGER, hunger + amount);
    }
    
    public void drink(double amount) {
        thirst = Math.min(MAX_THIRST, thirst + amount);
    }
    
    public void sleep(double amount) {
        energy = Math.min(MAX_ENERGY, energy + amount);
    }
    
    public double getHunger() {
        return hunger;
    }
    
    public double getThirst() {
        return thirst;
    }
    
    public double getEnergy() {
        return energy;
    }
    
    public double getHungerPercent() {
        return hunger / MAX_HUNGER;
    }
    
    public double getThirstPercent() {
        return thirst / MAX_THIRST;
    }
    
    public double getEnergyPercent() {
        return energy / MAX_ENERGY;
    }
    
    public boolean isHungry() {
        return hunger < MAX_HUNGER * 0.6;
    }
    
    public boolean isThirsty() {
        return thirst < MAX_THIRST * 0.5;
    }
    
    public boolean isTired() {
        return energy < MAX_ENERGY * 0.4;
    }
    
    public boolean isStarving() {
        return hunger < HUNGER_CRITICAL;
    }
    
    public boolean isDehydrated() {
        return thirst < THIRST_CRITICAL;
    }
    
    public boolean isExhausted() {
        return energy < ENERGY_CRITICAL;
    }
    
    public NeedPriority getMostUrgentNeed() {
        if (isDehydrated()) return NeedPriority.THIRST_CRITICAL;
        if (isStarving()) return NeedPriority.HUNGER_CRITICAL;
        if (isExhausted()) return NeedPriority.ENERGY_CRITICAL;
        if (isThirsty()) return NeedPriority.THIRST;
        if (isHungry()) return NeedPriority.HUNGER;
        if (isTired()) return NeedPriority.ENERGY;
        return NeedPriority.NONE;
    }
    
    public double getOverallWellbeing() {
        return (getHungerPercent() + getThirstPercent() + getEnergyPercent()) / 3.0;
    }
    
    public enum NeedPriority {
        NONE(0),
        ENERGY(1),
        HUNGER(2),
        THIRST(3),
        ENERGY_CRITICAL(10),
        HUNGER_CRITICAL(11),
        THIRST_CRITICAL(12);
        
        private final int urgency;
        
        NeedPriority(int urgency) {
            this.urgency = urgency;
        }
        
        public int getUrgency() {
            return urgency;
        }
        
        public boolean isCritical() {
            return urgency >= 10;
        }
    }
}
