# Building an MCP Agent Server with Spring AI

Spring AI를 활용하여 Tool Calling 기반의 LLM Agent Server를 구축하고, 최종적으로 MCP (Model Context Protocol) Server까지 확장하는 프로젝트이다.

## 목표

- Spring AI 프레임워크 이해 및 활용
- LLM Tool Calling 메커니즘 학습
- Agent Architecture 설계 원칙 이해
- MCP Protocol 표준 학습 및 서버 구현
- Transport 계층 분리 설계 (WebSocket / stdio)

**최종 목표** : Spring AI 기반 Agent + MCP Server (WebSocket + stdio)


## Architecture

```
Client (HTTP / WebSocket / stdio)
    ↓
MCP Router (JSON-RPC)
    ↓
ChatService (Spring AI)
    ↓
LLM (Ollama Llama3.2)
    ↓
Tool Calling
    ↓
WeatherTool
├─ GeoService → Geocoding API
└─ WeatherClient → Weather API
```


## Project structure

```
src/main/java/
├── config/         # Spring AI 설정, Bean 구성
├── controller/     # HTTP REST API (테스트/디버깅)
├── service/        # 비즈니스 로직, 외부 API 호출
├── tool/           # LLM이 호출 가능한 Tool 정의
├── websocket/      # MCP WebSocket Transport
└── stdio/          # MCP stdio Transport
```

### 각 layer의 역할

| 패키지        | 역할                          | 비고                           |
| ---------- | --------------------------- | ---------------------------- |
| config     | Spring Bean 및 ChatClient 설정 | AI 모델 연결 설정                  |
| controller | HTTP REST API               | 초기 테스트 및 디버깅용                |
| service    | 비즈니스 로직 / 외부 API 호출        | Geocoding, Weather Service    |
| tool       | LLM이 호출하는 Function         | `@Tool` 어노테이션 기반             |
| websocket  | MCP WebSocket Transport     | 양방향 실시간 통신                   |
| stdio      | MCP stdio Transport         | CLI 도구 연동 (Claude Desktop 등) |


## 핵심 개념

### 1. Spring AI

**Spring AI**는 AI 애플리케이션 개발을 위한 Spring 생태계 프레임워크이다.

#### 주요 특징

- **통합된 추상화** : OpenAI, Ollama, Anthropic 등 다양한 LLM Provider를 단일 인터페이스로 통합
- **Spring 생태계 통합** : 기존 Spring Boot 프로젝트에 자연스럽게 결합
- **Tool Calling 지원** : LLM이 외부 함수를 호출할 수 있도록 지원
- **Prompt Engineering** : 템플릿 기반 프롬프트 관리

#### 선택 이유

1. **벤더 종속성 회피** : LLM Provider 변경 시 코드 수정 최소화
2. **생산성** : Spring의 DI, AOP 등 기존 기능 활용
3. **확장성** : Tool, Memory, RAG 등으로 확장 가능


### 2. Tool Calling

**Tool Calling**은 LLM이 자연어 요청을 분석하여 적절한 함수를 자동으로 호출하는 메커니즘이다.

#### 동작 흐름

```
User : "서울 날씨 알려줘"
    ↓
LLM Reasoning : 날씨 정보 필요 → getWeather("서울") 호출 결정
    ↓
Tool Execution : WeatherTool.getWeather("서울")
    ↓
API Call : Geocoding API + Weather API
    ↓
Tool Result : "현재 기온 5.9°C, 풍속 6.8 km/h"
    ↓
LLM Final Response : Tool 결과를 자연어로 재구성
    ↓
User Output : "서울의 현재 기온은 5.9°C이고 풍속은 6.8 km/h입니다."
```

#### Tool vs Service

| 구분         | Tool                          | Service                       |
| ---------- | ----------------------------- | ----------------------------- |
| **역할**     | LLM이 호출하는 함수                  | 내부 비즈니스 로직                     |
| **어노테이션**  | `@Tool`                       | `@Service`                    |
| **호출 주체**  | LLM (자동)                      | 소스 코드 (수동)                    |
| **목적**     | AI Agent 확장                   | 재사용 가능한 로직 분리                  |
| **예시**     | `getWeather(String city)`     | `GeoService.getCoordinates()` |

