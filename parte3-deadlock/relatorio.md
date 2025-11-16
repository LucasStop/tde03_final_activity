# Parte 3 - Deadlock

## Objetivo

Reproduzir e analisar um **deadlock clássico** com duas threads e dois locks, mapear as **4 condições de Coffman** que o causam, e implementar uma **correção baseada em hierarquia de recursos** que elimina a espera circular.

---

## Implementações

### 1. DeadlockDemo.java

- **Objetivo** : Demonstrar deadlock propositalmente
- **Comportamento** : Programa trava indefinidamente
- **Causa** : Thread 1 adquire A→B enquanto Thread 2 adquire B→A (ordem inversa)
- **Resultado** : Espera circular - threads bloqueadas permanentemente

### 2. DeadlockCorrigido.java

- **Objetivo** : Corrigir o deadlock
- **Solução** : Hierarquia de recursos - todas as threads adquirem na ordem A→B
- **Comportamento** : Ambas threads concluem com sucesso
- **Tempo** : ~305ms para execução completa

---

## Como Executar

```bash
# Compilar
javac DeadlockDemo.java
javac DeadlockCorrigido.java

# Executar versão COM deadlock (vai travar!)
java DeadlockDemo
# Pressione Ctrl+C para encerrar

# Executar versão CORRIGIDA
java DeadlockCorrigido
```

---

## O que é Deadlock?

**Deadlock** é uma situação em que um conjunto de threads não progride porque cada uma aguarda que outra libere um recurso, formando um **ciclo de espera** onde nenhuma consegue avançar.

### Exemplo do Mundo Real

Imagine dois carros em uma ponte estreita de mão única:

- Carro A entra pela esquerda
- Carro B entra pela direita (simultaneamente)
- Ambos ficam travados no meio
- Nenhum pode avançar ou recuar
- **Deadlock!**

---

## Resultados Obtidos

### Execução COM Deadlock (DeadlockDemo)

```
DEMONSTRAÇÃO DE DEADLOCK (SEM CORREÇÃO)
Iniciando duas threads com ordem inversa de aquisição de locks...

[T1] Adquiriu LOCK_A
[T1] Aguardando para adquirir LOCK_B...
[T2] Adquiriu LOCK_B
[T2] Aguardando para adquirir LOCK_A...

DEADLOCK DETECTADO!
As threads estão travadas esperando uma pela outra.

Estado das threads:
  - T1: possui LOCK_A, aguardando LOCK_B
  - T2: possui LOCK_B, aguardando LOCK_A

Espera circular detectada! Nenhuma progredirá.

Threads ainda em execução:
  - Thread-1: BLOQUEADA
  - Thread-2: BLOQUEADA
```

**Observações:**

- Programa nunca termina (necessário Ctrl+C)
- Ciclo de espera formado: T1→B→T2→A→T1
- Ambas threads permanentemente bloqueadas
- Nenhuma mensagem de "Concluiu com sucesso!" é exibida

---

### Execução CORRIGIDA (DeadlockCorrigido)

```
DEMONSTRAÇÃO DE DEADLOCK (COM CORREÇÃO)
Estratégia: Hierarquia de Recursos (ordem global)
Regra: SEMPRE adquirir LOCK_A antes de LOCK_B

[T1] Adquiriu LOCK_A
[T1] Adquiriu LOCK_B
[T1] Executando seção crítica...
[T1] Concluiu com sucesso!
[T1] Liberou LOCK_B
[T1] Liberou LOCK_A
[T2] Adquiriu LOCK_A
[T2] Adquiriu LOCK_B
[T2] Executando seção crítica...
[T2] Concluiu com sucesso!
[T2] Liberou LOCK_B
[T2] Liberou LOCK_A

Ambas as threads concluíram.
Tempo total: 305ms

ANÁLISE DA CORREÇÃO
Ordem global imposta: A sempre antes de B
Espera circular eliminada
Deadlock impossível com esta estratégia

Estado final das threads:
  • Thread-1: Finalizada
  • Thread-2: Finalizada
```

**Observações:**

