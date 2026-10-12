# Ứng dụng Android Midterm AI

Đây là đồ án ứng dụng Android giữa kỳ, tích hợp các tính năng quản lý công việc (Task), lịch (Calendar), thời tiết (Weather), phát nhạc (Music Player) và các tiện ích màn hình chính (Home Screen Widgets).

## 🚀 Các Tính Năng Chính

- **Quản lý công việc (Task Management)**: Thêm, xem, cập nhật và xóa công việc sử dụng Room Database và Repository Pattern.
- **Lịch & Sự kiện**: Quản lý lịch trình và nhắc nhở cá nhân.
- **Thời tiết**: Tích hợp dữ liệu thời tiết thời gian thực qua Retrofit và Coroutines.
- **Trình phát nhạc (Music Player)**: Quản lý và điều khiển phát nhạc ngay trong ứng dụng.
- **Tiện ích màn hình chính (App Widgets)**: Các widget tương tác trực tiếp ngoài màn hình chính bao gồm:
  - Widget Công việc (Task Widget)
  - Widget Lịch (Calendar Widget)
  - Widget Thời tiết (Weather Widget)
  - Widget Phát nhạc (Music Widget)
  - Widget Đồng hồ (Clock Widget)
- **Kiểm thử (Testing Pyramid)**:
  - Unit Test (chạy trên máy tính): `TaskRepositoryTest`, `TaskRepositoryImplTest`
  - Integration Test (Room in-memory, chạy trên emulator): `TaskDaoIntegrationTest`
  - UI Test (Espresso, chạy trên emulator): `TaskActivityEspressoTest`, `TaskUiFlowEspressoTest`
  - Chạy: `./gradlew testDebugUnitTest` và `./gradlew connectedDebugAndroidTest`
- **Widget To-Do tương tác**: chạm vào một công việc trên widget để đánh dấu Xong / Chưa xong.

## 🛠️ Công Nghệ Sử Dụng

- **Ngôn ngữ**: Kotlin
- **Kiến trúc**: MVVM / Repository Pattern
- **Cơ sở dữ liệu cục bộ**: Room Persistence Library
- **Mạng**: Retrofit & Kotlin Coroutines
- **Giao diện & Widgets**: Android Views, Material Design, App Widgets (`RemoteViews`)
- **Kiểm thử**: JUnit, Espresso, AndroidX Test

## 📱 Cấu Trúc Dự Án

```
com.example.midterm_ai/
├── data/           # Cơ sở dữ liệu Room, Entities, DAOs, Repositories, Weather API
├── music/          # Quản lý trình phát nhạc (Music Player Manager)
├── ui/             # Activities, Adapters, Themes (Giao diện người dùng)
└── widget/         # Các Widget màn hình chính & RemoteViews Services
```

## ⚙️ Hướng Dẫn Chạy Dự Án

1. Clone repository về máy:
   ```bash
   git clone https://github.com/giakhiem090206-tech/Midterm-Androii.git
   ```
2. Mở thư mục dự án trong **Android Studio**.
3. Chờ Gradle đồng bộ xong và chạy ứng dụng trên máy ảo (Emulator) hoặc thiết bị thật.

---
Được phát triển cho Đồ án Giữa kỳ.
