package com.luanarabelo.treinodaluana.v12;

public final class WorkoutData {
    private WorkoutData() {}

    public static final int BLOCKS_PER_WORKOUT = 6;
    public static final int TOTAL_BLOCKS = 24;
    public static final int TOTAL_EXERCISES = 45;

    public static final String[] LETTERS = {"A", "B", "C", "D"};
    public static final String[] TYPES = {
            "SUPERIOR PUSH",
            "INFERIOR QUADS",
            "SUPERIOR PULL",
            "INFERIOR POSTERIOR"
    };
    public static final String[] FOCUSES = {
            "Peito, ombro e tríceps",
            "Quadríceps e glúteos",
            "Costas, bíceps e posterior de ombro",
            "Posterior de coxa e glúteos"
    };

    // Cinco duplas conjugadas e um finalizador. O treino A tem dois abdominais.
    public static final int[] BLOCK_STARTS = {0, 2, 4, 6, 8, 10};
    public static final int[][] BLOCK_SIZES = {
            {2, 2, 2, 2, 2, 2},
            {2, 2, 2, 2, 2, 1},
            {2, 2, 2, 2, 2, 1},
            {2, 2, 2, 2, 2, 1}
    };

    public static final String[][] NAMES = {
            {
                    "Supino na máquina",
                    "Tríceps francês com halter",
                    "Crucifixo com halteres no banco",
                    "Elevação lateral com halteres",
                    "Desenvolvimento na máquina",
                    "Tríceps coice com halteres",
                    "Supino inclinado na máquina",
                    "Elevação frontal com halteres",
                    "Mergulho assistido",
                    "Tríceps testa com halteres",
                    "Dead bug no tapete",
                    "Abdominal infra no banco inclinado"
            },
            {
                    "Leg press 45°",
                    "Panturrilha no leg press",
                    "Cadeira extensora",
                    "Agachamento isométrico na parede",
                    "Cadeira adutora",
                    "Cadeira abdutora",
                    "Hack squat na máquina",
                    "Agachamento na Smith",
                    "Búlgaro na Smith assistido",
                    "Hip thrust na máquina",
                    "Prancha"
            },
            {
                    "Puxada alta na frente",
                    "Remada baixa sentada",
                    "Puxada assistida na máquina",
                    "Pullover na máquina",
                    "Remada unilateral na máquina",
                    "Rosca martelo com halteres",
                    "Remada alta na máquina com apoio no peito",
                    "Pulldown com braços estendidos",
                    "Voador inverso",
                    "Encolhimento com halteres",
                    "Elevação de joelhos"
            },
            {
                    "Flexora sentada",
                    "Flexora deitada",
                    "Hip thrust na máquina",
                    "Glúteo na máquina ajoelhada",
                    "Cadeira abdutora",
                    "Leg press horizontal com pés altos",
                    "Flexora em pé unilateral",
                    "Afundo lateral com halter",
                    "Frog pump no tapete",
                    "Ponte de posterior com bola",
                    "Abdominal infra reverso"
            }
    };

    public static final String[][] REPS = {
            {
                    "10 a 12", "10 a 12", "10 a 12", "12 a 15", "8 a 10", "10 a 12",
                    "8 a 10", "12 a 15", "8 a 10", "10 a 12", "10 por lado", "12 a 15"
            },
            {
                    "10 a 12", "12 a 15", "10 a 12", "30 a 40 segundos", "12 a 15", "15 a 20",
                    "8 a 12", "10 a 12", "8 a 10 por perna", "8 a 12", "30 a 45 segundos"
            },
            {
                    "8 a 10", "8 a 10", "6 a 10", "10 a 12", "8 a 12 por lado", "10 a 12",
                    "8 a 12", "10 a 12", "12 a 15", "10 a 12", "10 a 15"
            },
            {
                    "10 a 12", "10 a 12", "8 a 12", "12 por perna", "15 a 20", "10 a 12",
                    "10 a 12 por perna", "10 por lado", "15 a 20", "10 a 12", "12 a 15"
            }
    };

    // As quatro primeiras duplas têm três voltas; a quinta tem duas; o abdômen tem três.
    public static final int[][] SETS = {
            {3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 3, 3},
            {3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 3},
            {3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 3},
            {3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 3}
    };

