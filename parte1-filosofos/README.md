# Parte 1 - Jantar dos Filósofos

## Objetivo

Analisar o **problema clássico do Jantar dos Filósofos**, demonstrar por que o protocolo ingênuo causa **deadlock**, e propor uma solução baseada em **hierarquia de recursos** que elimina a condição de espera circular, garantindo ausência de deadlock.

---

## Conteúdo

### 1. relatorio.md

Documento completo contendo:

- Descrição da dinâmica do problema
- Análise do protocolo ingênuo e origem do deadlock
- As 4 condições de Coffman aplicadas ao problema
- Solução proposta (hierarquia de recursos)
- Prova de ausência de deadlock
- Comparação com outras estratégias

### 2. pseudocodigo-ingenuo.txt

Pseudocódigo detalhado do protocolo que **CAUSA deadlock**:

- Cada filósofo pega garfo esquerdo, depois direito
- Demonstração do cenário de deadlock
- Análise das 4 condições de Coffman presentes

### 3. pseudocodigo-hierarquia.txt

Pseudocódigo da solução que **EVITA deadlock**:

- Ordem global: sempre garfo de menor índice primeiro
- Exemplo de execução sem travamento
- Análise de como a espera circular é eliminada

---

## O Problema

### Cenário

Cinco filósofos sentam-se ao redor de uma mesa circular. Para comer, cada filósofo precisa de **dois garfos** (esquerda e direita), mas há apenas **5 garfos** no total.

```
                    Filósofo 0
                   /          \
            Garfo 0            Garfo 4
               /                  \
        Filósofo 1              Filósofo 4
          |                          |
       Garfo 1                    Garfo 3
          |                          |
        Filósofo 2 — Garfo 2 — Filósofo 3
```

### Estados

- **PENSANDO**: não precisa de garfos
- **COM FOME**: quer comer, tenta pegar garfos
- **COMENDO**: possui ambos garfos e está comendo

---

## Por que o Protocolo Ingênuo Causa Deadlock?

### Algoritmo Ingênuo

```
1. Pegar garfo da esquerda
2. Pegar garfo da direita
3. Comer
4. Liberar garfos
```

### Cenário de Deadlock

Se **todos** os filósofos ficam com fome simultaneamente:

| Tempo | Ação                                                      |
| ----- | --------------------------------------------------------- |
| t₀    | Todos pegam garfo**esquerdo**                             |
| t₁    | Todos tentam pegar garfo**direito** (mas vizinho já tem!) |
| t₂+   | Todos**aguardam indefinidamente** → **DEADLOCK!**         |

### Grafo de Espera (Ciclo!)

```
F0 → G1 → F1 → G2 → F2 → G3 → F3 → G4 → F4 → G0 → F0
                           ↑______|
                           CICLO!
```

---

## As 4 Condições de Coffman no Problema

Para deadlock ocorrer, **todas as 4** devem estar presentes:

| #   | Condição             | Presente? | Onde ocorre                       |
| --- | -------------------- | --------- | --------------------------------- |
| 1   | **Exclusão Mútua**   | SIM       | Garfo usado por 1 filósofo        |
| 2   | **Manter-e-Esperar** | SIM       | Segura esquerda, aguarda direita  |
| 3   | **Não Preempção**    | SIM       | Garfo não pode ser tomado à força |
| 4   | **Espera Circular**  | SIM       | F0→F1→...→F4→F0 (ciclo!)          |

**4/4 condições satisfeitas** → **DEADLOCK INEVITÁVEL**

---

## Solução: Hierarquia de Recursos

### Estratégia

**Impor ordem global**: Sempre adquirir garfo de **menor índice** primeiro, depois o de **maior índice**.

### Regra

```
Para cada filósofo i:
  garfo_esq = i
  garfo_dir = (i + 1) mod 5

  primeiro = min(garfo_esq, garfo_dir)
  segundo = max(garfo_esq, garfo_dir)

  Adquirir(primeiro)  // Sempre menor ID primeiro!
  Adquirir(segundo)
  Comer()
  Liberar(segundo)
  Liberar(primeiro)
```

### Ordem de Aquisição por Filósofo

| Filósofo | Garfos Adjacentes | Ordem de Aquisição        |
| -------- | ----------------- | ------------------------- |
| F0       | esq=0, dir=1      | 0 → 1                     |
| F1       | esq=1, dir=2      | 1 → 2                     |
| F2       | esq=2, dir=3      | 2 → 3                     |
| F3       | esq=3, dir=4      | 3 → 4                     |
| F4       | esq=4, dir=0      | **0 → 4** ← Quebra ciclo! |

**Observação crítica**: F4 pega garfo 0 **antes** de 4, invertendo a ordem natural e quebrando o ciclo circular!

---

## Por que Elimina Deadlock?

### Ordem Parcial

Hierarquia impõe: G₀ < G₁ < G₂ < G₃ < G₄

Todos seguem: **adquirir menor antes de maior**

### Grafo Acíclico

**Com hierarquia:**

