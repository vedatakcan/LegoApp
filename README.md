# 🤖 Lego Robotik Eğitim Uygulaması

Bu Android uygulaması, çocukların robotik eğitim sürecini adım adım görselleştirilmiş şekilde öğrenmelerini desteklemek amacıyla geliştirilmiştir. Uygulama, bir robotun oluşturulma adımlarını resimli sunumlar halinde sunar. Kullanıcılar sunumlar arasında gezinebilir, arama yapabilir ve robotların montaj süreçlerini kolayca takip edebilirler.

## 📱 Ekran Görüntüleri

### Başlangıç Ekranı
![Start Screen](screenshots/ic_start_screen.png)

### Sunum Ekleme Ekranı
![Add Images Screen](screenshots/ic_add_images_screen.png)

### Proje Oluşturma Ekranı
![Create Project Screen](screenshots/ic_create_project_screen.png)

### Silme Onay Penceresi
![Delete Dialog Screen](screenshots/ic_delete_dialog_screen.png)

### Proje Arama Ekranı
![Search Screen](screenshots/ic_project_search_screen.png)

### Proje Seçim Ekranı
![Project Selection](screenshots/ic_project_selection.png)

### Adım Adım Görüntüleme Ekranı
![Step by Step Screen](screenshots/ic_step_by_step_screen.png)

### Adım Adım Görüntüleme (Son Adım)
![Step by Step End Screen](screenshots/ic_step_by_step_screen_end.png)


## 🚀 Özellikler

- 🔍 **Sunum Arama:** Proje adıyla hızlıca sunumları bulabilirsiniz.
- 🧩 **Adım Adım Gösterim:** Sunuma ait her bir adımı sırayla gezebilirsiniz.
- 📤 **Sunum Ekleme ve Silme (Admin):** Yalnızca şifre girilerek yapılabilir.
- ☁️ **Firebase Entegrasyonu:**
    - **Firestore** üzerinden sunum verileri,
    - **Firebase Storage** ile görseller saklanır.
- 📦 **MVVM Mimarisi & Repository Pattern:** Temiz ve sürdürülebilir yapı için kullanıldı.

## 🔒 Çocuk Kilidi & Güvenlik Özelliği

Uygulamada çocukların yalnızca sunumları görüntülemesi, ancak sunum **ekleme**, **düzenleme** veya **silme** gibi işlemleri yapamaması için bir güvenlik önlemi mevcuttur.

- 👤 **Yönetici işlemleri** için bir parola gereklidir.
- 🔐 Varsayılan parola: `1984`
- Parola yalnızca yönetimsel işlemler sırasında sorulur.

## 🛠️ Kurulum

1. **Projeyi klonlayın:**
    ```bash
    git clone https://github.com/kullaniciadi/LegoApp.git
    ```

2. **Android Studio ile açın.**

3. **Firebase yapılandırması:**
    - Kendi Firebase projenizi oluşturun.
    - `google-services.json` dosyasını oluşturun ve `app/` klasörünün içine yerleştirin.
    - Bu dosya güvenlik nedeniyle GitHub'a dahil edilmemiştir.

4. **Gradle bağımlılıkları otomatik olarak yüklenecektir.**

## 🧪 Kullanılan Teknolojiler

- Kotlin
- Firebase Firestore & Storage
- MVVM (Model-View-ViewModel)
- Glide (görsel yükleme için)
- Material Design bileşenleri

## 👨‍💻 Geliştirici

**Vedat Akcan**  
Bu proje, staj süreci kapsamında robotik eğitim içeriklerini desteklemek amacıyla geliştirilmiştir.
