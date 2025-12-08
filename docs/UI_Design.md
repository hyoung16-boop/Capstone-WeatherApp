# Capstone WeatherApp 화면/UX 설계서

본 문서는 현재 리포지토리의 Jetpack Compose 기반 안드로이드 날씨 애플리케이션을 코드 수준에서 분석하여 작성한 화면 설계 및 UI 스펙입니다.

## 글로벌 구조
- **네비게이션 맵**: `WeatherNavHost`가 홈(`home`)을 시작 화면으로 하고, 상세 날씨(`detail`), 예보(`forecast`), 설정(`settings`), CCTV 목록(`cctv`), CCTV 재생(`cctvPlayer/{cctvName}/{cctvUrl}`), 알림 목록(`alarm_list`), 알림 수정(`alarm_edit?alarmId={alarmId}`) 목적지를 선언합니다.【F:app/src/main/java/com/example/weatherproject/ui/Navigation.kt†L18-L103】
- **배경/테마**: `MainActivity`에서 날씨 아이콘에 따라 그라데이션 색을 동적으로 선택해 전체 배경으로 적용합니다.【F:app/src/main/java/com/example/weatherproject/MainActivity.kt†L118-L199】
- **상단 검색 & 빠른 액션**: 공통 상단바 `WeatherTopAppBar`는 도시 검색 필드, 현재 위치 새로고침, 알림 진입 아이콘을 포함하며 검색 결과 드롭다운으로 도시 선택 시 날씨·CCTV 위치를 모두 갱신합니다.【F:app/src/main/java/com/example/weatherproject/ui/components/WeatherTopAppBar.kt†L31-L116】

### 메뉴 트리 / 스크린 리스트
- **루트** `MainActivity` → `WeatherNavHost`로 Compose 네비게이션을 초기화.【F:app/src/main/java/com/example/weatherproject/MainActivity.kt†L86-L111】【F:app/src/main/java/com/example/weatherproject/ui/Navigation.kt†L18-L103】
- **홈(HomeScreen)**: 기본 진입, 날씨 요약·예보·옷차림·주변 CCTV 카드.
- **상세 날씨(DetailWeatherScreen)**: 홈의 현재 날씨 카드 확장 뷰.【F:app/src/main/java/com/example/weatherproject/ui/screens/DetailWeatherScreen.kt†L18-L43】
- **예보(ForecastScreen)**: 시간/주간 예보 집중 노출.【F:app/src/main/java/com/example/weatherproject/ui/screens/ForecastScreen.kt†L18-L45】
- **CCTV 목록(CctvScreen)** → **CCTV 재생(CctvPlayerScreen)** 흐름.【F:app/src/main/java/com/example/weatherproject/ui/screens/CctvScreen.kt†L67-L339】【F:app/src/main/java/com/example/weatherproject/ui/screens/CctvPlayerScreen.kt†L24-L134】
- **설정(SettingsScreen)** → **알림 목록(AlarmListScreen)** → **알림 편집(AlarmEditScreen)** 단계형 흐름.【F:app/src/main/java/com/example/weatherproject/ui/SettingsScreen.kt†L6-L36】【F:app/src/main/java/com/example/weatherproject/ui/AlarmScreens.kt†L107-L520】

