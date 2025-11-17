# TDE3 - Performance e Sistemas Ciberfísicos

Trabalho sobre concorrência, sincronização e deadlock em sistemas operacionais.

---

## Vídeo Explicativo

**Link do vídeo**: [TDE3 - TRABALHO FINAL](https://youtu.be/k8ycmSxj608)

---

## Estrutura do Projeto

```
parte1-filosofos/          - Jantar dos Filósofos (análise conceitual)
parte2-semaforos/          - Race condition e semáforos (código Java)
parte3-deadlock/           - Deadlock e correção (código Java)
```

---

## Conteúdo

### Parte 1 - Jantar dos Filósofos

- Análise do problema clássico de sincronização
- Protocolo ingênuo (com deadlock)
- Solução com hierarquia de recursos
- Aplicação das condições de Coffman

**Arquivos**: `README.md`, `relatorio.md`, pseudocódigos

---

### Parte 2 - Threads e Semáforos

- Demonstração de race condition
- Correção com semáforo binário
- Comparação de performance e correção
- Análise de happens-before e fairness

**Código Java**:

- `CorridaSemControle.java` - versão com race condition
- `CorridaComSemaphore.java` - versão corrigida

**Executar**:

```bash
cd parte2-semaforos
javac *.java
java CorridaSemControle
java CorridaComSemaphore
```

---

### Parte 3 - Deadlock

- Reprodução de deadlock com 2 threads e 2 locks
- Análise das 4 condições de Coffman
- Correção por hierarquia de recursos
- Eliminação da espera circular

**Código Java**:

- `DeadlockDemo.java` - demonstra deadlock (trava!)
- `DeadlockCorrigido.java` - versão corrigida

**Executar**:

```bash
cd parte3-deadlock
javac *.java
java DeadlockDemo        # Vai travar - use Ctrl+C
java DeadlockCorrigido   # Executa normalmente
```

---

## Conceitos Abordados

- Condições de Coffman para deadlock
- Hierarquia de recursos
- Exclusão mútua com semáforos
- Race conditions
- Happens-before e visibilidade de memória
- Fairness e prevenção de starvation

---

## Autor: Lucas Stopinski da Silva

---

## Referências

1. [Dining Philosophers Problem - Wikipedia](https://en.wikipedia.org/wiki/Dining_philosophers_problem)
2. [Java Semaphore Documentation](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Semaphore.html)
3. [Deadlock - Wikipedia](https://en.wikipedia.org/wiki/Deadlock)
4. Operating Systems Concepts (Silberschatz et al.)
5. Condições de Coffman (1971)

---

## Notas

- Todos os códigos Java foram testados com JDK 8+
- Cada parte tem seu próprio README com detalhes específicos
- Resultados das execuções estão documentados em `resultados.txt`
