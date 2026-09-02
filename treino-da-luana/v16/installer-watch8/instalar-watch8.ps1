$ErrorActionPreference = "Stop"

$PackageName = "com.luanarabelo.treinodaluana.v12.xiaomitest"
$ActivityName = "com.luanarabelo.treinodaluana.v12.wear.WatchMainActivity"
$ApkName = "Treino-da-Luana-v16-Galaxy-Watch8-TESTE.apk"
$ExpectedSha256 = "SUBSTITUIR_APOS_ASSINAR_O_APK_V16"
$GoogleToolsUrl = "https://dl.google.com/android/repository/platform-tools-latest-windows.zip"

$BaseDir = $PSScriptRoot
$ApkPath = Join-Path $BaseDir $ApkName
$ToolsRoot = Join-Path $BaseDir "google-platform-tools"
$ToolsZip = Join-Path $BaseDir "platform-tools-latest-windows.zip"
$AdbPath = Join-Path $ToolsRoot "platform-tools\adb.exe"

function Show-Title {
    Clear-Host
    Write-Host "============================================================" -ForegroundColor DarkGray
    Write-Host "  TREINO DA LUANA V16 TESTE - INSTALADOR WATCH8" -ForegroundColor Cyan
    Write-Host "============================================================" -ForegroundColor DarkGray
    Write-Host ""
}

function Stop-WithMessage([string]$Message) {
    Write-Host ""
    Write-Host "NAO FOI POSSIVEL CONTINUAR" -ForegroundColor Red
    Write-Host $Message -ForegroundColor Yellow
    Write-Host "O instalador foi interrompido." -ForegroundColor Gray
    Write-Host "Se nao for tentar novamente agora, desligue a Depuracao sem fio e a Depuracao ADB no relogio." -ForegroundColor Gray
    exit 1
}

function Read-Endpoint([string]$Prompt) {
    while ($true) {
        $Value = (Read-Host $Prompt).Trim()
        if ($Value -match '^\d{1,3}(\.\d{1,3}){3}:\d{1,5}$') {
            return $Value
        }
        Write-Host "Use o formato mostrado no relogio, por exemplo 192.168.0.20:37123" -ForegroundColor Yellow
    }
}

function Read-PairEndpoint([string]$MainEndpoint) {
    $MainAddress = $MainEndpoint.Split(":")[0]

    while ($true) {
        $Value = (Read-Host "Cole o IP e a PORTA DE EMPARELHAMENTO exibidos abaixo do codigo").Trim()

        if ($Value -match '^\d{6}$') {
            Write-Host "Esses 6 numeros sao o CODIGO. Agora precisamos da linha IP:PORTA mostrada abaixo dele." -ForegroundColor Yellow
            continue
        }
        if ($Value -notmatch '^\d{1,3}(\.\d{1,3}){3}:\d{1,5}$') {
            Write-Host "Copie a linha completa no formato ${MainAddress}:PORTA. Nao use o endereco do exemplo." -ForegroundColor Yellow
            continue
        }

        $PairAddress = $Value.Split(":")[0]
        if ($PairAddress -ne $MainAddress) {
            Write-Host "O IP precisa continuar sendo $MainAddress. Copie exatamente o IP:PORTA mostrado agora no relogio." -ForegroundColor Yellow
            continue
        }
        if ($Value -eq $MainEndpoint) {
            Write-Host "Essa e a PORTA DE CONEXAO. Toque em Emparelhar novo dispositivo e use a outra porta mostrada abaixo do codigo." -ForegroundColor Yellow
            continue
        }

        return $Value
    }
}

function Invoke-AdbCommand([string[]]$CommandArgs) {
    $PreviousPreference = $ErrorActionPreference
    try {
        # O ADB usa a saida de erro tambem para mensagens normais, como iniciar
        # o servico local. Com ErrorActionPreference=Stop, o PowerShell 5 trata
        # essas mensagens normais como uma excecao. Aqui capturamos texto e
        # codigo de saida separadamente para avaliar o resultado corretamente.
        $ErrorActionPreference = "Continue"
        $RawOutput = & $AdbPath @CommandArgs 2>&1
        $ExitCode = $LASTEXITCODE
        $OutputText = ($RawOutput | ForEach-Object { $_.ToString() }) -join [Environment]::NewLine
        return [PSCustomObject]@{
            Output = $OutputText
            ExitCode = $ExitCode
        }
    }
    finally {
        $ErrorActionPreference = $PreviousPreference
    }
}

