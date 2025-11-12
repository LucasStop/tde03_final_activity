// Versão com condição de corrida (sem sincronização)
// Demonstra o problema de race condition em contador compartilhado

import java.util.concurrent.*;

public class CorridaSemControle {

    static int count = 0;

    public static void main(String[] args) throws Exception {
        int T = 8;
        int M = 250_000;
        int esperado = T * M;

        System.out.println("EXECUÇÃO SEM SINCRONIZAÇÃO");
        System.out.println("Threads: " + T);
        System.out.println("Incrementos por thread: " + M);
        System.out.println("Valor esperado: " + esperado);
        System.out.println();

        ExecutorService pool = Executors.newFixedThreadPool(T);

        Runnable r = () -> {
            for (int i = 0; i < M; i++) {
                count++;
            }
        };

        long t0 = System.nanoTime();

        for (int i = 0; i < T; i++) {
            pool.submit(r);
        }

        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.MINUTES);

        long t1 = System.nanoTime();
        double tempoSegundos = (t1 - t0) / 1e9;

        System.out.println("RESULTADOS");
        System.out.printf("Esperado: %,d%n", esperado);
        System.out.printf("Obtido:   %,d%n", count);
        System.out.printf("Perdidos: %,d (%.2f%%)%n",
                esperado - count,
                100.0 * (esperado - count) / esperado);
        System.out.printf("Tempo:    %.3f segundos%n", tempoSegundos);

        if (count != esperado) {
            System.out.println("CONDIÇÃO DE CORRIDA DETECTADA!");
            System.out.println("Incrementos foram perdidos devido à falta de sincronização.");
        }
    }
}
