package com.nasim.launcher.common

data class Reciter(val id: String, val name: String, val serverUrl: String)

object QuranData {
    val RECITERS = listOf(
        Reciter("1", "عبد الباسط عبد الصمد", "https://server7.mp3quran.net/basit/"),
        Reciter("2", "محمود خليل الحصري", "https://server13.mp3quran.net/hssri/"),
        Reciter("3", "محمد صديق المنشاوي", "https://server10.mp3quran.net/minsh/"),
        Reciter("4", "مشاري راشد العفاسي", "https://server8.mp3quran.net/afs/"),
        Reciter("5", "سعود الشريم", "https://server7.mp3quran.net/shur/"),
        Reciter("6", "عبد الرحمن السديس", "https://server11.mp3quran.net/sds/"),
        Reciter("7", "ماهر المعيقلي", "https://server12.mp3quran.net/maher/"),
        Reciter("8", "سعد الغامدي", "https://server7.mp3quran.net/s_gmd/"),
        Reciter("9", "أحمد بن علي العجمي", "https://server10.mp3quran.net/ajm/"),
        Reciter("10", "ياسر الدوسري", "https://server11.mp3quran.net/yasser/")
    )

    val RADIOS = listOf(
        Pair("إذاعة القرآن الكريم من القاهرة", "https://stream.radiojar.com/8s44vhq6b7uvw"),
        Pair("إذاعة القارئ عبد الباسط عبد الصمد", "https://stream.radiojar.com/8s44vhq6b7uvw"),
        Pair("إذاعة التكبيرات والجمعة", "https://stream.radiojar.com/8s44vhq6b7uvw")
    )
}