### 데이터 흐름도(텍스트)
1. **위치 획득**: `LocationPermissionHelper`로 권한/ GPS 확인 → `MainViewModel.getCurrentLocationOnce` 또는 `startLocationTracking`에서 FusedLocationClient로 좌표 획득 후 `Geocoder`로 주소 변환.【F:app/src/main/java/com/example/weatherproject/util/LocationPermissionHelper.kt†L16-L50】【F:app/src/main/java/com/example/weatherproject/ui/MainViewModel.kt†L71-L158】
2. **좌표 → 격자 변환 & API 호출**: `GpsTransfer.convertToGrid`로 nx/ny 계산 → `RetrofitClient.weatherApi`가 현재/시간/주간 날씨 및 주변 CCTV를 요청해 응답 데이터를 도메인 모델(`WeatherState`, `CctvInfo`)로 매핑.【F:app/src/main/java/com/example/weatherproject/util/GpsTransfer.kt†L6-L92】【F:app/src/main/java/com/example/weatherproject/network/WeatherApiService.kt†L11-L39】【F:app/src/main/java/com/example/weatherproject/ui/MainViewModel.kt†L159-L276】【F:app/src/main/java/com/example/weatherproject/ui/CctvViewModel.kt†L45-L93】
3. **상태 업데이트 & 캐싱**: ViewModel이 `MutableStateFlow`를 통해 UI 상태를 갱신하고 `PreferenceManager`로 날씨/보정값을 캐시, 캐시된 상태는 앱 재실행 시 즉시 로드.【F:app/src/main/java/com/example/weatherproject/ui/MainViewModel.kt†L29-L70】【F:app/src/main/java/com/example/weatherproject/util/PreferenceManager.kt†L14-L97】
4. **UI 렌더링**: 각 Screen/카드 컴포넌트가 상태 플로우를 수집하여 Compose UI를 그리며, 사용자 입력(검색/토글/클릭)은 다시 ViewModel 액션(날씨 조회, CCTV 재검색, 알림 CRUD)으로 전달되어 순환 구조 형성.【F:app/src/main/java/com/example/weatherproject/ui/screens/HomeScreen.kt†L52-L168】【F:app/src/main/java/com/example/weatherproject/ui/screens/CctvScreen.kt†L97-L339】【F:app/src/main/java/com/example/weatherproject/ui/AlarmScreens.kt†L125-L520】
5. **백그라운드 갱신**: `WeatherUpdateWorker`가 캐시된 좌표 기반으로 주기적 날씨/예보를 가져와 스마트 알림을 발송하고, 향후 캐시 업데이트 지점을 명시적으로 분리.【F:app/src/main/java/com/example/weatherproject/worker/WeatherUpdateWorker.kt†L15-L82】

### 정책/운영 규칙
- **데이터 캐싱**: `PreferenceManager`가 최신 날씨 상태와 체감 온도 보정값을 SharedPreferences에 저장하여 오프라인/재시작 시 초기 렌더 속도를 확보합니다.【F:app/src/main/java/com/example/weatherproject/util/PreferenceManager.kt†L14-L97】【F:app/src/main/java/com/example/weatherproject/ui/MainViewModel.kt†L29-L70】
- **위치 정책**: 위치 권한이 없거나 GPS 비활성 시 UI에 안내 문구를 띄우고, GPS 활성화를 유도하는 다이얼로그를 제공합니다.【F:app/src/main/java/com/example/weatherproject/ui/MainViewModel.kt†L93-L118】【F:app/src/main/java/com/example/weatherproject/util/LocationPermissionHelper.kt†L29-L50】
- **알림 정책**: 알림 추가/수정 시 정확한 알람 권한이 필요한 경우 권한 안내 바를 노출하며, `AlarmScheduler`가 반복/단일 알람을 설정하고 `AlarmReceiver`에서 트리거 처리합니다.【F:app/src/main/java/com/example/weatherproject/ui/AlarmScreens.kt†L107-L182】【F:app/src/main/java/com/example/weatherproject/util/AlarmScheduler.kt†L9-L133】【F:app/src/main/java/com/example/weatherproject/receiver/AlarmReceiver.kt†L6-L78】
- **네트워크 정책**: 모든 API는 `RetrofitClient`를 통해 BASE_URL(`http://www.weapi.shop/`)로 호출하며, 로깅 인터셉터/타임아웃을 설정해 실패 시 UI 오류 메시지를 분기합니다.【F:app/src/main/java/com/example/weatherproject/network/RetrofitClient.kt†L10-L33】【F:app/src/main/java/com/example/weatherproject/ui/CctvViewModel.kt†L73-L101】【F:app/src/main/java/com/example/weatherproject/ui/MainViewModel.kt†L159-L238】
- **보정 정책**: 최초 실행 시 체감 온도 보정 다이얼로그를 노출하고 사용자가 저장한 보정값을 날씨 카드·알림 메시지·옷차림 추천에 반영합니다.【F:app/src/main/java/com/example/weatherproject/ui/screens/HomeScreen.kt†L43-L145】【F:app/src/main/java/com/example/weatherproject/worker/WeatherUpdateWorker.kt†L33-L70】

