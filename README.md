# Leitor Claro (Flutter / Dart)

Sim, dá para fazer em **.dart** ✅

Este projeto agora está em **Flutter**, com lógica principal em Dart, e entrega exatamente o fluxo pedido:

- Tirar foto do equipamento.
- Detectar múltiplos códigos de barras na imagem (ML Kit).
- Escolher qual código detectado será usado.
- Cadastrar equipamento com tipo, nome, contrato e data/hora.
- Salvar localização em tempo real + rua/bairro/CEP.
- Persistir localmente com SQLite (`sqflite`).

## Estrutura principal

- `lib/main.dart`: tela principal, formulário, seleção de código, listagem.
- `lib/src/services/barcode_service.dart`: leitura dos códigos.
- `lib/src/services/location_service.dart`: GPS + geocoding.
- `lib/src/services/database_service.dart`: persistência local.
- `lib/src/models/equipment.dart`: modelo de dados.

## Como rodar

1. Se a pasta foi convertida de um projeto Android antigo, gere a estrutura Flutter:
   - `flutter create .`
2. Instale dependências:
   - `flutter pub get`
3. Rode no Android:
   - `flutter run`

## Permissões Android necessárias

No `android/app/src/main/AndroidManifest.xml`, adicione:

- `android.permission.CAMERA`
- `android.permission.ACCESS_FINE_LOCATION`
- `android.permission.ACCESS_COARSE_LOCATION`

> Observação: como este repositório era Android nativo antes, os diretórios de plataforma (`android/`, `ios/`) podem precisar ser recriados via `flutter create .`.
