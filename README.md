# MiyavDoku

Android için renkli, kedi temalı mantık ve gizli kedi bulma oyunu.

## Oyun mantığı
- Her bölüm renk bölgelerine ayrılmış kare bir tahtadan oluşur.
- Her satırda tam bir kedi bulunur.
- Her sütunda tam bir kedi bulunur.
- Her renkte tam bir kedi bulunur.
- Kediler yatay, dikey veya çapraz komşu hücrelerde bulunamaz.
- Hücreye dokunarak boş olduğuna dair kalem işareti koyup kaldırabilirsin.
- Bir hücreyi kedi olarak doğrulamak can kullanır; yanlış tahmin bir can götürür.
- Oyuncu üç canla başlar. Tüm kediler bulunursa bölüm kazanılır; canlar biterse bölüm kaybedilir.

## Teknik kararlar
- Kotlin ve Jetpack Compose
- Android'den bağımsız saf Kotlin oyun motoru
- Değişmez (immutable) oyun durumu
- Seviye tanımları ile oyun kurallarının ayrılması
- JUnit birim testleri
- Room ve DataStore ilerleyen aşamada kalıcı ilerleme ve ayarlar için eklenecek

## Kod yapısı
- Oyun motoru: `app/src/main/java/com/miyavdoku/domain/GameEngine.kt`
- Motor testleri: `app/src/test/java/com/miyavdoku/domain/GameEngineTest.kt`
- İlk örnek bölüm özgün olarak oluşturulmuştur; üçüncü taraf kaynak kodu, görsel veya bölüm verisi kullanılmaz.

## Geliştirme durumu
Pawdoku tarzı gizli kedi bulma mekaniğine geçiş yapıldı. Android derlemesi ve otomatik test çalıştırması henüz doğrulanmadı.


## Current implementation

- The game screen is connected to a lifecycle-aware `GameViewModel` using `StateFlow`.
- Puzzle state and actions are kept outside Compose UI.
- The built-in catalog contains 30 generated 6×6 levels. Each level is validated for one cat per row, column and color region, with no touching cats.
- Players can select cells, place/remove pencil marks, reveal a selected cell, use a limited hint, restart, and advance after winning.
- Unit tests cover the catalog and the core game rules.

The 30 catalog entries currently use six rotated solution patterns. They are a starter level set, not 30 individually authored difficulty-balanced puzzles. Persistent progress, real coin/hint economy, full rule-solving hints, and final art assets remain future work.

## Build verification

GitHub Actions is configured to run Android unit tests on pushes and pull requests. A successful workflow run is required before considering the project build-verified.
