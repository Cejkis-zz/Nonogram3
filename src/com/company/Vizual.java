package com.company;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Čejkis on 20.04.2017.
 */
public class Vizual extends JFrame {

    JTextArea[][] okna;
    int tloustka = 15;

    public void printBorec(Individual i){

        boolean[][] taj = i.tajenka;

        for (int j = 0; j < okna.length; j++) {
            for (int k = 0; k < okna[0].length ; k++) {

                if (taj[k][j] == false)
                    okna[j][k].setBackground(Color.WHITE);
                else
                    okna[j][k].setBackground(Color.BLACK);
            }
        }


    }

    public Vizual(int sirka, int vyska){

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
        frame.setSize(new Dimension(tloustka*sirka + 15, tloustka * vyska + 15));

        frame.setVisible(true);

    }


}