### 권한 설정
- **정적 선언(Manifest)**: 인터넷, 정밀/대략 위치, 알림(POST_NOTIFICATIONS), 전체 화면 인텐트, 정확한 알람(SCHEDULE_EXACT_ALARM)을 선언합니다.【F:app/src/main/AndroidManifest.xml†L3-L37】
- **런타임 요청**:
  - 위치: `LocationPermissionHelper.requestLocationPermission`에서 FINE/COARSE 동시 요청, 미보유 시 홈/상단바 동작이 위치 없이 제한됩니다.【F:app/src/main/java/com/example/weatherproject/util/LocationPermissionHelper.kt†L31-L38】【F:app/src/main/java/com/example/weatherproject/ui/MainViewModel.kt†L93-L118】
  - 알림/정확한 알람: 알림 추가 흐름에서 권한 체크 후 편집 화면으로 이동하며, 미허용 시 하단 권한 안내 바를 노출합니다.【F:app/src/main/java/com/example/weatherproject/ui/AlarmScreens.kt†L107-L182】

### 시스템 검증 포인트
- **권한 검증**: 앱 시작/알림 추가 시 권한 상태를 분기하여 UI 안내 또는 기능 실행을 결정합니다.【F:app/src/main/java/com/example/weatherproject/ui/MainActivity.kt†L86-L155】【F:app/src/main/java/com/example/weatherproject/ui/AlarmScreens.kt†L107-L182】
- **위치/네트워크 실패 복원**: 위치 미확보 시 안내 문구, API 오류 시 오류 토스트/텍스트를 분기하며, CCTV·날씨 API 호출은 예외 처리 후 로딩 상태를 해제합니다.【F:app/src/main/java/com/example/weatherproject/ui/MainViewModel.kt†L179-L238】【F:app/src/main/java/com/example/weatherproject/ui/CctvViewModel.kt†L73-L101】【F:app/src/main/java/com/example/weatherproject/ui/screens/CctvScreen.kt†L211-L339】
- **알림 트리거**: `AlarmReceiver`와 `NotificationHelper`가 알림을 실제로 발송하며, `WeatherUpdateWorker`는 백그라운드에서 날씨 기반 알림 생성 시 로그/예외를 기록합니다.【F:app/src/main/java/com/example/weatherproject/receiver/AlarmReceiver.kt†L6-L78】【F:app/src/main/java/com/example/weatherproject/util/NotificationHelper.kt†L12-L82】【F:app/src/main/java/com/example/weatherproject/worker/WeatherUpdateWorker.kt†L15-L82】
- **데이터 일관성**: `PreferenceManager` 캐시와 ViewModel 상태를 초기 로드 시 동기화하고, 같은 위치를 중복 선택하면 CCTV 재호출을 건너뛰어 불필요한 네트워크를 줄입니다.【F:app/src/main/java/com/example/weatherproject/util/PreferenceManager.kt†L14-L97】【F:app/src/main/java/com/example/weatherproject/ui/CctvViewModel.kt†L34-L72】

## 화면별 설계
### 1) 홈 화면 (`HomeScreen`)
- **목적**: 현재 위치 날씨 요약, 시간/주간 예보, 체감 온도 보정 기반 옷차림 추천, 주변 CCTV 카드 제공.
- **레이아웃**:
  - 풀다운 갱신이 가능한 스크롤 컬럼 내에 현재 날씨, 예보, 옷차림, CCTV 카드 순으로 배치합니다.【F:app/src/main/java/com/example/weatherproject/ui/screens/HomeScreen.kt†L90-L168】
  - 상단 스니펫 영역에 데이터 제공처와 마지막 업데이트 시간을 표시합니다.【F:app/src/main/java/com/example/weatherproject/ui/screens/HomeScreen.kt†L149-L168】
  - 초기 진입 시 체감 온도 보정 슬라이더 다이얼로그를 노출할 수 있습니다.【F:app/src/main/java/com/example/weatherproject/ui/screens/HomeScreen.kt†L43-L248】
