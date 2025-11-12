// Versão correta com Semaphore binário justo
// Demonstra correção da race condition usando exclusão mútua

import java.util.concurrent.*;

public class CorridaComSemaphore {

    static int count = 0;

    static final Semaphore sem = new Semaphore(1, true);

    public static void main(String[] args) throws Exception {
        int T = 8;
        int M = 250_000;
        int esperado = T * M;

        System.out.println("EXECUÇÃO COM SEMÁFORO");
        System.out.println("Threads: " + T);
        System.out.println("Incrementos por thread: " + M);
        System.out.println("Valor esperado: " + esperado);
        System.out.println("Semáforo: binário (1 permissão) em modo FIFO");
        System.out.println();

        ExecutorService pool = Executors.newFixedThreadPool(T);

        Runnable r = () -> {
            for (int i = 0; i < M; i++) {
                try {
                    sem.acquire();

                    count++;

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Thread interrompida: "
                            + Thread.currentThread().getName());
                } finally {
                    sem.release();
                }
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
        System.out.printf("Diferença: %,d%n", Math.abs(esperado - count));
        System.out.printf("Tempo:    %.3f segundos%n", tempoSegundos);

        if (count == esperado) {
            System.out.println("SUCESSO! Resultado correto.");
            System.out.println("O semáforo garantiu exclusão mútua e visibilidade.");
        } else {
            System.out.println("ERRO INESPERADO!");
        }
    }
}
