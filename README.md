# YTM Stream Demo Application

## Description

This demo application streams bond **Yield to Maturity** (YTM) values to the frontend. It uses **Kafka Streams** to 
consume bond quotes from a Kafka topic, calculates the YTM for each received quote and then publishes the results to 
both **Redis** and a separate Kafka topic.

- **Kafka Streams**: is used to process incoming bond quotes and convert them to YTM values.
- The **Yield to Maturity** (YTM) values are published to a Kafka topic for consumption by other services, which use 
this data to aggregate information and generate chart data.
- **Redis** is used for real-time data streaming via WebSockets to the frontend, providing live updates of the YTM 
values.

This application leverages **Ktor** to handle WebSocket connections, enabling real-time communication between the 
backend and frontend. Importantly, WebSocket sessions are managed so that, regardless of how many clients are connected,
only a single Redis subscription is maintained. This approach reduces overhead and ensures that Redis resources are 
efficiently used, as it avoids multiple redundant subscriptions for each client. It also minimizes the complexity
of managing connections and allows for better scalability.

If you want to read more about this code, please check the 
[blog post](https://traderepublic.substack.com/p/real-time-bond-yield-to-maturity) that explains the code in detail.

## How to Run the Application

### Prerequisites

- **Java**: Ensure you have Java 21 installed. You can download it from [here](https://www.oracle.com/java/technologies/downloads/#java21?er=221886).
- **Docker Compose**: Docker Compose is used to set up the required services (Kafka, Redis). You can install Docker Compose from [here](https://docs.docker.com/compose/install/).

### Steps to Run

1. **Clone the Repository**:
    ```sh
    git clone https://github.com/lfojacintho/ytm-stream.git
    cd ytm-stream
    ```

2. **Start the Required Services**:
   Use Docker Compose to start Kafka and Redis services.
    ```sh
    docker-compose up -d
    ```

3. **Build the Application**:
   Use Gradle to build the application.
    ```sh
    ./gradlew build
    ```

4. **Run the Application**:
   Use Gradle to run the application.
    ```sh
    ./gradlew run
    ```

5. **Connect to the WebSocket**: 
   Use a WebSocket client like [websocat](https://github.com/vi/websocat) to connect to the WebSocket endpoint.
    ```sh
    websocat ws://localhost:8080/ytm/DE0001102440
    ```
A full list of all ISINs available can be found in the `InstrumentsAdapter` file [here](src/main/kotlin/adapters/InstrumentsAdapter.kt).

### Configuration

The application can be configured using environment variables or configuration files. Ensure that the Kafka and Redis configurations are correctly set up in the `application.conf` file.

### Stopping the Services

To stop the Docker Compose services, run:
```sh
docker-compose down
```