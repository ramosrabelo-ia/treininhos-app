package br.com.musculacao.watch;

/** Determina o próximo exercício de uma dupla de duas séries. Não altera o progresso. */
public final class DuoSequence {
    private DuoSequence() {}
    public static int nextOffset(int firstMask, int secondMask) {
        int first = Integer.bitCount(firstMask & 3);
        int second = Integer.bitCount(secondMask & 3);
        if (first >= 2 && second >= 2) return -1;
        if (first >= 2) return 1;
        if (second >= 2) return 0;
        return first <= second ? 0 : 1;
    }
    public static int markNext(int mask) {
        if ((mask & 1) == 0) return mask | 1;
        if ((mask & 2) == 0) return mask | 2;
        return mask;
    }
}
