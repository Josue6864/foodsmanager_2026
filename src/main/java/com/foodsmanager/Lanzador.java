package com.foodsmanager;

import javafx.application.Application;

/**
  Punto de entrada de FoodsManager.
 */
public final class Lanzador {

    private Lanzador() {
    }

    public static void main(String[] args) {
        Application.launch(
                FoodsManagerApp.class,
                args
        );
    }
}