- Programa termina normalmente
- Tempo total: 305ms
- Ambas threads executam completamente
- Execução sequencial: T1 completa → T2 completa

---

## Análise do Deadlock

### Cenário Problemático (DeadlockDemo)

```java
// Thread 1: Ordem A → B
synchronized(LOCK_A) {        // Adquire A
    synchronized(LOCK_B) {    // Aguarda B (T2 tem!)
        // nunca executa
    }
}

// Thread 2: Ordem B → A (INVERSA!)
synchronized(LOCK_B) {        // Adquire B
    synchronized(LOCK_A) {    // Aguarda A (T1 tem!)
        // nunca executa
    }
}
```

### Timeline do Deadlock

| Tempo    | Thread 1          | Thread 2          | Resultado        |
| -------- | ----------------- | ----------------- | ---------------- |
| t=0ms    | Adquire LOCK_A    | -                 | T1 possui A      |
| t=0ms    | -                 | Adquire LOCK_B    | T2 possui B      |
| t=50ms   | Aguarda LOCK_B... | -                 | T1 BLOQUEADA     |
| t=50ms   | Aguarda LOCK_B... | Aguarda LOCK_A... | AMBAS BLOQUEADAS |
| t=2000ms | bloquead          | bloqueada         | **DEADLOCK!**    |

### Grafo de Espera (Visualização)

```
┌─────────┐              ┌─────────┐
│ Thread1 │──aguarda→   │ LOCK_B  │
└────┬────┘              └────┬────┘
     │                        │
   possui                  possui
     │                        │
     ▼                        ▼
┌────────┐              ┌─────────┐
│ LOCK_A │   ←──aguarda─│ Thread2 │
└────────┘              └─────────┘

     ↑                        ↓
     └────────CICLO!──────────┘
```

**Ciclo detectado** → T1 espera T2 espera T1 → Deadlock!

---

## As 4 Condições de Coffman

Para que deadlock ocorra, **todas as 4** condições devem estar presentes **simultaneamente** :

### 1. **Exclusão Mútua**

> Apenas uma thread pode usar o recurso por vez

**No nosso código:**

```java
synchronized(LOCK_A) { ... }  // Apenas 1 thread por vez no bloco
```

**Presente no DeadlockDemo** : `synchronized` garante exclusão mútua sobre cada lock

---

### 2. **Manter-e-Esperar** (Hold and Wait)

> Thread segura um recurso enquanto aguarda outro

**No nosso código:**

```java
synchronized(LOCK_A) {        // T1 segura A
    dormir(50);
    synchronized(LOCK_B) {    // T1 aguarda B (mantendo A!)
```

**Presente no DeadlockDemo** : T1 mantém LOCK_A enquanto espera LOCK_B

---

### 3. **Não Preempção**

> Recursos não podem ser tirados à força, apenas liberados voluntariamente

**No nosso código:**

- Lock `synchronized` não pode ser "roubado" de outra thread
- Só é liberado automaticamente ao sair do bloco `{}`
- JVM não permite forçar liberação

**Presente no DeadlockDemo** : Locks não podem ser preemptados

---

### 4. **Espera Circular**

> Existe um ciclo de threads onde cada uma espera pela próxima

**No nosso código:**

```
Thread 1: possui LOCK_A → aguarda LOCK_B
Thread 2: possui LOCK_B → aguarda LOCK_A
       ↓                      ↑
       └───── CICLO! ─────────┘
```

**Presente no DeadlockDemo** : Ciclo de espera T1→T2→T1 formado

---

### Resumo - Condições no DeadlockDemo

| Condição             | Status | Onde ocorre no código                          |
| -------------------- | ------ | ---------------------------------------------- |
| **Exclusão Mútua**   | SIM    | `synchronized(LOCK_A)`e `synchronized(LOCK_B)` |
| **Manter-e-Esperar** | SIM    | Thread segura um lock e tenta adquirir outro   |
| **Não Preempção**    | SIM    | JVM não permite tomar locks à força            |
| **Espera Circular**  | SIM    | T1→LOCK_B→T2→LOCK_A→T1 (ciclo!)                |