function Test-TcpEndpoint([string]$Endpoint) {
    $Parts = $Endpoint.Split(":")
    $HostAddress = $Parts[0]
    $Port = [int]$Parts[1]
    $Client = New-Object System.Net.Sockets.TcpClient
    $AsyncResult = $null

    try {
        $AsyncResult = $Client.BeginConnect($HostAddress, $Port, $null, $null)
        if (-not $AsyncResult.AsyncWaitHandle.WaitOne(5000, $false)) {
            return $false
        }
        $Client.EndConnect($AsyncResult)
        return $true
    }
    catch {
        return $false
    }
    finally {
        if ($null -ne $AsyncResult) {
            $AsyncResult.AsyncWaitHandle.Close()
        }
        $Client.Close()
    }
}

Show-Title

Write-Host "ETAPA 1 DE 5 - Validando o aplicativo" -ForegroundColor Cyan
if (-not (Test-Path $ApkPath)) {
    Stop-WithMessage "O arquivo $ApkName nao esta na mesma pasta do instalador."
}

$ActualSha256 = (Get-FileHash -Path $ApkPath -Algorithm SHA256).Hash.ToUpperInvariant()
if ($ActualSha256 -ne $ExpectedSha256) {
    Stop-WithMessage "A verificacao de integridade do APK falhou. Nao instale esse arquivo."
}
Write-Host "APK integro e validado." -ForegroundColor Green

Write-Host ""
Write-Host "ETAPA 2 DE 5 - Preparando a ferramenta oficial do Google" -ForegroundColor Cyan
if (-not (Test-Path $AdbPath)) {
    Write-Host "A copia incluida do Android SDK Platform-Tools nao foi encontrada." -ForegroundColor White
    Write-Host "O instalador pode baixar outra copia diretamente do Google." -ForegroundColor White
    Write-Host "Fonte oficial: https://developer.android.com/tools/releases/platform-tools" -ForegroundColor Gray
    $Consent = (Read-Host "Digite SIM para baixar e continuar").Trim().ToUpperInvariant()
    if ($Consent -ne "SIM") {
        Stop-WithMessage "Download cancelado por voce."
    }

    try {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri $GoogleToolsUrl -OutFile $ToolsZip -UseBasicParsing
        if (Test-Path $ToolsRoot) {
            Remove-Item -Path $ToolsRoot -Recurse -Force
        }
        New-Item -Path $ToolsRoot -ItemType Directory | Out-Null
        Expand-Archive -Path $ToolsZip -DestinationPath $ToolsRoot -Force
        Remove-Item -Path $ToolsZip -Force
    }
    catch {
        Stop-WithMessage "Falha ao baixar a ferramenta oficial do Google: $($_.Exception.Message)"
    }
}

if (-not (Test-Path $AdbPath)) {
    Stop-WithMessage "O ADB oficial nao foi encontrado depois da preparacao."
}

$VersionResult = Invoke-AdbCommand -CommandArgs @("version")
$VersionLines = $VersionResult.Output -split "`r?`n"
$VersionLines | Select-Object -First 3 | ForEach-Object { Write-Host $_ }
if ($VersionResult.ExitCode -ne 0) {
    Stop-WithMessage "O ADB nao conseguiu iniciar no Windows."
}

$ServerResult = Invoke-AdbCommand -CommandArgs @("start-server")
if ($ServerResult.ExitCode -ne 0) {
    Stop-WithMessage "O servico local do ADB nao conseguiu iniciar no Windows."
}

