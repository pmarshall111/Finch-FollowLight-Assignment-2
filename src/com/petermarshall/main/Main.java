package com.petermarshall.main;

import edu.cmu.ri.createlab.terk.robot.finch.Finch;

public class Main {

    public static void main(String[] args) {
        Finch sharedfinch = new Finch();
        SearchForLight sFL = new SearchForLight(sharedfinch);
        sFL.start();
    }
}
