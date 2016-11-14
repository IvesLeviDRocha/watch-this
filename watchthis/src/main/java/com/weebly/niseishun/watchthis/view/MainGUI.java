package com.weebly.niseishun.watchthis.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

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
  private Font boldFont;
  private JList<Object> list;
  private JScrollPane listScroller;

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
    boldFont = new Font("Serif", Font.BOLD, 24);

    Border margin;
    Border border;

    mainFrame = new JFrame("Watch This - PROTOTYPE");
    mainFrame.setBounds(400, 100, 450, 300);
    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    mainFrame.setBackground(Color.DARK_GRAY);
    mainFrame.setResizable(false);

    controlPanel = new JPanel();
    controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.PAGE_AXIS));
    controlPanel.setPreferredSize(new Dimension(900, 640));
    margin = new EmptyBorder(10, 20, 20, 20);
    border = BorderFactory.createEtchedBorder();
    controlPanel.setBorder(new CompoundBorder(border, margin));
    controlPanel.setBackground(Color.DARK_GRAY);
    inputPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 0));
    inputPanel.setPreferredSize(new Dimension(500, 260));
    JLabel urlLabel = new JLabel("Input a url:");
    urlLabel.setFont(mainFont);
    urlLabel.setForeground(Color.WHITE);
    inputPanel.add(urlLabel);
    urlField = new JTextField(30);
    urlField.setFont(mainFont);
    urlField.setPreferredSize(new Dimension(100, 40));
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
    inputPanel.setBackground(Color.DARK_GRAY);
    inputPanel.setPreferredSize(new Dimension(100, 140));
    margin = new EmptyBorder(30, 5, 10, 5);
    inputPanel.setBorder(margin);
    controlPanel.add(inputPanel);

    margin = new EmptyBorder(10, 5, 100, 5);
    cardLayout = new CardLayout();
    cardsPanel = new JPanel(cardLayout);
    cardsPanel.setBackground(Color.DARK_GRAY);
    cardsPanel.setPreferredSize(new Dimension(800, 700));
    standbyCard = new JPanel(new GridLayout(0, 1, 0, 30));
    standbyCard.setBackground(Color.DARK_GRAY);
    JLabel standbyLabel = new JLabel("Enter a series above", JLabel.CENTER);
    standbyLabel.setFont(mainFont);
    standbyLabel.setForeground(Color.WHITE);
    standbyCard.add(standbyLabel);
    standbyCard.setBorder(margin);
    loadingCard = new JPanel(new GridLayout(0, 1, 0, 30));
    loadingCard.setBackground(Color.DARK_GRAY);
    ImageIcon loadingIcon = new ImageIcon(getClass().getClassLoader().getResource("loading.gif"));
    JLabel loadingLabel =
        new JLabel(" Searching... This may take a minute. ", loadingIcon, JLabel.CENTER);
    loadingLabel.setFont(mainFont);
    loadingLabel.setForeground(Color.WHITE);
    loadingCard.add(loadingLabel);
    loadingCard.setBorder(margin);
    Object[] data = {};
    list = new JList<Object>(data); // data has type Object[]
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setLayoutOrientation(JList.VERTICAL);
    list.setVisibleRowCount(-1);
    list.setFont(mainFont);
    list.setBackground(Color.LIGHT_GRAY);
    list.setForeground(Color.BLUE);
    list.setSelectedIndex(0);
    list.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        @SuppressWarnings("unchecked")
        JList<Object> list = (JList<Object>) evt.getSource();
        if (evt.getClickCount() == 2) {
          int index = list.locationToIndex(evt.getPoint());
          Entry clickedEntry = (Entry) list.getModel().getElementAt(index);
          open(clickedEntry.getUrl());
        }
      }
    });
    listScroller = new JScrollPane(list);
    listScroller.setPreferredSize(new Dimension(800, 640));
    resultsCard = new JPanel();
    resultsCard.setLayout(new BorderLayout());
    resultsCard.add(listScroller, BorderLayout.CENTER);
    resultsCard.setBackground(Color.DARK_GRAY);
    margin = new EmptyBorder(10, 10, 10, 10);
    border = new TitledBorder(BorderFactory.createEtchedBorder(), "Results",
        TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, boldFont, Color.WHITE);
    resultsCard.setBorder(new CompoundBorder(border, margin));
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
    list.setListData(results.toArray());
    list.setSelectedIndex(0);
    list.repaint();
    list.revalidate();
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
