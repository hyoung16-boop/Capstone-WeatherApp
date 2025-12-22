앱 실행 사진 모음 
https://blog.naver.com/jueun3010/224115564573 

12. 9 발표 이후 개선사항

  1. 개인 맞춤형 체감온도 및 옷차림 추천 알고리즘
  > 개선 포인트: 기존 하드코딩된 데이터값을 단계별로 나누어 섬세한 데이터 제공 

  이 기능은 [기상학적 수식] + [사용자 보정값] + [데이터베이스 매핑]의 3단계 흐름으로 작동합니다.

  A. 로직 메커니즘 (Technical Logic)
   1. 기상학적 체감온도 산출 (`FeelsLikeTempCalculator.kt`):
      기본적으로 기온($T$), 풍속($V$), 습도($H$)를 기반으로 체감온도를 계산합니다.
      겨울철에는 Wind Chill(체감온도) 공식을, 여름철에는 Heat Index(열지수) 공식을 적용하거나 간소화된 공식을 사용하여 베이스 값을 산출합니다.
   2. 사용자 개인화 보정 (`PreferenceManager`, `WeatherRepositoryImpl`):
      사용자가 설정에서 "추위를 많이 탐(+값)" 또는 "더위를 많이 탐(-값)"을 선택하면, 이 값을 Offset으로 저장합니다 (-3 ~ +3).
      WeatherRepositoryImpl에서 데이터를 가공할 때, `산출된 체감온도 + 사용자 보정값 = 최종 체감온도`를 도출합니다.
      효과: 같은 영하 5도라도 추위를 많이 타는 사용자에게는 "영하 8도" 수준의 강력한 옷차림을 추천하게 됩니다.
   3. 옷차림 매칭 시스템 (`ClothingRecommender.kt`):
      최종 체감온도를 7단계 구간(VERY_HOT ~ VERY_COLD)으로 나눕니다.
      ClothingDatabase에서 해당 구간 태그(Tag)를 가진 아이템을 쿼리합니다.
      동적 필터링: 비가 오면(isRainy) 우산을, 바람이 불면(isWindy) 윈드브레이커를 추가로 추천 목록에 포함합니다.

   B. 데이터 흐름 요약
  > API 기온 데이터 → 체감온도 공식 적용 → 사용자 보정값 합산 → 최종 체감온도 결정 → 옷차림 DB 조회 → UI 표시

  2. 스마트 기상 알림 (Smart Alert System)
  > 개선 포인트: "사용자가 매번 앱을 켜서 확인하지 않아도, 급변하는 날씨(눈/비)를 놓치지 않게 해달라"는 피드백 해결.

  기존의 정해진 시간에 울리는 알람(Static Alarm)을 넘어, 앱이 스스로 예보를 분석하여 필요할 때만 알림을 보내는
  백그라운드 서비스입니다.

  A. 로직 메커니즘 (Technical Logic)
   1. 백그라운드 스케줄링 (`SmartAlertScheduler`, `WorkManager`):
       * 안드로이드의 WorkManager를 사용하여 3시간 주기로 SmartAlertWorker를 백그라운드에서 실행합니다. 앱이 꺼져 있어도
         작동합니다.
   2. 미래 예보 데이터 분석 (`SmartAlertWorker.kt`):
       * 워커가 실행되면 서버에서 최신 단기 예보(Hourly Forecast)를 가져옵니다.
       * 향후 3시간 이내의 데이터를 분석합니다.
   3. 강수 형태(PTY) 코드 감지:
       * 기상청 API의 PTY 코드(0: 없음, 1: 비, 2: 비/눈, 3: 눈, 4: 소나기)를 확인합니다.
       * hourlyForecast.take(3).any { it.pty != "0" } 로직을 통해 3시간 내 눈이나 비 예보가 있는지 탐지합니다.
   4. 중복 알림 방지 (Anti-Spamming):
       * 비가 계속 온다고 해서 3시간마다 알림을 보내면 피로도가 높아집니다.
       * PreferenceManager에 lastAlertTime(마지막 발송 시간)을 저장하고, 일정 시간(예: 3시간)이 지나지 않았으면 알림을
         스킵(Skip)하는 방어 로직이 구현되어 있습니다.