    public static final String[][] TIPS = {
            {
                    "Pés firmes, escápulas apoiadas e punhos neutros.",
                    "Mantenha os cotovelos apontados para cima e o tronco firme.",
                    "Abra os braços com controle e mantenha as escápulas apoiadas no banco.",
                    "Suba até a linha dos ombros sem encolher o pescoço.",
                    "Abdômen firme e lombar apoiada durante todo o movimento.",
                    "Fixe os braços junto ao tronco e estenda somente os cotovelos.",
                    "Desça com controle e mantenha os ombros apoiados.",
                    "Use carga leve e evite balanço do tronco.",
                    "Desça até uma amplitude confortável e empurre pelas mãos.",
                    "Cotovelos estáveis; leve os halteres na direção da testa.",
                    "Mantenha a lombar apoiada e alterne braço e perna opostos.",
                    "Segure o banco e eleve o quadril sem embalo."
            },
            {
                    "Joelhos acompanham os pés e não travam no topo.",
                    "Mantenha os joelhos quase estendidos e mova apenas os tornozelos.",
                    "Estenda com controle e evite chutar o peso.",
                    "Encoste a lombar na parede e mantenha joelhos alinhados.",
                    "Controle a abertura e não bata as placas na volta.",
                    "Abra os joelhos com controle sem tirar o quadril do assento.",
                    "Mantenha os pés firmes e desça somente até preservar a lombar apoiada.",
                    "Use os trilhos como guia e mantenha joelhos acompanhando os pés.",
                    "Desça no eixo com o pé da frente totalmente apoiado.",
                    "Apoie as costas, eleve o quadril e faça uma pausa curta no alto.",
                    "Corpo alinhado, glúteos firmes e respiração contínua."
            },
            {
                    "Leve os cotovelos para baixo sem jogar o tronco para trás.",
                    "Puxe com os cotovelos e mantenha os ombros longe das orelhas.",
                    "Use a assistência necessária e conduza o peito em direção às mãos.",
                    "Mantenha os braços quase estendidos e leve os cotovelos para baixo.",
                    "Tronco estável; puxe o cotovelo em direção às costelas.",
                    "Punhos neutros e descida controlada.",
                    "Apoie o peito e conduza os cotovelos para cima e para trás.",
                    "Braços quase estendidos e costelas encaixadas.",
                    "Apoie o peito e abra os braços sem elevar os ombros.",
                    "Eleve os ombros sem girá-los e desça devagar.",
                    "Suba os joelhos sem balançar e controle a descida."
            },
            {
                    "Flexione os joelhos sem levantar o quadril do banco.",
                    "Flexione os joelhos sem levantar o quadril do banco.",
                    "Queixo levemente recolhido e pausa no alto.",
                    "Apoie o tronco e empurre a plataforma para trás com o glúteo.",
                    "Abra com controle e não bata as placas na volta.",
                    "Use os pés mais altos e mantenha a lombar totalmente apoiada.",
                    "Flexione uma perna por vez sem girar o quadril.",
                    "Leve o quadril para trás e mantenha o outro pé apoiado.",
                    "Una as solas dos pés e contraia os glúteos no alto.",
                    "Pressione a bola pelos calcanhares sem arquear a lombar.",
                    "Enrole o quadril sem impulso e controle a volta ao chão."
            }
    };

    public static int blockStart(int workout, int block) {
        return BLOCK_STARTS[block];
    }

    public static int blockSize(int workout, int block) {
        return BLOCK_SIZES[workout][block];
    }

    public static String blockLabel(int block) {
        return block == BLOCKS_PER_WORKOUT - 1 ? "FINALIZADOR" : "DUPLA " + (block + 1);
    }

    public static String exerciseLabel(int block, int offset) {
        if (block == BLOCKS_PER_WORKOUT - 1) return offset == 0 ? "ABS A" : "ABS B";
        return (block + 1) + (offset == 0 ? "A" : "B");
    }

    public static String imagePath(int workout, int exercise) {
        return "exercises/" + Character.toLowerCase(LETTERS[workout].charAt(0)) + "_" + exercise + ".jpg";
    }
}
