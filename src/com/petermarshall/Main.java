package com.petermarshall;

import edu.cmu.ri.createlab.terk.robot.finch.Finch;

public class Main {

    public static void main(String[] args) {
	// write your code here
        Finch sharedfinch = new Finch();
        SearchForLight.start(sharedfinch);

//        sharedfinch.setWheelVelocities(255,-255);
    }
}
