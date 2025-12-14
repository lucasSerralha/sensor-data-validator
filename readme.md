Smart Parking System (Event-Driven Architecture)

Este projeto implementa um sistema de Estacionamento Inteligente utilizando uma Arquitetura Orientada a Eventos (EDA). O objetivo é cruzar dados de sensores IoT (ocupação de vagas) com pagamentos realizados pelos condutores em tempo real.

🏗️ Arquitetura & Fluxo

O sistema comunica de forma assíncrona através do Apache Kafka:

driver-api-gateway: Recebe pagamentos via REST e publica no tópico payment-events.

iot-sensor-producer: Simula sensores e publica mudanças de estado no tópico sensor-events.

common-dto: Biblioteca partilhada que contém os modelos de dados (POJOs), garantindo consistência entre os microsserviços.

🛠️ Tech Stack & Pré-requisitos

Java: OpenJDK 21

Build Tool: Maven 3.8+

Messaging: Apache Kafka (via Docker)

Containerization: Docker & Docker Compose

🚀 Instalação e Execução

⚠️ IMPORTANTE: Siga esta ordem estrita para evitar erros de dependência.

1. Iniciar Infraestrutura (Kafka)

Na raiz do projeto (onde está o docker-compose.yml), inicie o broker:

docker-compose up -d


Aguarde alguns segundos até o Kafka estar pronto na porta 9092.

2. Compilar a Biblioteca Partilhada (CRÍTICO)

Como os outros serviços dependem do common-dto, este deve ser compilado e instalado no repositório local (.m2) primeiro. Sempre que alterar uma classe DTO, repita este passo.

cd common-dto
mvn clean install


3. Rodar os Microsserviços

Abra terminais separados para cada serviço e execute:

Terminal A - IoT Sensor Producer (Simulação):

cd iot-sensor-producer
mvn spring-boot:run


Terminal B - Driver API Gateway (Pagamentos):

cd driver-api-gateway
mvn spring-boot:run


Nota: Certifique-se de que o driver-api-gateway está configurado para a porta 8082 (ou 8080) no application.properties.

🧪 Como Testar (Manual de Uso)

1. Monitorizar o Kafka

Para verificar se as mensagens estão a chegar, pode "escutar" os tópicos diretamente no container do Kafka.

Ver Eventos de Pagamento:

docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic payment-events --from-beginning


Ver Eventos de Sensor:

docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic sensor-events --from-beginning


2. Realizar um Pagamento (Simulação de App)

Utilize o cURL ou Postman para enviar um pagamento.

Parâmetros obrigatórios: plate (matrícula), amount (valor) e parkingSpot (vaga).

Exemplo: Carro na vaga A1 pagando 15.50

curl -X POST "http://localhost:8082/api/payments/pay?plate=AA-00-XX&amount=15.50&parkingSpot=A1"


Resposta de Sucesso (200 OK):

Payment processed for plate AA-00-XX at spot A1


3. Simular Evento de Sensor IoT (Manual)

Além da geração automática, é possível disparar manualmente um evento de sensor via API para testar cenários específicos.

Parâmetros: id (identificador do sensor) e time (tempo simulado).

curl -X POST "http://localhost:8081/api/simulation/trigger?id=sensor-teste-02&time=10"


📦 Contratos de Dados (JSON Payloads)

Estas são as estruturas que viajam no Kafka.

Tópico: payment-events (Gerado pelo driver-api-gateway)

{
"plate": "AA-00-XX",
"parkingSpot": "A1",
"amount": 15.50,
"timestamp": 1702568888888
}


Tópico: sensor-events (Gerado pelo iot-sensor-producer)

{
"sensorId": "A1",
"time": 1702569999999
}

