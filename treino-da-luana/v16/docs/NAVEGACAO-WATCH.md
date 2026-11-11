# Navegação do relógio (V19) — voltar e botão de baixo

## Regra única do botão voltar

O botão físico/gesto **voltar** sempre leva à **tela anterior** e **nunca apaga marcações**: cada série e cada exercício é salvo no relógio no momento do toque. Em telas de confirmação, voltar equivale a **cancelar** e devolve à tela de origem. Na Home, voltar fecha o aplicativo (o progresso continua salvo).

As setas laterais `‹` `›`, a seta `‹` e o chip **DUPLAS** do cabeçalho foram removidos (a borda redonda cortava o chip, e voltar já leva à lista de duplas). Fica **um único botão de ação na parte de baixo**.

## Tabela por tela

| Tela | Voltar (botão/gesto) | Botão de baixo | Observação |
| --- | --- | --- | --- |
| Home (A, B, C, D) | Fecha o app | — | Toque no cartão abre o treino |
| Lista de duplas | Home | **FINALIZAR** | Com pendências abre a confirmação; sem pendências vai ao resumo |
| Exercício, séries abertas | Lista de duplas | **MARCAR SÉRIE n** | Marca a primeira série aberta; tocar nos números ainda marca/desmarca qualquer série |
| Exercício, todas as séries marcadas | Lista de duplas | **PRÓXIMO ›** | Conclui o exercício e abre o seguinte |
| Último exercício, todas marcadas | Lista de duplas | **FINALIZAR** | Conclui o exercício e segue para finalizar |
| Confirmação de término antecipado | **Cancela** e volta à tela de origem (lista ou exercício) | **SIM, FINALIZAR** | Informa quantos exercícios estão pendentes |
| Resumo do treino | Home | **SINCRONIZAR SAMSUNG HEALTH** (no corpo da tela) | Volta sozinho à Home em 10 s |

Para pular um exercício: voltar à lista de duplas e abrir outro. O exercício não concluído fica pendente e entra na confirmação ao finalizar.

Desmarcar uma série de um exercício já concluído reabre o exercício; as demais marcações continuam salvas.

## Como conferir

A regra está em `wear/src/main/java/.../wear/WatchNavigation.java` (Java puro) e é verificada por `docs/tools/WatchNavigationCheck.java` (21 verificações), executada também no GitHub Actions antes da compilação.

## Ainda a validar no relógio

* O **gesto de deslizar para voltar** do Galaxy Watch8 precisa acionar o mesmo `onBackPressed` do botão físico. Se, no teste, o gesto fechar o app em vez de voltar uma tela, o ajuste é no tema da Activity.
* Pausa, descanso ajustável e frequência cardíaca do desenho Figma **não** fazem parte desta versão.
