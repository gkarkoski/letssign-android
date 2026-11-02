# LetsSign — Plataforma Mobile de Assinatura Eletrônica e Gestão de Documentos

> **FIAP Challenge — 2º Ano de Análise e Desenvolvimento de Sistemas (Sprint 1)**  
> Aplicativo Android nativo desenvolvido em **Kotlin** com **Jetpack Compose** para preparação de documentos PDF, criação de grupos de signatários, detecção assistida por IA, posicionamento responsivo de campos de assinatura e rubrica, envio de solicitações, coleta de assinatura manuscrita (Canvas) ou digitada (fontes cursivas) e gravação definitiva no PDF físico com validade jurídica (timestamp UTC e endereço IP).

---

## Sumário

1. [Visão Geral da Solução](#1-visão-geral-da-solução)
2. [Credenciais de Teste (Login por Código de 6 Dígitos)](#2-credenciais-de-teste-login-por-código-de-6-dígitos)
3. [Como Executar o Projeto](#3-como-executar-o-projeto)
4. [Roteiro Passo a Passo para Testar o App (Fim a Fim)](#4-roteiro-passo-a-passo-para-testar-o-app-fim-a-fim)
5. [Arquitetura e Preparação para a Sprint 2 (API REST)](#5-arquitetura-e-preparação-para-a-sprint-2-api-rest)
6. [Regras de Negócio e Requisitos Técnicos Atendidos](#6-regras-de-negócio-e-requisitos-técnicos-atendidos)
7. [Stack Tecnológico e Suíte de Testes](#7-stack-tecnológico-e-suíte-de-testes)

---

## 1. Visão Geral da Solução

O aplicativo **LetsSign** implementa integralmente todos os fluxos e telas da Biblioteca LetsSign no Figma (incluindo as atualizações V2), divididos entre o perfil **Administrador (`ADMIN`)** e o perfil **Signatário (`SIGNER`)**:

### Fluxo 1 — Login Geral por Código de 6 Dígitos (5 telas)
* **Login Unificado (`Login Geral`):** Qualquer usuário (Admin ou Signatário) informa seu e-mail e recebe um código de acesso de 6 dígitos (`Login Carregando` -> `Login Código` com contagem regressiva `00:42`), além dos estados `Login Erro` (validação de formato de e-mail) e `Login Código inválido` (botão *Reenviar código*). Após confirmar o código, o app redireciona automaticamente para a Home Admin ou Home Signatário conforme o perfil cadastrado.

### Fluxos 2 e 4 — Home Admin, Contatos, Novo Grupo e Perfil
* **Menu Inferior do Admin (4 abas):** **Documentos** | **Minhas Assinaturas** | **Contatos** | **Perfil**.
* **Menu `Documentos`:** Abas **Enviados** (com badges *Enviado* para `0/N` e *Parcialmente assinado* para `1..N-1`) e **Concluídos**, busca por título ou nome do signatário, contagem de resultados, estados *Vazio*, *Carregando* (skeletons), *Erro* (com retry e snackbar) e botão flutuante **+ Novo documento**.
* **Menu `Minhas Assinaturas` (Fusão com Aguardando Terceiros):** Abas **Pendentes** e **Assinados**. Na aba **Pendentes**, os cards exibem duas linhas de status:
  1. Status geral do documento (*Parcialmente assinado* + `2/3 assinaturas`);
  2. Status individual do usuário + prazo (`05/11/2026`): chip lilás **Aguardando você** (quando falta sua assinatura) ou chip verde **✓ Assinado por você** (quando você já assinou e aguarda os demais signatários).
* **Menu `Contatos` + Fluxo `Novo Grupo`:**
  * Listagem dos contatos cadastrados (**6 contatos iniciais** pré-carregados), busca por nome, e-mail ou CPF/CNPJ (com destaque em roxo do termo buscado), e formulário simplificado para **Pessoa Física** (*CPF\**, *Nome\**, *E-mail\**, *Data de nascimento\**) e **Pessoa Jurídica** (*CNPJ\**, *Razão social\**, *E-mail\**), com resumo de erros no topo (*"Corrija N campos para salvar o contato."*) e exclusão.
  * **Criar Grupo de Signatários (`+ Novo grupo`):** Permite informar o *Nome do grupo* (ex.: *"Jurídico"* ou *"Turma 15 Calculo II AGO - 2027"*), abrir o seletor de contatos com busca e checkboxes (incluindo o botão **Cadastrar novo contato** sem sair do fluxo do grupo) e salvar o grupo no banco local.
* **Menu `Perfil` (`Dados do perfil`):** Exibe e permite atualizar os dados cadastrais do usuário logado (*Pessoa Tipo*, *CPF/CNPJ*, *Nome*, *E-mail* e *Data de nascimento*), além da opção de sair da conta.

### Fluxos 5, 6, 7 e 8 — Novo Documento em 6 Etapas + IA Assistiva
1. **Etapa 1 (Título e arquivo):** Importação de PDF com **Seletor Integrado à prova de emulador vazio** (permite escolher com 1 toque o PDF real de 6 páginas `contrato-servicos.pdf`, simular erro com `relatorio-final.pdf` ou abrir o seletor nativo `ACTION_OPEN_DOCUMENT` do Android, onde a pasta pública `Downloads` também já recebe o PDF automaticamente no boot).
2. **Etapa 2 (Quem vai assinar?):**
   * Seleção manual com botão em destaque **Cadastrar novo contato** (caso um signatário ainda não esteja na lista e precise ser cadastrado no meio do processo) e busca por **grupo, nome ou e-mail** (ex.: digitar `"Jurídico"` permite marcar o grupo inteiro de uma só vez).
   * Detecção assistida **✨ Sugerir com IA** (em negrito: *Procurando signatários* -> *Encontramos 3 possíveis signatários*), classificação individual (*Contratante*, *Contratado*, *Testemunha*), substituição (`⇄`) e remoção em cascata.
3. **Etapa 3 (Envio e autenticação Geral):** Card único aplicando as configurações de envio e autenticação para todos os signatários do documento: *Enviar link de assinatura por\** (*E-mail*, *SMS*, *WhatsApp*, *Não enviar*), *Método de autenticação\** (*E-mail*, *SMS*, *WhatsApp*, *Certificado Digital*, *Sem Autenticação*), *Autenticações adicionais* (*Selfie*, *Documento com foto*, *Biometria facial*, *Prova de vida*) e **Idioma** (*Português*, *Inglês*, *Espanhol*).
4. **Etapa 4 (Campos de assinatura + IA nos campos):** Visualização das páginas do PDF via `PdfRenderer`, botão **+ Campo**, pílulas por signatário com cores distintas (`#6d3bf5`, `#17123a`, `#0a7886`), *Drag & Drop* com barra flutuante de seleção, conversão para coordenadas relativas (`0.0..1.0`), bloqueio de limites da página (*"O campo precisa ficar dentro da página."*) e botão **✨ Sugerir posições com IA** (sugere campos de *Assinatura* sobre a linha e *Rubrica* no canto inferior direito da folha).
5. **Etapa 5 (Revisar e enviar):** Conferência do documento, lista de signatários/campos/páginas, alerta de pendência caso falte campo para algum participante (*"Beatriz Nogueira ainda não tem campo de assinatura"*) e personalização da **Mensagem do convite**.
6. **Etapa 6 (Documento enviado!):** Confirmação de envio com atalho direto para **Acompanhar documento** ou **Voltar ao início**.

### Fluxos 3, 9 e 10 — Home Signatário, Assinar Documento em 3 Etapas e Detalhe do Documento
* **Home Signatário (`Minhas Assinaturas` + `Perfil`):** Barra inferior com 2 abas (**Minhas Assinaturas** e **Perfil**), saudação personalizada e sub-abas **Pendentes** (exibindo cards com *Aguardando você* e *✓ Assinado por você* + prazo `05/11/2026`) e **Assinados**.
* **Assinar Documento (3 etapas):**
  * **Etapa 1 (Leia o documento):** Foco automático na página e nos campos atribuídos ao signatário (`Assine aqui` na linha de assinatura e `Rubrica` no canto inferior direito da página).
  * **Etapa 2 (Sua assinatura e Sua rubrica):** Captura via **Desenhar** (Canvas manuscrito) ou **Digitar** (renderizando o nome e as iniciais nas 3 fontes cursivas oficiais: *Dancing Script*, *Caveat* e *Homemade Apple*).
  * **Etapa 3 (Confirmar):** Preview lado a lado da Assinatura e da Rubrica -> gravação definitiva dos Bitmaps nas coordenadas exatas do PDF via **PDFBox** (`PDPageContentStream`), registro de data/hora e IP local, conclusão automática do documento quando todos assinam e tela **Documento assinado!** (com botão *Ver documentos assinados* abrindo diretamente a aba **Assinados**).
* **Detalhe do Documento:** Status de cada signatário, linha do tempo **Acompanhamento**, botão **Reenviar lembrete** + **Excluir documento** (para documentos pendentes/parciais) e botão **Baixar documento assinado** + **Remover documento** (para documentos concluídos, exportando fisicamente o arquivo `*-assinado.pdf` em Downloads).

---

## 2. Credenciais de Teste (Login por Código de 6 Dígitos)

Ao abrir o aplicativo pela primeira vez, o banco de dados local (**Room**) é populado automaticamente (`DatabaseSeeder`) com **1 Administrador**, **6 Contatos Signatários** e **5 Documentos de Exemplo** (com PDF válido de 6 páginas gerado via PDFBox):

| Perfil | Nome | E-mail para Login | Código de 6 Dígitos (OTP) | Observação |
| :--- | :--- | :--- | :--- | :--- |
| **Administrador** | Administrador LetsSign | `admin@exemplo.com.br` *(ou `admin@letssign.com`)* | Qualquer código de 6 dígitos (ex: **`482910`** ou **`123456`**) | Use `482915` ou `000000` para testar a tela *Código inválido* |
| **Signatário 1** | Rafael Tavares | `rafael.tavares@exemplo.com.br` *(ou `carlos@letssign.com`)* | Qualquer código de 6 dígitos (ex: **`482910`** ou **`123456`**) | Grupo: *Comercial* (possui assinatura + rubrica no canto inferior direito) |
| **Signatária 2** | Marina Albuquerque | `marina.albuquerque@exemplo.com.br` *(ou `mariana@letssign.com`)* | Qualquer código de 6 dígitos (ex: **`482910`** ou **`123456`**) | Grupo: *Jurídico* |
| **Signatária 3** | Beatriz Nogueira | `beatriz.nogueira@exemplo.com.br` | Qualquer código de 6 dígitos (ex: **`482910`** ou **`123456`**) | Grupo: *Jurídico* (falta apenas ela no *Aditivo contratual* 2/3) |

---

## 3. Como Executar o Projeto

### Opção A: Executando pelo Android Studio (Recomendado)
1. Abra a pasta raiz `challenge_letsSign` no **Android Studio**.
2. Aguarde a finalização do *Gradle Sync*.
3. Inicie um Emulador Android (API 24+) e clique em **Run ('app')**.

### Opção B: Compilando e Rodando a Suíte Completa de Testes via Terminal
```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleDebug testDebugUnitTest --rerun-tasks
```

---

## 4. Roteiro Passo a Passo para Testar o App (Fim a Fim)

### Passo 1 — Login Geral por Código de 6 Dígitos (Admin)
1. Na tela de Login, digite **`admin@exemplo.com.br`** e toque em **Enviar código**.
2. Digite **`482910`** (ou `123456`) e toque em **Entrar**.
3. Explore a Home Admin pelos 4 itens do menu inferior: **Documentos** (abas *Enviados* e *Concluídos* + busca), **Minhas Assinaturas** (abas *Pendentes* e *Assinados*), **Contatos** e **Perfil**.

### Passo 2 — Contatos e Criação de Grupo (`+ Novo grupo`)
1. Na aba **Contatos**, veja os 6 contatos cadastrados (*Beatriz*, *Camila*, *Lucas*, *Marina*, *Nova Soluções Ltda.* e *Rafael*).
2. Toque em **+ Novo grupo**, digite o nome do grupo (ex.: `"Diretoria"`), toque em **+ Adicionar contato**, selecione os contatos desejados (ou cadastre um novo contato ali mesmo clicando em **Cadastrar novo contato**), toque em **Adicionar selecionados** e depois em **Criar grupo**.

### Passo 3 — Novo Documento em 6 Etapas com IA
1. Na aba **Documentos**, toque em **+ Novo documento**.
2. **Etapa 1 (Título e arquivo):** Toque em **Escolher arquivo** e selecione **`contrato-servicos.pdf (PDF de exemplo · 6 páginas · 1,8 MB)`** direto no sheet integrado (não precisa ter arquivos externos no emulador!). Toque em **Continuar**.
3. **Etapa 2 (Quem vai assinar?):** Toque em **✨ Sugerir com IA** (ou em **+ Adicionar signatários**, onde você pode buscar pelo grupo criado ou clicar em **Cadastrar novo contato**). Confira a classificação (*Contratante*, *Contratado*, *Testemunha*) e toque em **Continuar**.
4. **Etapa 3 (Envio e autenticação):** Configure o envio do link, método de autenticação, autenticações adicionais e idioma no card geral. Toque em **Continuar**.
5. **Etapa 4 (Campos de assinatura):** Toque em **✨ Sugerir posições com IA** e depois em **Aceitar 5 campos** (ou posicione manualmente com **+ Campo**). Toque em **Continuar**.
6. **Etapa 5 e 6 (Revisar e enviar):** Confira o resumo e toque em **Confirmar envio** -> na tela **Documento enviado!**, toque em **Acompanhar documento**.

### Passo 4 — Fluxo do Signatário (Assinatura + Rubrica no Canto Inferior Direito)
1. Toque em **Sair** e entre com **`rafael.tavares@exemplo.com.br`** (código **`123456`**).
2. Observe o menu inferior do Signatário (**Minhas Assinaturas** e **Perfil**) e os dois tipos de cards na aba **Pendentes** (*Aguardando você* no `Contrato de serviços` e *✓ Assinado por você* no `Aditivo contratual`).
3. Toque em **Contrato de serviços** -> **Etapa 1 (Leia o documento):** veja o campo `Assine aqui` na linha de assinatura e o campo `Rubrica` no canto inferior direito da folha. Toque em **Assinar**.
4. **Etapa 2 (Sua assinatura e Sua rubrica):** Desenhe ou digite sua **Assinatura** e toque em **Usar assinatura**; em seguida, desenhe ou digite sua **Rubrica** e toque em **Usar rubrica**.
5. **Etapa 3 (Confirmar):** Confira o preview lado a lado da Assinatura e da Rubrica e toque em **Confirmar** -> toque em **Ver documentos assinados** para abrir diretamente a aba **Assinados**!

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
| **Regra 4 (Imutabilidade pós-envio e isolamento por usuário)** | `SaveSignatureFieldUseCase.kt`, `DeleteSignatureFieldUseCase.kt` e `DocumentDao.kt`. |
| **Regra 5 (Conclusão automática ao coletar todas as assinaturas)** | Transação `DocumentDao.signAndAutoCompleteIfAllSigned` acionada após `SignDocumentUseCase.kt`. |
| **Gravação real de Assinatura e Rubrica no PDF via PDFBox** | `LocalPdfStorageRepository.embedSignaturesIntoPdf` convertendo coordenadas relativas para o `cropBox` da `PDPage`. |

---

## 7. Stack Tecnológico e Suíte de Testes

* **Linguagem:** Kotlin `2.2.10`
* **UI Toolkit:** Jetpack Compose (Material 3) + Navigation Compose
* **Tipografia Oficial:** *Plus Jakarta Sans*, *Manrope*, *Dancing Script*, *Caveat* & *Homemade Apple* (`res/font/`)
* **Arquitetura & DI:** Clean Architecture + MVVM (`ViewModel` + `StateFlow`) + Dagger Hilt `2.59.2` (KSP)
* **Persistência Local:** Room Database `2.8.4` (`version = 3`) + Internal File Storage (`context.filesDir`)
* **Motor PDF:** `android.graphics.pdf.PdfRenderer` (visualização) + `com.tom-roush:pdfbox-android:2.0.27.0` (gravação de assinaturas e rubricas)
* **Testes Automatizados:** JUnit 4 + Kotlinx Coroutines Test (**56 testes automatizados passando** em 12 suítes cobrindo Regras de Negócio 1–5, Usabilidade V2, Fluxo Ponta a Ponta e Edge Cases)
