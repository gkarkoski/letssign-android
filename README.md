# LetsSign — Plataforma de Assinatura Eletrônica e Gestão de Documentos

Aplicativo Android nativo desenvolvido em **Kotlin** com **Jetpack Compose** para gestão de documentos PDF e coleta de assinaturas eletrônicas com validade jurídica (registro de timestamp UTC e endereço IP).

O sistema opera em dois fluxos principais:
1. **Fluxo do Administrador (`ADMIN`):**
   * **Dashboard de Acompanhamento:** Abas *Enviados* (inclui seção *Em preparação* para rascunhos), *Parcialmente Assinados* e *Concluídos*, com cards de progresso de assinaturas.
   * **Gestão de Signatários:** Listagem, busca por nome/e-mail, cadastro, edição e exclusão com validações de formato e unicidade de e-mail.
   * **Wizard de Preparação de Documento (5 etapas):**
     1. *Título e Arquivo:* Seleção de PDF do dispositivo (`ACTION_OPEN_DOCUMENT`), validação de integridade com `PdfRenderer` e cópia segura para armazenamento interno (`context.filesDir`).
     2. *Quem vai assinar:* Seleção múltipla de signatários e classificação de papel (*Contratante*, *Contratado* ou *Testemunha*).
     3. *Posicionamento Drag & Drop:* Visualização das páginas do PDF em `Bitmap` e posicionamento interativo das caixas de assinatura (1 cor por signatário) usando coordenadas relativas (`0.0` a `1.0`) com bloqueio de drop fora dos limites da página.
     4. *Revisão e Envio:* Conferência dos campos por signatário e disparo do documento (`DRAFT` -> `PENDING`).
   * **Detalhes e Auditoria:** Visualização de quem já assinou, quem falta assinar e linha do tempo de eventos com data/hora e IP.

2. **Fluxo do Signatário (`SIGNER`):**
   * **Dashboard do Signatário:** Abas *Pendentes* e *Assinados* exibindo apenas documentos enviados aos quais o signatário foi vinculado.
   * **Assinatura Guiada e Gravação com PDFBox:** Ao abrir o documento, o aplicativo navega e rola automaticamente até a página e posição demarcada para o usuário logado. O signatário desenha sua assinatura em um Canvas interativo e o motor **PDFBox** grava a imagem diretamente dentro do arquivo PDF físico nas coordenadas absolutas do `cropBox`, atualizando o status para `SIGNED` (com IP e timestamp UTC) e concluindo automaticamente o documento (`COMPLETED`) quando todos assinam.

---

## Stack Tecnológico

* **Linguagem:** Kotlin (`2.2.10`)
* **Interface Visual:** Jetpack Compose (Material 3) em arquitetura *Single Activity* + Design System fiel ao Figma (fontes oficiais **Plus Jakarta Sans** e **Manrope**)
* **Navegação:** Jetpack Navigation Compose
* **Gerenciamento de Estado:** MVVM com `ViewModel` + `StateFlow`
* **Injeção de Dependência:** Dagger Hilt + KSP
* **Persistência Local (Sprint 1):** Room Database + Armazenamento Interno (`context.filesDir`) para arquivos PDF
* **Motor de PDF:**
  * `android.graphics.pdf.PdfRenderer` nativo para renderização de páginas em `Bitmap` na UI
  * `com.tom-roush:pdfbox-android` (inicializado via `PDFBoxResourceLoader.init` na `LetsSignApplication`) para manipulação estrutural, inserção das assinaturas (`PDPageContentStream`) e salvamento do PDF
* **Concorrência & Assincronismo:** Kotlin Coroutines & Flows (todas as operações de I/O de Room e PDF isoladas em `Dispatchers.IO`)

---

## Arquitetura do Projeto

O projeto segue os princípios de **Clean Architecture** divididos em camadas claras, garantindo que a camada de Apresentação (`ui`) e a camada de Domínio (`domain`) **não conheçam detalhes de implementação** de banco de dados local (Room) ou sistema de arquivos, preparando o aplicativo para acoplamento transparente com uma API REST na Sprint 2:

