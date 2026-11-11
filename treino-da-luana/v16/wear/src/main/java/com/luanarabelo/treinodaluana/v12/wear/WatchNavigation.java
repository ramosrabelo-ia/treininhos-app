package com.luanarabelo.treinodaluana.v12.wear;

/**
 * Regras de navegação do relógio, sem dependência de Android para poderem ser testadas.
 *
 * Regra única do botão voltar (físico ou gesto): volta sempre para a tela anterior e nunca
 * apaga marcações, porque cada série e exercício é salvo no momento do toque. Em telas de
 * confirmação, voltar equivale a cancelar e devolve para a tela de origem.
 */
public final class WatchNavigation {
    private WatchNavigation() {}

    public enum Screen { HOME, WORKOUT, EXERCISE, CONFIRM_FINISH, SUMMARY }

    public enum BottomAction {
        NONE,
        /** Marca a próxima série ainda aberta do exercício atual. */
        MARK_SERIES,
        /** Todas as séries marcadas: conclui o exercício e abre o próximo. */
        NEXT_EXERCISE,
        /** Último exercício com séries marcadas: conclui e segue para finalizar. */
        FINISH_WORKOUT,
        /** Lista de duplas: encerra o treino (com confirmação se houver pendências). */
        FINISH_FROM_LIST,
        /** Tela de confirmação: confirma o encerramento antecipado. */
        CONFIRM_FINISH,
        /** Resumo: pede a sincronização com o Samsung Health. */
        SYNC
    }

    /** Destino do botão voltar; null significa sair do aplicativo. */
    public static Screen backTarget(Screen current, Screen cameFrom) {
        switch (current) {
            case HOME:
                return null;
            case WORKOUT:
                return Screen.HOME;
            case EXERCISE:
                return Screen.WORKOUT;
            case CONFIRM_FINISH:
                return cameFrom == null ? Screen.WORKOUT : cameFrom;
            case SUMMARY:
                return Screen.HOME;
            default:
                return Screen.HOME;
        }
    }

    public static BottomAction bottomAction(Screen screen, int marked, int totalSets, boolean lastExercise) {
        switch (screen) {
            case WORKOUT:
                return BottomAction.FINISH_FROM_LIST;
            case EXERCISE:
                if (marked < totalSets) return BottomAction.MARK_SERIES;
                return lastExercise ? BottomAction.FINISH_WORKOUT : BottomAction.NEXT_EXERCISE;
            case CONFIRM_FINISH:
                return BottomAction.CONFIRM_FINISH;
            case SUMMARY:
                return BottomAction.SYNC;
            default:
                return BottomAction.NONE;
        }
    }

    /** Índice (0 a total-1) da próxima série aberta, ou -1 se todas estiverem marcadas. */
    public static int nextOpenSeries(int mask, int totalSets) {
        for (int index = 0; index < totalSets; index++) {
            if ((mask & (1 << index)) == 0) return index;
        }
        return -1;
    }

    public static String bottomLabel(BottomAction action, int nextSeriesNumber) {
        switch (action) {
            case MARK_SERIES:
                return "MARCAR SÉRIE " + nextSeriesNumber;
            case NEXT_EXERCISE:
                return "PRÓXIMO  ›";
            case FINISH_WORKOUT:
            case FINISH_FROM_LIST:
                return "FINALIZAR";
            case CONFIRM_FINISH:
                return "SIM, FINALIZAR";
            case SYNC:
                return "SINCRONIZAR";
            default:
                return "";
        }
    }
}
