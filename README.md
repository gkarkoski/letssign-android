# LetsSign — Plataforma de Assinatura Eletrônica e Gestão de Documentos

Aplicativo Android nativo desenvolvido em **Kotlin** com **Jetpack Compose** para gestão de documentos PDF e coleta de assinaturas eletrônicas com validade jurídica (registro de timestamp UTC e endereço IP).

O sistema opera em dois fluxos principais:
1. **Fluxo do Administrador (`ADMIN`):** Importação de arquivos PDF, cadastro e gestão de signatários, atribuição de papéis (*Contratante*, *Contratado*, *Testemunha*), posicionamento visual interativo (*Drag & Drop*) das caixas de assinatura sobre as páginas do PDF e acompanhamento do status de envio/conclusão.
2. **Fluxo do Signatário (`SIGNER`):** Visualização de documentos pendentes, navegação guiada até os campos de assinatura atribuídos, desenho da assinatura em Canvas e gravação definitiva da assinatura diretamente no arquivo PDF físico.

---

## Stack Tecnológico

* **Linguagem:** Kotlin (`2.2.10`)
* **Interface Visual:** Jetpack Compose (Material 3) em arquitetura *Single Activity*
* **Gerenciamento de Estado:** MVVM com `ViewModel` + `StateFlow`
* **Injeção de Dependência:** Dagger Hilt + KSP
* **Persistência Local (Sprint 1):** Room Database + Armazenamento Interno (`context.filesDir`) para arquivos PDF
* **Motor de PDF:**
  * `android.graphics.pdf.PdfRenderer` nativo para renderização de páginas em `Bitmap` na UI
  * `com.tom-roush:pdfbox-android` para manipulação estrutural, inserção das assinaturas e salvamento do PDF
* **Concorrência & Assincronismo:** Kotlin Coroutines & Flows (operações de I/O isoladas em `Dispatchers.IO`)

---

## Arquitetura do Projeto

O projeto segue os princípios de **Clean Architecture** divididos em camadas claras, garantindo que a camada de Apresentação (`ui`) e a camada de Domínio (`domain`) **não conheçam detalhes de implementação** de banco de dados local (Room) ou sistema de arquivos, preparando o aplicativo para acoplamento transparente com uma API REST na Sprint 2:

```text
com.example.challenge_letssign
├── data/
│   ├── di/                # Módulos Hilt (DatabaseModule, RepositoryModule, DispatcherModule)
│   ├── local/             # Room Database, Entities, DAOs, TypeConverters e Seed inicial
│   └── repository/
│       └── local/         # Implementações locais (LocalUserRepository, LocalDocumentRepository)
├── domain/
│   ├── model/             # Modelos de domínio puros (User, Document, DocumentSigner, SignatureField)
│   ├── repository/        # Contratos/Interfaces puras de repositório
│   └── usecase/           # Regras de negócio e validações
├── ui/
│   ├── auth/              # Fluxo de Login e Autenticação
│   ├── admin/             # Dashboard Admin, Gestão de Signatários, Setup e Posicionamento no PDF
│   ├── signer/            # Dashboard Signatário e Canvas de Assinatura
│   └── theme/             # Design System, Cores, Tipografia e Componentes reutilizáveis
└── utils/                 # Wrappers de resultado (AppResult), conversores de coordenadas e extensões
```

### Modelagem de Dados (Room)

1. **`UserEntity` (`users`):** Armazena usuários com `id` (UUID), `name`, `email` (único), `phone`, `password` e `role` (`ADMIN` | `SIGNER`).
2. **`DocumentEntity` (`documents`):** Armazena metadados do PDF com `id` (UUID), `title`, `fileName` (referência ao arquivo no armazenamento interno), `status` (`DRAFT` | `PENDING` | `COMPLETED`), `createdAt` (Timestamp UTC) e `ownerAdminId`.
3. **`DocumentSignerCrossRef` (`document_signer_cross_ref`):** Tabela associativa N:N entre documentos e signatários contendo `roleClass` (`CONTRATANTE` | `CONTRATADO` | `TESTEMUNHA`), `status` (`PENDING` | `SIGNED`), `signedAt` e `signatureIp`.
4. **`SignatureFieldEntity` (`signature_fields`):** Campos de assinatura posicionados no PDF com `pageNumber` (0-indexed) e coordenadas relativas normalizadas de `0.0` a `1.0` (`relativeX`, `relativeY`, `relativeWidth`, `relativeHeight`) para garantir responsividade entre diferentes tamanhos de tela.

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

Para rodar os testes unitários:
```bash
./gradlew testDebugUnitTest
```

> **Dica (macOS):** Caso o terminal não tenha a variável `JAVA_HOME` apontando para um JDK instalado globalmente, você pode utilizar o JBR do Android Studio:
> ```bash
> JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleDebug
> ```
