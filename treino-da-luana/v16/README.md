# Arquitetura da versão atual

## Módulos

- `app`: Android, minSdk 26
- `wear`: Wear OS, minSdk 30

Os módulos usam o mesmo identificador de aplicação de teste e compartilham o domínio de treino em `WorkoutData`.

## Estado local

`SharedPreferences` mantém séries concluídas, cargas, conclusão de exercícios e blocos, duração da sessão e check-in semanal.

## Sincronização

`PhoneProgressSync` e `WatchProgressSync` trocam progresso pelo Wear OS Data Layer. A sincronização não depende de servidor próprio.

`WatchOngoingActivity` mantém um indicador do treino em andamento e um atalho de retorno no relógio.

`HealthConnectBridge` registra opcionalmente a sessão concluída. O aplicativo não lê métricas de saúde.

## Interface V19

O sistema visual utiliza superfícies claras, contraste cacau e imagens monocromáticas. O relógio não usa fundo preto em nenhuma tela estrutural. Os controles preservam áreas de toque compatíveis com a tela circular.

## Versionamento

- `versionCode 21`
- `versionName 19.0-organic-wellness`

O nome interno da pasta permanece `v16` por estabilidade do projeto.

## Segurança

- nenhuma chave de assinatura é versionada
- `signing.properties` permanece ignorado
- não existem tokens, credenciais ou endpoints privados no código
- imagens e referências antigas não utilizadas não são empacotadas
