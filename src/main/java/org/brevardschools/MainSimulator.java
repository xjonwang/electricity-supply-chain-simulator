package org.brevardschools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class MainSimulator
{
    private static CaisoDataHandler caisoDataHandler = new CaisoDataHandler();
    private static Map<String, Double> map;
    private static Map<Integer, Integer> intervalMap;
    
    private static List<String> dateList2021 = dateRange("2021-01-01", "2021-12-31");

    private static Workbook workbook = new XSSFWorkbook();

    private static Sheet sheet;

    private static CellStyle headerStyle;

    public static Workbook getWorkbook() {
        return workbook;
    }

    public static void setWorkbook(Workbook workbook) {
        MainSimulator.workbook = workbook;
    }

    public static Sheet getSheet() {
        return sheet;
    }

    public static void setSheet(Sheet sheet) {
        MainSimulator.sheet = sheet;
    }

    public static List<String> dateRange(String startDate, String endDate)
    {
        String s = startDate;
        String e = endDate;
        LocalDate start = LocalDate.parse(s);
        LocalDate end = LocalDate.parse(e);
        List<String> totalDates = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
        while (!start.isAfter(end))
        {
            totalDates.add(formatter.format(start));
            start = start.plusDays(1);
        }
        return totalDates;
    }

    public static List<Battery> makeBatteries(double tripDistance, double batteryDensity, double batteryMass, double cRating, double truckBatteryCapacity, int quantity)
    {
        List<Battery> batteries = new ArrayList<>();
        IntStream.range(1, quantity + 1).forEach(n -> {
            batteries.add(new Battery(tripDistance, batteryDensity, batteryMass, cRating, truckBatteryCapacity, 200, 142.5));
        });
        return batteries;
    }

    public static void determineFullCharge(Battery chargingBattery, double chargingTime)
    {
        if ((chargingBattery.getBatteryCapacity() - chargingBattery.getCharge()) < chargingBattery.getBatteryPower() * chargingTime)
        {
            chargingBattery.setChargingRate(chargingBattery.getBatteryPower());
            chargingBattery.setCharge(chargingBattery.getBatteryCapacity());
            chargingBattery.setStatus(BatteryStatus.IDLE);
            chargingBattery.setLastLocation(BatteryLocation.FARM);
            chargingBattery.setLocation(BatteryLocation.TRANSIT_TO_WAREHOUSE);
            chargingBattery.setTruckChargingRate((2 * (chargingBattery.getTruckBatteryCapacity() * chargingBattery.getTripDistance() / chargingBattery.getTruckRange()) - chargingBattery.getTruckCharge()) / (chargingBattery.getTripDistance() * 60));
            chargingBattery.setTransitTimeRemaining(chargingBattery.getTransitTime() - (chargingTime - ((chargingBattery.getBatteryCapacity() - chargingBattery.getCharge()) / chargingBattery.getChargingRate())));
            chargingBattery.setChargingRate(0.0);
        }
        else
        {
            chargingBattery.setChargingRate(chargingBattery.getBatteryPower());
            chargingBattery.setCharge(chargingBattery.getCharge() + (chargingBattery.getChargingRate() * chargingTime));
            chargingBattery.setStatus(BatteryStatus.CHARGING);
            chargingBattery.setLastLocation(BatteryLocation.FARM);
            chargingBattery.setLocation(BatteryLocation.FARM);
        }
    }

    public static void determineFullCharge(Battery chargingBattery, double remainderPower, double chargingTime)
    {
        if ((chargingBattery.getBatteryCapacity() - chargingBattery.getCharge()) < remainderPower * chargingTime)
        {
            chargingBattery.setChargingRate(remainderPower);
            chargingBattery.setCharge(chargingBattery.getBatteryCapacity());
            chargingBattery.setStatus(BatteryStatus.IDLE);
            chargingBattery.setLastLocation(BatteryLocation.FARM);
            chargingBattery.setLocation(BatteryLocation.TRANSIT_TO_WAREHOUSE);
            chargingBattery.setTruckChargingRate((2 * (chargingBattery.getTruckBatteryCapacity() * chargingBattery.getTripDistance() / chargingBattery.getTruckRange()) - chargingBattery.getTruckCharge()) / (chargingBattery.getTripDistance() * 60));
            chargingBattery.setDischargingRate(chargingBattery.getTruckChargingRate());
            chargingBattery.setTransitTimeRemaining(chargingBattery.getTransitTime() - (chargingTime - ((chargingBattery.getBatteryCapacity() - chargingBattery.getCharge()) / chargingBattery.getChargingRate())));
            chargingBattery.setChargingRate(0.0);
        }
        else
        {
            chargingBattery.setCharge(chargingBattery.getCharge() + (remainderPower * chargingTime));
            chargingBattery.setStatus(BatteryStatus.CHARGING);
            chargingBattery.setLastLocation(BatteryLocation.FARM);
            chargingBattery.setLocation(BatteryLocation.FARM);
        }
    }

    public static void chargeTruckBattery(Battery chargingBattery, int timeInterval)
    {
        if (chargingBattery.getTruckBatteryCapacity() - chargingBattery.getTruckCharge() <= timeInterval * chargingBattery.getTruckPower())
        {
            chargingBattery.setTruckCharge(chargingBattery.getTruckBatteryCapacity());
        }
        else
        {
            chargingBattery.setTruckCharge(chargingBattery.getTruckCharge() + (timeInterval * chargingBattery.getTruckPower()));
        }
    }

    public static void chargeTruckBattery(Battery chargingBattery, double remainderPower, int timeInterval)
    {
        if (chargingBattery.getTruckBatteryCapacity() - chargingBattery.getTruckCharge() <= timeInterval * remainderPower)
        {
            chargingBattery.setTruckCharge(chargingBattery.getTruckBatteryCapacity());
        }
        else
        {
            chargingBattery.setTruckCharge(chargingBattery.getTruckCharge() + (timeInterval * remainderPower));
        }
    }

    public static List<Double> simulateSupplyChain(double tripDistance, double batteryDensity, double batteryMass, double cRating, double truckBatteryCapacity, double producerDiscount, int numBatteries, int timeInterval, double batteryCycles, double truckPrice)
    {
        final List<Battery> batteries = makeBatteries(tripDistance, batteryDensity, batteryMass, cRating, truckBatteryCapacity, numBatteries);

        for (String i : dateList2021)
        {
            IntStream.range(1, 25).forEach(j -> {
                IntStream.range(1, (3600 / timeInterval) + 1).forEach(k -> {
                    if (map.containsKey(i + "_" + j + "_" + intervalMap.get(k)))
                    {
                        double curtailment = map.get(i + "_" + j + "_" + intervalMap.get(k));
                        int numBatteriesNeeded = (int) (curtailment * 1000 / (batteries.get(0).getBatteryPower() + batteries.get(0).getTruckPower())) + 1;
                        double remainderPower = curtailment % (batteries.get(0).getBatteryPower() + batteries.get(0).getTruckPower());

                        List<Battery> farmOpenBatteries = batteries.stream().filter(battery -> (battery.getLocation() == BatteryLocation.FARM && battery.getCharge() != battery.getBatteryCapacity())).collect(Collectors.toList());
                        List<Battery> farmBatteriesChargeDescending = farmOpenBatteries;
                        Collections.sort(farmBatteriesChargeDescending, new Comparator<Battery>() {
                            @Override
                            public int compare(Battery battery1, Battery battery2) {
                                return Double.compare(battery2.getCharge(), battery1.getCharge());
                            }
                        });

                        if (numBatteriesNeeded <= farmBatteriesChargeDescending.size())
                        {
                            for (Battery chargingBattery : farmBatteriesChargeDescending.subList(0, numBatteriesNeeded - 1))
                            {
                                determineFullCharge(chargingBattery, timeInterval);
                                chargeTruckBattery(chargingBattery, timeInterval);
                            }
                            if (remainderPower > batteries.get(0).getTruckPower())
                            {
                                chargeTruckBattery(farmBatteriesChargeDescending.get(numBatteriesNeeded - 1), timeInterval);
                            }
                        }
                        else
                        {
                            for (Battery chargingBattery : farmBatteriesChargeDescending)
                            {
                                determineFullCharge(chargingBattery, timeInterval);
                                chargeTruckBattery(chargingBattery, timeInterval);
                                //System.out.println(farmBatteriesChargeDescending + " " + i + "_" + j + "_" + k);
                            }
                        }
                    }

                    List<Battery> transitBatteries = batteries.stream().filter(battery -> (battery.getLocation() == BatteryLocation.TRANSIT_TO_WAREHOUSE && battery.getLastLocation() != BatteryLocation.FARM)).collect(Collectors.toList());
                    List<Battery> transitDischargingBatteries = batteries.stream().filter(battery -> (battery.getLocation() == BatteryLocation.TRANSIT_TO_WAREHOUSE && battery.getTransitTimeRemaining() > 15 * 60 && battery.getTransitTimeRemaining() < (battery.getTransitTime() - 15 * 60 + 5))).collect(Collectors.toList());
                    
                    for (Battery transitDischargingBattery : transitDischargingBatteries)
                    {
                        if (transitDischargingBattery.getTransitTimeRemaining() - timeInterval <= 15 * 60)
                        {
                            transitDischargingBattery.setDischargingRate(transitDischargingBattery.getTruckChargingRate());
                            transitDischargingBattery.setCharge(transitDischargingBattery.getCharge() - transitDischargingBattery.getDischargingRate() * (transitDischargingBattery.getTransitTimeRemaining() - 15 * 60));
                            transitDischargingBattery.setTruckCharge(transitDischargingBattery.getTruckBatteryCapacity());
                            transitDischargingBattery.setTruckEnergyConsumed(transitDischargingBattery.getTruckEnergyConsumed() + transitDischargingBattery.getTruckDischargingRate() * (transitDischargingBattery.getTransitTimeRemaining() - 15 * 60));
                            transitDischargingBattery.setStatus(BatteryStatus.IDLE);
                        }
                        else if (transitDischargingBattery.getTransitTimeRemaining() > transitDischargingBattery.getTransitTime() - 15 * 60)
                        {
                            transitDischargingBattery.setDischargingRate(transitDischargingBattery.getTruckChargingRate());
                            transitDischargingBattery.setCharge(transitDischargingBattery.getCharge() - transitDischargingBattery.getDischargingRate() * (timeInterval - transitDischargingBattery.getTransitTimeRemaining() + (transitDischargingBattery.getTransitTime() - 15 * 60)));
                            transitDischargingBattery.setTruckCharge(transitDischargingBattery.getTruckCharge() + (timeInterval - transitDischargingBattery.getTransitTimeRemaining() + (transitDischargingBattery.getTransitTime() - 15 * 60)) * (transitDischargingBattery.getTruckChargingRate() - transitDischargingBattery.getTruckDischargingRate()));
                            transitDischargingBattery.setTruckEnergyConsumed(transitDischargingBattery.getTruckEnergyConsumed() + transitDischargingBattery.getTruckDischargingRate() * (timeInterval - transitDischargingBattery.getTransitTimeRemaining() + (transitDischargingBattery.getTransitTime() - 15 * 60)));
                        }
                        else
                        {
                            transitDischargingBattery.setDischargingRate(transitDischargingBattery.getTruckChargingRate());
                            transitDischargingBattery.setCharge(transitDischargingBattery.getCharge() - transitDischargingBattery.getDischargingRate() * timeInterval);
                            transitDischargingBattery.setTruckCharge(transitDischargingBattery.getTruckCharge() + timeInterval * (transitDischargingBattery.getTruckChargingRate() - transitDischargingBattery.getTruckDischargingRate()));
                            transitDischargingBattery.setTruckEnergyConsumed(transitDischargingBattery.getTruckEnergyConsumed() + transitDischargingBattery.getTruckDischargingRate() * timeInterval);
                            transitDischargingBattery.setStatus(BatteryStatus.DISCHARGING);
                        }
                    }

                    for (Battery transitBattery : transitBatteries)
                    {
                        if (transitBattery.getTransitTimeRemaining() <= timeInterval)
                        {
                            transitBattery.setDischargingRate(transitBattery.getTruckChargingRate());
                            transitBattery.setLastLocation(BatteryLocation.TRANSIT_TO_WAREHOUSE);
                            transitBattery.setLocation(BatteryLocation.WAREHOUSE);
                            transitBattery.setStatus(BatteryStatus.DISCHARGING);
                            transitBattery.setDischargingRate(transitBattery.getBatteryPower());
                            transitBattery.setCharge(transitBattery.getCharge() - (timeInterval - transitBattery.getTransitTimeRemaining()) * transitBattery.getDischargingRate());
                            transitBattery.setEnergyDelivered(transitBattery.getEnergyDelivered() + (timeInterval - transitBattery.getTransitTimeRemaining()) * transitBattery.getDischargingRate());
                            transitBattery.setTransitTimeRemaining(0);
                        }
                        else
                        {
                            transitBattery.setTransitTimeRemaining(transitBattery.getTransitTimeRemaining() - timeInterval);
                            transitBattery.setLastLocation(BatteryLocation.TRANSIT_TO_WAREHOUSE);
                            transitBattery.setLocation(BatteryLocation.TRANSIT_TO_WAREHOUSE);
                        }
                    }
                    

                    List<Battery> warehouseBatteries = batteries.stream().filter(battery -> (battery.getLocation() == BatteryLocation.WAREHOUSE && battery.getLastLocation() != BatteryLocation.TRANSIT_TO_WAREHOUSE)).collect(Collectors.toList());
                    for (Battery warehouseBattery : warehouseBatteries)
                    {
                        if (warehouseBattery.getCharge() <= (warehouseBattery.getBatteryPower() * timeInterval))
                        {
                            warehouseBattery.setLastLocation(BatteryLocation.WAREHOUSE);
                            warehouseBattery.setLocation(BatteryLocation.TRANSIT_TO_FARM);
                            warehouseBattery.setTransitTimeRemaining(warehouseBattery.getTransitTime() - (warehouseBattery.getCharge() / warehouseBattery.getBatteryPower()));
                            warehouseBattery.setEnergyDelivered(warehouseBattery.getEnergyDelivered() + warehouseBattery.getCharge());
                            warehouseBattery.setStatus(BatteryStatus.IDLE);
                            warehouseBattery.setCharge(0.0);
                            warehouseBattery.setDischargingRate(0.0);
                            warehouseBattery.setDeliveryCounter(warehouseBattery.getDeliveryCounter() + 1);
                        }
                        else
                        {
                            warehouseBattery.setCharge(warehouseBattery.getCharge() - (warehouseBattery.getBatteryPower() * timeInterval));
                            warehouseBattery.setEnergyDelivered(warehouseBattery.getEnergyDelivered() + (warehouseBattery.getBatteryPower() * timeInterval));
                            warehouseBattery.setLastLocation(BatteryLocation.WAREHOUSE);
                            warehouseBattery.setLocation(BatteryLocation.WAREHOUSE);
                            warehouseBattery.setDischargingRate(warehouseBattery.getBatteryPower());
                        }
                    }

                    List<Battery> transitFarmBatteries = batteries.stream().filter(battery -> (battery.getLocation() == BatteryLocation.TRANSIT_TO_FARM && battery.getLastLocation() != BatteryLocation.WAREHOUSE)).collect(Collectors.toList());
                    for (Battery transitFarmBattery : transitFarmBatteries)
                    {
                        if (transitFarmBattery.getTransitTimeRemaining() <= timeInterval)
                        {
                            transitFarmBattery.setLastLocation(BatteryLocation.TRANSIT_TO_FARM);
                            transitFarmBattery.setLocation(BatteryLocation.FARM);
                            transitFarmBattery.setStatus(BatteryStatus.IDLE);
                            transitFarmBattery.setTransitTimeRemaining(0);
                        }
                        else
                        {
                            transitFarmBattery.setStatus(BatteryStatus.IDLE);
                            transitFarmBattery.setTransitTimeRemaining(transitFarmBattery.getTransitTimeRemaining() - timeInterval);
                            transitFarmBattery.setLastLocation(BatteryLocation.TRANSIT_TO_FARM);
                            transitFarmBattery.setLocation(BatteryLocation.TRANSIT_TO_FARM);
                        }
                    }
                    for (Battery battery : batteries)
                    {
                        battery.setLastLocation(battery.getLocation());
                    }
                });
            });
        }
        double energyDelivered = batteries.stream().map(battery -> battery.getEnergyDelivered()).reduce((double) 0, Double::sum);
        //System.out.println("Energy delivered: " + energyDelivered);
        double truckEnergyConsumed = batteries.stream().map(battery -> battery.getTruckEnergyConsumed()).reduce((double) 0, Double::sum);
        //System.out.println("Energy consumed: " + truckEnergyConsumed);
        double deliveryCounter = batteries.stream().map(battery -> battery.getDeliveryCounter()).reduce((int) 0, Integer::sum);
        double averageDeliveries = deliveryCounter / batteries.size();
        System.out.println(averageDeliveries);
        //System.out.println("Delivery counter: " + deliveryCounter);
        double batteryCaptialCosts = (batteries.size() * batteries.get(0).getBatteryCapacity() / 3600) * CostParameters.BATTERY_PRICE / (batteryCycles / averageDeliveries);
        double truckCapitalCosts = batteries.size() * truckPrice / (batteryCycles / (2 * averageDeliveries));
        double electricityCosts = ((energyDelivered + truckEnergyConsumed) / 3600) * (CostParameters.ELECTRICITY_RETAIL_PRICE * producerDiscount - CostParameters.ENVIRONMENTAL_BENEFIT);
        double truckingCosts = batteries.size() * CostParameters.TRUCKER_WAGES;
        double revenue = energyDelivered * CostParameters.ELECTRICITY_RETAIL_PRICE * CostParameters.SALE_DISCOUNT / 3600;
        double profit = (revenue - truckingCosts - electricityCosts - truckCapitalCosts - batteryCaptialCosts);
        double curtailmentReduced = (energyDelivered + truckEnergyConsumed) / 3600000;
        System.out.println("Profit: " + profit + " energy delivered: " + energyDelivered);
        List<Double> statsList = new ArrayList<>();
        statsList.add(profit);
        statsList.add(curtailmentReduced);
        return statsList;
    }

    public static void main(String[] args) throws IOException
    {
        caisoDataHandler.readData();
        caisoDataHandler.createIntervalMap(100);
        map = caisoDataHandler.getMap();
        intervalMap = caisoDataHandler.getIntervalMap();
    
        sheet = workbook.createSheet("Profitability");
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 6000);
        sheet.setColumnWidth(2, 6000);
        
        Row header = sheet.createRow(0);
        
        headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        headerStyle.setFont(font);
        
        Cell headerCell = header.createCell(0);
        headerCell.setCellValue("Curtailment_Discount");
        headerCell.setCellStyle(headerStyle);
        
        headerCell = header.createCell(1);
        headerCell.setCellValue("Profitability");
        headerCell.setCellStyle(headerStyle);
    
        headerCell = header.createCell(2);
        headerCell.setCellValue("Curtailment_Reduced");
        headerCell.setCellStyle(headerStyle);

        ExecutorService executor = Executors.newFixedThreadPool(61);

        double batteryCycles = 5000;
        double producerDiscount = 0.4;
        double truckPrice = 333803.3898;
        for (double tripDistance = 180; tripDistance <= 181; tripDistance += 5)
        {
            RunnableSimulator simulator = new RunnableSimulator(producerDiscount, batteryCycles, tripDistance, truckPrice);
            executor.execute(simulator);
            System.out.println("Schedule simulator");
        }
        executor.shutdown();
        while (!executor.isTerminated())
        {

        }
        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        String fileLocation = path.substring(0, path.length() - 1) + "2024_Condtions_V2.xlsx";

        FileOutputStream outputStream = new FileOutputStream(fileLocation);
        workbook.write(outputStream);
        workbook.close();
    }
}