**4/4 condições satisfeitas** → **DEADLOCK INEVITÁVEL!**

---

## Solução: Hierarquia de Recursos

### Estratégia

**Impor uma ordem global de aquisição de recursos** que todas as threads devem seguir, eliminando a possibilidade de ciclos.

**Regra** : SEMPRE adquirir LOCK_A antes de LOCK_B

### Implementação (DeadlockCorrigido)

```java
// AMBAS as threads seguem a MESMA ordem: A → B

Thread 1:
  synchronized(LOCK_A) {      // 1. A primeiro
    dormir(50);
    synchronized(LOCK_B) {    // 2. B depois
      // executa seção crítica
    }
  }

Thread 2:
  synchronized(LOCK_A) {      // 1. A primeiro (IGUAL T1!)
    dormir(50);
    synchronized(LOCK_B) {    // 2. B depois
      // executa seção crítica
    }
  }
```

### Fluxo de Execução

```
Timeline com hierarquia:

t=0ms:   T1 adquire LOCK_A
t=0ms:   T2 tenta LOCK_A... (aguarda T1)
t=50ms:  T1 adquire LOCK_B
t=150ms: T1 executa seção crítica
t=250ms: T1 libera LOCK_B
t=250ms: T1 libera LOCK_A
t=250ms: T2 adquire LOCK_A  (agora disponível!)
t=300ms: T2 adquire LOCK_B
t=400ms: T2 executa seção crítica
t=500ms: T2 libera LOCK_B
t=500ms: T2 libera LOCK_A
t=505ms: ✅ Ambas concluídas!
```

### Por que Funciona?

Com ordem global **A sempre antes de B** :

1. **T1 consegue ambos locks** (A→B) e executa
2. **T2 aguarda LOCK_A** (não tem B ainda, então não causa ciclo)
3. **T1 libera tudo** quando termina
4. **T2 consegue ambos locks** (A→B) e executa

**Impossível formar ciclo!**

```
Análise de ciclo:
- T1 pode esperar B? Só se já tiver A
- T2 pode esperar A? Sim, mas T2 nunca terá B antes de A
- Logo: Nunca haverá T1→B→T2→A→T1

Ordem parcial garante: A < B (sempre)
```

---

## Qual Condição Foi Eliminada?

### DeadlockCorrigido - Análise de Coffman

| Condição             | Status         | Situação                     |
| -------------------- | -------------- | ---------------------------- |
| **Exclusão Mútua**   | AINDA PRESENTE | `synchronized`ainda usado    |
| **Manter-e-Esperar** | AINDA PRESENTE | Thread segura A e aguarda B  |
| **Não Preempção**    | AINDA PRESENTE | Locks não podem ser forçados |
| **Espera Circular**  | **ELIMINADA!** | Ordem global impede ciclo    |

**3/4 condições presentes** → **Deadlock IMPOSSÍVEL!**

### Por que Espera Circular Foi Eliminada?

A hierarquia cria uma **ordem total** nos recursos:

```
LOCK_A tem prioridade 1 (maior)
LOCK_B tem prioridade 2 (menor)

Regra: sempre adquirir em ordem crescente de ID
```

**Resultado matemático:**

- Para haver ciclo: T1→R1→T2→R2→T1
- Com hierarquia: R1 > R2 sempre (não pode ter R2 > R1 no ciclo)
- Logo: grafo de espera é **acíclico** (DAG - Directed Acyclic Graph)

  **Conclusão** : Deadlock é matematicamente impossível com ordem global!

---

## Comparação dos Resultados

| Aspecto                | DeadlockDemo         | DeadlockCorrigido |
| ---------------------- | -------------------- | ----------------- |
| **Termina?**           | Não (trava)          | Sim (305ms)       |
| **T1 completa?**       | Bloqueada            | Sim               |
| **T2 completa?**       | Bloqueada            | Sim               |
| **Espera circular?**   | Sim (T1↔T2)          | Não (T1→T2)       |
| **Ordem de aquisição** | Inversa (A→B vs B→A) | Igual (A→B ambas) |
| **Condições Coffman**  | 4/4                  | 3/4               |

