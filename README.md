# Leitor Claro (Android)

Aplicativo Android em Kotlin + Jetpack Compose para:

- Tirar uma foto e detectar múltiplos códigos de barras via ML Kit.
- Escolher qual código detectado será salvo.
- Cadastrar equipamento com tipo, nome, contrato e data/hora.
- Salvar localização em tempo real (lat/lng) e endereço (rua, bairro e CEP).
- Persistir os registros localmente com Room.

## Como executar

1. Abra no Android Studio (Hedgehog+).
2. Sincronize o Gradle.
3. Rode em dispositivo com permissões de câmera e localização.

## Observações

- O app usa `FusedLocationProviderClient` e `Geocoder` para resolver endereço.
- O registro é salvo localmente no banco `leitor-claro.db`.
