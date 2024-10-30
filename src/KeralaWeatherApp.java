import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import javax.swing.table.DefaultTableModel;

public class KeralaWeatherApp extends JFrame {

    private JTextArea resultArea;
    private JTextField searchField;
    private Connection connection;
    private JTable historyTable;
    private DefaultTableModel tableModel;

    // Districts and their API URLs
    private static final String[][] DISTRICTS = {
            {"Thiruvananthapuram", "8.4855", "76.9492"},
            {"Kollam", "8.8811", "76.5847"},
            {"Pathanamthitta", "9.2667", "76.7833"},
            {"Alappuzha", "9.49", "76.3264"},
            {"Kottayam", "9.5869", "76.5213"},
            {"Idukki", "9.85", "76.9667"},
            {"Ernakulam", "9.9399", "76.2602"},
            {"Thrissur", "10.5167", "76.2167"},
            {"Palakkad", "10.7732", "76.6537"},
            {"Malappuram", "11.042", "76.0815"},
            {"Kozhikode", "11.248", "75.7804"},
            {"Wayanad", "11.6994", "76.0773"},
            {"Kannur", "11.4454", "75.7387"},
            {"Kasaragod", "12.4984", "74.9896"}
    };


    private JLabel latestWeatherLabel;
    private JLabel temperatureLabel;
    private JLabel windSpeedLabel;
    private JLabel humidityLabel;
    private JLabel rainLabel;
    private JLabel weatherIconLabel;

