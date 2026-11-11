package br.com.musculacao.watch;
public final class WorkoutData {
 private WorkoutData() {}
 public static final int BLOCKS_PER_WORKOUT=5, TOTAL_BLOCKS=20, TOTAL_EXERCISES=40;
 public static final String[] LETTERS={"A","B","C","D"};
 public static final String[] TYPES={"SUPERIOR PUSH","INFERIOR QUADS","SUPERIOR PULL","INFERIOR POSTERIOR"};
 public static final String[] FOCUSES={"Peito, ombro e tríceps","Quadríceps e glúteos","Costas e bíceps","Posterior e glúteos"};
 public static final String[][] NAMES={{"Supino na máquina","Elevação lateral com halteres","Supino inclinado na máquina","Dead bug no tapete","Desenvolvimento na máquina","Panturrilha no leg press","Tríceps francês com halter","Prancha","Crucifixo com halteres","Abdução de quadril"},{"Leg press 45°","Panturrilha no leg press","Cadeira extensora","Prancha","Cadeira abdutora","Dead bug no tapete","Agachamento com halter","Elevação lateral com halteres","Cadeira adutora","Panturrilha no leg press"},{"Puxada alta na frente","Rosca martelo com halteres","Remada unilateral","Dead bug no tapete","Voador inverso","Panturrilha no leg press","Pulldown","Prancha","Rosca alternada","Abdução de quadril"},{"Flexora sentada","Panturrilha no leg press","Hip thrust na máquina","Prancha","Leg press 45°","Dead bug no tapete","Stiff com halteres","Elevação lateral com halteres","Cadeira abdutora","Panturrilha no leg press"}};
 public static final String[][] REPS={{"","","","","","","","","",""},{"","","","","","","","","",""},{"","","","","","","","","",""},{"","","","","","","","","",""}};
 public static final String[][] TIPS={{"","","","","","","","","",""},{"","","","","","","","","",""},{"","","","","","","","","",""},{"","","","","","","","","",""}};
 public static final int[][] SETS={{2,2,2,2,2,2,2,2,2,2},{2,2,2,2,2,2,2,2,2,2},{2,2,2,2,2,2,2,2,2,2},{2,2,2,2,2,2,2,2,2,2}};
 public static int blockStart(int workout,int block){return block*2;}
 public static int blockSize(int workout,int block){return 2;}
 public static String blockLabel(int block){return "DUPLA "+(block+1);}
 public static String exerciseLabel(int block,int offset){return (block+1)+(offset==0?"A":"B");}
 public static String imagePath(int workout,int exercise){return "exercises/"+Character.toLowerCase(LETTERS[workout].charAt(0))+"_"+exercise+".jpg";}
}