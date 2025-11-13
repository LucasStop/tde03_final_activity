// Demonstração de DEADLOCK com dois locks
// Este código TRAVA propositalmente para evidenciar o problema

public class DeadlockDemo {

    static final Object LOCK_A = new Object();
    static final Object LOCK_B = new Object();

    public static void main(String[] args) {
        System.out.println("DEMONSTRAÇÃO DE DEADLOCK (SEM CORREÇÃO)");
        System.out.println("Iniciando duas threads com ordem inversa de aquisição de locks...\n");

        Thread t1 = new Thread(() -> {
            synchronized (LOCK_A) {
                System.out.println("[T1] Adquiriu LOCK_A");
                System.out.println("[T1] Aguardando para adquirir LOCK_B...");

                dormir(50);

                synchronized (LOCK_B) {
                    System.out.println("[T1] Adquiriu LOCK_B");
                    System.out.println("[T1] Concluiu com sucesso!");
                }
            }
        }, "Thread-1");

        Thread t2 = new Thread(() -> {
            synchronized (LOCK_B) {
                System.out.println("[T2] Adquiriu LOCK_B");
                System.out.println("[T2] Aguardando para adquirir LOCK_A...");

                dormir(50);

                synchronized (LOCK_A) {
                    System.out.println("[T2] Adquiriu LOCK_A");
                    System.out.println("[T2] Concluiu com sucesso!");
                }
            }
        }, "Thread-2");

        t1.start();
        t2.start();

        dormir(2000);

        System.out.println("\nDEADLOCK DETECTADO!");
        System.out.println("As threads estão travadas esperando uma pela outra.");
        System.out.println("\nEstado das threads:");
        System.out.println("  - T1: possui LOCK_A, aguardando LOCK_B");
        System.out.println("  - T2: possui LOCK_B, aguardando LOCK_A");
        System.out.println("\nEspera circular detectada! Nenhuma progredirá.");

        System.out.println("\nThreads ainda em execução:");
        System.out.println("  - " + t1.getName() + ": "
                + (t1.isAlive() ? "BLOQUEADA" : "Finalizada"));
        System.out.println("  - " + t2.getName() + ": "
                + (t2.isAlive() ? "BLOQUEADA" : "Finalizada"));

        System.out.println("\nPressione Ctrl+C para encerrar o programa travado.");
    }

    static void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
