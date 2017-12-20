package com.company;

import javax.swing.*;
import java.awt.*;

import static com.company.Main.sirka;
import static com.company.Main.vyska;

/**
 * Created by Čejkis on 20.04.2017.
 */
public class Vizual extends JFrame {

    JTextArea[][] okna;
    JTextArea bestEver;
    JTextArea bestNow;
    int tloustka = 8;

    public void printBorec(Individual i, int g, int islandnr){

        int[] taj = i.tajenka;

        for (int j = 0; j < sirka; j++) {
            for (int k = 0; k < vyska ; k++) {

                if (taj[ vyska*j + k ] == 0)
                    okna[j][k].setBackground(Color.WHITE);
                else
                    okna[j][k].setBackground(Color.BLACK);
            }
        }

        bestNow.setText(g + " " + i.fitness );

    }

    public void printBestEver(Individual i, int g, int islandnr){

        bestEver.setText(g + " " + i.fitness );
    }

    public Vizual(int sirka, int vyska, int islandnr){

        JFrame frame = new JFrame("Vizual");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLayout(null);

        okna = new JTextArea[sirka][vyska];

        for (int i = 0; i < sirka ; i++) {
            for (int j = 0; j <  vyska; j++) {
                //JLabel a = new JLabel("x");
                JTextArea a = new JTextArea("");
                a.setLocation(new Point(i*tloustka, j*tloustka));
                a.setSize(new Dimension(tloustka,tloustka));

                if (Math.random() < 0.5)
                {a.setBackground(Color.white);

                }
                else{a.setBackground(Color.BLACK);}

                okna[i][j] = a;
                frame.add(a);
            }
        }

        bestEver = new JTextArea("best fitness");
        bestEver.setLocation(new Point(0, vyska * tloustka + tloustka ));
        bestEver.setSize(new Dimension(70,20));
        frame.add(bestEver);

        bestNow = new JTextArea("best fitness");
        bestNow.setLocation(new Point(80, vyska * tloustka + tloustka ));
        bestNow.setSize(new Dimension(70,20));
        frame.add(bestNow);

        frame.setSize(new Dimension(tloustka*sirka + tloustka, tloustka * vyska + 80));

        if(islandnr < 4)
            frame.setLocation(islandnr * frame.getWidth(),0);
        else
            frame.setLocation((islandnr -4)* frame.getWidth() ,tloustka * vyska + 75);
        frame.setVisible(true);

    }


}
