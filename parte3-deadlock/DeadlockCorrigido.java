// Correção do DEADLOCK usando HIERARQUIA DE RECURSOS
// Solução: todas as threads adquirem locks na MESMA ORDEM

public class DeadlockCorrigido {

    static final Object LOCK_A = new Object();
    static final Object LOCK_B = new Object();

    public static void main(String[] args) {
        System.out.println("DEMONSTRAÇÃO DE DEADLOCK (COM CORREÇÃO)");
        System.out.println("Estratégia: Hierarquia de Recursos (ordem global)");
        System.out.println("Regra: SEMPRE adquirir LOCK_A antes de LOCK_B\n");

        Thread t1 = new Thread(() -> {
            synchronized (LOCK_A) {
                System.out.println("[T1] Adquiriu LOCK_A");
                dormir(50);

                synchronized (LOCK_B) {
                    System.out.println("[T1] Adquiriu LOCK_B");
                    System.out.println("[T1] Executando seção crítica...");
                    dormir(100);
                    System.out.println("[T1] Concluiu com sucesso!");
                }
                System.out.println("[T1] Liberou LOCK_B");
            }
            System.out.println("[T1] Liberou LOCK_A");
        }, "Thread-1");

        Thread t2 = new Thread(() -> {
            synchronized (LOCK_A) {
                System.out.println("[T2] Adquiriu LOCK_A");
                dormir(50);

                synchronized (LOCK_B) {
                    System.out.println("[T2] Adquiriu LOCK_B");
                    System.out.println("[T2] Executando seção crítica...");
                    dormir(100);
                    System.out.println("[T2] Concluiu com sucesso!");
                }
                System.out.println("[T2] Liberou LOCK_B");
            }
            System.out.println("[T2] Liberou LOCK_A");
        }, "Thread-2");

        long t0 = System.currentTimeMillis();

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long t1Time = System.currentTimeMillis();

        System.out.println("\nAmbas as threads concluíram.");
        System.out.println("Tempo total: " + (t1Time - t0) + "ms");
        System.out.println("\nANÁLISE DA CORREÇÃO");
        System.out.println("Ordem global imposta: A sempre antes de B");
        System.out.println("Espera circular eliminada");
        System.out.println("Deadlock impossível com esta estratégia");

        System.out.println("\nEstado final das threads:");
        System.out.println("  • " + t1.getName() + ": "
                + (!t1.isAlive() ? "Finalizada" : "Ainda executando"));
        System.out.println("  • " + t2.getName() + ": "
                + (!t2.isAlive() ? "Finalizada" : "Ainda executando"));
    }

    static void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