---

## Relação com o Problema dos Filósofos

A solução do deadlock é **idêntica em conceito** à solução do Jantar dos Filósofos (Parte 1):

### Filósofos (Parte 1)

```
Problema: Cada filósofo pega garfos em ordem diferente
Solução:  Atribuir ID aos garfos, sempre pegar menor ID primeiro
Resultado: Elimina espera circular entre filósofos
```

### Locks (Parte 3)

```
Problema: Threads pegam locks em ordem diferente
Solução:  Atribuir ordem aos locks, sempre pegar A antes de B
Resultado: Elimina espera circular entre threads
```

**Princípio Unificador** :

> "Hierarquia de recursos com ordem global elimina espera circular em qualquer sistema concorrente"

---

## Outras Estratégias de Tratamento de Deadlock

### 1. **Prevenção** (usamos esta!)

Quebra uma das 4 condições antes do deadlock ocorrer:

- **Hierarquia de recursos** → elimina espera circular (nossa solução)
- Alocação atômica → elimina manter-e-esperar
- Preempção de locks → elimina não-preempção (complexo!)
- Recursos não exclusivos → elimina exclusão mútua (nem sempre possível)

  **Vantagem** : Sistema nunca trava
  **Desvantagem** : Pode reduzir concorrência

---

### 2. **Evitar** (Banker's Algorithm)

Analisa requisições antes de conceder recursos:

- Só permite alocação se sistema permanece em "estado seguro"
- Requer conhecimento prévio de necessidades máximas
- Overhead computacional alto

  **Vantagem** : Mais flexível que prevenção
  **Desvantagem** : Complexo, requer informação prévia

---

### 3. **Detectar e Recuperar**

Permite deadlock ocorrer, depois age:

- Monitora grafo de espera em tempo de execução
- Detecta ciclos periodicamente
- Recupera: mata threads, rollback de transações

  **Vantagem** : Máxima concorrência
  **Desvantagem** : Complexo recuperar sem perda de dados

---

### 4. **Ignorar** (Ostrich Algorithm)

Assume que deadlock é raro e aceita o risco:

- Não faz nada para prevenir/detectar
- Deixa usuário reiniciar se necessário
- Usado em alguns SOs para certos recursos

  **Vantagem** : Zero overhead
  **Desvantagem** : Sistema pode travar

---

**Nossa escolha** : **Prevenção por hierarquia**

- Simples de implementar
- Overhead mínimo
- Garantia matemática
- Fácil de raciocinar
- Melhor custo-benefício

---

## Conclusões

1. **Deadlock requer 4 condições simultâneas** (Coffman, 1971)
   - Basta quebrar **UMA** para prevenir completamente
2. **Hierarquia de recursos = solução elegante e eficaz**
   - Ordem global elimina espera circular
   - Fácil de implementar (1 linha de mudança!)
   - Funciona para N threads e M recursos
3. **Mesma técnica resolve múltiplos problemas clássicos**
   - Jantar dos Filósofos (Parte 1)
   - Deadlock com locks (Parte 3)
   - Transferências bancárias concorrentes
   - Princípio universal em sistemas concorrentes
4. **Prevenção > Detecção** na maioria dos casos práticos
   - Menor complexidade
   - Mais previsível
   - Sistema nunca para inesperadamente
5. **Trade-off aceitável: serialização parcial**
   - Threads podem aguardar locks na ordem correta
   - Mas sistema sempre progride (sem deadlock)
   - Performance ≫ Sistema travado!

---

## Referências

- [Wikipedia - Deadlock](https://en.wikipedia.org/wiki/Deadlock)
- Coffman, E. G., et al. "System Deadlocks" (1971) - Condições de Coffman
- Dijkstra, E. W. - Hierarquia de recursos para exclusão mútua
- Operating Systems Concepts (Silberschatz) - Capítulo sobre Deadlock
- Java Language Specification - Synchronized statements
