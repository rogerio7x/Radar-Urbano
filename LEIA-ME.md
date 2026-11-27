# Radar de Problemas Urbanos (Java)

## Como montar
1. Android Studio > New Project > **Empty Views Activity**
   - Name: RadarUrbano | Package: `br.com.uemg.prog3.radarurbano` | Language: Java | Min SDK: API 23
2. `app/build.gradle.kts` > em `dependencies { }` acrescente e clique em Sync Now:
   `implementation("org.osmdroid:osmdroid-android:6.1.10")`
3. Apague o `MainActivity.java` e o `activity_main.xml` gerados e copie desta pasta,
   mantendo a estrutura (`app/src/main/...`): AndroidManifest.xml, res/layout, res/xml e os .java.
   (Se o Studio gerou outro `android:theme` no manifest, pode manter o seu.)
4. Rode em um celular real (GPS, câmera e acelerômetro funcionam melhor que no emulador).

## Recursos das aulas
| Aula | Onde |
|---|---|
| Simple Paint | ViewCanvas, DesenhoActivity |
| Cadastro (SQLite/CRUD/ListView/menu de contexto) | Model, DAO, MainActivity |
| Câmera | OcorrenciaActivity (FileProvider + ActivityResult) |
| Smart Walk (GPS, osmdroid, acelerômetro) | OcorrenciaActivity, MapaActivity, MainActivity (balançar) |
| Lembrete Inteligente (notificações) | NotificacaoHelper |
| Guia de tamanhos | sp para textos, dp para margens/alturas nos layouts |
