import br.com.musculacao.watch.DuoSequence;
public class DuoSequenceCheck {
    public static void main(String[] args) {
        int a=0,b=0;
        int[] expected={0,1,0,1,-1};
        for (int want: expected) {
            int got=DuoSequence.nextOffset(a,b);
            if (got!=want) throw new AssertionError("esperado "+want+", recebido "+got);
            if (got==0) a=DuoSequence.markNext(a);
            if (got==1) b=DuoSequence.markNext(b);
        }
        for(int x=0;x<4;x++) for(int y=0;y<4;y++) {
            int next=DuoSequence.nextOffset(x,y);
            if(next==0 && Integer.bitCount(x)==2) throw new AssertionError("A completo");
            if(next==1 && Integer.bitCount(y)==2) throw new AssertionError("B completo");
            if(next==-1 && (x!=3 || y!=3)) throw new AssertionError("dupla incompleta");
        }
        if(DuoSequence.markNext(3)!=3) throw new AssertionError("progressão já concluída");
        System.out.println("Fluxo A B A B e retomada: OK");
    }
}
