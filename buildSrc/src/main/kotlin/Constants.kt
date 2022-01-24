import java.text.SimpleDateFormat
import java.util.Date

val buildNumber: String = SimpleDateFormat("yyMMddHHmm").format(Date())

object Versions {
    const val COMMONS_IO = "2.5"

    const val KOTLIN = "1.5.10"

    const val JAYBIRD = "3.0.0"

    const val SPRING = "4.2.5.RELEASE"

    const val HIKARICP = "2.4.3"

    const val MARIADB = "2.4.0"

    const val JUNIT = "4.12"

    const val COMMONS_DBUTILS = "1.6"

    const val JAVAX_XML_BIND = "2.3.0"

    const val AIS_MODEL2 = "2.1.0"

    const val AIS_COMMONS = "1.1.0"
}

