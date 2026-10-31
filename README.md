# LetsSign — Plataforma Mobile de Assinatura Eletrônica e Gestão de Documentos

> **FIAP Challenge — 2º Ano de Análise e Desenvolvimento de Sistemas (Sprint 1)**  
> Aplicativo Android nativo desenvolvido em **Kotlin** com **Jetpack Compose** para preparação de documentos PDF, posicionamento responsivo de campos de assinatura, envio de solicitações, coleta de assinatura manuscrita em Canvas e gravação definitiva no PDF físico com validade jurídica (registro de timestamp UTC e endereço IP).

---

## Sumário

1. [Visão Geral da Solução](#1-visão-geral-da-solução)
2. [Credenciais de Teste (Seed Automático)](#2-credenciais-de-teste-seed-automático)
3. [Como Executar o Projeto](#3-como-executar-o-projeto)
4. [Roteiro Passo a Passo para Testar o App (Fim a Fim)](#4-roteiro-passo-a-passo-para-testar-o-app-fim-a-fim)
5. [Arquitetura e Preparação para a Sprint 2 (API REST)](#5-arquitetura-e-preparação-para-a-sprint-2-api-rest)
6. [Regras de Negócio e Requisitos Técnicos Atendidos](#6-regras-de-negócio-e-requisitos-técnicos-atendidos)
7. [Stack Tecnológico](#7-stack-tecnológico)

---

## 1. Visão Geral da Solução

O aplicativo **LetsSign** resolve o desafio de transformar a preparação, o envio e a coleta de assinaturas eletrônicas em documentos PDF em uma experiência móvel simples, segura e intuitiva, operando em dois perfis distintos:

### Perfil Administrador (`ADMIN`)
* **Dashboard de Acompanhamento:** Visualização organizada em abas (**Enviados** — incluindo seção *Em preparação* para rascunhos —, **Parcialmente Assinados** e **Concluídos**), com barra de progresso e contagem de assinaturas.
* **Gestão de Signatários:** Cadastro, edição, exclusão e busca por nome ou e-mail de signatários (*nome completo*, *e-mail*, *telefone* e *senha de autenticação*), com validação de formato e bloqueio de e-mail duplicado.
* **Wizard de Criação e Envio de Documento (5 etapas):**
  1. **Título e Arquivo PDF:** Importação de PDF do dispositivo (`ACTION_OPEN_DOCUMENT`), validação real de integridade via `PdfRenderer` (exibindo alerta visual para arquivos inválidos ou corrompidos) e cópia para o armazenamento interno seguro (`context.filesDir`).
  2. **Seleção e Classificação de Signatários:** Seleção múltipla de signatários e classificação individual como **Contratante**, **Contratado** ou **Testemunha** (permitindo remover ou substituir signatários antes do envio).
  3. **Posicionamento Visual de Assinaturas (Drag & Drop):** Visualização de todas as páginas do PDF e posicionamento livre de múltiplos campos de assinatura por signatário (uma cor por participante). Salva coordenadas relativas (`0.0` a `1.0`) para preservar a posição exata em qualquer tamanho de tela e bloqueia o *drop* fora dos limites da página.
  4. **Revisão e Disparo:** Conferência do documento final, lista de signatários, páginas/campos atribuídos e **mensagem personalizada que acompanhará o convite** antes de disparar o envio.
* **Detalhes e Trilha de Auditoria:** Consulta da situação geral do documento, status individual de cada signatário, linha do tempo cronológica (*Criação*, *Envio com mensagem do convite*, *Assinaturas individuais com IP e horário*, *Conclusão*) e visualização do **Documento Final Assinado (PDF)**.

### Perfil Signatário (`SIGNER`)
* **Dashboard do Signatário:** Abas **Pendentes** e **Assinados**, exibindo exclusivamente os documentos enviados nos quais o usuário logado foi incluído.
* **Foco Automático e Assinatura em Canvas:** Ao abrir um documento pendente, o app navega até a página correta e rola automaticamente até a posição da caixa atribuída ao signatário. Ao tocar para assinar, abre um **Canvas de desenho manuscrito**, grava a assinatura diretamente dentro do PDF real usando **PDFBox** (`PDPageContentStream`), registra data/hora e IP local e atualiza automaticamente o documento para **Concluído** assim que todos os participantes obrigatórios assinam.

---

## 2. Credenciais de Teste (Seed Automático)

Ao abrir o aplicativo pela primeira vez, o banco de dados local (**Room**) é populado automaticamente (`DatabaseSeeder`) com **1 Administrador** e **2 Signatários** prontos para uso:

| Perfil | Nome | E-mail (Login) | Senha | Papel no Sistema |
| :--- | :--- | :--- | :--- | :--- |
| **Administrador** | Administrador LetsSign | `admin@letssign.com` | `123456` | `ADMIN` |
| **Signatário 1** | Carlos Silva | `carlos@letssign.com` | `123456` | `SIGNER` |
| **Signatário 2** | Mariana Souza | `mariana@letssign.com` | `123456` | `SIGNER` |

> **Nota:** Qualquer novo signatário cadastrado pelo Admin na aba *Assinaturas* também pode fazer login imediatamente no app utilizando o e-mail e a senha definidos no momento do cadastro!

---

## 3. Como Executar o Projeto

### Pré-requisitos
* **Android Studio** (compatível com Android Gradle Plugin `9.4.1`)
* **JDK 17+** (ou o JBR embutido no próprio Android Studio)
* **Dispositivo ou Emulador Android:** `minSdk 24` (Android 7.0+) / `compileSdk 37`

### Opção A: Executando pelo Android Studio (Recomendado)
1. Descompacte o projeto (ou clone o repositório) e abra a pasta raiz `challenge_letsSign` no **Android Studio**.
2. Aguarde a finalização do *Gradle Sync*.
3. Inicie um Emulador Android (API 24+) e clique no botão **Run ('app')** (`Shift + F10` no Windows/Linux ou `Ctrl + R` no macOS).

### Opção B: Compilando e Testando via Terminal
Para gerar o APK de Debug (`app/build/outputs/apk/debug/app-debug.apk`) e rodar os **37 testes unitários automatizados**:

```bash
./gradlew assembleDebug testDebugUnitTest
```

> **Dica para macOS:** Caso seu terminal não tenha a variável `JAVA_HOME` configurada globalmente, utilize o JDK embutido no Android Studio:
> ```bash
> JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleDebug testDebugUnitTest
> ```

---

## 4. Roteiro Passo a Passo para Testar o App (Fim a Fim)

Siga este roteiro rápido de 3 minutos para validar todos os requisitos obrigatórios da Sprint 1:

### Passo 0 — Colocando um arquivo PDF no Emulador
* Antes de iniciar o teste, arraste e solte qualquer arquivo `.pdf` do seu computador para cima da janela do **Emulador Android** (ele será salvo automaticamente na pasta **Downloads** do emulador).

### Passo 1 — Login como Admin e Gestão de Signatários
1. Na tela de Login, entre com **`admin@letssign.com`** e senha **`123456`**.
2. Na barra inferior, toque na aba **Assinaturas** para ver os signatários já cadastrados (*Carlos Silva* e *Mariana Souza*).
3. *(Opcional)* Toque em **+ Novo signatário**, cadastre um novo participante (teste colocar um e-mail inválido ou repetido para ver as validações visuais) e salve.

### Passo 2 — Criando e Configurando um Novo Documento PDF
1. Volte para a aba **Documentos** e toque em **+ Novo documento**.
2. **Etapa 1 (Título e Arquivo):** Digite um título (ex: *Contrato de Prestação de Serviços*), toque em **Escolher arquivo** e selecione o PDF da pasta *Downloads* do emulador. Clique em **Continuar**.
3. **Etapa 2 (Quem vai assinar?):** Toque em **+ Adicionar signatários**, marque **Carlos Silva** e **Mariana Souza** e confirme. Defina o papel de Carlos como **Contratante** e de Mariana como **Contratado** (ou **Testemunha**). Clique em **Continuar**.
4. **Etapa 3 (Posicionamento das Assinaturas no PDF):**
   * Com **Carlos** selecionado na barra superior, toque em **Inserir assinatura nesta página** e arraste a caixa roxa até a posição desejada no PDF.
   * *(Teste de Regra de Negócio)* Tente arrastar a caixa para fora da folha do PDF: observe que ela fica vermelha (*"Fora do limite"*), exibe o alerta *"Campo fora dos limites da página"* e retorna automaticamente para dentro da página.
   * Selecione **Mariana** na barra superior, toque em **Inserir assinatura nesta página** e posicione a caixa dela. Clique em **Continuar**.
5. **Etapa 4 (Revisão e Disparo):**
   * Confira o resumo do documento, os signatários, as páginas onde assinarão e personalize a **Mensagem que acompanhará o convite**.
   * Toque em **Confirmar envio**. O documento aparecerá na aba **Enviados** (`0/2 assinaturas`).

### Passo 3 — Assinando como Signatário 1 (Carlos)
1. No Admin, vá na aba **Perfil** (canto inferior direito) e toque em **Sair da conta**.
2. Faça login como Signatário 1: **`carlos@letssign.com`** / senha **`123456`**.
3. Na aba **Pendentes**, toque no documento *Contrato de Prestação de Serviços*.
4. O aplicativo abrirá o PDF já focado na página e na posição da caixa de Carlos. Toque na caixa **"Toque para assinar aqui"** (ou no botão **Assinar documento**).
5. Desenhe a assinatura com o mouse/dedo no quadro do Canvas e toque em **Confirmar assinatura**.
6. Veja que a assinatura desenhada agora faz parte do próprio PDF!

### Passo 4 — Verificando o Status "Parcialmente Assinado" no Admin
1. Saia da conta de Carlos (aba **Perfil** -> **Sair da conta**) e entre novamente como Admin (**`admin@letssign.com`** / **`123456`**).
2. No Dashboard, toque na aba **Parcialmente Assinados**: o documento estará lá com barra em **50% (`1/2 assinaturas`)**.
3. Toque no card do documento para abrir a tela de **Detalhes do Documento** e veja que Carlos consta como **Assinado** (com data, hora e IP registrado) e Mariana consta como **Pendente**, além do **Histórico de eventos** completo.

### Passo 5 — Assinando como Signatário 2 (Mariana) e Conclusão Automática
1. Saia da conta do Admin e entre como Signatária 2: **`mariana@letssign.com`** / senha **`123456`**.
2. Abra o documento pendente, desenhe a assinatura de Mariana e confirme.
3. Volte para a conta do Admin (**`admin@letssign.com`**): o documento terá migrado automaticamente para a aba **Concluídos** (`2/2 assinaturas` em verde).
4. Abra os detalhes do documento e clique em **Ver documento final (PDF)** para visualizar o PDF com ambas as assinaturas gravadas definitivamente!

---

## 5. Arquitetura e Preparação para a Sprint 2 (API REST)

O projeto foi construído seguindo **Clean Architecture + MVVM**, com separação estrita entre `ui`, `domain` e `data`.

Para garantir que na **Sprint 2** a persistência local seja substituída por chamadas de API REST (Spring Boot / .NET + MongoDB) sem alterar uma única linha das telas (`ui`) ou das regras de negócio (`domain/usecase`):
* A camada `domain/repository` define contratos puros (`UserRepository`, `DocumentRepository`, `PdfStorageRepository`) trabalhando apenas com modelos de domínio puros.
* A camada `data/repository/local` contém as implementações atuais baseadas em **Room** e **File System** (`LocalUserRepository`, `LocalDocumentRepository`, `LocalPdfStorageRepository`), injetadas via **Dagger Hilt** (`RepositoryModule.kt`).

```text
com.example.challenge_letssign
├── data/
│   ├── di/                # Módulos Hilt (DatabaseModule, RepositoryModule, DispatcherModule)
│   ├── local/             # Room Database, Entities, DAOs, TypeConverters e DatabaseSeeder
│   └── repository/
│       └── local/         # Implementações locais (Room + Internal File Storage + PDFBox)
├── domain/
│   ├── model/             # Modelos puros (User, Document, DocumentSigner, SignatureField, ImportedPdfFile)
│   ├── repository/        # Interfaces puras desacopladas (prontas para RemoteRepository na Sprint 2)
│   └── usecase/           # Casos de uso e validações de negócio
├── ui/
│   ├── auth/              # Tela e ViewModel de Login
│   ├── admin/             # Dashboard, Signatários, Wizard PDF, Drag & Drop, Review e Detalhes
│   ├── signer/            # Dashboard do Signatário, Visualizador Focado e Modal Canvas de Assinatura
│   ├── components/        # Componentes base do Design System (Botões, Inputs, Stepper, Chips, Avatares)
│   ├── navigation/        # Grafo de navegação (LetsSignNavHost)
│   └── theme/             # Tokens de Cores, Gradientes e Tipografia (Plus Jakarta Sans & Manrope)
└── utils/                 # AppResult, PdfCoordinateConverter, PdfPageRenderer, DateTimeFormatters, IpAddressResolver
```

---

## 6. Regras de Negócio e Requisitos Técnicos Atendidos

| Regra / Requisito do Edital | Como e Onde foi Implementado |
| :--- | :--- |
| **Mínimo de 1 signatário e todos com campo posicionado antes do envio** | Validado em `ValidateDocumentPositioningUseCase.kt` e `SendDocumentForSignatureUseCase.kt`. |
| **Coordenadas relativas (`0.0` a `1.0`) preservando posição entre dispositivos** | Calculado em `PdfCoordinateConverter.kt` (`relativeX = dropX / viewWidth`) e persistido em `SignatureFieldEntity.kt`. |
| **Campos devem permanecer dentro dos limites da página** | Validado em tempo real no arraste (`PdfPositioningScreen.kt`) e no domínio (`SaveSignatureFieldUseCase.kt`). |
| **Exclusão em cascata ao remover/substituir signatário em rascunho** | Transação atômica em `DocumentDao.removeSignerWithCascadeFields` excluindo os `SignatureFieldEntity` do signatário removido. |
| **Preservação e imutabilidade do documento enviado** | Documentos `PENDING` ou `COMPLETED` bloqueiam edição/exclusão na UI e nos UseCases (`SaveSignatureFieldUseCase`, `DeleteSignatureFieldUseCase`, `DocumentDetailsViewModel`). |
| **Conclusão automática quando todos os obrigatórios assinam** | Transação em `DocumentDao.signAndAutoCompleteIfAllSigned`: verifica se `countPendingSignersForDocument == 0` e atualiza o documento para `COMPLETED`. |
| **Isolamento de acesso aos documentos por usuário** | Admin consulta apenas `observeDocumentsByAdmin(adminId)`; Signatário consulta apenas documentos enviados (`status != 'DRAFT'`) onde foi vinculado (`observeDocumentsBySigner(signerId)`). |
| **Gravação real da assinatura no PDF com PDFBox** | Implementado em `LocalPdfStorageRepository.embedSignaturesIntoPdf` convertendo coordenadas relativas para o `cropBox` da `PDPage` e desenhando via `PDPageContentStream`. |

---

## 7. Stack Tecnológico

* **Linguagem:** Kotlin `2.2.10`
* **UI Toolkit:** Jetpack Compose (Material 3) + Navigation Compose
* **Tipografia Oficial:** *Plus Jakarta Sans* & *Manrope* (`res/font/`)
* **Arquitetura & Estado:** Clean Architecture + MVVM (`ViewModel` + `StateFlow`)
* **Injeção de Dependência:** Dagger Hilt `2.59.2` + KSP
* **Banco de Dados Local:** Room Database `2.8.4` (`users`, `documents`, `document_signer_cross_ref`, `signature_fields`)
* **Visualização e Manipulação de PDF:**
  * `android.graphics.pdf.PdfRenderer` (renderização nativa de páginas em `Bitmap`)
  * `com.tom-roush:pdfbox-android:2.0.27.0` (inserção definitiva do `Bitmap` da assinatura no PDF)
* **Testes Automatizados:** JUnit 4 + Kotlinx Coroutines Test (**37 testes unitários** cobrindo repositórios, conversores, matemática de coordenadas e todas as regras de negócio)
