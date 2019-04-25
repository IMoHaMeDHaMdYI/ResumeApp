package mohamed.mohamedresume.hardcodeddata

import androidx.appcompat.app.AppCompatActivity
import mohamed.mohamedresume.alarm.AlarmActivity
import mohamed.mohamedresume.imageeditor.ui.ImageBrowserActivity
import mohamed.mohamedresume.intro.models.Way
import mohamed.mohamedresume.mediaplayer.audio.ui.TestActivity
import mohamed.mohamedresume.retrofitrxcomponentarch.ui.GitHubSearchActivity

object Trip {
    val questionList =
        arrayListOf(
            "Bored from regular CVs?",
            "This is the right place",
            "Ohh, man you are strong",
            "Please be my guest"
        )
    var currentIndex = 0
    val no = 2
    val yes = 1
    val cont = 3
    val wayList: ArrayList<Way<out AppCompatActivity>> = arrayListOf(
        Way(GitHubSearchActivity::class.java, "GitHub search")
        , Way(TestActivity::class.java, "Media App Test")
        , Way(AlarmActivity::class.java, "Alarm Test")
        , Way(ImageBrowserActivity::class.java, "Image Editor")
    )
}