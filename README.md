# Evora Sağlık — Adım Sayar & Sağlık Takip Uygulaması

Android (Kotlin) için geliştirilmiş, tamamen **internetsiz** çalışan adım sayar ve su takip uygulaması.

## Neden native Kotlin (HTML-to-APK değil)?

İstediğin özelliklerin bir kısmı (arka planda sürekli adım sayma, ana ekran widget'ı,
zamanlanmış bildirimler) donanım sensörlerine ve Android sistem API'lerine doğrudan erişim
gerektirir. HTML-to-APK sarmalayıcılar (WebView tabanlı) bu özellikleri güvenilir şekilde
desteklemez — özellikle **widget ekleme mümkün değildir** ve arka planda sensör dinleme çok
kısıtlıdır. Bu yüzden proje tamamen native Kotlin/Android olarak hazırlandı. GitHub'a
yükleyip Android Studio ile açman yeterli.

## Kurulum

1. [Android Studio](https://developer.android.com/studio) kur (Hedgehog veya üzeri önerilir).
2. Bu klasörü (`EvoraSaglik`) GitHub'a yükle veya doğrudan Android Studio'da
   **File > Open** ile aç.
3. Gradle senkronizasyonunun bitmesini bekle (ilk seferde bağımlılıklar indirilir,
   internet gerekir — sadece derleme için, uygulamanın çalışması için değil).
4. Bir Android cihaz/emülatör seç ve **Run ▶** tuşuna bas.
5. Gerçek cihazda test etmen önerilir çünkü adım sayar sensörü çoğu emülatörde yoktur.

## Uygulama İzinleri

İlk açılışta şu izinler istenir:
- `ACTIVITY_RECOGNITION` — adım sayar sensörünü kullanabilmek için (Android 10+ zorunlu).
- `POST_NOTIFICATIONS` — su hatırlatma bildirimleri için (Android 13+ zorunlu).

Kullanıcı izin vermezse adım sayısı 0'da kalır; su takibi ve manuel özellikler yine çalışır.

## Özellik — Kod Eşleştirmesi

| Özellik | Dosya |
|---|---|
| Yaş/kilo girişi, güncelleme, local kayıt | `ui/ProfileFragment.kt`, `data/PrefsManager.kt` |
| Adım sayar sensörü, arka plan takip | `service/StepCounterService.kt` |
| İdeal adım/su hedefi, mesafe, kalori formülleri | `util/HealthCalculator.kt` |
| Su +100 ml butonu, hedefe kalan, sıfırlama | `ui/WaterFragment.kt` |
| Günlük sıfırlama (gece yarısı + her sensör olayında) | `receiver/DailyResetReceiver.kt`, `PrefsManager.resetIfNewDay()` |
| Telefon yeniden başlayınca servisi ayağa kaldırma | `receiver/BootReceiver.kt` |
| Su içme hatırlatma bildirimi (periyodik) | `worker/WaterReminderWorker.kt` |
| Ana ekran widget'ı (adım + su özeti) | `widget/EvoraWidgetProvider.kt`, `res/layout/widget_evora.xml` |
| İlerleme çubukları | Her sekmenin layout dosyası (`ProgressBar`) |
| Alt sekmeler (Ana Sayfa/Adım/Su/Profil) | `MainActivity.kt`, `ui/*Fragment.kt`, `res/menu/bottom_nav_menu.xml` |

## Hesaplama Formülleri (basitleştirilmiş, tıbbi tavsiye değildir)

- **İdeal adım hedefi:** 10.000 taban, yaşa göre ±1000–2500, kiloya göre ±500–1000 ayar (bkz. `HealthCalculator.idealDailySteps`).
- **Su hedefi:** kilo (kg) × 35 ml, yaş düzeltmesiyle.
- **Mesafe:** adım × 0.762 m (ortalama adım uzunluğu) / 1000.
- **Kalori:** adım × (0.04 kcal × kilo/70).

Bu sabitleri `HealthCalculator.kt` içinden kolayca değiştirebilirsin.

## Widget Ekleme

Uygulamayı cihaza kurduktan sonra ana ekranda boş bir alana uzun bas → **Widget'lar** →
**Evora Sağlık** widget'ını sürükle bırak. Widget, adım/su verileri her değiştiğinde
(servis her sensör güncellemesinde) otomatik güncellenir; ayrıca sistem en fazla 30
dakikada bir de zorunlu günceller (Android widget kısıtlaması).

## Sekmeler (Bottom Navigation)

Uygulama artık tek ekran değil, 4 sekmeli bir yapıya sahip (`BottomNavigationView`):

| Sekme | İçerik | Dosyalar |
|---|---|---|
| 🏠 Ana Sayfa | Logo, günlük durum mesajı, adım/su/mesafe/kalori özeti | `ui/HomeFragment.kt`, `fragment_home.xml` |
| 🚶 Adım | Büyük adım sayacı, hedef, ilerleme çubuğu, mesafe/kalori | `ui/StepsFragment.kt`, `fragment_steps.xml` |
| 💧 Su | Büyük su takibi, +100 ml butonu, sıfırlama, hedefe kalan | `ui/WaterFragment.kt`, `fragment_water.xml` |
| 👤 Profil | Yaş/kilo girişi-güncelleme, hesaplanan hedefler, uygulama hakkında | `ui/ProfileFragment.kt`, `fragment_profile.xml` |

Tüm sekmeler `ui/BaseFragment.kt` üzerinden adım güncelleme yayınını (broadcast) dinler,
böylece hangi sekmede olursan ol veriler anlık güncellenir.

## Logo

Uygulama logon (`logo_evora.png`) hem uygulama içi Ana Sayfa/Profil ekranlarında hem de
launcher (uygulama) ikonunda kullanılıyor:
- Legacy ikon: `mipmap-*/ic_launcher.png` ve `ic_launcher_round.png` (tüm yoğunluklar için üretildi).
- Adaptive ikon (Android 8+): `mipmap-anydpi-v26/ic_launcher.xml` → beyaz arkaplan + `drawable-*/ic_launcher_foreground.png`.

Logoyu değiştirmek istersen, orijinal görseli aynı işlemden geçirip (kare + şeffaf arkaplan)
ilgili klasörlere tekrar koyman yeterli.

## Bilinen Sınırlamalar / Geliştirme Önerileri

- Launcher icon şu an basit bir vektör; Android Studio'nun **Image Asset** aracıyla
  (sağ tık `res` > New > Image Asset) kendi logonu oluşturman önerilir.
- `TYPE_STEP_COUNTER` sensörü olmayan çok eski/düşük bütçeli bazı cihazlarda adım
  sayımı çalışmaz; bu cihazlarda uygulama diğer özellikleriyle (su takibi, profil) çalışmaya devam eder.
- Su hatırlatma sıklığı `WaterReminderWorker.schedule(this, intervalHours = 2)` satırından
  değiştirilebilir.
