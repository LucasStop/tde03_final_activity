# Parte 2 - Threads e Semáforos

## Objetivo

Demonstrar uma **condição de corrida** (race condition) ao incrementar um contador compartilhado com múltiplas threads sem sincronização, e em seguida **corrigi-la** usando um semáforo binário, comparando correção, fairness e impacto no throughput.

---

## Implementações

### 1. CorridaSemControle.java

- **Descrição**: Incrementa contador compartilhado sem qualquer sincronização
- **Comportamento**: Perde incrementos devido a race condition
- **Configuração**: 8 threads × 250.000 incrementos = 2.000.000 esperado

### 2. CorridaComSemaphore.java

- **Descrição**: Usa `Semaphore(1, true)` para garantir exclusão mútua
- **Comportamento**: Sempre produz resultado correto
- **Fairness**: Modo FIFO evita starvation

---

## Como Executar

```bash
# Compilar
javac CorridaSemControle.java
javac CorridaComSemaphore.java

# Executar versão SEM controle
java CorridaSemControle

# Executar versão COM semáforo
java CorridaComSemaphore
```

---

## Resultados Obtidos

### Resumo - Sem Sincronização (5 execuções)

| Execução  | Esperado      | Obtido      | Perdidos      | Perda (%)  | Tempo (s) |
| --------- | ------------- | ----------- | ------------- | ---------- | --------- |
| 1         | 2.000.000     | 492.328     | 1.507.672     | 75,38%     | 0,044     |
| 2         | 2.000.000     | 526.490     | 1.473.510     | 73,68%     | 0,027     |
| 3         | 2.000.000     | 956.222     | 1.043.778     | 52,19%     | 0,038     |
| 4         | 2.000.000     | 461.008     | 1.538.992     | 76,95%     | 0,037     |
| 5         | 2.000.000     | 513.142     | 1.486.858     | 74,34%     | 0,036     |
| **Média** | **2.000.000** | **589.838** | **1.410.162** | **70,51%** | **0,036** |

**Observações:**

- Nenhuma execução produziu resultado correto
- Perda média de ~70% dos incrementos
- Valores variam drasticamente entre execuções (461k a 956k)
- Tempo extremamente rápido (~0,036s)

### Resumo - Com Semáforo (5 execuções)

| Execução  | Esperado      | Obtido        | Diferença | Tempo (s)  |
| --------- | ------------- | ------------- | --------- | ---------- |
| 1         | 2.000.000     | 2.000.000     | 0         | 9,583      |
| 2         | 2.000.000     | 2.000.000     | 0         | 10,198     |
| 3         | 2.000.000     | 2.000.000     | 0         | 10,098     |
| 4         | 2.000.000     | 2.000.000     | 0         | 9,404      |
| 5         | 2.000.000     | 2.000.000     | 0         | 10,715     |
| **Média** | **2.000.000** | **2.000.000** | **0**     | **10,000** |

**Observações:**

- Todas as execuções 100% corretas
- Zero incrementos perdidos
- Tempo ~278x mais lento que sem sincronização
- Tempo consistente entre execuções (9,4s a 10,7s)

---

## Análise Detalhada

### Por que ocorre a condição de corrida?

A operação `count++` **parece** atômica, mas na verdade é decomposta em 3 instruções:

```java
count++

// Na prática, vira:
temp = count;      // 1. LOAD  - lê da memória
temp = temp + 1;   // 2. ADD   - incrementa no registrador
count = temp;      // 3. STORE - escreve na memória
```

**Cenário de perda (exemplo):**

```
Tempo | Thread A          | Thread B          | count na memória
------|-------------------|-------------------|------------------
t0    | LOAD count (100)  |                   | 100
t1    |                   | LOAD count (100)  | 100  ← Lê MESMO valor!
t2    | ADD → temp = 101  |                   | 100
t3    |                   | ADD → temp = 101  | 100
t4    | STORE 101         |                   | 101
t5    |                   | STORE 101         | 101  ← Sobrescreve!
```

**Resultado**: 2 threads incrementaram, mas valor aumentou apenas de 100 → 101 (perda de 1 incremento).

Com 8 threads e milhões de operações, essas colisões acontecem constantemente, explicando a perda de ~70%.

---

### Como o Semáforo resolve?

#### 1. Exclusão Mútua