------------------------------------------------------------------------------------------------------------------------------------

 3. 상황별 자연어 날씨 요약 (Weather Summarization)
  > 개선 포인트: "숫자 데이터(습도 80%, 풍속 4m/s)는 직관적이지 않다"는 피드백 해결.

  단순한 데이터 나열이 아닌, 종합적인 상황 판단 로직을 통해 마치 사람이 말해주는 듯한 텍스트를 생성합니다.

  A. 로직 메커니즘 (WeatherSummarizer.kt)
   1. 우선순위 기반 판단 (Priority Logic): 가장 위험하거나 중요한 정보를 최우선으로 노출합니다.
    1순위 (위험): 강수(눈/비) 여부. ("눈이 내리니 미끄럼 주의하세요")
    2순위 (계절/기온): 한파, 폭염 등 계절적 특이사항. ("매서운 한파가 찾아왔어요")
    3순위 (쾌적도): 미세먼지, 습도, 바람 등. ("공기는 차갑지만 하늘은 맑아요")
   2. 복합 조건 분석:
      단순히 "기온이 낮다"가 아니라, "기온은 적당한데(조건 A) + 바람이 강해서(조건 B) = 체감온도가 낮다(결론)"와 같은 복합 추론 로직이 들어있습니다.
      예: if (isWinter && windSpeed >= 4.0) -> "칼바람 때문에 체감온도가 뚝 떨어졌어요."




1. 아키텍처:
   패턴: MVVM (Model-View-ViewModel) 및 클린 아키텍처 지향.
   DI (의존성 주입): Hilt를 전반적으로 사용 (AppModule, @HiltViewModel, 워커/리시버를 위한 EntryPointAccessors).
   UI: Jetpack Compose 기반 (단일 액티비티 MainActivity, WeatherNavHost를 통한 네비게이션).
   비동기 처리: Kotlin Coroutines 및 Flow 사용.

2. 핵심 기능:
   실시간 날씨: WeatherApiService를 통해 현재, 시간별, 주간 예보 조회.
   위치 기반: GPS(FusedLocationProvider) 및 주소 검색(Geocoder) 지원.
   데이터 지속성: Room DB(AppDatabase)에 전체 날씨 상태 캐싱, PreferenceManager로 설정 값(체감 온도 보정 등) 관리.
   백그라운드 작업: WeatherUpdateWorker가 주기적 또는 알람 시 날씨 업데이트 및 브리핑 수행.
   스마트 알림: SmartAlertWorker가 향후 3시간 내 강수(눈/비) 예보 시 알림 발송.
   사용자 맞춤: "체감 온도 보정" 기능을 통해 사용자의 더위/추위 민감도 반영.
   CCTV: 별도 API(/get_cctv)를 통해 주변 교통 CCTV 영상/이미지 제공.
   옷차림 추천: ClothingRecommender가 체감 온도를 분석하여 적절한 옷차림 제안.
   날씨 요약: WeatherSummarizer가 날씨 상태를 자연어 문장으로 요약 (예: "겨울이지만 봄처럼 포근해요").

 3. 데이터 흐름:
    요청: MainViewModel -> WeatherRepository -> WeatherApiService (원격) / WeatherDao (로컬).
    처리: 레포지토리에서 체감 온도 계산, 어제 날씨 비교, 데이터 매핑 후 WeatherState 생성.
    표시: HomeScreen이 MainViewModel.uiState를 구독하여 화면 렌더링.
    
 4. 네트워크:
    Base URL: https://www.weapi.shop/
    엔드포인트: /api/weather/current, /api/weather/forecast, /api/weather/week, /get_cctv.
    클라이언트: OkHttp (로깅 인터셉터 포함).



    
