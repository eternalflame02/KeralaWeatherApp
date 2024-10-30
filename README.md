

### **README.md**

# 🌧️ Kerala Weather Application 🌞

A Java desktop application for fetching, storing, and displaying weather data for Kerala's districts. This app uses the Open-Meteo API and a MySQL database to display both current and past weather conditions for each district.

## 🚀 Features

- **Real-Time Data**: Fetches real-time weather data for Kerala's districts.
- **Historical Tracking**: Stores weather data in a SQL database for history tracking.
- **District Search**: Quickly find weather data by district name.
- **Current Metrics**: View temperature, humidity, wind speed, and rainfall.
- **Weather History**: Explore past weather trends in a clean tabular format.

## 🔧 Prerequisites

Before you get started, ensure you have the following installed:

- **Java Development Kit (JDK)**: Version 11 or higher.
- **MySQL Database**: Installed and configured locally.
- **Visual Studio Code**: Recommended IDE, but feel free to use any Java-compatible IDE.
- **Dependencies**:
  - `org.json.JSONObject` for parsing JSON from the weather API.
  - `javax.swing` for creating a user-friendly graphical interface.
  - **MySQL Connector/J**: Essential for database connectivity (included in the `lib` folder).

## 🛠️ Setup Instructions

### Step 1: Clone the Repository

```bash
git clone https://github.com/your-username/KeralaWeatherApp.git
cd KeralaWeatherApp
```

### Step 2: Set Up the Database

1. Launch MySQL via command line or a GUI tool like MySQL Workbench.
2. Create the `weather_db` database:

   ```sql
   CREATE DATABASE weather_db;
   USE weather_db;
   ```

3. Create the `weather_data` table:

   ```sql
   CREATE TABLE weather_data (
       id INT PRIMARY KEY AUTO_INCREMENT,
       district VARCHAR(50),
       temperature DOUBLE,
       wind_speed DOUBLE,
       humidity DOUBLE,
       rain DOUBLE,
       timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   );
   ```

### Step 3: Configure Database Connection in Code

1. Open `src/KeralaWeatherApp.java`.
2. Update your database credentials in the `connectToDatabase` method:

   ```java
   connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/weather_db", "your_username", "your_password");
   ```

### Step 4: Obtain and Set Up Weather API Access

1. No sign up required for [Open-Meteo](https://open-meteo.com) or use another weather API that supports latitude-longitude data.
2. Update the `apiUrl` in `fetchWeatherData` with your API endpoint and key, if necessary.

### Step 5: Compile and Run the Application

Execute the following commands to get your app running:

```bash
javac -cp ".:lib/*" src/KeralaWeatherApp.java
java -cp ".:lib/*:src" KeralaWeatherApp
```

## 📚 Usage

1. **Fetch All Districts' Weather**: Click “Fetch Weather for All Districts” to see real-time data.
2. **Search by District**: Enter the district name and hit “Search District” to retrieve weather info.
3. **View Weather History**: Click “Show Weather History” for past weather data.
4. **Refresh Data**: Click “Refresh” to update the weather for your selected district.

## 📂 Project Structure

```plaintext
KeralaWeatherApp/
├── src/
│   └── KeralaWeatherApp.java    # Main application code
├── lib/                         # Libraries (e.g., MySQL connector)
├── elements/                    # Weather icons (sunny, cloudy, rainy)
└── README.md                    # Project README
```

## 📦 Dependencies

- **JDK**: For running Java applications.
- **MySQL JDBC Driver**: Place the MySQL JDBC driver (e.g., `mysql-connector-java`) in the `lib` folder for database access.
- **org.json**: For parsing JSON responses from the API.

## 🌟 Future Enhancements

- Enable automatic data refresh at defined intervals.
- Improve the user interface with weather icons and trends.
- Add advanced search and filter options for historical data.

## 📝 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