- **인터랙션/상태**:
  - 당겨서 새로고침 시 날씨 데이터를 갱신하고 현재 위치 CCTV를 재요청합니다.【F:app/src/main/java/com/example/weatherproject/ui/screens/HomeScreen.kt†L52-L79】
  - `CurrentWeatherCard`와 `HourlyForecastCard`는 토글형 확장 영역을 갖고 세부 날씨/주간 예보를 표시합니다.【F:app/src/main/java/com/example/weatherproject/ui/components/WeatherCards.kt†L43-L235】
  - 옷차림 추천 카드는 체감온도와 사용자가 조정한 보정값을 반영하여 텍스트/칩을 보여줍니다.【F:app/src/main/java/com/example/weatherproject/ui/components/WeatherCards.kt†L464-L505】
  - 주변 CCTV 카드는 로딩/오류/성공 상태에 따라 메시지·스피너·요약행을 렌더링하며 전체 목록/재생으로 이동할 수 있습니다.【F:app/src/main/java/com/example/weatherproject/ui/components/WeatherCards.kt†L371-L460】

### 2) 상세 날씨 화면 (`DetailWeatherScreen`)
- **목적**: 홈에서 접혀 있던 상세 날씨 정보를 항상 확장된 상태로 제공.
- **레이아웃**: 투명 상단바와 스크롤 컬럼 안에 `CurrentWeatherCard`를 확장 형태로 배치합니다.【F:app/src/main/java/com/example/weatherproject/ui/screens/DetailWeatherScreen.kt†L18-L43】

### 3) 예보 화면 (`ForecastScreen`)
- **목적**: 시간별/주간 예보를 전면적으로 노출.
- **레이아웃**: 상단바 뒤로가기 포함, 본문은 항상 확장된 `HourlyForecastCard` 하나로 구성하여 시간/주간 예보를 동시에 보여줍니다.【F:app/src/main/java/com/example/weatherproject/ui/screens/ForecastScreen.kt†L18-L45】

### 4) CCTV 목록 화면 (`CctvScreen`)
- **목적**: 검색/현재 위치 기반 CCTV 검색, 리스트 확인, 재생 화면 이동.
- **레이아웃 & 요소**:
  - 상단바: 뒤로가기, 위치 검색 입력, 현재 위치 버튼, 선택된 주소 표시, 검색 결과 드롭다운을 포함합니다.【F:app/src/main/java/com/example/weatherproject/ui/screens/CctvScreen.kt†L97-L200】
  - 본문: 로딩/오류/데이터 상태를 분기하며, CCTV 카드 목록과 “밀어서 더 보기” 힌트를 제공합니다.【F:app/src/main/java/com/example/weatherproject/ui/screens/CctvScreen.kt†L211-L339】
  - `CctvListItem`은 썸네일, 도로명, 타입/거리, 재생 아이콘으로 구성된 클릭 가능한 행입니다.【F:app/src/main/java/com/example/weatherproject/ui/screens/CctvScreen.kt†L344-L448】
- **인터랙션**:
  - 초기 렌더 시 선택 위치가 없으면 현재 GPS 기반으로 CCTV를 조회합니다.【F:app/src/main/java/com/example/weatherproject/ui/screens/CctvScreen.kt†L67-L95】
  - 리스트 바닥 도달 시 추가 항목을 불러오는 가상 페이징을 수행합니다.【F:app/src/main/java/com/example/weatherproject/ui/screens/CctvScreen.kt†L77-L95】
  - CCTV 클릭 시 URL을 Base64 인코딩하여 플레이어 화면으로 네비게이트합니다.【F:app/src/main/java/com/example/weatherproject/ui/screens/CctvScreen.kt†L259-L289】

### 5) CCTV 재생 화면 (`CctvPlayerScreen`)
- **목적**: 선택된 CCTV 스트림을 ExoPlayer로 재생하고 로딩/오류 안내 제공.
- **레이아웃**: 검정 배경 위에 상단 뒤로가기 바, 중앙 PlayerView, 로딩 스피너 또는 오류 메시지/복귀 버튼 오버레이를 배치합니다.【F:app/src/main/java/com/example/weatherproject/ui/screens/CctvPlayerScreen.kt†L24-L134】

### 6) 알림 목록 화면 (`AlarmListScreen`)
- **목적**: 날씨 알림 관리, 권한 안내, 신규 알림 추가 진입.
- **레이아웃**:
  - 투명 상단바(뒤로가기, 추가 버튼)와 하단 권한 안내 바(정확한 알람 권한 미보유 시)로 구성합니다.【F:app/src/main/java/com/example/weatherproject/ui/AlarmScreens.kt†L125-L182】
  - 본문은 전체 알림 토글 카드와 알림 목록 리스트를 포함하며, 각 아이템은 `AlarmCard`로 시간/반복 정보와 스위치를 제공합니다.【F:app/src/main/java/com/example/weatherproject/ui/AlarmScreens.kt†L189-L247】【F:app/src/main/java/com/example/weatherproject/ui/AlarmScreens.kt†L252-L302】
