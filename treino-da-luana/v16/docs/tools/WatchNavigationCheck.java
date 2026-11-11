import com.luanarabelo.treinodaluana.v12.wear.WatchNavigation;
import com.luanarabelo.treinodaluana.v12.wear.WatchNavigation.BottomAction;
import com.luanarabelo.treinodaluana.v12.wear.WatchNavigation.Screen;

/**
 * Confere a tabela de voltar e do botão de baixo (ver docs/NAVEGACAO-WATCH.md).
 * Executar a partir de treino-da-luana/v16:
 *   javac -d /tmp/navcheck wear/src/main/java/com/luanarabelo/treinodaluana/v12/wear/WatchNavigation.java docs/tools/WatchNavigationCheck.java
 *   java -cp /tmp/navcheck WatchNavigationCheck
 */
public final class WatchNavigationCheck {
    private static int failures = 0;

    private static void check(String name, Object expected, Object actual) {
        boolean ok = expected == null ? actual == null : expected.equals(actual);
        System.out.println((ok ? "OK    " : "FALHA ") + name + " -> " + actual);
        if (!ok) failures++;
    }

    public static void main(String[] args) {
        // Voltar: regra única
        check("voltar na Home sai do app", null, WatchNavigation.backTarget(Screen.HOME, null));
        check("voltar na lista de duplas -> Home", Screen.HOME, WatchNavigation.backTarget(Screen.WORKOUT, null));
        check("voltar no exercício -> lista de duplas", Screen.WORKOUT, WatchNavigation.backTarget(Screen.EXERCISE, null));
        check("voltar na confirmação (origem lista) cancela", Screen.WORKOUT,
                WatchNavigation.backTarget(Screen.CONFIRM_FINISH, Screen.WORKOUT));
        check("voltar na confirmação (origem exercício) cancela", Screen.EXERCISE,
                WatchNavigation.backTarget(Screen.CONFIRM_FINISH, Screen.EXERCISE));
        check("voltar no resumo -> Home", Screen.HOME, WatchNavigation.backTarget(Screen.SUMMARY, null));

        // Botão de baixo
        check("lista: finalizar", BottomAction.FINISH_FROM_LIST, WatchNavigation.bottomAction(Screen.WORKOUT, 0, 0, false));
        check("exercício 0/3: marcar série", BottomAction.MARK_SERIES, WatchNavigation.bottomAction(Screen.EXERCISE, 0, 3, false));
        check("exercício 2/3: marcar série", BottomAction.MARK_SERIES, WatchNavigation.bottomAction(Screen.EXERCISE, 2, 3, false));
        check("exercício 3/3: próximo", BottomAction.NEXT_EXERCISE, WatchNavigation.bottomAction(Screen.EXERCISE, 3, 3, false));
        check("último exercício 2/2: finalizar", BottomAction.FINISH_WORKOUT, WatchNavigation.bottomAction(Screen.EXERCISE, 2, 2, true));
        check("último exercício 1/2: marcar série", BottomAction.MARK_SERIES, WatchNavigation.bottomAction(Screen.EXERCISE, 1, 2, true));
        check("confirmação: confirmar", BottomAction.CONFIRM_FINISH, WatchNavigation.bottomAction(Screen.CONFIRM_FINISH, 0, 0, false));
        check("resumo: sincronizar", BottomAction.SYNC, WatchNavigation.bottomAction(Screen.SUMMARY, 0, 0, false));
        check("home: nenhuma", BottomAction.NONE, WatchNavigation.bottomAction(Screen.HOME, 0, 0, false));

        // Próxima série aberta (marcações são preservadas, só a primeira aberta é usada)
        check("máscara 0b000 -> série 0", 0, WatchNavigation.nextOpenSeries(0b000, 3));
        check("máscara 0b001 -> série 1", 1, WatchNavigation.nextOpenSeries(0b001, 3));
        check("máscara 0b101 -> série 1", 1, WatchNavigation.nextOpenSeries(0b101, 3));
        check("máscara 0b111 -> nenhuma", -1, WatchNavigation.nextOpenSeries(0b111, 3));
        check("2 séries 0b11 -> nenhuma", -1, WatchNavigation.nextOpenSeries(0b11, 2));

        check("rótulo série 2", "MARCAR SÉRIE 2", WatchNavigation.bottomLabel(BottomAction.MARK_SERIES, 2));

        if (failures > 0) {
            System.out.println(failures + " verificação(ões) falharam");
            System.exit(1);
        }
        System.out.println("Todas as verificações passaram");
    }
}
