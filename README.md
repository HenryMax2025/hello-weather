# hello天气

一个美丽的 Android 天气应用，采用毛玻璃/液体玻璃 UI 设计。

## 功能特点

- 🌤️ 实时天气查询
- 📅 7天天气预报
- 🕐 小时级预报
- 🏙️ 支持多个城市切换（北/上/广/深/杭/成/武/西）
- 🎨 毛玻璃/液体玻璃界面设计
- 🌈 根据天气动态变换背景

## 技术栈

- Kotlin
- Jetpack Compose
- Material Design 3
- Retrofit + OkHttp
- Open-Meteo API（免费天气数据）
- MVVM + Clean Architecture

## 截图

![Screenshot](screenshots/screenshot.png)

## 编译

```bash
./gradlew assembleDebug
```

APK位置：`app/build/outputs/apk/debug/app-debug.apk`

## 数据来源

天气数据由 [Open-Meteo](https://open-meteo.com/) 提供，无需API密钥。

## 开源协议

MIT License