package com.erpflow.service;


import java.util.Arrays;
import java.util.Comparator;
import com.erpflow.model.enums.CartonSize;

public class CartonSelectionService {

    public CartonSize selectCarton(double requiredVolume) {

        return Arrays.stream(CartonSize.values())
                .sorted(Comparator.comparingDouble(
                        CartonSize::getVolume))
                .filter(carton ->
                        carton.getVolume() >= requiredVolume)
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order exceeds available carton sizes"));
    }
}