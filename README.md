#  Coupon API – Desafio Técnico

API REST para gestão de cupons promocionais, desenvolvida para atender ao desafio técnico TENDA.  
O projeto foi construído com **Java 17 + Spring Boot 3**, utilizando banco em memória **H2** e cobrindo regras de negócio através de testes automatizados.

---

##  Tecnologias utilizadas

- Java 17
- Spring Boot 3 (Web, Validation, JPA)
- H2 Database
- Maven
- JUnit 5 + Mockito
- Spring WebMvcTest (Controller tests)

---

##  Arquitetura

O projeto segue o padrão **MVC com separação de responsabilidades**, organizado da seguinte forma:

```
src/main/java/com.example.demo
│
├── controller → Camada HTTP (endpoints)
├── service → Regras de negócio
├── entity → Modelo de dados (JPA)
├── enum → Enum representando STATUS (ACTIVE, INACTIVE e DELETED)
├── repository → Persistência (Spring Data JPA)
├── dto → Objetos de entrada/saída
└── exceptions → Exceções e handlers globais
```

---

#  Endpoints

##  Criar cupom
- **URL:** `/coupon`
- **Método:** `POST`
- **Descrição:** Cria um novo cupom promocional.

    ### 📥 Body (JSON)
```json
{
  "code": "ABC123!!",
  "description": "Cupom de teste",
  "discountValue": 10.0,
  "expirationDate": "2025-11-04T17:14:45.180Z",
  "published": false
}
  ```
- ** Resposta (JSON)**
```json
{
  "id": "d11fa7b2-714d-43a1-bc76-1ec8b8b1ba50",
  "code": "ABC123",
  "description": "Cupom de teste",
  "discountValue": 10.0,
  "expirationDate": "2025-11-04T17:36:46.577Z",
  "status": "ACTIVE",
  "published": false,
  "redeemed": false
}
```
- **Códigos de resposta:** `201 Created`, `400 Bad Request`
- **Regras de negócio:**
  - O código do cupom deve possuir exatamente 6 caracteres alfanuméricos, após a remoção de caracteres especiais.
  - A data de expiração deve ser futura.
  - O valor do desconto deve ser maior ou igual a 0.5.

## Listar cupom
- **URL:** `/coupon/d11fa7b2-714d-43a1-bc76-1ec8b8b1ba50`
- **Método:** `GET`
- **Descrição:** Retorna um dos cupons cadastrados.

- **📤 Resposta (JSON)**
```json
{
  "id": "d11fa7b2-714d-43a1-bc76-1ec8b8b1ba50",
  "code": "ABC123",
  "description": "Cupom de teste",
  "discountValue": 10.0,
  "expirationDate": "2025-11-04T17:36:46.577Z",
  "status": "ACTIVE",
  "published": false,
  "redeemed": false
}
```
- **Códigos de resposta:** `200 OK`, `404 Not Found`, `409 Conflict`
- **Regras de negócio:**
  - Retorna erro se o cupom não existir.
  - Retorna erro se o cupom estiver expirado.
  - Retorna erro se o cupom não estiver publicado.

## Deletar cupom (SoftDelete)
- **URL:** `/coupon/d11fa7b2-714d-43a1-bc76-1ec8b8b1ba50`
- **Método:** `DELETE`
- **Descrição:** Deleta (soft delete) um cupom cadastrado.
- **Códigos de resposta:** `204 No Content`, `404 Not Found`, `409 Conflict`
- **Regras de negócio:**
  - Retorna erro se o cupom não existir.
  - O soft delete é realizado através do campo `status`.
  - Quando deletado, o status do cupom é alterado para `DELETED`.
  - Cupons com status `DELETED` não são retornados em consultas.
---

## Regras de Negócio Gerais

### ✔ Campos obrigatórios
- `code`
- `description`
- `discountValue`
- `expirationDate`

---

### ✔ Validação do **discountValue**
- Valor mínimo permitido: **0.5**
- Sem valor máximo definido

---

### ✔ Validação da data de expiração
- Não pode estar no passado
- Tipo utilizado: `Instant`
- Formato esperado (ISO 8601):
  `yyyy-MM-dd'T'HH:mm:ss.SSSX`
---

###  Sanitização do código (`code`)
O campo pode conter caracteres especiais, porém:

1. Todos os caracteres **não alfanuméricos** são removidos
2. Se o resultado tiver **mais de 6 caracteres**, é truncado para 6
3. Se o resultado tiver **menos de 6 caracteres**, a API retorna **400 Bad Request**


### Exemplo

**Entrada:**
**Sanitização:**
```json
{ "code": "A!B@1" }
```
- Remove caracteres especiais: "AB1"
- Verifica tamanho: 3 caracteres → Inválido
**Resposta:**
400 Bad Request

## Tratamento Global de Exceções Criação de um ControllerAdvice

### Todas as exceções de negócio e regras violadas retornam status adequados:

####  BusinessException → 400 Bad Request

Usada para:

código sanitizado com menos de 6 caracteres

discountValue inválido

data inválida

#### CouponNotFoundException → 404 Not Found
#### CouponAlreadyDeletedException → 409 Conflict


## Testes Automatizados

O projeto possui cobertura para:

###  Service Layer

- Criação válida de cupom
- Falha com:
    - `discountValue < 0.5`
    - `expirationDate` no passado
    - `code` sanitizado com menos de 6 caracteres
- Sucesso no soft delete
- Falha ao deletar cupom inexistente
- Falha ao deletar cupom já deletado

---

###  Controller Layer
Testado com `@WebMvcTest`:

- **POST** `/coupon` → **201 Created**
- **POST** `/coupon` com payload inválido → **400 Bad Request**
- **DELETE** `/coupon/{id}` → **204 No Content**
- **DELETE** cupom inexistente → **404 Not Found**
- **DELETE** cupom já removido → **409 Conflict**

### Como rodar os testes

```bash
mvn test
```

---

## Banco de dados (H2)

A aplicação usa banco em memória.

Acessar console H2:
```
http://localhost:8081/h2-console
User: admin
Password: 123
```
---

## Como rodar o projeto

```bash
mvn clean package
mvn spring-boot:run
```
Ou via IntelliJ IDEA:
```
Run → DemoApplication
```



