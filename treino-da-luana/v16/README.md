# Treino da Luana V19 — prévia de teste

Esta versão reorganiza a tela por exercício: uma imagem grande, nome, repetições,
carga e um único botão de conclusão. Cada integrante da dupla conserva seu próprio
estado. Os pendentes são listados antes dos concluídos, e avançar de dupla não
marca os exercícios automaticamente.

A carga continua salva por exercício e agora também pode ser alterada no relógio,
com sincronização bidirecional pelo Wear OS Data Layer.

As imagens novas de A1 e A2 foram incorporadas nos dois módulos. As imagens dos
demais exercícios ainda são as referências anteriores; o acervo visual da V19
segue em revisão. Os APKs gerados por CI são builds de validação, assinados com
chave de depuração e podem exigir desinstalação da versão assinada anteriormente.

Código dos aplicativos Android e Wear OS usados no pacote de teste V18.

## Módulos

| Pasta | Conteúdo |
| --- | --- |
| `app` | Aplicativo Android do Xiaomi, cargas, progresso e Health Connect |
| `wear` | Aplicativo do Galaxy Watch8, duplas, séries e resumo final |
| `installer-watch8` | Instalador guiado para Windows |
| `docs/mockups` | Interfaces aprovadas em PNG e SVG |

## Comportamento do relógio

* lista rolável de duplas com duas fotografias por cartão
* entrada no primeiro exercício incompleto ao reabrir uma dupla
* fotografia original como elemento principal da tela de exercício
* séries tocáveis e reversíveis
* progresso parcial preservado
* botões circulares `‹` e `›` sempre visíveis na borda inferior segura
* nome, repetições e carga em formato compacto para não disputar espaço com a foto
* carga somente para consulta
* finalização permitida com exercícios pendentes
* resumo com tempo e exercícios concluídos
* retorno automático ao início em 10 segundos
* atividade contínua do Wear OS durante o treino, com atalho para voltar ao exercício atual

## Treinos reorganizados

* `A` — superior push com crucifixo no banco e finalizador abdominal duplo
* `B` — inferior com mais aparelhos e leg press/panturrilha na mesma estação
* `C` — superior pull simplificado, com apenas uma rosca direta
* `D` — posterior com máquinas de flexora, glúteo e hip thrust, sem stiff ou terra

A V18 preserva a meta de quatro semanas, o histórico semanal, as cargas e a sincronização entre celular e relógio.
O Samsung Health continua recebendo o resumo pelo Health Connect; durante a sessão, o relógio mantém o Treino da Luana acessível como atividade contínua independente.

## Instalar no Galaxy Watch8 pelo computador

### 1. Mostrar as opções do desenvolvedor

1. No relógio, abra **Configurações**.
2. Entre em **Sobre o relógio** e depois em **Informações do software** ou **Versões**.
3. Localize **Número da versão / Build number** e toque sete vezes.
4. Digite o PIN do relógio, se solicitado. A mensagem de modo desenvolvedor será exibida.
5. Volte à tela principal de **Configurações**. O item **Opções do desenvolvedor** aparecerá próximo ao final da lista.

### 2. Ativar a conexão com o computador

1. Conecte o relógio e o computador à mesma rede Wi-Fi privada.
2. Abra **Configurações > Opções do desenvolvedor**.
3. Ative **Depuração ADB**.
4. Ative **Depuração sem fio** e confirme **Permitir nesta rede**.
5. Abra **Depuração sem fio**. Anote o IP e a **porta de conexão** mostrados na tela principal.
6. Toque em **Emparelhar novo dispositivo**. Anote o código de seis números e o IP com a **porta de emparelhamento**.

> A porta de emparelhamento e a porta de conexão são diferentes. O código expira; mantenha a tela de emparelhamento aberta durante o procedimento.

### 3. Instalar pelo Windows

1. Baixe e extraia completamente o ZIP do instalador da V18.
2. Abra `INSTALAR-NO-WATCH8.bat`.
3. Informe primeiro o IP e a porta de conexão.
4. Quando solicitado, informe o IP e a porta de emparelhamento.
5. Digite o código de seis números exibido no relógio.
6. Aguarde a mensagem de instalação concluída.
7. Depois do teste, desative **Depuração sem fio** e **Depuração ADB**.

O computador e o relógio precisam estar na mesma rede e ela deve permitir comunicação entre dispositivos. Redes corporativas e de convidados podem bloquear essa conexão; nesse caso, use uma rede doméstica ou um hotspot particular.

## Build

```bash
gradle :app:assembleDebug :wear:assembleDebug --no-daemon
```

Para assinar como atualização da V14.1, use a mesma chave privada e configure localmente um `signing.properties`. Nunca envie a chave ou as senhas ao Git.
