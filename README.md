# MCP Gateway

AI-powered gateway service that uses the Model Context Protocol (MCP) to provide intelligent access to payment gateway APIs through natural language queries.

## What is this?

An intelligent API gateway that:
- Uses **DeepSeek-V3 AI** (via OpenRouter) to understand natural language questions
- Implements **Model Context Protocol (MCP)** for structured tool calling
- Connects to real **payment gateway services** to fetch live data
- Provides a **beautiful chat interface** and **REST API** for interaction

## Features

- 🤖 **AI-Powered**: Uses DeepSeek-V3 for intelligent query processing
- 🔧 **MCP Tools**: Modular tool system for backend integration
- 💱 **Exchange Rates**: Query currency exchange rates and payment methods
- 🎨 **Web Interface**: Beautiful chat UI with markdown rendering
- 🔌 **REST API**: Programmatic access via `/ask-ai` endpoint

## Prerequisites

- Java 23
- Maven 3.x
- OpenRouter API key (get one at https://openrouter.ai)
- Access to payment gateway service

## Configuration

Edit `src/main/resources/application.yml`:

```yaml
llm:
  base-url: https://openrouter.ai/api/v1
  model: deepseek/deepseek-chat
  api-key: YOUR_API_KEY_HERE

backend:
  services:
    payment-gateway:
      url: YOUR_PAYMENT_GATEWAY_URL
```

## How to Run

```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8082`

## Usage

### Web Interface

Open your browser and navigate to:
```
http://localhost:8082
```

Try asking:
- "What can you help me with?"
- "What is the EUR to USD exchange rate?"
- "Show me all exchange rates from CAD"
- "What currencies can be converted to EUR?"

### REST API

```bash
curl -X POST http://localhost:8082/ask-ai \
  -H "Content-Type: application/json" \
  -d '{"question": "What is the exchange rate from EUR to USD?"}'
```

Response:
```json
{
  "answer": "The current exchange rate from EUR to USD is: 1 EUR = 1.126201 USD"
}
```

### Check Status

```bash
curl http://localhost:8082/ask-ai/status
```

## Available Tools

### Get Exchange Rates
Queries currency exchange rates from the payment gateway.

**Parameters:**
- `fromCurrency` (optional): Source currency code (e.g., USD, EUR, CAD)
- `toCurrency` (optional): Target currency code (e.g., EUR, USD)

**Example queries:**
- "What is the USD to EUR rate?"
- "Show me all rates from CAD"
- "What payment methods support HUF to EUR?"

## Architecture

```
User → Chat UI / REST API
    ↓
MCP Gateway (Spring Boot)
    ↓
DeepSeek AI (analyzes & calls tools)
    ↓
MCP Tools (GetExchangeRatesTool)
    ↓
Payment Gateway API
    ↓
Response → Formatted Answer
```

## Project Structure

```
mcp-gateway/
├── src/main/java/com/example/mcpgateway/
│   ├── controller/         # REST endpoints
│   ├── service/           # LLM & tool dispatch services
│   ├── mcp/               # MCP server & tools
│   ├── dto/               # Data transfer objects
│   └── config/            # Spring configuration
├── src/main/resources/
│   ├── static/            # Web interface
│   └── application.yml    # Configuration
└── pom.xml               # Maven dependencies
```

## Adding New Tools

1. Create a class implementing `MCPTool` interface
2. Add `@Component` annotation
3. Implement the required methods
4. Tool is automatically discovered and available to the AI

Example:
```java
@Component
public class MyNewTool implements MCPTool {
    public String getName() { return "my_tool"; }
    public String getDescription() { return "What this tool does"; }
    public Map<String, Object> getInputSchema() { /* schema */ }
    public String execute(Map<String, Object> args) { /* logic */ }
}
```

## Technologies

- **Spring Boot 3.3.4** - Application framework
- **Spring AI MCP** - Model Context Protocol integration
- **DeepSeek-V3** - AI model via OpenRouter
- **Bootstrap 5.3** - UI framework
- **Marked.js** - Markdown rendering
- **Java 23** - Programming language

## Development

```bash
# Compile
mvn compile

# Run tests
mvn test

# Package
mvn package

# Clean build
mvn clean install
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Empty responses | Check API key in `application.yml` |
| Connection refused | Ensure payment gateway URL is correct |
| Port 8082 in use | Change `server.port` in config |
| Tool not found | Verify `@Component` annotation on tool class |

## License

This project is a prototype for demonstration purposes.

