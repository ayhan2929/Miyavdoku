# MiyavDoku

Android için renkli, kedi temalı 9×9 blok bulmaca oyunu.

## Teknik kararlar
- Kotlin ve Jetpack Compose
- Oyun kuralları için Android'den bağımsız saf Kotlin
- Değişmez (immutable) oyun durumu
- Jetpack Compose ekranında oyun durumu yönetimi
- JUnit ile oyun motoru birim testleri
- Room ve DataStore sonraki veri katmanı aşamasında eklenecek

## Oyun kuralları
- 9×9 oyun tahtası
- Her elde birbirinden farklı 3 şekil
- Şekiller tahtadaki boş hücrelere yerleştirilir
- Tamamlanan satırlar, sütunlar ve 3×3 bölgeler temizlenir
- Her hamlede şekil hücreleri kadar temel puan kazanılır
- Temizlenen hücreler ve tamamlanan gruplar ek puan kazandırır
- Arka arkaya temizleme hamleleri kombo bonusu verir
- Eldeki üç şekil kullanıldığında yeni üç şekil dağıtılır
- Hiçbir eldeki şekil tahtaya yerleşemiyorsa oyun sona erer
- En iyi puan yeni oyun başlatıldığında korunur

## Oyun motoru
Motor `app/src/main/java/com/miyavdoku/domain/GameEngine.kt` dosyasındadır.
Android veya Compose bağımlılığı yoktur. Hamleler yeni bir `GameState` üretir; geçersiz hamleler mevcut durumu değiştirmez.

## Testler
`app/src/test/java/com/miyavdoku/domain/GameEngineTest.kt` dosyasında tahta sınırları, geçersiz hamleler, satır ve 3×3 temizleme, şekil tüketimi, yeni şekil dağıtımı ve oyun sonu durumları için testler bulunur.

## Geliştirme durumu
Oyun motorunun ilk kural seti ve ekran bağlantısı GitHub'a eklendi. Android derlemesi ve otomatik test çalıştırması henüz doğrulanmadı.