#### Tool 설계 원칙

1. **단일 책임** : 하나의 Tool은 하나의 명확한 기능만 수행
2. **명확한 시그니처** : 파라미터와 반환 타입이 명확해야 LLM이 정확히 호출 가능
3. **에러 처리** : 실패 시 LLM이 이해할 수 있는 명확한 메시지 반환
4. **문서화** : `description`으로 Tool의 용도를 명시 (LLM의 정확한 선택을 위함)


### 3. Agent Architecture

**Agent**는 목표 달성을 위해 자율적으로 도구를 선택하고 실행하는 시스템이다.

#### Agent 구성 요소

1. **Planning** : 사용자 요청 분석 및 실행 계획 수립
2. **Tool Selection** : 필요한 도구 선택
3. **Execution** : 도구 실행 및 결과 수집
4. **Reflection** : 결과 평가 및 다음 단계 결정

#### ReAct (Reasoning + Acting) 패턴

```
Thought     : 사용자가 날씨를 물어봤다. 위치 정보가 필요하다.
Action      : getWeather("서울")
Observation : 서울의 기온은 5.9°C이다.
Thought     : 충분한 정보를 얻었다. 답변을 생성한다.
Answer      : 서울의 현재 날씨는 맑고 기온은 5.9°C입니다.
```

#### 본 프로젝트의 Agent

- **Stateless Agent** : 각 요청을 독립적으로 처리
- **Tool-augmented** : WeatherTool을 통해 실시간 데이터 조회
- **Single-turn** : 한 번의 대화로 완결 (Multi-turn으로 확장 가능)


### 4. Model Context Protocol (MCP)

**MCP**는 AI 애플리케이션과 데이터 소스/도구 간의 표준 통신 프로토콜이다.

#### MCP가 해결하는 문제

- 각 LLM 제공자마다 다른 Tool 연동 방식
- 컨텍스트 공유의 어려움
- 재사용 불가능한 통합 코드

#### MCP 계층 구조

```
┌─────────────────────────────────┐
│   Application Layer             │  ← Claude, ChatGPT 등
│   (MCP Client)                  │
└─────────────────────────────────┘
             ↕ JSON-RPC
┌─────────────────────────────────┐
│   Transport Layer               │
│   (WebSocket / stdio / SSE)     │  ← 통신 방식
└─────────────────────────────────┘
             ↕
┌─────────────────────────────────┐
│   MCP Server                    │
│   (Tools, Resources, Prompts)   │  ← 본 프로젝트
└─────────────────────────────────┘
```

#### JSON-RPC 2.0 기반 통신

**요청 예시**
```json
{
  "jsonrpc": "2.0",
  "id": "req-001",
  "method": "chat",
  "params": { "message": "서울 날씨 알려줘" }
}
```

**정상 응답 예시**
```json
{
  "jsonrpc": "2.0",
  "id": "req-001",
  "result": { "answer": "서울의 현재 기온은 5.9°C입니다." }
}
```

**에러 응답 예시**
```json
{
  "jsonrpc": "2.0",
  "id": "req-001",
  "error": {
    "code": -32600,
    "message": "Invalid Request"
  }
}
```

#### MCP 리소스 타입

1. **Tools** : LLM이 호출 가능한 함수 (본 프로젝트에서 구현)
2. **Resources** : 파일, DB 등 읽기 가능한 데이터
3. **Prompts** : 재사용 가능한 프롬프트 템플릿



### 5. Transport 분리 설계

#### 분리 이유

- **다양한 클라이언트 지원** : 웹 앱(WebSocket), CLI 도구(stdio)
- **관심사의 분리** : 통신 방식과 비즈니스 로직 독립
- **테스트 용이성** : 각 레이어를 독립적으로 테스트 가능