- **인터랙션**: 알림 추가 시 알림/정확한 알람 권한 체크 후 편집 화면으로 이동합니다.【F:app/src/main/java/com/example/weatherproject/ui/AlarmScreens.kt†L107-L123】

### 7) 알림 편집 화면 (`AlarmEditScreen`)
- **목적**: 시/분 입력, 오전/오후 선택, 반복 요일 또는 단일 날짜 설정, 저장/삭제 제공.
- **레이아웃 & 흐름**:
  - 상단 뒤로가기와 저장 버튼이 있는 투명 상단바, 아래로 시간 입력·AM/PM 선택, 반복/날짜 토글 버튼, 반복 요일 토글, 날짜 선택 버튼, 삭제 버튼 순으로 세로 스크롤 구성입니다.【F:app/src/main/java/com/example/weatherproject/ui/AlarmScreens.kt†L304-L520】

### 8) 설정 화면 (`SettingsScreen`)
- **목적**: 알림 설정 진입점을 단순 제공.
- **레이아웃**: 기본 상단바와 “알림 설정” 리스트 아이템 하나로 구성되며 클릭 시 알림 목록으로 이동합니다.【F:app/src/main/java/com/example/weatherproject/ui/SettingsScreen.kt†L6-L36】

## 카드/컴포넌트 스펙 요약
- **CurrentWeatherCard**: 주소, 온도/설명/최고·최저/체감 온도와 확장 시 강수량·습도·풍속·미세먼지 게이지를 제공. 접힘/펼침 화살표 애니메이션 포함.【F:app/src/main/java/com/example/weatherproject/ui/components/WeatherCards.kt†L43-L155】【F:app/src/main/java/com/example/weatherproject/ui/components/WeatherCards.kt†L241-L369】
- **HourlyForecastCard**: 시간별 예보 가로 스크롤, 확장 시 주간 예보 리스트를 추가로 노출하며 안내 텍스트가 상태에 따라 변합니다.【F:app/src/main/java/com/example/weatherproject/ui/components/WeatherCards.kt†L160-L235】
- **NearbyCctvCard**: 상태별 콘텐츠와 “CCTV 목록 전체 보기” 버튼을 포함해 홈에서 CCTV 기능으로 진입시킵니다.【F:app/src/main/java/com/example/weatherproject/ui/components/WeatherCards.kt†L371-L460】
- **ClothingRecommendationCard**: 체감온도·사용자 보정값에 따른 추천 문구와 의상 칩 목록을 보여줍니다.【F:app/src/main/java/com/example/weatherproject/ui/components/WeatherCards.kt†L464-L505】

## 사용자 흐름 요약
1. 앱 실행 시 위치 권한을 확인하고, 날씨 상태에 맞춘 배경과 홈 콘텐츠를 로드합니다.【F:app/src/main/java/com/example/weatherproject/MainActivity.kt†L109-L155】
2. 홈 상단 검색 또는 현재 위치 버튼으로 위치를 선택하면 날씨·CCTV 위치가 함께 갱신됩니다.【F:app/src/main/java/com/example/weatherproject/ui/components/WeatherTopAppBar.kt†L58-L116】
3. 홈 카드에서 더보기/전체보기/재생 버튼을 통해 상세 날씨, 예보, CCTV 목록/플레이어로 이동합니다.【F:app/src/main/java/com/example/weatherproject/ui/screens/HomeScreen.kt†L123-L145】【F:app/src/main/java/com/example/weatherproject/ui/components/WeatherCards.kt†L371-L460】
4. 알림 설정은 상단바 알림 아이콘 또는 설정 화면을 통해 접근하며, 목록→편집→저장/삭제 순으로 진행합니다.【F:app/src/main/java/com/example/weatherproject/ui/components/WeatherTopAppBar.kt†L78-L86】【F:app/src/main/java/com/example/weatherproject/ui/SettingsScreen.kt†L6-L36】【F:app/src/main/java/com/example/weatherproject/ui/AlarmScreens.kt†L125-L520】
