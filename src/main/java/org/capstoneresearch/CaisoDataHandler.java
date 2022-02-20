package org.capstoneresearch;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

public class CaisoDataHandler
{
    Map<String, Double> map = new HashMap<>();

    Map<Integer, Integer> intervalMap = new HashMap<>();
    
    static boolean firstRow = true;
    
    public void readData()
    {
        try {
            InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("ProductionAndCurtailmentsData_2021 - Curtailments.csv");
            CSVReader reader = new CSVReader(new InputStreamReader(in));
            List<String[]> r = reader.readAll();
            r.forEach(x -> {
                //System.out.println(Arrays.toString(x));
                if (firstRow) {
                    firstRow = false;
                } else {
                    if ((x[3] != "") && (x[4] != ""))
                    {
                        map.put(x[0] + "_" + x[1] + "_" + x[2], Double.parseDouble(x[3]) + Double.parseDouble(x[4]));
                    }
                    else if (x[3] != "")
                    {
                        map.put(x[0] + "_" + x[1] + "_" + x[2], Double.parseDouble(x[3]));
                    }
                    else if (x[4] != "")
                    {
                        map.put(x[0] + "_" + x[1] + "_" + x[2], Double.parseDouble(x[4]));
                    }
                }
            }); 
        } catch(IOException|CsvException ex) {
            ex.printStackTrace();
        }
    }

    public void createIntervalMap(int intervalSize)
    {
        IntStream.range(1, (3600 / intervalSize) + 1).forEach(n -> {
            intervalMap.put(n, (n - 1) / (300 / intervalSize));
        });
    }

    public Map<String, Double> getMap() {
        return map;
    }

    public Map<Integer, Integer> getIntervalMap() {
        return intervalMap;
    }

    public double readCurtailment(String key) {
        return map.get(key);
    }
}
