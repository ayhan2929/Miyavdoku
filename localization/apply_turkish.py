#!/usr/bin/env python3
import json
import re
import sys
from pathlib import Path

root = Path(sys.argv[1] if len(sys.argv) > 1 else "MeyveKesme")
game_dir = root / "app/src/main/assets/game"

translations = {
    "GameModesKey": "OYUN MODLARI",
    "ArcadeKey": "ARCADE",
    "TrialKey": "DENEME",
    "QueueKey": "SIRALI",
    "InstructionsTitleKey": "Nasıl Oynanır?",
    "GameGoalKey": "OYUNUN AMACI:\nTÜM MALZEMELERİ KES!",
    "Instructions_textKey": "SINIRSIZ ARCADE MODUNDA SINIRLARINI ZORLA. PARLAYAN BONUS MALZEMELERİ KAÇIRMA!\n\nDENEME MODUNDA 3 MALZEME DÜŞÜRÜNCEYE KADAR OYUN DEVAM EDER. 3 MALZEME DÜŞÜRÜRSEN OYUN BİTER.\n\nSIRALI MODDA YALNIZCA BANTIN EN SOLUNDA BULUNAN MALZEMELERİ KES.\n\nALTIN KUPAYI KAZANABİLECEK MİSİN?\nAMA UNUTMA: NE OLURSA OLSUN BOMBAYI KESME!",
    "Controls_textKey": "KONTROLLER:\nHer malzemeyi parmağınla üzerinden kaydırarak kes!",
    "Controls_pc_textKey": "KONTROLLER:\nSol fare tuşunu basılı tut ve her malzemeyi kesmek için üzerinden kaydır!",
    "PlayKey": "OYNA",
    "BestScoreKey": "EN İYİ SKOR",
    "NextTargetKey": "SONRAKİ HEDEF",
    "ArcadeModeInfoKey": "HİÇBİR BOMBAYA ÇARPMA!",
    "TrialModeInfoKey": "3 MALZEMENİN DÜŞMESİNE İZİN VERME!",
    "QueueModeInfoKey": "BANTTAKİ MALZEMELERİ SIRAYLA KES",
    "PauseKey": "DURAKLAT",
    "ContinueKey": "DEVAM ET",
    "QuitToMenuKey": "ANA MENÜYE DÖN",
    "GameOverKey": "OYUN BİTTİ",
    "NewBestScoreKey": "YENİ REKOR!",
    "RetryKey": "TEKRAR DENE",
    "ScoreKey": "SKOR",
    "FreedomKey": "SERBESTLİK",
    "DoubleKey": "İKİ KAT",
    "FreezeKey": "DONDUR",
    "NewLiveKey": "EK CAN"
}

lang_file = game_dir / "lang.json"
data = json.loads(lang_file.read_text(encoding="utf-8"))
items = data["stringresources"]["strings"]["string"]
for item in items:
    key = item["-id"]
    if key not in translations:
        raise RuntimeError("Çevirisi eksik oyun metni: " + key)
    item["tr"] = translations[key]
lang_file.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

game_file = game_dir / "game.js"
js = game_file.read_text(encoding="utf-8")
js = js.replace('var da = "en de es fr it br ru".split(" "),',
                'var da = "en de es fr it br ru tr".split(" "),')
start = js.find('            var a = navigator.xa || navigator.language,')
end_marker = '            w = da.indexOf(z);'
end = js.find(end_marker, start)
if start < 0 or end < 0:
    raise RuntimeError("Oyun dili seçimi beklenen biçimde bulunamadı.")
js = js[:start] + '            z = "tr";\n' + end_marker + js[end + len(end_marker):]
game_file.write_text(js, encoding="utf-8")

strings_file = root / "app/src/main/res/values/strings.xml"
xml = strings_file.read_text(encoding="utf-8")
replacements = {
    "GAME NAME": "Meyve Kesme",
    "Privacy Policy": "Gizlilik Politikası",
    "Try this new game": "Bu eğlenceli meyve kesme oyununu dene!",
    "Are you sure you want to quit?": "Oyundan çıkmak istediğine emin misin?",
    ">Exit<": ">Çıkış<",
    ">Cancel<": ">İptal<",
    "No Internet Connection": "İnternet bağlantısı yok",
    ">Retry<": ">Tekrar dene<"
}
for old, new in replacements.items():
    xml = xml.replace(old, new)
strings_file.write_text(xml, encoding="utf-8")

