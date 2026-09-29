package dev.frameart.core.application.port;

import java.util.concurrent.Executor;

/** Abstração das threads do servidor. */
public interface TaskScheduler {

    /** Trabalho pesado (download, processamento, disco). */
    Executor async();

    /** Thread principal do servidor (acesso ao mundo). */
    Executor sync();
}
