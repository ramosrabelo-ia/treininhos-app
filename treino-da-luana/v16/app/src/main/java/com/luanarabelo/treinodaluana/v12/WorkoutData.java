package com.luanarabelo.treinodaluana.v12;

public final class WorkoutData {
    private WorkoutData() {}

    public static final int BLOCKS_PER_WORKOUT = 6;
    public static final int TOTAL_BLOCKS = 24;
    public static final int TOTAL_EXERCISES = 44;

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

    // Cinco duplas conjugadas e um finalizador abdominal.
    public static final int[] BLOCK_STARTS = {0, 2, 4, 6, 8, 10};
    public static final int[] BLOCK_SIZES = {2, 2, 2, 2, 2, 1};

    public static final String[][] NAMES = {
            {
                    "Supino na máquina",
                    "Tríceps francês com halter",
                    "Voador peck deck",
                    "Elevação lateral com halteres",
                    "Desenvolvimento na máquina",
                    "Tríceps coice com halteres",
                    "Supino inclinado na máquina",
                    "Elevação frontal com halteres",
                    "Mergulho assistido",
                    "Tríceps testa com halteres",
                    "Abdominal na máquina"
            },
            {
                    "Leg press 45°",
                    "Agachamento goblet com halter",
                    "Cadeira extensora",
                    "Afundo reverso com halteres",
                    "Cadeira adutora",
                    "Agachamento sumô com halter",
                    "Búlgaro na Smith assistido",
                    "Stiff com halteres",
                    "Panturrilha no leg press",
                    "Agachamento isométrico com anilha",
                    "Prancha"
            },
            {
                    "Puxada alta na frente",
                    "Remada baixa sentada",
                    "Remada articulada",
                    "Rosca bíceps na máquina",
                    "Remada unilateral na máquina",
                    "Rosca martelo com halteres",
                    "Face pull na polia com corda",
                    "Pulldown com braços estendidos",
                    "Voador inverso",
                    "Rosca alternada com halteres",
                    "Elevação de joelhos"
            },
            {
                    "Flexora sentada",
                    "Stiff com halteres",
                    "Hip thrust na máquina",
                    "Stiff unilateral com halter",
                    "Glúteo kickback na máquina",
                    "Afundo reverso com halteres",
                    "Cadeira abdutora",
                    "Afundo lateral com halter",
                    "Panturrilha em pé na máquina",
                    "Terra sumô com halter",
                    "Abdominal infra reverso"
            }
    };

    public static final String[][] REPS = {
            {
                    "10 a 12", "10 a 12", "10 a 12", "12 a 15", "8 a 10", "10 a 12",
                    "8 a 10", "12 a 15", "8 a 10", "10 a 12", "12 a 15"
            },
            {
                    "10 a 12", "10 a 12", "10 a 12", "10 por perna", "12 a 15", "10 a 12",
                    "8 a 10 por perna", "8 a 10", "12 a 15", "30 a 40 segundos", "30 a 45 segundos"
            },
            {
                    "8 a 10", "8 a 10", "8 a 10", "10 a 12", "8 a 12 por lado", "10 a 12",
                    "12 a 15", "10 a 12", "12 a 15", "10 a 12", "10 a 15"
            },
            {
                    "10 a 12", "8 a 10", "8 a 12", "8 a 10 por lado", "12 por perna", "10 por perna",
                    "15 a 20", "10 por lado", "12 a 15", "10 a 12", "12 a 15"
            }
    };

    // As quatro primeiras duplas têm três voltas; a quinta tem duas; o abdômen tem três.
    public static final int[][] SETS = {
            {3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 3},
            {3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 3},
            {3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 3},
            {3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 3}
    };

    public static final String[][] TIPS = {
            {
                    "Pés firmes, escápulas apoiadas e punhos neutros.",
                    "Mantenha os cotovelos apontados para cima e o tronco firme.",
                    "Apoie as costas e feche os braços sem bater as placas.",
                    "Suba até a linha dos ombros sem encolher o pescoço.",
                    "Abdômen firme e lombar apoiada durante todo o movimento.",
                    "Fixe os braços junto ao tronco e estenda somente os cotovelos.",
                    "Desça com controle e mantenha os ombros apoiados.",
                    "Use carga leve e evite balanço do tronco.",
                    "Desça até uma amplitude confortável e empurre pelas mãos.",
                    "Cotovelos estáveis; leve os halteres na direção da testa.",
                    "Expire ao fechar o tronco e não puxe o pescoço."
            },
            {
                    "Joelhos acompanham os pés e não travam no topo.",
                    "Peito aberto, abdômen firme e joelhos alinhados.",
                    "Estenda com controle e evite chutar o peso.",
                    "Dê o passo para trás e empurre o chão para subir.",
                    "Controle a abertura e não bata as placas na volta.",
                    "Pés abertos, joelhos alinhados e halter próximo ao corpo.",
                    "Desça no eixo com o pé da frente totalmente apoiado.",
                    "Leve o quadril para trás mantendo a coluna neutra.",
                    "Faça uma pausa curta no alto e desça devagar.",
                    "Mantenha as costas apoiadas e segure a anilha junto ao peito.",
                    "Corpo alinhado, glúteos firmes e respiração contínua."
            },
            {
                    "Leve os cotovelos para baixo sem jogar o tronco para trás.",
                    "Puxe com os cotovelos e mantenha os ombros longe das orelhas.",
                    "Apoie o peito e conduza os cotovelos para trás.",
                    "Mantenha os braços apoiados e evite tirar os ombros do banco.",
                    "Tronco estável; puxe o cotovelo em direção às costelas.",
                    "Punhos neutros e descida controlada.",
                    "Abra a corda na direção do rosto e una as escápulas.",
                    "Braços quase estendidos e costelas encaixadas.",
                    "Apoie o peito e abra os braços sem elevar os ombros.",
                    "Cotovelos junto ao corpo e sem embalo.",
                    "Suba os joelhos sem balançar e controle a descida."
            },
            {
                    "Flexione os joelhos sem levantar o quadril do banco.",
                    "Empurre o quadril para trás e mantenha a coluna neutra.",
                    "Queixo levemente recolhido e pausa no alto.",
                    "Quadris alinhados e peso próximo à perna de apoio.",
                    "Evite girar o quadril e aperte o glúteo no final.",
                    "Dê o passo para trás e suba pelo calcanhar da frente.",
                    "Abra com controle e não bata as placas na volta.",
                    "Leve o quadril para trás e mantenha o outro pé apoiado.",
                    "Suba pelo dedão, faça uma pausa no topo e desça devagar.",
                    "Pés abertos, joelhos alinhados e halter perto do corpo.",
                    "Enrole o quadril sem impulso e controle a volta ao chão."
            }
    };

    public static int blockStart(int block) {
        return BLOCK_STARTS[block];
    }

    public static int blockSize(int block) {
        return BLOCK_SIZES[block];
    }

    public static String blockLabel(int block) {
        return block == BLOCKS_PER_WORKOUT - 1 ? "FINALIZADOR" : "DUPLA " + (block + 1);
    }

    public static String exerciseLabel(int block, int offset) {
        if (block == BLOCKS_PER_WORKOUT - 1) return "ABS";
        return (block + 1) + (offset == 0 ? "A" : "B");
    }

    public static String imagePath(int workout, int exercise) {
        return "exercises/" + Character.toLowerCase(LETTERS[workout].charAt(0)) + "_" + exercise + ".jpg";
    }
}
