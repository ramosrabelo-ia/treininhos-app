# Treino da Luana V16 TESTE — instalador para Galaxy Watch8 no Windows

Este pacote contém tudo o que foi usado no fluxo que funcionou no Galaxy Watch8:

- `Treino-da-Luana-v16-Galaxy-Watch8-TESTE.apk` — aplicativo V16 de teste do relógio;
- `INSTALAR-NO-WATCH8.bat` — arquivo para abrir o instalador com dois cliques;
- `instalar-watch8.ps1` — assistente de emparelhamento e instalação;
- se a pasta `google-platform-tools/platform-tools/` da instalação anterior não estiver presente, o assistente oferece baixar o ADB oficial do Google.

O pacote não altera o sistema do relógio, não desbloqueia o bootloader e não pede senha da Samsung ou do Google. Ele usa apenas a depuração sem fio oficial do Wear OS para instalar o APK.

## Antes de começar

1. Extraia o ZIP inteiro para uma pasta do Windows. Não execute o `.bat` de dentro do ZIP.
2. Deixe o computador e o Galaxy Watch8 na mesma rede Wi-Fi privada.
3. Mantenha o relógio com pelo menos 30% de bateria e a tela acesa durante o pareamento.
4. Se o Firewall do Windows perguntar, permita o ADB apenas em redes privadas.
5. Se **Opções do desenvolvedor** ainda não aparecer, abra `Configurações > Sobre o relógio > Informações do software` ou `Versões`, encontre **Número da versão / Build number** e toque sete vezes.
6. Volte para `Configurações > Opções do desenvolvedor` e ative temporariamente:
   - `Depuração ADB`;
   - `Desativar Wi-Fi automático`;
   - `Depuração sem fio`.
7. Confirme `Permitir nesta rede` quando o relógio perguntar.

## Instalação

1. Abra `INSTALAR-NO-WATCH8.bat`.
2. Na tela principal de `Depuração sem fio`, informe o **IP e a porta de conexão**.
3. Entre em `Emparelhar novo dispositivo` e informe o **IP e a porta de emparelhamento** mostrados abaixo do código.
4. Quando o ADB escrever `Enter pairing code`, digite o código de seis números.
5. Espere a confirmação da instalação e do lançamento do aplicativo.

As portas de conexão e de emparelhamento são diferentes. O código e a porta de emparelhamento expiram; se uma tentativa falhar, feche e reabra `Emparelhar novo dispositivo` para gerar dados novos.

## Depois de instalar

Desative novamente:

- `Depuração sem fio`;
- `Depuração ADB`;
- `Desativar Wi-Fi automático`.

Se houver uma chave geral das opções do desenvolvedor, ela também pode ser desligada.

## Integridade e origem

SHA-256 do APK final do Watch8:

```text
PENDENTE — preencher depois de assinar o APK V16 com a mesma chave da V15.
```

O instalador permanece bloqueado enquanto esse hash não for preenchido, evitando instalar por engano um APK ainda não assinado como atualização.

O Platform-Tools incluído foi obtido no endereço oficial do Google:

- <https://developer.android.com/tools/releases/platform-tools>
- <https://dl.google.com/android/repository/platform-tools-latest-windows.zip>

Se a pasta do ADB for removida, o instalador oferece baixar uma nova cópia do mesmo endereço oficial.