#### Adapter Pattern 적용

```
┌─────────────────┐
│ WebSocketHandler│ ──┐
└─────────────────┘   │
                      ├──→ McpRouter ──→ ChatService
┌─────────────────┐   │
│  StdioHandler   │ ──┘
└─────────────────┘
```

각 Transport는 동일한 `McpRouter` 인터페이스를 호출한다.


## 구현 과정

### Step 1. Spring AI + Ollama 연동

#### Ollama

- 로컬에서 LLM을 실행하는 오픈소스 도구
- Docker처럼 모델을 `pull` / `run` 가능
- API 서버 제공 (default port: 11434)

#### dependency

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-ollama</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

#### application.yml

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: llama3.2
          temperature: 0.7
```

**Temperature**
- `0.0 ~ 0.2` : 정확성, 결정론적 (코딩, 수학, 사실 정보)
- `0.3 ~ 0.5` : 일관성 유지 (데이터 추출, 요약)
- `0.6 ~ 0.7` : 자연스러운 대화 (일상 대화)
- `0.8 ~ 1.0` : 창의성, 다양성 (아이디어 생성, 창작)

#### ChatClient 구성

```java
@Configuration
public class AiConfig {
    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
```

**ChatClient vs ChatModel**
- `ChatModel` : 저수준 API, 세밀한 제어
- `ChatClient` : 고수준 API, Builder Pattern으로 편리한 사용


### Step 2. Tool Calling 구현

#### Tool 등록

```java
@Component
public class WeatherTool {
    
    @Tool(description = "특정 도시의 현재 날씨를 조회합니다.")
    public String getWeather(String city) {
        // Tool 구현
    }
}
```

- `@Tool` : Spring AI가 자동으로 LLM에게 함수 스키마 전달
- `description` : LLM의 Tool 선택 기준

#### 실행 로그

```
========== LLM REQUEST ==========
User Message : 관악구 날씨 알려줘
=================================

DEBUG DefaultToolCallingManager : Executing tool call: getWeather
DEBUG MethodToolCallback        : Starting execution of tool: getWeather

>>> TOOL START : getWeather(city=관악구)
<<< TOOL END : result=해당 도시의 날씨 정보를 찾을 수 없습니다., latency=0ms

DEBUG MethodToolCallback        : Successful execution of tool: getWeather
```

- LLM이 자연어 입력에서 `getWeather` 함수 호출 필요성 판단
- Tool이 정상 실행되었으나 "관악구"는 Geocoding API에서 인식 실패
- Tool Calling 메커니즘은 정상 작동 확인


### Step 3. Hallucination 방지

#### Hallucination이란?

LLM이 **존재하지 않는 패턴이나 객체를 인식하여 무의미하나 부정확한 아웃풋을 생성하는 경우**이다.

**발생 사례**
```
TOOL END : result=해당 도시의 날씨 정보를 찾을 수 없습니다.

========== LLM RESPONSE ==========
Answer :
관악구 날씨는 현재 관악구의 기상 상황을 보여줍니다.
* 기온: 23°C
* 기상: 구름이 조금 많음
* 비: none
==================================
```

**문제점** : Tool이 명확히 "정보를 찾을 수 없다"고 반환했음에도 LLM이 임의로 날씨 데이터를 생성

#### System Prompt로 해결

```java
String systemPrompt = """
    You are a helpful weather assistant.
    
    IMPORTANT RULES:
    - When a tool is used, the tool result is the single source of truth.
    - Never make up data if the tool returns no data.
    - If the tool says data is unavailable, tell the user that the data is unavailable.
    - Do NOT fabricate weather information.
    - Do NOT use your own knowledge for weather.
    
    Use tools whenever the question requires real-world data.
    """;

chatClient.prompt()
    .system(systemPrompt)
    .user(message)
    .call()
    .content();
```

#### System Prompt 작성 원칙

1. **역할 정의** - "You are a..."로 시작
2. **금지 사항 명시** - "Do NOT..."로 명확히 제한
3. **우선순위 지정** - "Tool result is the single source of truth"
4. **예시 제공** - 가능하면 Good/Bad 케이스 포함

#### 개선 후 로그

```
TOOL END : result=해당 도시의 날씨 정보를 찾을 수 없습니다.

========== LLM RESPONSE ==========
Answer :
I apologize... I couldn't retrieve the current weather...
==================================
```

**개선 확인** : Tool 결과를 반영하여 정직하게 "정보를 가져올 수 없다"고 응답


### Step 4. Real API 연동

무료로 사용 가능한 공공 기상 API인 Open-Meteo API를 사용하였다.

**API 흐름**
```
1. Geocoding API: 도시명 → 위도/경도
   https://geocoding-api.open-meteo.com/v1/search?name=Seoul

2. Weather API: 위도/경도 → 날씨 정보
   https://api.open-meteo.com/v1/forecast?latitude=37.5665&longitude=126.978
```

#### 서비스 계층 분리

```
WeatherTool (@Tool)
   ├─ GeoService (@Service)
   │   └─ GeocodingClient (RestClient)
   └─ WeatherClient (RestClient)
```

#### 구현 예시

```java
@Service
@RequiredArgsConstructor
public class GeoService {