```
F0 possui G0 → aguarda G1
F1 aguarda G1 (bloqueado)
F2 aguarda G2 (bloqueado)
F3 aguarda G3 (bloqueado)
F4 aguarda G0 (bloqueado)  ← Não possui G4!

Grafo é ACÍCLICO → SEM DEADLOCK!
```

### Prova por Contradição

**Suponha** que existe ciclo:

- Para fechar ciclo: algum Fₓ deve ter Gⱼ (maior) e esperar Gᵢ (menor)
- Mas pela hierarquia: Fₓ deveria ter pegado Gᵢ **antes** de Gⱼ
- **Contradição!** Logo, não pode existir ciclo.

---

## Qual Condição de Coffman Foi Eliminada?

| Condição            | Protocolo Ingênuo | Hierarquia     |
| ------------------- | ----------------- | -------------- |
| Exclusão Mútua      | SIM               | Ainda presente |
| Manter-e-Esperar    | SIM               | Ainda presente |
| Não Preempção       | SIM               | Ainda presente |
| **Espera Circular** | SIM               | **ELIMINADA!** |

**Resultado**: 3/4 condições → **DEADLOCK IMPOSSÍVEL**

### Por que Espera Circular Foi Eliminada?

A ordem global garante que o grafo de dependências é sempre **acíclico** (DAG - Directed Acyclic Graph). Matematicamente, é impossível formar um ciclo quando todos os processos adquirem recursos em uma ordem total crescente.

---

## Garantias da Solução

### 1. Ausência de Deadlock (Safety)

**Garantia matemática**: Ordem global → grafo acíclico → sem ciclos → sem deadlock

### 2. Progresso (Liveness)

**Sistema sempre progride**: Pelo menos um filósofo pode comer (o que espera garfo de menor índice disponível)

### 3. Justiça (Fairness)

**Condicional**: Depende dos semáforos serem FIFO

- Com FIFO: nenhum filósofo sofre starvation
- Sem FIFO: possível (mas improvável) starvation

---

## Comparação das Abordagens

| Aspecto          | Protocolo Ingênuo    | Hierarquia           |
| ---------------- | -------------------- | -------------------- |
| **Deadlock**     | Possível             | Impossível           |
| **Starvation**   | Possível             | Evitável (FIFO)      |
| **Complexidade** | Muito simples        | Simples (+1 cálculo) |
| **Paralelismo**  | Alto (se não travar) | Moderado             |
| **Correção**     | Não garantida        | Provada              |
| **Overhead**     | Baixo                | Baixo (min/max)      |

---

## Relação com Deadlock (Parte 3)

O Jantar dos Filósofos e o problema de deadlock com locks são **isomórficos**:

| Filósofos             | Locks (Parte 3)         |
| --------------------- | ----------------------- |
| 5 garfos              | 2 locks (A, B)          |
| 5 filósofos           | 2 threads               |
| Ordem de pegar garfos | Ordem de adquirir locks |
| Ciclo F0→...→F4→F0    | Ciclo T1→T2→T1          |

**Solução idêntica**: Hierarquia quebra espera circular em ambos!

**Princípio Universal**:

> "Ordenação total de recursos elimina ciclos de espera em qualquer sistema concorrente"

---

## Outras Estratégias (Comparação)

### 1. Garçom (Árbitro)

- Evita deadlock (máximo N-1 filósofos tentam)
- Ponto único de falha

### 2. Alocação Atômica

- Evita deadlock (pega ambos ou nenhum)
- Serializa verificação (lock global)

### 3. Timeout e Retry

- Pode evitar deadlock
- Risco de livelock (tentativas infinitas)

**Escolha**: **Hierarquia** - melhor trade-off (simples, garantido, baixo overhead)

---

## Conclusões

1. **Jantar dos Filósofos é modelo clássico** de problemas de concorrência

   - Aparece em bancos de dados, SOs, sistemas distribuídos

2. **Protocolo ingênuo inevitavelmente causa deadlock**

   - Todas as 4 condições de Coffman presentes

3. **Hierarquia de recursos é solução elegante**

   - Quebra espera circular
   - Simples de implementar (1 cálculo min/max)
   - Garantia matemática (grafo acíclico)

4. **Mesma técnica resolve múltiplos problemas**

   - Filósofos, deadlock com locks (Parte 3)
   - Princípio universal em sistemas concorrentes

5. **Trade-off aceitável**

   - Leve redução de paralelismo
   - Em troca de correção garantida

---

## Referências

- [Wikipedia - Dining Philosophers Problem](https://en.wikipedia.org/wiki/Dining_philosophers_problem)
- Dijkstra, E. W. (1965) - Cooperating Sequential Processes
- Coffman, E. G. et al. (1971) - System Deadlocks
- Operating Systems Concepts (Silberschatz et al.)
- The Art of Multiprocessor Programming (Herlihy & Shavit)

---

## Arquivos Relacionados

- `relatorio.md` - Análise detalhada completa
- `pseudocodigo-ingenuo.txt` - Versão com deadlock
- `pseudocodigo-hierarquia.txt` - Solução sem deadlock
