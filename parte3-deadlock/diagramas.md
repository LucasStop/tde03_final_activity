# Diagramas - Deadlock

## Cenário com Deadlock (DeadlockDemo)

### Ordem de Aquisição

```
Thread 1:  LOCK_A  →  LOCK_B
Thread 2:  LOCK_B  →  LOCK_A  ← ORDEM INVERSA!
```

### Timeline do Deadlock

```
Tempo | Thread 1          | Thread 2          | Estado
------|-------------------|-------------------|------------------
t0    | Adquire LOCK_A    |                   | T1 possui A
t1    |                   | Adquire LOCK_B    | T1→A, T2→B
t2    | Tenta LOCK_B...   |                   | T1 BLOQUEADA
t3    |    (aguardando)   | Tenta LOCK_A...   | T2 BLOQUEADA
t4    |    (aguardando)   |   (aguardando)    | DEADLOCK!
t5    |    (aguardando)   |   (aguardando)    | Para sempre...
```

### Grafo de Espera (Deadlock)

```
    ┌─────────┐
    │ Thread1 │
    └────┬────┘
         │ possui
         ▼
    ┌────────┐        aguarda         ┌─────────┐
    │ LOCK_A │◄───────────────────────┤ Thread2 │
    └────────┘                        └───┬─────┘
         ▲                                │ possui
         │                                ▼
         │ aguarda                   ┌────────┐
         └───────────────────────────┤ LOCK_B │
                                     └────────┘

    CICLO DETECTADO! → Deadlock
```

---

## Cenário Corrigido (DeadlockCorrigido)

### Ordem de Aquisição Padronizada

```
Thread 1:  LOCK_A  →  LOCK_B
Thread 2:  LOCK_A  →  LOCK_B  ← MESMA ORDEM!
```

### Timeline Sem Deadlock

```
Tempo | Thread 1          | Thread 2          | Estado
------|-------------------|-------------------|------------------
t0    | Adquire LOCK_A    |                   | T1 possui A
t1    |                   | Tenta LOCK_A...   | T2 aguarda A
t2    | Adquire LOCK_B    |   (bloqueada)     | T1→A,B T2 espera
t3    | [trabalha...]     |   (bloqueada)     | T1 executando
t4    | Libera LOCK_B     |   (bloqueada)     | T1 ainda com A
t5    | Libera LOCK_A     |                   | T1 liberou tudo
t6    |                   | Adquire LOCK_A    | T2 conseguiu A
t7    |                   | Adquire LOCK_B    | T2→A,B
t8    |                   | [trabalha...]     | T2 executando
t9    |                   | Libera B e A      | Tudo liberado
```

### Grafo de Espera (Sem Ciclo)

```
Cenário 1: T1 executando primeiro

    ┌─────────┐
    │ Thread1 │ ← Executa primeiro
    └────┬────┘
         │ possui
         ▼
    ┌────────┐
    │ LOCK_A │
    └────────┘
         │ possui
         ▼
    ┌────────┐
    │ LOCK_B │
    └────────┘

    Thread2 aguarda LOCK_A (bloqueada, mas SEM CICLO)


Cenário 2: Após T1 liberar

    ┌─────────┐
    │ Thread2 │ ← Agora pode executar
    └────┬────┘
         │ possui
         ▼
    ┌────────┐
    │ LOCK_A │
    └────────┘
         │ possui
         ▼
    ┌────────┐
    │ LOCK_B │
    └────────┘

    SEM CICLO → Sem Deadlock
```

---

## Comparação das Condições de Coffman

### DeadlockDemo (COM deadlock)

| Condição                | Presente? | Explicação                               |
| ----------------------- | --------- | ---------------------------------------- |
| **1. Exclusão Mútua**   | SIM       | `synchronized`= apenas 1 thread por lock |
| **2. Manter-e-Esperar** | SIM       | Thread segura A e aguarda B              |
| **3. Não Preempção**    | SIM       | Lock não pode ser tirado à força         |
| **4. Espera Circular**  | SIM       | T1→B→T2→A→T1 (CICLO!)                    |

**Resultado** : 4/4 condições satisfeitas → **DEADLOCK OCORRE**

### DeadlockCorrigido (SEM deadlock)

| Condição                | Presente? | Explicação                       |
| ----------------------- | --------- | -------------------------------- |
| **1. Exclusão Mútua**   | SIM       | `synchronized`ainda usado        |
| **2. Manter-e-Esperar** | SIM       | Thread segura A e aguarda B      |
| **3. Não Preempção**    | SIM       | Lock não pode ser tirado à força |
| **4. Espera Circular**  | NÃO       | Ordem global impede ciclo!       |

**Resultado** : 3/4 condições → **DEADLOCK IMPOSSÍVEL**

---

## Por que Hierarquia de Recursos Funciona?

### Problema Original

```
Possíveis caminhos de espera:
T1 → LOCK_B → T2 → LOCK_A → T1  (CICLO!)
```

### Com Ordem Global (A sempre antes de B)

```
Caminhos possíveis:
T1 → LOCK_A (ninguém o precede)
T2 → LOCK_A (ninguém o precede)

Impossível criar ciclo quando todos seguem A→B!
```

### Analogia

Imagine duas portas:

- **Sem ordem** : uma pessoa quer entrar pela esquerda e sair pela direita, outra o inverso → trancam!
- **Com ordem** : todos entram pela esquerda e saem pela direita → fluxo funciona!
