package com.example.weatherproject.util

object ClothingRecommender {

    fun getRecommendation(feelsLikeTemp: Int): Pair<String, List<String>> {

        return when {
            feelsLikeTemp >= 28 -> Pair(
                "푹푹 찌는 무더위예요. 민소매나 린넨 소재처럼 통기성이 좋은 시원한 옷차림이 좋아요.",
                listOf("민소매", "반바지", "원피스", "린넨 셔츠", "샌들")
            )
            feelsLikeTemp >= 23 -> Pair(
                "조금 더울 수 있는 날씨예요. 가벼운 반팔 티셔츠나 얇은 셔츠를 추천드려요.",
                listOf("반팔 티셔츠", "얇은 셔츠", "반바지", "면바지")
            )
            feelsLikeTemp >= 20 -> Pair(
                "활동하기 딱 좋은 날씨네요! 긴팔 티셔츠나 셔츠에 얇은 가디건을 걸치면 좋아요.",
                listOf("긴팔 티셔츠", "셔츠", "가디건", "면바지", "청바지")
            )
            feelsLikeTemp >= 17 -> Pair(
                "아침저녁으로 쌀쌀해요. 맨투맨이나 니트, 혹은 입고 벗기 편한 가벼운 외투를 챙기세요.",
                listOf("니트", "맨투맨", "후드티", "가디건", "청바지", "슬랙스")
            )
            feelsLikeTemp >= 12 -> Pair(
                "찬 바람이 느껴져요. 자켓이나 야상 점퍼, 도톰한 가디건으로 보온에 신경 써주세요.",
                listOf("자켓", "야상", "트렌치코트", "니트", "스타킹")
            )
            feelsLikeTemp >= 9 -> Pair(
                "꽤 쌀쌀한 날씨입니다. 트렌치코트나 두께감 있는 점퍼를 입고, 목을 따뜻하게 해주세요.",
                listOf("트렌치코트", "라이더자켓", "기모 후드", "니트")
            )
            feelsLikeTemp >= 5 -> Pair(
                "본격적인 추위가 시작됐어요. 코트 안에도 따뜻한 니트나 히트텍을 챙겨 입으시는 게 좋겠어요.",
                listOf("코트", "가죽자켓", "히트텍", "기모 바지", "레깅스")
            )
            else -> Pair(
                "매우 추운 날씨입니다! 두꺼운 패딩과 목도리, 장갑 등으로 꽁꽁 싸매서 체온을 지키세요.",
                listOf("롱패딩", "숏패딩", "목도리", "장갑", "털모자", "방한화")
            )
        }
    }
}
