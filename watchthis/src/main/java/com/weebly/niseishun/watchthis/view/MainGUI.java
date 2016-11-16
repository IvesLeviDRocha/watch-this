package com.weebly.niseishun.watchthis.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
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
import javax.swing.JOptionPane;
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

  public static final String STANDBY = "standby";
  public static final String LOADING = "loading";
  public static final String RESULTS = "results";
  public static final Font MAINFONT = new Font("Serif", Font.PLAIN, 24);
  public static final Font BOLDFONT = new Font("Serif", Font.BOLD, 24);

  public static final Color BACKGROUND = Color.DARK_GRAY;
  public static final Color TEXT = Color.WHITE;
  public static final Color RESULTS_BACKGROUND = Color.LIGHT_GRAY;
  public static final Color RESULTS_TEXT = Color.BLUE;

  public static final String URL_HELP_MESSAGE =
      "<html>Enter a MAL URL (e.g. https://myanimelist.net/anime/47/Akira)<br/>"
          + "and click \"Search by url\" to receive recomendations on similar series you might like.<br/><br/>"
          + "Double-click a result to open its url in your browser.";

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
  private JList<Object> list;
  private JScrollPane listScroller;
  private JLabel standbyLabel;

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

  public void showMessageAndStandby(String message) {
    standbyLabel.setText(message);
    standbyCard.revalidate();
    standbyCard.repaint();
    switchStatus(STANDBY);
  }

  private void init() {
    initFrame();
    initControlPane();
    initInputPanel();
    controlPanel.add(inputPanel);
    initCardsPanel();
    controlPanel.add(cardsPanel);
    mainFrame.getContentPane().add(controlPanel, BorderLayout.NORTH);
  }

  private void initCardsPanel() {
    Border margin = new EmptyBorder(10, 5, 100, 5);
    cardLayout = new CardLayout();
    cardsPanel = new JPanel(cardLayout);
    cardsPanel.setBackground(BACKGROUND);
    cardsPanel.setPreferredSize(new Dimension(800, 700));
    initStandbyCard(margin);
    initLoadingCard(margin);
    initResultsCard();
    cardsPanel.add(standbyCard, STANDBY);
    cardsPanel.add(loadingCard, LOADING);
    cardsPanel.add(resultsCard, RESULTS);
    cardLayout.show(cardsPanel, STANDBY);
  }

  private void initResultsCard() {
    Border margin;
    Object[] data = {};
    list = new JList<Object>(data);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setLayoutOrientation(JList.VERTICAL);
    list.setVisibleRowCount(-1);
    list.setFont(MAINFONT);
    list.setBackground(RESULTS_BACKGROUND);
    list.setForeground(RESULTS_TEXT);
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
    resultsCard.setBackground(BACKGROUND);
    margin = new EmptyBorder(10, 10, 10, 10);
    Border border =
        new TitledBorder(BorderFactory.createEtchedBorder(), "Results - [Match Value] | [Title]",
            TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, BOLDFONT, TEXT);
    resultsCard.setBorder(new CompoundBorder(border, margin));
  }

  private void initLoadingCard(Border margin) {
    loadingCard = new JPanel(new GridLayout(0, 1, 0, 30));
    loadingCard.setBackground(BACKGROUND);
    ImageIcon loadingIcon = new ImageIcon(getClass().getClassLoader().getResource("loading.gif"));
    JLabel loadingLabel =
        new JLabel(" Searching... This may take a minute. ", loadingIcon, JLabel.CENTER);
    loadingLabel.setFont(MAINFONT);
    loadingLabel.setForeground(TEXT);
    loadingCard.add(loadingLabel);
    loadingCard.setBorder(margin);
  }

  private void initStandbyCard(Border margin) {
    standbyCard = new JPanel(new GridLayout(0, 1, 0, 30));
    standbyCard.setBackground(BACKGROUND);
    standbyLabel = new JLabel("Enter a series above", JLabel.CENTER);
    standbyLabel.setFont(MAINFONT);
    standbyLabel.setForeground(TEXT);
    standbyCard.add(standbyLabel);
    standbyCard.setBorder(margin);
  }

  private void initInputPanel() {
    inputPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 0));
    JLabel urlLabel = new JLabel("Input a url:");
    urlLabel.setFont(MAINFONT);
    urlLabel.setForeground(TEXT);
    inputPanel.add(urlLabel);
    urlField = new JTextField(22);
    urlField.setFont(MAINFONT);
    urlField.setPreferredSize(new Dimension(100, 40));
    inputPanel.add(urlField);
    urlQuery = new JButton("Search by url");
    urlQuery.setFont(MAINFONT);
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
    JButton urlHelpButton = new JButton("?");
    urlHelpButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(mainFrame, URL_HELP_MESSAGE, "How to use",
            JOptionPane.INFORMATION_MESSAGE);
      }
    });
    urlHelpButton.setMargin(new Insets(0, 0, 0, 0));
    urlHelpButton.setPreferredSize(new Dimension(24, 42));
    urlHelpButton.setBackground(Color.CYAN);
    inputPanel.add(urlHelpButton);
    inputPanel.setBackground(BACKGROUND);
    inputPanel.setPreferredSize(new Dimension(100, 240));
    Border margin = new EmptyBorder(30, 0, 10, 0);
    inputPanel.setBorder(margin);
  }

  private void initControlPane() {
    controlPanel = new JPanel();
    controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.PAGE_AXIS));
    controlPanel.setPreferredSize(new Dimension(780, 640));
    Border margin = new EmptyBorder(10, 20, 20, 20);
    Border border = BorderFactory.createEtchedBorder();
    controlPanel.setBorder(new CompoundBorder(border, margin));
    controlPanel.setBackground(BACKGROUND);
  }

  private void initFrame() {
    mainFrame = new JFrame("Watch This - PROTOTYPE");
    mainFrame.setBounds(400, 100, 450, 300);
    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    mainFrame.setBackground(Color.DARK_GRAY);
    mainFrame.setResizable(false);
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
