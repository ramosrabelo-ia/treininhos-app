# Treino da Luana

Aplicativo pessoal de musculação com módulos Android e Wear OS.

## Versão atual

**V19 — Organic Wellness**

- interface off-white, nude, taupe e cacau
- imagens de movimento monocromáticas
- quatro treinos A–D
- registro de séries e cargas
- meta semanal e ciclo de quatro semanas
- sincronização bidirecional entre celular e Galaxy Watch8
- atividade contínua no Wear OS durante o treino
- registro opcional de sessão pelo Health Connect

## Estrutura

```text
treino-da-luana/v16/
├── app/   Android
└── wear/  Wear OS
```

O diretório mantém o identificador técnico `v16` para evitar uma migração desnecessária do projeto Gradle. A versão distribuída é definida por `versionCode` e `versionName`.

## Dados

O aplicativo não possui backend, conta, publicidade ou rastreamento. Cargas e progresso ficam nos dispositivos e são sincronizados pelo Wear OS Data Layer. O Health Connect é opcional e usado somente para escrever a sessão concluída.

## Build

Requisitos: Java 17, Android SDK 36, Build Tools 35 e Gradle 8.11.1.

```bash
cd treino-da-luana/v16
gradle :app:assembleDebug :wear:assembleDebug --no-daemon
```

Saídas:

```text
app/build/outputs/apk/debug/app-debug.apk
wear/build/outputs/apk/debug/wear-debug.apk
```

O workflow atual executa o mesmo build no GitHub Actions. Chaves e senhas de assinatura não pertencem ao repositório.

## Documentação

- [Jornada técnica](CHANGELOG.md)
- [Detalhes da arquitetura](treino-da-luana/v16/README.md)