    private final GeocodingClient geocodingClient;

    public double[] getCoordinates(String city) {
        return geocodingClient.getCoordinates(city);
    }
}
```

#### API 연동 성공 로그

```
========== LLM REQUEST ==========
User Message : 서울 날씨 알려줘
=================================

Executing tool call: getWeather

>>> TOOL START : getWeather(city=seoul)

지오코딩 API 호출 city=seoul
좌표 변환 성공 lat=37.566, lon=126.9784

날씨 API 호출 lat=37.566, lon=126.9784

<<< TOOL END : 현재 기온 5.9°C, 풍속 6.8 km/h (1556ms)

========== LLM RESPONSE ==========
Answer :
서울의 현재 기온은 5.9°C이고 풍속은 6.8 km/h입니다.

Latency : 2206 ms
==================================
```

1. LLM이 "서울 날씨" → `getWeather("seoul")` 호출 결정
2. Geocoding API로 "seoul" → `(37.566, 126.9784)` 좌표 변환
3. Weather API로 실시간 날씨 조회
4. Tool이 결과 반환 (1556ms 소요)
5. LLM이 자연어로 재구성 (총 2206ms)

### Step 5. MCP Router 설계

#### Router의 역할

Transport 레이어와 Business 레이어를 연결하는 **중재자(Mediator)** 이다.

```java
public class McpRouter {
    private final ChatService chatService;
    
    public JsonRpcResponse route(JsonRpcRequest request) {
        return switch (request.getMethod()) {
            case "chat" -> handleChat(request);
            case "tools/list" -> handleToolsList();
            case "initialize" -> handleInitialize();
            default -> createErrorResponse("Method not found");
        };
    }
}
```

#### 설계 원칙

1. **Single Responsibility** : Router는 라우팅만 담당
2. **Open/Closed** : 새로운 메서드 추가 시 확장 용이
3. **Dependency Inversion** : 인터페이스에 의존

### Step 6. WebSocket Transport 구현

#### WebSocket 이점

- **양방향 통신** : Server → Client 푸시 가능
- **낮은 지연시간** : HTTP보다 오버헤드 적음
- **스트리밍 지원** : LLM 응답 실시간 전송 가능

#### Handler 구현

```java
@Component
public class McpWebSocketHandler extends TextWebSocketHandler {
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) 
            throws Exception {
        
        McpRequest request = objectMapper.readValue(
            message.getPayload().toString(), 
            McpRequest.class
        );

        Object result = router.route(request);

        McpResponse response = new McpResponse("2.0", request.id(), result);

        session.sendMessage(
            new TextMessage(objectMapper.writeValueAsString(response))
        );
    }
}
```

#### 테스트

```javascript
const ws = new WebSocket('ws://localhost:8080/mcp');