privacy = """<br><br>
<p><strong>Gizliliğinizi korumayı taahhüt ediyoruz.</strong></p>
<p>Size tatmin edici bir hizmet sunmak için gerekli olan asgari miktarda bilgiyi topluyoruz. Bu politika, hakkınızda veri toplanmasına neden olabilecek süreçlerin türlerini açıklar. Bu oyunu kullanmanız, söz konusu bilgilerin toplanmasına izin verdiğiniz anlamına gelir.</p>
<p><strong>Toplanan Bilgiler</strong></p>
<p>Gerçekleştirdiğiniz işlemin türüne bağlı olarak adınız, e-posta adresiniz ve oyunu kullanımınıza ilişkin veriler dâhil olmak üzere bize verdiğiniz bilgilerin bir kısmını veya tamamını toplayabiliriz. Bir isteği işlemek için zaman zaman gerekebilecek diğer bilgiler de oyunda belirtildiği şekilde toplanabilir.</p>
<p>Uygulama ücretsizse reklam gösterebiliriz. Bu süreçte AAID, ROM sürümü, EMUI sürümü, ekran yüksekliği, ekran genişliği, uygulama paket adı, uygulama sürümü ve analiz SDK'sının adı gibi bazı veriler işlenebilir.</p>
<p><strong>Bilgilerin Kullanımı</strong></p>
<p>Toplanan bilgileri öncelikle oyunu kullanma amacınızı yerine getirmek için kullanırız. Toplanan veriler, geçerli veri koruma mevzuatına uygun şekilde saklanır. Bu bilgilere yetkisiz erişimi önlemek için makul tüm tedbirler alınır. Hesap bilgileriniz hakkında bilgi edinmek istediğinizde kimliğinizi doğrulamak için ek bilgiler sağlamanız gerekebilir.</p>
<p><strong>Orijinal Geliştirici</strong></p>
<p>Bu oyun, Gradle Code, Entity (JACOB SERVICES LLC) tarafından oluşturulmuş özel mülkiyete tabi bir koddur. Kodun dağıtımında bu bölüm korunmalı ve değiştirilmemelidir. Bu bölümün gizlilik metninden kaldırılması YASAL DEĞİLDİR. JACOB SERVICES, dağıtımcıyla olan iş birliğini bildirme ve askıya alma haklarını saklı tutar.</p>
<p><strong>Bilgilerin Paylaşılması</strong></p>
<p>Kayıt veya yarışma formlarında ilgili kutuları işaretleyerek izin vermediğiniz sürece, bu oyundan elde ettiğimiz kişisel bilgilerinizi üçüncü taraflarla paylaşmayız. Bilgileri sizinle iletişim kurmak ve bizimle ilgili gelişmelerden haberdar etmek için de kullanabiliriz. Herhangi bir e-posta listesinden veya benzer iletişim kanalından ayrılma fırsatı sunulur. Gelecekte bu web sitesinde toplanan bilgileri üçüncü bir tarafla paylaşmak istersek bunu yalnızca bilginiz ve onayınız dâhilinde yaparız.</p>
<p>Zaman zaman oyunumuzu ziyaret eden kişi sayısı gibi genel nitelikteki bilgileri üçüncü taraflara sağlayabiliriz. Ancak bu kişilerin kimliğini belirlemeye yarayabilecek bilgileri kullanmayız.</p>
<p>Ayrıca Dummy, Dummy oyununda ilgi alanlarına dayalı hedefli reklamlar sunmak amacıyla üçüncü taraflarla çalışabilir. Oyunumuzu kullanımınıza ilişkin anonim bilgiler, ilginizi çekebilecek hizmetlerle ilgili daha uygun reklamlar sunmak için kullanılabilir.</p>
<p><strong>Bu Politikadaki Değişiklikler</strong></p>
<p>Gizlilik Politikamızda yapılacak değişiklikler burada yayımlanır ve politikanın önceki sürümünün yerini alır. Değişikliklere dikkatinizi çekmek için makul adımlar atarız. Bununla birlikte, politikanın onayınıza uygun olmaya devam ettiğinden emin olmak için oyunu her kullandığınızda bu belgeyi okumanızı öneririz.</p>
<p><strong>Bizimle İletişime Geçin</strong></p>
<p>Gizlilik Politikamız hakkında sorularınız varsa veya hakkınızda hangi bilgileri topladığımızı öğrenmek istiyorsanız aşağıdaki e-posta adresinden bize ulaşabilirsiniz:</p>
"""
(game_dir / "privarcypolicy.txt").write_text(privacy, encoding="utf-8")

# Reklamları tamamen devre dışı bırak ve banner alanını yerleşimden kaldır.
layout_file = root / "app/src/main/res/layout/activity_main.xml"
layout = layout_file.read_text(encoding="utf-8")
layout = re.sub(
    r'\\s*<com\\.google\\.android\\.gms\\.ads\\.AdView\\b.*?</com\\.google\\.android\\.gms\\.ads\\.AdView>',
    '',
    layout,
    flags=re.DOTALL
)
layout_file.write_text(layout, encoding="utf-8")

# Reklam SDK'sının başlatılmasını ve reklam kimliği iznini kaldır.
manifest_file = root / "app/src/main/AndroidManifest.xml"
manifest = manifest_file.read_text(encoding="utf-8")
manifest = re.sub(r'\\s*<uses-permission android:name="com\\.google\\.android\\.gms\\.permission\\.AD_ID"\\s*/>', '', manifest)
manifest = re.sub(r'\\s*<meta-data\\s+android:name="com\\.google\\.android\\.gms\\.ads\\.DELAY_APP_MEASUREMENT_INIT".*?/>', '', manifest, flags=re.DOTALL)
manifest = re.sub(r'\\s*<meta-data\\s+android:name="com\\.google\\.android\\.gms\\.ads\\.APPLICATION_ID".*?/>', '', manifest, flags=re.DOTALL)
manifest_file.write_text(manifest, encoding="utf-8")

# Banner, geçiş ve ödüllü reklam anahtarlarını kapat.
resource_file = root / "app/src/main/res/values/strings.xml"
resources = resource_file.read_text(encoding="utf-8")
for key in ("enable_banner", "enable_inter", "enable_reward"):
    resources = re.sub(
        r'(<bool\\s+name="' + key + r'">)\\s*true\\s*(</bool>)',
        r'\\1false\\2',
        resources
    )
resource_file.write_text(resources, encoding="utf-8")

# Oyun modları arasında reklam çağrısı yapılmasını da engelle.
hooks_file = game_dir / "hooks.js"
hooks = hooks_file.read_text(encoding="utf-8")
hooks = hooks.replace("gradle.showInter();", "// Reklamlar kapalı.")
hooks_file.write_text(hooks, encoding="utf-8")

print("Türkçe yerelleştirme uygulandı: 26 oyun metni ve reklam kaldırma, Android sistem metinleri ve gizlilik bildirimi.")
