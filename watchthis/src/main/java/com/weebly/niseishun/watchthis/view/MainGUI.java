package com.weebly.niseishun.watchthis.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import com.weebly.niseishun.watchthis.controllers.MainController;
import com.weebly.niseishun.watchthis.model.Entry;

public class MainGUI {

  private MainController controller;

  private JFrame mainFrame;
  private JPanel controlPanel;
  private JPanel resultsCard;
  private JPanel standbyCard;
  private JPanel loadingCard;
  private JPanel cardsPanel;
  private JPanel inputPanel;
  private JTextField urlField;
  private JButton urlQuery;
  private CardLayout cardLayout;
  private Font mainFont;
  private Font smallFont;

  public static final String STANDBY = "standby";
  public static final String LOADING = "loading";
  public static final String RESULTS = "results";

  public MainGUI(MainController controller) {
    this.controller = controller;
    init();
  }

  public void launch() {
    mainFrame.pack();
    mainFrame.setVisible(true);
  }

  public void displayResults(ArrayList<Entry> entries) {
    updateResultsCard(entries);
    switchStatus(RESULTS);
  }

  public void showLoading() {
    switchStatus(LOADING);
  }

  private void init() {

    mainFont = new Font("Serif", Font.PLAIN, 24);
    smallFont = new Font("Sans-Serif", Font.PLAIN, 18);

    mainFrame = new JFrame("Watch This - PROTOTYPE");
    mainFrame.setBounds(400, 100, 450, 300);
    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    controlPanel = new JPanel();
    controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.PAGE_AXIS));
    controlPanel.setPreferredSize(new Dimension(680, 720));
    controlPanel.setBorder(new EmptyBorder(10, 20, 50, 20));
    inputPanel = new JPanel();
    inputPanel.setPreferredSize(new Dimension(500, 260));
    JLabel urlLabel = new JLabel("Input a url:");
    urlLabel.setFont(mainFont);
    inputPanel.add(urlLabel);
    urlField = new JTextField(30);
    urlField.setFont(mainFont);
    inputPanel.add(urlField);
    urlQuery = new JButton("Search by url");
    urlQuery.setFont(mainFont);
    urlQuery.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        new SwingWorker<Void, String>() {
          @Override
          protected Void doInBackground() throws Exception {
            urlSearch(urlField.getText());
            return null;
          }

          @Override
          protected void done() {
            // nothing
          }
        }.execute();

      }
    });
    inputPanel.add(urlQuery);
    controlPanel.add(inputPanel);

    cardLayout = new CardLayout();
    cardsPanel = new JPanel(cardLayout);
    cardsPanel.setPreferredSize(new Dimension(500, 460));
    standbyCard = new JPanel(new GridLayout(0, 1, 0, 30));
    JLabel standbyLabel = new JLabel("Enter a series above", JLabel.CENTER);
    standbyLabel.setFont(mainFont);
    standbyCard.add(standbyLabel);
    loadingCard = new JPanel();
    ImageIcon loadingIcon = new ImageIcon("medium-spinner.gif");
    JLabel loadingLabel =
        new JLabel(" Searching... This will take a minute. ", loadingIcon, JLabel.CENTER);
    loadingLabel.setFont(mainFont);
    loadingCard.add(loadingLabel);
    resultsCard = new JPanel(new GridLayout(0, 1, 0, 0));
    cardsPanel.add(standbyCard, STANDBY);
    cardsPanel.add(loadingCard, LOADING);
    cardsPanel.add(resultsCard, RESULTS);
    cardLayout.show(cardsPanel, STANDBY);
    controlPanel.add(cardsPanel);

    mainFrame.getContentPane().add(controlPanel, BorderLayout.NORTH);
  }

  private void urlSearch(String url) {
    switchStatus(LOADING);
    controller.urlSearch(url);
  }

  private void switchStatus(String index) {
    cardLayout.show(cardsPanel, index);
    controlPanel.repaint();
    controlPanel.revalidate();
    mainFrame.repaint();
    mainFrame.revalidate();
    mainFrame.pack();
  }

  private void updateResultsCard(ArrayList<Entry> results) {
    resultsCard.removeAll();
    JLabel resultsLabel = new JLabel("Watch these:", JLabel.CENTER);
    resultsLabel.setFont(mainFont);
    resultsCard.add(resultsLabel);
    int limit = 10;
    if (limit > results.size()) {
      limit = results.size();
    }
    for (int i = 0; i < limit; i++) {
      resultsCard.add(generateEntryCard(results.get(i)));
    }
  }

  private JPanel generateEntryCard(Entry entry) {
    JPanel panel = new JPanel();
    panel.add(buttonToPage(entry));
    return panel;
  }

  private JButton buttonToPage(Entry entry) {
    String text = entry.getTitle() + " |  Match: " + entry.getCounter() + "%";
    JButton button = new JButton(text);
    button.addActionListener(new OpenUrlAction(entry.getUrl()));
    button.setFont(smallFont);
    return button;
  }

  class OpenUrlAction implements ActionListener {
    private String url;

    public OpenUrlAction(String url) {
      this.url = url;
    }

    public void actionPerformed(ActionEvent e) {
      open(this.url);
    }
  }

  private static void open(String url) {
    if (Desktop.isDesktopSupported()) {
      try {
        URI uri = new URI(url);
        Desktop.getDesktop().browse(uri);
      } catch (Exception e) {
        System.out.println("error caught");
      }
    } else {
      System.out.println("else");
    }
  }

}