```java
sem.acquire();  // Bloqueia até conseguir a permissão
count++;        // SEÇÃO CRÍTICA - apenas 1 thread por vez
sem.release();  // Devolve permissão, libera próxima thread
```

O semáforo garante que **apenas 1 thread** esteja na seção crítica por vez, eliminando a possibilidade de leituras/escritas simultâneas.

#### 2. Happens-Before Relationship

O Java Memory Model garante que:

- Todas as escritas **antes** de `release()` na Thread A
- São **visíveis** após `acquire()` na Thread B

Isso previne problemas de:

- Cache desatualizado
- Reordenação de instruções pelo compilador/processador

#### 3. Fairness (Modo FIFO)

```java
Semaphore sem = new Semaphore(1, true);
//                                  ↑
//                                  modo fair
```

**Com fair=true:**

- Threads aguardam em **fila FIFO**
- Próxima thread a entrar = primeira que chamou `acquire()`
- **Evita starvation**: nenhuma thread fica esperando indefinidamente

**Trade-off:**

- Justiça garantida
- Pequeno overhead adicional (gerenciamento da fila)

---

## Comparação e Trade-offs

| Métrica             | Sem Controle       | Com Semáforo   | Diferença       |
| ------------------- | ------------------ | -------------- | --------------- |
| **Correção**        | 29,49% correto     | 100% correto   | -               |
| **Tempo médio**     | 0,036s             | 10,000s        | 278x mais lento |
| **Consistência**    | Altamente variável | Consistente    | -               |
| **Thread-safety**   | Race condition     | Thread-safe    | -               |
| **Previsibilidade** | Imprevisível       | Determinístico | -               |

### Por que 278x mais lento?

1. **Serialização**: Código paralelo vira sequencial na seção crítica
   - 8 threads executando simultaneamente → 1 por vez
2. **Contenção**: Threads aguardam bloqueadas no semáforo
   - Cada `acquire()` pode bloquear se permissão não disponível
3. **Context Switching**: Sistema operacional troca threads constantemente
   - Overhead de salvar/restaurar contexto
4. **Gerenciamento FIFO**: Manutenção da fila de espera
   - Pequeno custo adicional do modo fair

### Vale a pena o overhead?

**SIM, absolutamente!** Em sistemas reais:

**Correção é não-negociável**

- Dados incorretos são **inúteis** ou **perigosos**
- Bug de concorrência pode corromper base de dados

**Bugs de race condition são extremamente difíceis de debugar**

- Aparecem **aleatoriamente** (heisenbug)
- Difíceis de reproduzir em ambiente de teste
- Podem aparecer apenas em produção sob alta carga

**Manutenção e confiabilidade**

- Código thread-safe é mais fácil de manter
- Menos bugs em produção

**Se performance for crítica**:

- Use estruturas concorrentes otimizadas (`AtomicInteger`, `ConcurrentHashMap`)
- Considere lock-free algorithms
- Mas **sempre com correção garantida**

---

## Conceitos Demonstrados

### 1. Race Condition

Situação onde o resultado depende do **timing** imprevisível de execução das threads.

### 2. Seção Crítica

Trecho de código que acessa recurso compartilhado e deve ser protegido.

### 3. Exclusão Mútua

Garantia de que apenas 1 thread execute a seção crítica por vez.

### 4. Semáforo Binário

Semáforo com 1 permissão = **mutex** (mutual exclusion).

### 5. Happens-Before

Relação de ordem de memória garantida pelo Java Memory Model.

### 6. Fairness

Política que evita **starvation** usando fila FIFO.

---

## Conclusões

1. **Operações simples (count++) não são thread-safe** sem proteção

   - Assumir atomicidade é erro comum e perigoso

2. **Semáforo binário = solução elegante** para exclusão mútua

   - API simples: `acquire()` / `release()`
   - Garantias fortes de memória

3. **Correção tem custo de performance**, mas é essencial

   - Trade-off aceitável na maioria dos casos
   - Otimização prematura = raiz de todo mal

4. **Modo fair previne starvation** ao custo de leve overhead

   - Importante para sistemas de longa duração
   - Garante progresso de todas as threads

5. **Sempre priorize correção**, depois otimize se necessário

   - "Make it work, make it right, make it fast" - nessa ordem!

---

## 📚 Referências

- [Oracle - Java Semaphore API](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Semaphore.html)
- Java Concurrency in Practice (Goetz et al.)
- Java Memory Model e Happens-Before ordering
- Condições de Coffman para deadlock