Write-Host ""
Write-Host "ETAPA 3 DE 5 - Preparando o Galaxy Watch8" -ForegroundColor Cyan
Write-Host "No relogio, confirme que ele e o computador estao no mesmo Wi-Fi." -ForegroundColor White
Write-Host "Se o Firewall do Windows perguntar, permita somente em redes privadas." -ForegroundColor Yellow
Write-Host "Se Opcoes do desenvolvedor nao aparecer, abra:" -ForegroundColor White
Write-Host "Configuracoes > Sobre o relogio > Informacoes do software (ou Versoes)" -ForegroundColor Yellow
Write-Host "Toque 7 vezes em Numero da versao / Build number e informe o PIN." -ForegroundColor Yellow
Write-Host "Depois abra:" -ForegroundColor White
Write-Host "Configuracoes > Opcoes do desenvolvedor" -ForegroundColor Yellow
Write-Host "Ative temporariamente:" -ForegroundColor White
Write-Host "  1. Depuracao ADB" -ForegroundColor White
Write-Host "  2. Desativar Wi-Fi automatico" -ForegroundColor White
Write-Host "  3. Depuracao sem fio" -ForegroundColor White
Write-Host ""
Read-Host "Quando essa tela estiver pronta, pressione ENTER"

Write-Host ""
Write-Host "ETAPA 4 DE 5 - Emparelhando e instalando" -ForegroundColor Cyan
$ConnectEndpoint = Read-Endpoint "Na tela Depuracao sem fio, digite primeiro o IP e a PORTA DE CONEXAO"
Write-Host "Testando se o Windows consegue alcancar o relogio nessa rede..." -ForegroundColor Gray
if (-not (Test-TcpEndpoint -Endpoint $ConnectEndpoint)) {
    Stop-WithMessage "O Windows nao alcancou a porta de conexao do relogio. Isso confirma bloqueio entre os aparelhos na rede ou no Firewall; o APK ainda nao foi acessado."
}
Write-Host "Porta de conexao alcancavel. A rede permite comunicacao com o relogio." -ForegroundColor Green

$PairSucceeded = $false

for ($Attempt = 1; $Attempt -le 3; $Attempt++) {
    Write-Host ""
    Write-Host "TENTATIVA $Attempt DE 3" -ForegroundColor Cyan
    Write-Host "No relogio, entre em Depuracao sem fio > Emparelhar novo dispositivo." -ForegroundColor White
    Write-Host "Mantenha essa tela aberta. A porta desta tela deve ser diferente da porta de conexao." -ForegroundColor Yellow

    $PairEndpoint = Read-PairEndpoint -MainEndpoint $ConnectEndpoint

    Write-Host "Emparelhando diretamente pelo ADB oficial..." -ForegroundColor Gray
    Write-Host "O proprio ADB perguntara Enter pairing code. So entao digite os 6 numeros e pressione ENTER." -ForegroundColor Yellow
    $PreviousPreference = $ErrorActionPreference
    try {
        $ErrorActionPreference = "Continue"
        & $AdbPath pair $PairEndpoint
        $PairExitCode = $LASTEXITCODE
    }
    finally {
        $ErrorActionPreference = $PreviousPreference
    }

    if ($PairExitCode -eq 0) {
        $PairSucceeded = $true
        break
    }

    if ($Attempt -lt 3) {
        Write-Host ""
        Write-Host "Essa tentativa foi recusada pelo relogio." -ForegroundColor Yellow
        Write-Host "O codigo ou a porta podem ter expirado; eles mudam quando a tela e reaberta." -ForegroundColor Yellow
        Write-Host "Saia de Emparelhar novo dispositivo e abra essa opcao novamente." -ForegroundColor White
        if ($Attempt -eq 2) {
            Write-Host "Se continuar, desligue e ligue Depuracao sem fio antes de gerar os novos dados." -ForegroundColor White
        }
        Write-Host "A proxima pergunta ja sera o novo IP:PORTA de emparelhamento; nao digite o codigo antes dela." -ForegroundColor Cyan
    }
}