ws.onopen = () => {
    ws.send(JSON.stringify({
        jsonrpc: "2.0",
        id: "1",
        method: "chat",
        params: { message: "서울 날씨 알려줘" }
    }));
};

ws.onmessage = (event) => {
    console.log(JSON.parse(event.data));
};
```


### Step 7. stdio Transport 구현

#### stdio란?

**Standard Input/Output**을 통한 프로세스 간 통신 방식이다.

- **CLI 통합** : Claude Desktop, Cursor 등 로컬 AI 도구 연동
- **샌드박스** : 네트워크 없이 로컬에서만 동작
- **간단한 배포** : 단일 JAR 파일로 배포 가능

#### 실행 방식

```bash
java -Dmcp.stdio=true -jar spring-ai-mcp-server.jar
```

#### Handler 구현

```java
public class McpStdioRunner implements CommandLineRunner {
    
    @Override
    public void run(String... args) throws Exception {
        if (!System.getProperty("mcp.stdio", "false").equals("true")) {
            return;
        }

        log.info("MCP stdio mode started");

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(System.in)
        );

        String line;
        while ((line = reader.readLine()) != null) {
            McpRequest request = objectMapper.readValue(line, McpRequest.class);
            Object result = router.route(request);
            McpResponse response = new McpResponse("2.0", request.id(), result);
            
            System.out.println(objectMapper.writeValueAsString(response));
            System.out.flush();
        }
    }
}
```

#### Claude Desktop 연동

`~/Library/Application Support/Claude/claude_desktop_config.json`
```json
{
  "mcpServers": {
    "spring-ai-weather": {
      "command": "java",
      "args": [
        "-Dmcp.stdio=true",
        "-jar",
        "/path/to/spring-ai-mcp-server.jar"
      ]
    }
  }
}
```


## Check List

| 기능                 | 지원  | 설명                              |
| ------------------ | --- | ------------------------------- |
| Spring AI Chat     | ✅   | Ollama 기반 대화형 AI               |
| Tool Calling       | ✅   | WeatherTool 자동 호출               |
| Real API Agent     | ✅   | Open-Meteo API 연동               |
| MCP WebSocket      | ✅   | 웹 애플리케이션 통합                     |
| MCP stdio          | ✅   | CLI 도구 통합 (Claude Desktop)      |
| System Prompt      | ✅   | Hallucination 방지                |
| Logging            | ✅   | 요청/응답/Latency 추적               |
| Transport 독립 설계  | ✅   | WebSocket ↔ stdio 분리            |


## 학습 정리

1. **Spring AI 아키텍처**
   - ChatClient API 활용법
   - Tool 자동 등록 및 스키마 생성
   - Ollama 로컬 모델 연동

2. **LLM Tool Calling**
   - Function Calling 동작 원리
   - Tool과 Service의 계층 분리
   - System Prompt를 통한 Hallucination 방지

3. **Agent 설계**
   - ReAct 패턴 구현
   - Stateless Agent 설계
   - 외부 API 통합 전략

4. **MCP 프로토콜**
   - JSON-RPC 2.0 구조
   - Capability Negotiation
   - Tool Discovery 메커니즘

5. **아키텍처 패턴**
   - Transport 레이어 분리 (Adapter Pattern)
   - Router 기반 메서드 라우팅
   - WebSocket / stdio 이중 지원

6. **설계 원칙**
    - 관심사 분리 : Transport ↔ Business ↔ External API
    - 확장성 : 새로운 Tool 추가 용이
    - 테스트 가능성 : 각 레이어 독립적 테스트
    - 표준 준수 : MCP 프로토콜 스펙 준수


## Reference

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Model Context Protocol](https://modelcontextprotocol.io/)
- [Ollama](https://ollama.ai/)
- [Open-Meteo API](https://open-meteo.com/)
- [JSON-RPC 2.0 Specification](https://www.jsonrpc.org/specification)