    public KeralaWeatherApp() {
        // Frame
        setTitle("Kerala Weather App");
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        //UI components
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton fetchWeatherButton = new JButton("Fetch Weather for All Districts");
        JButton showHistoryButton = new JButton("Show Weather History");
        JButton refreshButton = new JButton("Refresh Data");
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search District");

        //GridBagLayout
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(fetchWeatherButton, gbc);

        gbc.gridx = 1;
        inputPanel.add(showHistoryButton, gbc);

        gbc.gridx = 2;
        inputPanel.add(refreshButton, gbc); 

        gbc.gridx = 3;
        inputPanel.add(new JLabel("Search District:"), gbc);

        gbc.gridx = 4;
        inputPanel.add(searchField, gbc);

        gbc.gridx = 5;
        inputPanel.add(searchButton, gbc);

        // Latest weather update panel
        JPanel latestWeatherPanel = new JPanel(new GridLayout(6, 1));
        latestWeatherLabel = new JLabel("Latest Weather Update", SwingConstants.CENTER);
        latestWeatherLabel.setFont(new Font("Arial", Font.BOLD, 18));
        temperatureLabel = new JLabel("Temperature: N/A");
        windSpeedLabel = new JLabel("Wind Speed: N/A");
        humidityLabel = new JLabel("Humidity: N/A");
        rainLabel = new JLabel("Rain: N/A");
        weatherIconLabel = new JLabel("", SwingConstants.LEFT); // Icon label

        latestWeatherPanel.add(latestWeatherLabel);
        latestWeatherPanel.add(weatherIconLabel); 
        latestWeatherPanel.add(temperatureLabel);
        latestWeatherPanel.add(windSpeedLabel);
        latestWeatherPanel.add(humidityLabel);
        latestWeatherPanel.add(rainLabel);

        latestWeatherPanel.setBorder(BorderFactory.createTitledBorder("Latest Weather"));

        // Text area for results
        resultArea = new JTextArea(5, 50);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setBackground(Color.WHITE);
        resultArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        // Table for history
        String[] columnNames = {"District", "Temperature (°C)", "Wind Speed (km/h)", "Humidity (%)", "Rain (mm)", "Timestamp"};
        tableModel = new DefaultTableModel(columnNames, 0);
        historyTable = new JTable(tableModel);
        historyTable.setFillsViewportHeight(true);
        historyTable.setBackground(Color.LIGHT_GRAY);

        // Components 
        add(inputPanel, BorderLayout.NORTH);
        add(latestWeatherPanel, BorderLayout.CENTER);
        add(new JScrollPane(historyTable), BorderLayout.SOUTH);
        add(new JScrollPane(resultArea), BorderLayout.EAST);

        // Button actions
        fetchWeatherButton.addActionListener(e -> fetchWeatherData());
        showHistoryButton.addActionListener(e -> showWeatherHistory());
        searchButton.addActionListener(e -> searchWeatherData());
        refreshButton.addActionListener(e -> refreshLatestWeatherData()); 

        // Database connection
        connectToDatabase();
    }

    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/weather_db", "username", "password");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to the database.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fetchWeatherData() {
        resultArea.setText("Fetching weather data...");
        new Thread(() -> {
            for (String[] district : DISTRICTS) {
                String name = district[0];
                String latitude = district[1];
                String longitude = district[2];
                String apiUrl = String.format("https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current=temperature_2m,relative_humidity_2m,rain,wind_speed_10m", latitude, longitude);

                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }

                        JSONObject jsonResponse = new JSONObject(response.toString());
                        double temperature = jsonResponse.getJSONObject("current").getDouble("temperature_2m");
                        double windSpeed = jsonResponse.getJSONObject("current").getDouble("wind_speed_10m");
                        double humidity = jsonResponse.getJSONObject("current").getDouble("relative_humidity_2m");
                        double rain = jsonResponse.getJSONObject("current").getDouble("rain");

                        storeWeatherData(name, temperature, windSpeed, humidity, rain);
                        updateLatestWeather(name, temperature, windSpeed, humidity, rain);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error fetching data for " + name, "Fetch Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            SwingUtilities.invokeLater(() -> resultArea.setText("Weather data fetched successfully!"));
        }).start();
    }

    private void storeWeatherData(String district, double temperature, double windSpeed, double humidity, double rain) {
        String insertSQL = "INSERT INTO weather_data (district, temperature, wind_speed, humidity, rain) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setString(1, district);
            pstmt.setDouble(2, temperature);
            pstmt.setDouble(3, windSpeed);
            pstmt.setDouble(4, humidity);
            pstmt.setDouble(5, rain);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showWeatherHistory() {
        tableModel.setRowCount(0); 
        String selectSQL = "SELECT * FROM weather_data ORDER BY timestamp DESC";
        try (Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(selectSQL)) {
            while (rs.next()) {
                String district = rs.getString("district");
                double temperature = rs.getDouble("temperature");
                double windSpeed = rs.getDouble("wind_speed");
                double humidity = rs.getDouble("humidity");
                double rain = rs.getDouble("rain");
                Timestamp timestamp = rs.getTimestamp("timestamp");
                tableModel.addRow(new Object[]{district, temperature, windSpeed, humidity, rain, timestamp});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateLatestWeather(String district, double temperature, double windSpeed, double humidity, double rain) {
        temperatureLabel.setText(String.format("Temperature: %.2f °C", temperature));
        windSpeedLabel.setText(String.format("Wind Speed: %.2f km/h", windSpeed));
        humidityLabel.setText(String.format("Humidity: %.2f %%", humidity));
        rainLabel.setText(String.format("Rain: %.2f mm", rain));
        latestWeatherLabel.setText("Latest Weather Update for " + district);
        weatherIconLabel.setIcon(getWeatherIcon(temperature)); 
    }

    private Icon getWeatherIcon(double temperature) {
        // Setting icons based on temperature;
        ImageIcon originalIcon;
        
        if (temperature > 30) {
            originalIcon = new ImageIcon("elements/sunny_icon.png"); 
        } else if (temperature > 20) {
            originalIcon = new ImageIcon("elements/cloudy_icon.png"); 
        } else {
            originalIcon = new ImageIcon("elements/rainy_icon.png"); 
        }
    
        Image scaledImage = originalIcon.getImage().getScaledInstance(originalIcon.getIconWidth() / 8, 
                                                                    originalIcon.getIconHeight() / 8, 
                                                                    Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    private void searchWeatherData() {
        String searchDistrict = searchField.getText().trim();
        boolean districtFound = false;

        for (String[] district : DISTRICTS) {
            if (district[0].equalsIgnoreCase(searchDistrict)) {
                districtFound = true;
                String name = district[0];
                String latitude = district[1];
                String longitude = district[2];
                String apiUrl = String.format("https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current=temperature_2m,relative_humidity_2m,rain,wind_speed_10m", latitude, longitude);

                // Fetching weather data for the searched district
                new Thread(() -> {
                    try {
                        URL url = new URL(apiUrl);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");

                        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                            StringBuilder response = new StringBuilder();
                            String inputLine;
                            while ((inputLine = in.readLine()) != null) {
                                response.append(inputLine);
                            }

                            JSONObject jsonResponse = new JSONObject(response.toString());
                            double temperature = jsonResponse.getJSONObject("current").getDouble("temperature_2m");
                            double windSpeed = jsonResponse.getJSONObject("current").getDouble("wind_speed_10m");
                            double humidity = jsonResponse.getJSONObject("current").getDouble("relative_humidity_2m");
                            double rain = jsonResponse.getJSONObject("current").getDouble("rain");

                            SwingUtilities.invokeLater(() -> updateLatestWeather(name, temperature, windSpeed, humidity, rain));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Error fetching data for " + name, "Fetch Error", JOptionPane.ERROR_MESSAGE);
                    }
                }).start();
                break;
            }
        }

        if (!districtFound) {
            JOptionPane.showMessageDialog(this, "District not found!", "Search Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refreshLatestWeatherData() {
        String searchDistrict = searchField.getText().trim();
        if (!searchDistrict.isEmpty()) {
            searchWeatherData(); // To refresh data
        } else {
            JOptionPane.showMessageDialog(this, "Please enter a district name to refresh.", "Refresh Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            KeralaWeatherApp app = new KeralaWeatherApp();
            app.setVisible(true);
        });
    }
}
