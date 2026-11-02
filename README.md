# LetsSign — Plataforma Mobile de Assinatura Eletrônica e Gestão de Documentos

> **FIAP Challenge — 2º Ano de Análise e Desenvolvimento de Sistemas (Sprint 1)**  
> Aplicativo Android nativo desenvolvido em **Kotlin** com **Jetpack Compose** para preparação de documentos PDF, detecção assistida por IA, posicionamento responsivo de campos de assinatura e rubrica, envio de solicitações, coleta de assinatura manuscrita (Canvas) ou digitada (fontes cursivas) e gravação definitiva no PDF físico com validade jurídica (timestamp UTC e endereço IP).

---

## Sumário

1. [Visão Geral da Solução](#1-visão-geral-da-solução)
2. [Credenciais de Teste (Login por Código de 6 Dígitos)](#2-credenciais-de-teste-login-por-código-de-6-dígitos)
3. [Como Executar o Projeto](#3-como-executar-o-projeto)
4. [Roteiro Passo a Passo para Testar o App (Fim a Fim)](#4-roteiro-passo-a-passo-para-testar-o-app-fim-a-fim)
5. [Arquitetura e Preparação para a Sprint 2 (API REST)](#5-arquitetura-e-preparação-para-a-sprint-2-api-rest)
6. [Regras de Negócio e Requisitos Técnicos Atendidos](#6-regras-de-negócio-e-requisitos-técnicos-atendidos)
7. [Stack Tecnológico](#7-stack-tecnológico)

---

## 1. Visão Geral da Solução

O aplicativo **LetsSign** implementa integralmente as **10 linhas de fluxo (74 telas/estados)** da Biblioteca LetsSign no Figma, divididas entre o perfil **Administrador (`ADMIN`)** e o perfil **Signatário (`SIGNER`)**:

### Fluxo 1 — Login Geral por Código de 6 Dígitos (5 telas)
* **Login Unificado (`Login Geral`):** Qualquer usuário (Admin ou Signatário) informa seu e-mail e recebe um código de acesso de 6 dígitos (`Login Carregando` -> `Login Código` com contagem regressiva `00:42`), além dos estados `Login Erro` (validação de formato de e-mail) e `Login Código inválido` (botão *Reenviar código*). Após confirmar o código, o app redireciona automaticamente para a Home Admin ou Home Signatário conforme o perfil cadastrado.

### Fluxos 2 e 4 — Home Admin (10 telas) e Contatos (9 telas)
* **Menu `Documentos`:** Abas **Enviados** e **Concluídos**, busca por título ou nome do signatário, contagem de resultados, estados *Vazio*, *Carregando* (skeletons), *Erro* (com retry e snackbar) e botão flutuante **+ Novo documento**.
* **Menu `Minhas Assinaturas`:** Abas **Pendentes** (com chip *Aguardando você* e prazo `05/11/2026`, abrindo direto o fluxo de assinatura) e **Assinados**.
* **Menu `Aguardando Terceiros`:** Lista dedicada aos documentos **Parcialmente assinados** com busca e barra de progresso.
* **Menu `Contatos`:** Listagem dos contatos cadastrados (**6 contatos iniciais** pré-carregados), busca por nome, e-mail ou CPF/CNPJ (com destaque em roxo do termo buscado), e formulário simplificado para **Pessoa Física** (*CPF\**, *Nome\**, *E-mail\**, *Data de nascimento\**) e **Pessoa Jurídica** (*CNPJ\**, *Razão social\**, *E-mail\**), com resumo de erros no topo (*"Corrija N campos para salvar o contato."*) e exclusão.

### Fluxos 5, 6, 7 e 8 — Novo Documento em 6 Etapas + IA Assistiva (29 telas)
1. **Etapa 1 (Título e arquivo):** Importação de PDF via `ACTION_OPEN_DOCUMENT` (`InputStream` -> `context.filesDir`), validação real com `PdfRenderer`, estado `Processando páginas · 66%`, card de arquivo válido (`6 páginas · 1,8 MB`) e tratamento de arquivo corrompido/inválido.
2. **Etapa 2 (Quem vai assinar?):** Seleção manual com busca por **grupo, nome ou e-mail** (ex.: digitar `"Jurídico"` permite marcar o grupo inteiro de 2 signatários de uma só vez), detecção assistida **✨ Sugerir com IA** (*Procurando signatários* -> *Encontramos 3 possíveis signatários*), classificação individual (*Contratante*, *Contratado*, *Testemunha*), substituição (`⇄`) e remoção em cascata.
3. **Etapa 3 (Envio e autenticação):** Configuração em acordeão por signatário com bottom sheets para *Enviar link de assinatura por* (*E-mail*, *SMS*, *WhatsApp*, *Não enviar*), *Método de autenticação* (*E-mail*, *SMS*, *WhatsApp*, *Certificado Digital*, *Sem Autenticação*), *Autenticações adicionais* (*Selfie*, *Documento com foto*, *Biometria facial*, *Prova de vida*) e *Idioma*.
4. **Etapa 4 (Campos de assinatura + IA nos campos):** Visualização das páginas do PDF via `PdfRenderer`, botão **+ Campo** (sheet para escolher quem assina naquela página), pílulas por signatário com cores distintas (`#6d3bf5`, `#17123a`, `#0a7886`), *Drag & Drop* com barra flutuante de seleção, conversão para coordenadas relativas (`0.0..1.0`), bloqueio de limites da página (*"O campo precisa ficar dentro da página."*) e botão **✨ Sugerir posições com IA** (sugere campos de *Assinatura* e *Rubrica*, permitindo revisar cada campo individualmente ou aceitar todos).
5. **Etapa 5 (Revisar e enviar):** Conferência do documento, lista de signatários/campos/páginas, alerta de pendência caso falte campo para algum participante (*"Beatriz Nogueira ainda não tem campo de assinatura"*) e personalização da **Mensagem do convite**.
6. **Etapa 6 (Documento enviado!):** Confirmação de envio com atalho direto para **Acompanhar documento** ou **Voltar ao início**.

### Fluxos 3, 9 e 10 — Home Signatário (5 telas), Assinar Documento em 3 Etapas (10 telas) e Detalhe do Documento (6 telas)
* **Home Signatário:** Saudação personalizada, abas **Pendentes** (`Aguardando você` + prazo) e **Assinados**.
* **Assinar Documento (3 etapas):**
  * **Etapa 1 (Leia o documento):** Foco automático na página e nos campos atribuídos ao signatário (`Assine aqui` e `Rubrica`).
  * **Etapa 2 (Sua assinatura e Sua rubrica):** Captura via **Desenhar** (Canvas manuscrito) ou **Digitar** (renderizando o nome e as iniciais nas 3 fontes cursivas oficiais: *Dancing Script*, *Caveat* e *Homemade Apple*).
  * **Etapa 3 (Confirmar):** Preview lado a lado da Assinatura e da Rubrica -> gravação definitiva dos Bitmaps nas coordenadas exatas do PDF via **PDFBox** (`PDPageContentStream`), registro de data/hora e IP local, conclusão automática do documento quando todos assinam e tela **Documento assinado!**.
* **Detalhe do Documento:** Status de cada signatário, linha do tempo **Acompanhamento**, botão **Reenviar lembrete** (para pendentes/parciais) e botão **Baixar documento assinado** (para concluídos, exibindo aviso de salvamento em Downloads e ação **Abrir**).

---

## 2. Credenciais de Teste (Login por Código de 6 Dígitos)

Ao abrir o aplicativo pela primeira vez, o banco de dados local (**Room**) é populado automaticamente (`DatabaseSeeder`) com **1 Administrador**, **6 Contatos Signatários** e **5 Documentos de Exemplo** (com PDF válido de 6 páginas gerado via PDFBox):

| Perfil | Nome | E-mail para Login | Código de 6 Dígitos (OTP) | Observação |
| :--- | :--- | :--- | :--- | :--- |
| **Administrador** | Administrador LetsSign | `admin@exemplo.com.br` *(ou `admin@letssign.com`)* | Qualquer código de 6 dígitos (ex: **`482910`** ou **`123456`**) | Use `482915` ou `000000` para testar a tela *Código inválido* |
| **Signatário 1** | Rafael Tavares | `rafael.tavares@exemplo.com.br` *(ou `carlos@letssign.com`)* | Qualquer código de 6 dígitos (ex: **`482910`** ou **`123456`**) | Grupo: *Comercial* (possui assinatura + rubrica pendentes) |
| **Signatária 2** | Marina Albuquerque | `marina.albuquerque@exemplo.com.br` *(ou `mariana@letssign.com`)* | Qualquer código de 6 dígitos (ex: **`482910`** ou **`123456`**) | Grupo: *Jurídico* |
| **Signatária 3** | Beatriz Nogueira | `beatriz.nogueira@exemplo.com.br` | Qualquer código de 6 dígitos (ex: **`482910`** ou **`123456`**) | Grupo: *Jurídico* (falta apenas ela no *Aditivo contratual* 2/3) |

---

## 3. Como Executar o Projeto

### Opção A: Executando pelo Android Studio (Recomendado)
1. Abra a pasta raiz `challenge_letsSign` no **Android Studio**.
2. Aguarde a finalização do *Gradle Sync*.
3. Inicie um Emulador Android (API 24+) e clique em **Run ('app')**.

### Opção B: Compilando e Rodando os Testes Unitários via Terminal
```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleDebug testDebugUnitTest
```

---

## 4. Roteiro Passo a Passo para Testar o App (Fim a Fim)

### Passo 1 — Login Geral por Código de 6 Dígitos (Admin)
1. Na tela de Login, digite **`admin@exemplo.com.br`** e toque em **Enviar código**.
2. *(Opcional)* Digite `482915` e toque em **Entrar** para visualizar o estado **Código inválido**; depois toque em **Reenviar código**, digite **`482910`** (ou `123456`) e toque em **Entrar**.
3. Explore a Home Admin pelos 4 itens do menu inferior: **Documentos** (abas *Enviados* e *Concluídos* + busca), **Minhas Assinaturas**, **Aguardando Terceiros** e **Contatos**.

### Passo 2 — Contatos (Pessoa Física e Jurídica)
1. Na aba **Contatos**, veja os 6 contatos cadastrados (*Beatriz*, *Camila*, *Lucas*, *Marina*, *Nova Soluções Ltda.* e *Rafael*).
2. Teste buscar por `"mar"` ou `"carlos"` (para ver o estado sem resultados e o botão *Limpar busca*).
3. Toque em **+ Novo contato**, alterne o seletor **Tipo** entre **Física** e **Jurídica**, teste clicar em **Salvar** com campos vazios para ver o banner *"Corrija N campos para salvar o contato."* e salve um novo contato.

### Passo 3 — Novo Documento em 6 Etapas com IA
1. Na aba **Documentos**, toque em **+ Novo documento** (ou arraste um `.pdf` para o emulador antes).
2. **Etapa 1 (Título e arquivo):** Digite um título, escolha um arquivo PDF e veja o processamento e o card verde *"Arquivo válido e pronto para uso."*. Toque em **Continuar**.
3. **Etapa 2 (Quem vai assinar?):** Toque em **✨ Sugerir com IA** (ou em **+ Adicionar signatários** e busque por `"Jurídico"` para selecionar o grupo inteiro). Confira a classificação (*Contratante*, *Contratado*, *Testemunha*) e toque em **Continuar**.
4. **Etapa 3 (Envio e autenticação):** Toque nos seletores do card para abrir os sheets de *Enviar link por*, *Método de autenticação* e *Autenticações adicionais*. Toque em **Continuar**.
5. **Etapa 4 (Campos de assinatura):**
   * Toque em **✨ Sugerir posições com IA**, revise um campo sugerido (alternando entre *Assinatura* e *Rubrica*) e toque em **Aceitar campos** (ou use o botão **+ Campo**).
   * Arraste qualquer campo na página (tente arrastá-lo para fora da folha para ver o bloqueio *"O campo precisa ficar dentro da página."*). Toque em **Continuar**.
6. **Etapa 5 e 6 (Revisar e enviar):** Confira o resumo, edite a **Mensagem do convite** e toque em **Confirmar envio** -> na tela **Documento enviado!**, toque em **Acompanhar documento**.

### Passo 4 — Fluxo do Signatário (Desenhar, Digitar e Rubrica + Conclusão Automática)
1. Volte ao início, toque em **Sair** (canto superior direito do cabeçalho) e entre com **`beatriz.nogueira@exemplo.com.br`** (código **`123456`**).
2. Na aba **Pendentes**, toque em **Aditivo contratual** (que já possui 2/3 assinaturas coletadas).
3. **Etapa 1 (Leia o documento):** Veja os campos destacados na página e toque em **Assinar**.
4. **Etapa 2 (Sua assinatura e Sua rubrica):** Desenhe sua assinatura no Canvas (ou alterne para a aba **Digitar** e escolha uma das 3 fontes cursivas) e toque em **Usar assinatura**. Em seguida, desenhe ou digite sua **Rubrica** e toque em **Usar rubrica**.
5. **Etapa 3 (Confirmar):** Confira o preview lado a lado da Assinatura e da Rubrica e toque em **Confirmar**.
6. Na tela **Documento assinado!**, toque em **Ver documentos assinados** e abra o **Aditivo contratual**: ele agora está **Concluído (3 de 3 assinaturas)**, permitindo clicar em **Baixar documento assinado** e **Abrir** o PDF final gravado via PDFBox!

---

## 5. Arquitetura e Preparação para a Sprint 2 (API REST)

O projeto segue **Clean Architecture + MVVM**, mantendo `ui` e `domain` 100% desacoplados de Room e File System por meio das interfaces puras em `domain/repository` (`UserRepository`, `DocumentRepository`, `PdfStorageRepository`), prontas para receber a implementação remota (`data/repository/remote`) na Sprint 2.

---

## 6. Regras de Negócio e Requisitos Técnicos Atendidos

| Regra / Requisito | Onde foi Implementado |
| :--- | :--- |
| **Regra 1 (Validação de Envio — mínimo 1 signatário e todos com campo)** | `ValidateDocumentPositioningUseCase.kt`, `SendDocumentForSignatureUseCase.kt` e `DocumentReviewScreen.kt` (*Revisão com Pendência*). |
| **Regra 2 (Limites do PDF `0.0..1.0`)** | `PdfCoordinateConverter.kt`, `SaveSignatureFieldUseCase.kt` e `PdfPositioningScreen.kt` (*Campo Fora dos Limites*). |
| **Regra 3 (Exclusão em cascata de campos ao remover/substituir signatário)** | `DocumentDao.removeSignerWithCascadeFields` e `PrepareDraftDocumentUseCase.kt`. |
| **Regra 4 (Imutabilidade pós-envio e isolamento por usuário)** | `SaveSignatureFieldUseCase.kt`, `DeleteSignatureFieldUseCase.kt`, `DocumentDetailsViewModel.kt` e `DocumentDao.kt`. |
| **Regra 5 (Conclusão automática ao coletar todas as assinaturas)** | Transação `DocumentDao.signAndAutoCompleteIfAllSigned` acionada após `SignDocumentUseCase.kt`. |
| **Gravação real de Assinatura e Rubrica no PDF via PDFBox** | `LocalPdfStorageRepository.embedSignaturesIntoPdf` convertendo coordenadas relativas para o `cropBox` da `PDPage`. |

---

## 7. Stack Tecnológico

* **Linguagem:** Kotlin `2.2.10`
* **UI Toolkit:** Jetpack Compose (Material 3) + Navigation Compose
* **Tipografia Oficial:** *Plus Jakarta Sans*, *Manrope*, *Dancing Script*, *Caveat* & *Homemade Apple* (`res/font/`)
* **Arquitetura & DI:** Clean Architecture + MVVM (`ViewModel` + `StateFlow`) + Dagger Hilt `2.59.2` (KSP)
* **Persistência Local:** Room Database `2.8.4` + Internal File Storage (`context.filesDir`)
* **Motor PDF:** `android.graphics.pdf.PdfRenderer` (visualização) + `com.tom-roush:pdfbox-android:2.0.27.0` (gravação de assinaturas e rubricas)
* **Testes Automatizados:** JUnit 4 + Kotlinx Coroutines Test (**42 testes unitários** passando)
