# 🌦️ 위치 기반 스마트 날씨 & CCTV 앱 (Capstone Design)

사용자의 실시간 위치(GPS)를 기반으로 정확한 **날씨 정보**, 체감 온도에 따른 **옷차림 추천**, 그리고 주변 교통 상황을 확인할 수 있는 **CCTV 스트리밍** 기능을 제공하는 안드로이드 애플리케이션입니다.

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-7F52FF?style=flat&logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material-4285F4?style=flat&logo=android)
![Hilt](https://img.shields.io/badge/DI-Hilt-2E7D32?style=flat)
![Retrofit](https://img.shields.io/badge/Network-Retrofit2-square?style=flat)

---

## ✨ 주요 기능 (Key Features)

*   **📍 실시간 위치 기반 날씨:** GPS를 추적하여 현재 위치의 정확한 날씨(기온, 강수확률, 미세먼지 등)를 제공합니다.
*   **👕 스마트 옷차림 추천:** 단순 기온이 아닌 '체감 온도'와 사용자별 '더위/추위 타는 정도' 설정(Preference)을 반영하여 맞춤형 옷차림을 추천합니다.
*   **📹 주변 CCTV 영상:** 현재 위치 근처의 고속도로 및 국도 CCTV 영상을 실시간 스트리밍으로 확인할 수 있습니다.
*   **📉 어제 날씨 비교:** 어제 동시간대 기온과 비교하여 "어제보다 2도 높아요"와 같은 직관적인 정보를 제공합니다.
*   **🔄 부드러운 UX:** 데이터 로딩 시 깜빡임을 방지하는 **Minimum Loading Time** 로직과 **Crossfade 애니메이션**이 적용되어 있습니다.

---

## 🛠️ 기술 스택 (Tech Stack)

이 프로젝트는 최신 안드로이드 권장 아키텍처와 라이브러리를 사용하여 개발되었습니다.

| 분류 | 기술 및 라이브러리 | 사용 목적 |
| --- | --- | --- |
| **Language** | Kotlin | 100% Kotlin 기반 개발 |
| **UI** | **Jetpack Compose** | 선언형 UI 프레임워크로 직관적이고 재사용 가능한 UI 컴포넌트 구현 |
| **Architecture** | **MVVM, Clean Architecture** | UI와 비즈니스 로직 분리, 유지보수성 향상 |
| **DI** | **Hilt** | 의존성 주입을 통한 결합도 감소 및 테스트 용이성 확보 |
| **Async** | **Coroutines & Flow** | 비동기 네트워크 통신 및 반응형 데이터 스트림 처리 |
| **Network** | Retrofit2, OkHttp3, Gson | REST API 통신 및 데이터 파싱 |
| **Local DB** | **Room** | 오프라인 캐싱을 통해 네트워크가 없을 때도 최근 데이터 표시 |
| **Video** | **Media3 (ExoPlayer)** | HLS 기반의 CCTV 영상 스트리밍 재생 |
| **Image** | Coil | 비동기 이미지 로딩 및 메모리 캐싱 |

---

## 🏗️ 아키텍처 및 구조 (Architecture)

**Google 권장 앱 아키텍처**를 준수하며, 단방향 데이터 흐름(UDF)을 따릅니다.

```mermaid
graph LR
    UI[Compose UI] -->|Event| VM[ViewModel]
    VM -->|StateFlow| UI
    VM -->|Request| Repo[Repository]
    Repo -->|Fetch| Remote[Retrofit (Remote)]
    Repo -->|CRUD| Local[Room DB (Local)]
    Remote -->|Response| Repo
    Local -->|Data| Repo
```

### 디렉토리 구조
```text
com.example.weatherproject
├── data            # 데이터 계층 (Repository, Room Entity, API Response)
├── di              # Hilt 의존성 주입 모듈
├── network         # Retrofit API 인터페이스
├── ui              # 프레젠테이션 계층
│   ├── components  # 재사용 가능한 Composable (WeatherCard, CCTVCard 등)
│   ├── screens     # 화면 단위 (HomeScreen, DetailScreen 등)
│   ├── theme       # 앱 테마 및 스타일
│   └── MainViewModel.kt
├── util            # 유틸리티 (위치 권한, 날씨 아이콘 매핑 등)
└── worker          # 백그라운드 작업 (WorkManager)
```

---

## 🚀 최근 개선 사항 (Refactoring Log)

캡스톤 디자인의 완성도를 높이기 위해 다음과 같은 엔지니어링 최적화를 수행했습니다.

1.  **UX/Performance 최적화:**
    *   **Fake Delay 제거 & 최소 로딩 시간 보장:** 기존의 고정 딜레이(1.5초)를 제거하고, 데이터 로드가 빠를 때는 즉시, 느릴 때는 최소 시간만 기다리도록 `Smart Loading Logic` 구현.
    *   **Crossfade 애니메이션:** 날씨 데이터 갱신 시 화면이 뚝뚝 끊기지 않고 부드럽게 전환되도록 애니메이션 적용.
2.  **네트워크 에러 핸들링 강화:**
    *   단순한 "에러 발생" 메시지 대신, `UnknownHostException`(인터넷 끊김), `SocketTimeoutException`(서버 지연) 등을 구분하여 사용자에게 명확한 가이드 제공.
3.  **코드 모듈화:**
    *   `libs.versions.toml` (Version Catalog)을 도입하여 프로젝트 전체의 의존성 버전을 중앙에서 관리.

---

## ⚙️ 실행 방법 (Getting Started)

1. **프로젝트 클론:**
   ```bash
   git clone https://github.com/your-username/WeatherApp.git
   ```
2. **API Key 설정:**
   * `local.properties` 파일에 다음 키를 추가해야 정상 작동합니다.
   * (사용하는 API 서비스명을 적어주세요, 예: 공공데이터포털)
   ```properties
   WEATHER_API_KEY=your_api_key_here
   ```
3. **빌드 및 실행:**
   * Android Studio Koala 이상 권장.
   * JDK 17 설정 확인.

---

## 📞 Contact
* **Developer:** [본인 이름/팀명]
* **Email:** [이메일 주소]
