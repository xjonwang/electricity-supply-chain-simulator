package org.capstoneresearch;

public class Battery
{
    private double tripDistance;              // distance of trip in miles
    private double transitTime;               // transit time in seconds
    private double batteryDensity;            // in KWs/kg
    private double batteryMass;               // in kg
    private double batteryCapacity;           // in KWs
    private double cRating;                   // in hours^-1 (gives rate of charging and discharging, same for these batteries)
    private double batteryPower;              // continuous KW able to be charged or discharged by battery
    private double truckBatteryCapacity;      // in KWs
    private double truckRange;                // range for truck on a full charge
    private double charge;                    // in KWs
    private double chargingRate;              // in KW (cannot exceed battery power)
    private double dischargingRate;           // in KW (cannot exceed battery power)
    private BatteryLocation location;
    private BatteryLocation lastLocation;     // location immediately before the current one    
    private BatteryStatus status;
    private BatteryStatus lastStatus;         // status immediately before the current one
    private double transitTimeRemaining;      // in seconds
    private double truckCharge;               // charge left on the truck in KWs
    private double truckPower;                // max KW able to be charged by the truck
    private double truckCRating;              // in hours^-1 (gives rate of charging for the truck)
    private double truckChargingRate;         // rate at which the truck is charging
    private double truckDischargingRate;      // rate at which the truck discharges when driving (this is a constant)
    private double truckEnergyConsumed;       // electricity consumed by truck
    private double energyDelivered;           // electricity actually delivered to warehouse (not used to charge trucks) in KWs
    private int deliveryCounter;                    

    public Battery(double tripDistance, double batteryDensity, double batteryMass, double cRating, double truckBatteryCapacity, double truckRange, double truckDischargingRate)
    {
        this.tripDistance = tripDistance;
        this.transitTime = (this.tripDistance + 30) * 60;
        this.batteryDensity = batteryDensity * 3600;
        this.batteryMass = batteryMass;
        this.batteryCapacity = this.batteryDensity * batteryMass / 2.20462;
        this.cRating = cRating;
        this.batteryPower = this.batteryCapacity * cRating / 3600;
        this.truckBatteryCapacity = truckBatteryCapacity;
        this.truckRange = truckRange;
        this.truckDischargingRate = truckDischargingRate;
        charge = 0;
        chargingRate = 0;
        dischargingRate = 0;
        location = BatteryLocation.FARM;
        lastLocation = BatteryLocation.FARM;
        status = BatteryStatus.IDLE;
        lastStatus = BatteryStatus.IDLE;
        transitTimeRemaining = 0;
        truckCharge = 475 * 3600;
        truckPower = 240;
        this.truckCRating = (truckPower / truckCharge) * 3600;
        truckEnergyConsumed = 0;
        energyDelivered = 0;
        deliveryCounter = 0;
    }

    public double getTripDistance() {
        return tripDistance;
    }

    public void setTripDistance(double tripDistance) {
        if (tripDistance < 0 || tripDistance > 250)
            throw new IllegalArgumentException("Trip distance is out of range");
        this.tripDistance = tripDistance;
    }

    public double getTransitTime() {
        return transitTime;
    }

    public void setTransitTime(double transitTime) {
        this.transitTime = transitTime;
    }

    public double getBatteryDensity() {
        return batteryDensity;
    }

    public void setBatteryDensity(double batteryDensity) {
        this.batteryDensity = batteryDensity;
    }

    public double getBatteryMass() {
        return batteryMass;
    }

    public void setBatteryMass(double batteryMass) {
        this.batteryMass = batteryMass;
    }

    public double getBatteryCapacity() {
        return batteryCapacity;
    }

    public void setBatteryCapacity(double batteryCapacity) {
        this.batteryCapacity = batteryCapacity;
    }

    public double getcRating() {
        return cRating;
    }

    public void setcRating(double cRating) {
        this.cRating = cRating;
    }

    public double getBatteryPower() {
        return batteryPower;
    }

    public void setBatteryPower(double batteryPower) {
        this.batteryPower = batteryPower;
    }

    public double getTruckBatteryCapacity() {
        return truckBatteryCapacity;
    }

    public void setTruckBatteryCapacity(double truckBatteryCapacity) {
        this.truckBatteryCapacity = truckBatteryCapacity;
    }

    public double getCharge() {
        return charge;
    }

    public void setCharge(double charge) {
        this.charge = charge;
    }

    public double getChargingRate() {
        return chargingRate;
    }

    public void setChargingRate(double chargingRate) {
        if (chargingRate < 0 || chargingRate > batteryPower)
            throw new IllegalArgumentException("Charging rate is out of range");
        this.chargingRate = chargingRate;
    }

    public double getDischargingRate() {
        return dischargingRate;
    }

    public void setDischargingRate(double dischargingRate) {
        if (dischargingRate < 0 || dischargingRate > batteryPower)
            throw new IllegalArgumentException("Discharging rate is out of range");
        this.dischargingRate = dischargingRate;
    }

    public BatteryLocation getLocation() {
        return location;
    }

    public void setLocation(BatteryLocation location) {
        this.location = location;
    }

    public BatteryStatus getStatus() {
        return status;
    }

    public void setStatus(BatteryStatus status) {
        this.status = status;
    }

    public BatteryLocation getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(BatteryLocation lastLocation) {
        this.lastLocation = lastLocation;
    }

    public BatteryStatus getLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(BatteryStatus lastStatus) {
        this.lastStatus = lastStatus;
    }

    public double getTransitTimeRemaining() {
        return transitTimeRemaining;
    }

    public void setTransitTimeRemaining(double transitTimeRemaining) {
        this.transitTimeRemaining = transitTimeRemaining;
    }

    public double getTruckRange() {
        return truckRange;
    }

    public void setTruckRange(double truckRange) {
        this.truckRange = truckRange;
    }

    public double getTruckCharge() {
        return truckCharge;
    }

    public void setTruckCharge(double truckCharge) {
        if (truckCharge <= 0 || truckCharge > 475 * 3600)
            throw new IllegalArgumentException("Truck charge is out of range");
        this.truckCharge = truckCharge;
    }

    public double getTruckPower() {
        return truckPower;
    }

    public void setTruckPower(double truckPower) {
        this.truckPower = truckPower;
    }

    public double getTruckCRating() {
        return truckCRating;
    }

    public void setTruckCRating(double truckCRating) {
        this.truckCRating = truckCRating;
    }

    public double getTruckChargingRate() {
        return truckChargingRate;
    }

    public void setTruckChargingRate(double truckChargingRate) {
        if (truckChargingRate < 0 || truckChargingRate > 240)
            throw new IllegalArgumentException("Truck charging rate is out of range");
        this.truckChargingRate = truckChargingRate;
    }

    public double getTruckDischargingRate() {
        return truckDischargingRate;
    }

    public void setTruckDischargingRate(double truckDischargingRate) {
        this.truckDischargingRate = truckDischargingRate;
    }

    public double getTruckEnergyConsumed() {
        return truckEnergyConsumed;
    }

    public void setTruckEnergyConsumed(double truckEnergyConsumed) {
        this.truckEnergyConsumed = truckEnergyConsumed;
    }

    public double getEnergyDelivered() {
        return energyDelivered;
    }

    public void setEnergyDelivered(double energyDelivered) {
        this.energyDelivered = energyDelivered;
    }

    public int getDeliveryCounter() {
        return deliveryCounter;
    }

    public void setDeliveryCounter(int deliveryCounter) {
        this.deliveryCounter = deliveryCounter;
    }

    @Override
    public String toString()
    {
        return "Battery charge: " + charge;
    }
}