if (-not $PairSucceeded) {
    Stop-WithMessage "A porta principal respondeu, mas o servidor de pareamento do relogio recusou tres codigos. A rede foi validada; a falha esta no pareamento ADB do Wear OS, nao no APK."
}

Write-Host ""
Write-Host "Voltando a usar a PORTA DE CONEXAO validada no inicio: $ConnectEndpoint" -ForegroundColor Yellow

$ConnectResult = Invoke-AdbCommand -CommandArgs @("connect", $ConnectEndpoint)
Write-Host $ConnectResult.Output.Trim()
if ($ConnectResult.ExitCode -ne 0 -or $ConnectResult.Output -notmatch 'connected to|already connected') {
    Stop-WithMessage "O computador nao conseguiu se conectar ao relogio. Confira a porta principal e o Wi-Fi."
}

Write-Host ""
Write-Host "Instalando o Treino da Luana no relogio..." -ForegroundColor Cyan
$InstallResult = Invoke-AdbCommand -CommandArgs @("-s", $ConnectEndpoint, "install", "-r", $ApkPath)
Write-Host $InstallResult.Output.Trim()

if ($InstallResult.Output -match 'INSTALL_FAILED_UPDATE_INCOMPATIBLE') {
    Invoke-AdbCommand -CommandArgs @("disconnect", $ConnectEndpoint) | Out-Null
    Stop-WithMessage "Ja existe uma versao assinada de forma diferente no relogio. Ela precisa ser removida manualmente antes de tentar novamente; isso apaga o progresso antigo."
}
if ($InstallResult.ExitCode -ne 0 -or $InstallResult.Output -notmatch 'Success') {
    Invoke-AdbCommand -CommandArgs @("disconnect", $ConnectEndpoint) | Out-Null
    Stop-WithMessage "O Galaxy Watch8 recusou a instalacao. A mensagem tecnica aparece acima."
}

$PackageResult = Invoke-AdbCommand -CommandArgs @("-s", $ConnectEndpoint, "shell", "pm", "path", $PackageName)
if ($PackageResult.ExitCode -ne 0 -or $PackageResult.Output -notmatch 'package:') {
    Invoke-AdbCommand -CommandArgs @("disconnect", $ConnectEndpoint) | Out-Null
    Stop-WithMessage "O APK informou sucesso, mas o pacote nao foi localizado no relogio."
}

Write-Host "Aplicativo confirmado no Galaxy Watch8." -ForegroundColor Green
Write-Host "Abrindo o Treino da Luana..." -ForegroundColor Cyan
Invoke-AdbCommand -CommandArgs @("-s", $ConnectEndpoint, "shell", "am", "start", "-n", "$PackageName/$ActivityName") | Out-Null

Invoke-AdbCommand -CommandArgs @("disconnect", $ConnectEndpoint) | Out-Null
Invoke-AdbCommand -CommandArgs @("kill-server") | Out-Null

Write-Host ""
Write-Host "ETAPA 5 DE 5 - Fechando o acesso de desenvolvedor" -ForegroundColor Cyan
Write-Host "No relogio, volte para Configuracoes > Opcoes do desenvolvedor e desative:" -ForegroundColor White
Write-Host "  1. Depuracao sem fio" -ForegroundColor Yellow
Write-Host "  2. Depuracao ADB" -ForegroundColor Yellow
Write-Host "  3. Desativar Wi-Fi automatico" -ForegroundColor Yellow
Write-Host "Se houver uma chave geral de Opcoes do desenvolvedor, desligue-a tambem." -ForegroundColor White
Write-Host ""

$Closed = (Read-Host "Depois de desligar essas opcoes, digite DESATIVEI").Trim().ToUpperInvariant()
if ($Closed -ne "DESATIVEI") {
    Write-Host "A instalacao terminou, mas lembre-se de desligar a depuracao no relogio." -ForegroundColor Yellow
} else {
    Write-Host "Depuracao encerrada. Instalacao concluida com seguranca." -ForegroundColor Green
}

Write-Host ""
Write-Host "Pronto. Procure Treino da Luana na lista de aplicativos do Watch8." -ForegroundColor Green
