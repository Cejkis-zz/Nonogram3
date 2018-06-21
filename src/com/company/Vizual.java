package com.company;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Čejkis on 20.04.2017.
 */
public class Vizual extends JFrame {

    JTextArea[][] windows;
    JTextArea bestEver;
    JTextArea bestNow;
    int thickness = 8;

    public void vizualizeBestInd(Individual i, int g){

        i.printToViz(windows);

        bestNow.setText(g + " " + i.fitness );

    }

    public void printBestEver(Individual i, int g){

        bestEver.setText(g + " " + i.fitness );
    }

    public Vizual(int sirka, int vyska){

        JFrame frame = new JFrame("Vizual");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLayout(null);

        windows = new JTextArea[sirka][vyska];

        for (int i = 0; i < sirka ; i++) {
            for (int j = 0; j <  vyska; j++) {
                //JLabel a = new JLabel("x");
                JTextArea a = new JTextArea("");
                a.setLocation(new Point(i* thickness, j* thickness));
                a.setSize(new Dimension(thickness, thickness));

                if (Math.random() < 0.5)
                {a.setBackground(Color.white);

                }
                else{a.setBackground(Color.BLACK);}

                windows[i][j] = a;
                frame.add(a);
            }
        }

        bestEver = new JTextArea("best fitness");
        bestEver.setLocation(new Point(0, vyska * thickness + thickness));
        bestEver.setSize(new Dimension(70,20));
        frame.add(bestEver);

        bestNow = new JTextArea("best fitness");
        bestNow.setLocation(new Point(80, vyska * thickness + thickness));
        bestNow.setSize(new Dimension(70,20));
        frame.add(bestNow);

        frame.setSize(new Dimension(thickness *sirka + thickness, thickness * vyska + 80));

        frame.setVisible(true);
        frame.setLocation(0,0);


    }


}