```text
com.example.challenge_letssign
├── data/
│   ├── di/                # Módulos Hilt (DatabaseModule, RepositoryModule, DispatcherModule)
│   ├── local/             # Room Database, Entities, DAOs, TypeConverters e Seed inicial
│   └── repository/
│       └── local/         # Implementações locais (LocalUserRepository, LocalDocumentRepository, LocalPdfStorageRepository)
├── domain/
│   ├── model/             # Modelos de domínio puros (User, Document, DocumentSigner, SignatureField, ImportedPdfFile)
│   ├── repository/        # Contratos/Interfaces puras (UserRepository, DocumentRepository, PdfStorageRepository)
│   └── usecase/           # Regras de negócio (Login, Signatários, Importação, Posicionamento, Envio e Assinatura)
├── ui/
│   ├── auth/              # Tela e ViewModel de Login
│   ├── admin/             # Dashboard Admin, Gestão de Signatários, Wizard PDF, Drag & Drop, Review e Detalhes
│   ├── signer/            # Dashboard Signatário, Visualizador Focado e Modal Canvas de Assinatura
│   ├── components/        # Componentes base do Design System (Botões pílula, Inputs, Stepper, Chips, Avatares, Ícones)
│   ├── navigation/        # Grafo de rotas (LetsSignNavHost)
│   └── theme/             # Tokens de Cores, Gradientes e Tipografia (Plus Jakarta Sans & Manrope)
└── utils/                 # AppResult, PdfCoordinateConverter, PdfPageRenderer, DateTimeFormatters e IpAddressResolver
```

### Regras de Negócio Implementadas e Testadas

* **Regra 1 (Validação de Envio):** Um documento `DRAFT` só pode virar `PENDING` se possuir ao menos 1 signatário associado e **todos** os signatários associados tiverem pelo menos 1 `SignatureField` posicionado no PDF.
* **Regra 2 (Limites do PDF):** `relativeX + relativeWidth <= 1.0` e `relativeY + relativeHeight <= 1.0`. O drop fora dos limites da página é bloqueado visualmente (*"Fora do limite"*) e no domínio (*"Campo fora dos limites da página"*).
* **Regra 3 (Reatividade de Exclusão):** Se um signatário for removido na fase `DRAFT`, todos os `SignatureFieldEntity` associados a ele são excluídos em cascata.
* **Regra 4 (Imutabilidade e Visibilidade):** Documentos `PENDING` ou `COMPLETED` bloqueiam qualquer edição/exclusão de campos ou documento. Administradores veem apenas seus próprios documentos; signatários veem apenas documentos enviados onde foram incluídos.
* **Regra 5 (Conclusão Automática):** Quando um signatário assina (`SIGNED`), o sistema verifica imediatamente se ainda há algum signatário `PENDING` no documento. Caso não exista, o documento é atualizado automaticamente para `COMPLETED`.

---

## Credenciais de Teste (Seed Automático)

Na primeira execução do aplicativo, o banco de dados local é populado automaticamente (`DatabaseSeeder`) com **1 Administrador** e **2 Signatários** para facilitar os testes dos fluxos:

| Perfil | Nome | E-mail | Senha | Papel (`UserRole`) |
| :--- | :--- | :--- | :--- | :--- |
| **Administrador** | Administrador LetsSign | `admin@letssign.com` | `123456` | `ADMIN` |
| **Signatário 1** | Carlos Silva | `carlos@letssign.com` | `123456` | `SIGNER` |
| **Signatário 2** | Mariana Souza | `mariana@letssign.com` | `123456` | `SIGNER` |

---

## Como Executar o Projeto

### Pré-requisitos
* **Android Studio** (versão recente compatível com AGP `9.4.1`)
* **JDK 17+** (ou o JBR embutido no próprio Android Studio)
* **Android SDK:** `compileSdk 37` / `minSdk 24` (Android 7.0+)

### 1. Executando pelo Android Studio
1. Clone o repositório e abra a pasta raiz `challenge_letsSign` no **Android Studio**.
2. Aguarde a sincronização do Gradle (*Gradle Sync*).
3. Selecione um emulador ou dispositivo físico (API 24+) e clique em **Run ('app')** (`Shift + F10` / `Ctrl + R`).

### 2. Executando via Linha de Comando (Terminal)

Para compilar o APK de Debug:
```bash
./gradlew assembleDebug
```

Para rodar a suíte completa de testes unitários (37 testes cobrindo todas as regras e milestones):
```bash
./gradlew testDebugUnitTest
```

> **Dica (macOS):** Caso o terminal não tenha a variável `JAVA_HOME` apontando para um JDK instalado globalmente, você pode utilizar o JBR do Android Studio:
> ```bash
> JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleDebug testDebugUnitTest
> ```
